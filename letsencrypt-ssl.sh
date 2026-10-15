#!/bin/bash
#
# LetsEncrypt renewal script for Headwind MDM
#

# Set this parameter to 1 if you're redirecting port 80 to 8080 to be able to run Headwind MDM on port 80
HTTP_REDIRECT=0
DOMAIN=your-domain.com
TOMCAT_HOME=$(ls -d /var/lib/tomcat* | tail -n1)
TOMCAT_SERVICE=$(echo $TOMCAT_HOME | awk '{n=split($1,A,"/"); print A[n]}')

if [ "$DOMAIN" = "your-domain.com" ]; then
    echo "Please edit this script and update HTTP_REDIRECT and DOMAIN variables!"
    exit 1
fi

# Written by hmdm_install.sh. Single source of the certificate name, its lineage
# path and the Tomcat account -- the deploy hook reads the same file, so the two
# cannot drift apart.
HOOK_ENV=/etc/hmdm/tls/hook.env
if [ ! -r "$HOOK_ENV" ]; then
    echo "$HOOK_ENV is missing or unreadable."
    echo "Run hmdm_install.sh to set up the certbot deploy hook before renewing."
    exit 1
fi
. "$HOOK_ENV"                     # CERT_NAME, TOMCAT_USER, TOMCAT_GROUP, HMDM_LINEAGE

if [ -z "$CERT_NAME" ] || [ -z "$HMDM_LINEAGE" ]; then
    echo "$HOOK_ENV does not define CERT_NAME and HMDM_LINEAGE"
    exit 1
fi

# The lineage must agree with the name, or hook.env has been edited inconsistently.
if [ "$HMDM_LINEAGE" != "/etc/letsencrypt/live/$CERT_NAME" ]; then
    echo "hook.env inconsistent: HMDM_LINEAGE=$HMDM_LINEAGE, CERT_NAME=$CERT_NAME"
    exit 1
fi

DEPLOY_HOOK=/etc/letsencrypt/renewal-hooks/deploy/hmdm-tls
if [ ! -x "$DEPLOY_HOOK" ]; then
    echo "$DEPLOY_HOOK is missing or not executable."
    echo "Run hmdm_install.sh to install the certbot deploy hook before renewing."
    exit 1
fi

# --- port-80 redirect ------------------------------------------------------
# The redirect has to come down for certbot's standalone challenge and go back up
# afterwards. Both directions are checked: a silent failure to remove it makes the
# challenge fail, and a blind re-add either duplicates the rule or invents one on a
# host that never had it.
REDIRECT_ARGS="PREROUTING -t nat -p tcp -m tcp --dport 80 -j REDIRECT --to-ports 8080"
REDIRECT_REMOVED=0

do_restore_redirect() {             # returns a status; never exits
    [ "$REDIRECT_REMOVED" = "1" ] || return 0
    if /sbin/iptables -A $REDIRECT_ARGS; then
        REDIRECT_REMOVED=0
        return 0
    fi
    echo "FATAL: could not restore the port-80 redirect -- restore it by hand:" >&2
    echo "  /sbin/iptables -A $REDIRECT_ARGS" >&2
    return 1
}

# INT/TERM must not share the EXIT handler: inside a signal handler $? can be 0,
# so an interrupted renewal would restore the redirect and exit 0 -- reporting
# success without ever having published a certificate. Give them explicit
# non-zero exits, which then fire the EXIT trap.
#
# The EXIT trap only ever UPGRADES a zero status, never overwrites a non-zero one.
# A failed redirect restore at the very end happens after the certificate is
# published and every consumer signalled, so reporting it as 1 ("nothing was
# published") sent the caller down the re-issue path for a certificate that was
# already live. It gets its own status instead; see the exit contract at the end.
trap 'exit 130' INT
trap 'exit 143' TERM
trap 'rc=$?; if ! do_restore_redirect && [ "$rc" -eq 0 ]; then rc=4; fi; exit $rc' EXIT

if [ "$HTTP_REDIRECT" = "1" ] && [ "${HMDM_REPUBLISH_ONLY:-}" != "1" ]; then
    if /sbin/iptables -C $REDIRECT_ARGS 2>/dev/null; then
        if /sbin/iptables -D $REDIRECT_ARGS; then
            REDIRECT_REMOVED=1
        else
            echo "FATAL: port-80 redirect present but could not be removed;" >&2
            echo "       the standalone challenge would fail -- aborting" >&2
            exit 1
        fi
    fi
    # Rule genuinely absent: nothing removed, so nothing to restore.
fi

# HMDM_REPUBLISH_ONLY re-runs the publication half against the lineage that is
# already on disk: no certbot call, no challenge, no port-80 dance, nothing
# issued. It is the recovery for status 3 below -- a certificate that certbot
# obtained but the deploy hook failed to publish. Without it the only way to
# retry publication was to renew again, which spends a Let's Encrypt rate-limit
# slot (five duplicate certificates per week) to re-obtain material the host
# already has sitting in /etc/letsencrypt/live.
if [ "${HMDM_REPUBLISH_ONLY:-}" = "1" ]; then
    echo "HMDM_REPUBLISH_ONLY=1: republishing the existing lineage $HMDM_LINEAGE"
    echo "                       certbot will NOT run and nothing will be issued."
    CERTBOT_STATUS=0
else
    # --cert-name pins the lineage so the live path is known before certbot runs.
    # --no-directory-hooks guarantees exactly one publication: certbot runs the
    # renewal-hooks/deploy/ directory for certonly too, not only for renew, so without
    # this flag the hook would fire once from certbot and once from the call below.
    certbot certonly --agree-tos --no-eff-email --standalone --force-renewal \
            --cert-name "$CERT_NAME" --no-directory-hooks -d "$DOMAIN"
    CERTBOT_STATUS=$?
fi

# Restore before any exit path, and check that it worked.
#
# The status this failure carries depends on what just happened above it. If
# certbot succeeded, a certificate EXISTS on disk that nothing has published yet
# -- which is exactly what status 3 means. Reporting 1 here instead would tell
# the installer "nothing was issued, re-run normally", and re-running issues
# again: a rate-limit slot spent re-obtaining material already in
# /etc/letsencrypt/live. If certbot failed, nothing was issued and 1 is right.
# do_restore_redirect has already printed the manual iptables fix either way.
if ! do_restore_redirect; then
    if [ "${CERTBOT_STATUS:-1}" -eq 0 ]; then
        echo "       A certificate WAS issued for $CERT_NAME but has NOT been published." >&2
        echo "       Once the redirect is restored, publish it WITHOUT issuing another:" >&2
        echo "         HMDM_REPUBLISH_ONLY=1 $0" >&2
        exit 3
    fi
    exit 1
fi

# Certbot's status is checked, which is what the old TODO here was about: without
# it a failed renewal leaves the PREVIOUS lineage directory in place, so the
# existence check below passes and the hook republishes stale material as if it
# were fresh.
#
# Collapsed to 1 rather than passed through. Certbot's own status is not drawn
# from this script's exit contract, so forwarding it verbatim could hand the
# caller a 2, 3 or 4 meaning something entirely different -- "published, recovery
# restart failed" for what was actually a failure to issue anything at all.
if [ "$CERTBOT_STATUS" -ne 0 ]; then
    echo "certbot failed (status $CERTBOT_STATUS) -- NOT publishing; previous certificate left in place"
    exit 1
fi

if [ ! -d "$HMDM_LINEAGE" ]; then
    echo "expected lineage $HMDM_LINEAGE missing after certonly"
    exit 1
fi

# Exactly one publication, with the lineage passed explicitly. The outcomes are
# distinguished because they call for different operator action, and conflating
# them is worse than any of them: reporting a live certificate as un-published
# sends an operator hunting for a failure that did not happen, skips the restart
# that would actually finish the job, and -- because the only remedy on offer was
# "run it again" -- spends a rate-limit slot re-issuing what is already on disk.
#
#   0  published and every consumer signalled
#   2  published, but a consumer was not signalled -- recoverable by restarting
#      Tomcat, which rewrites the Artemis pemcfg at startup
#   3  ISSUED but NOT published: certbot succeeded and the lineage is on disk,
#      the deploy hook failed. Retry with HMDM_REPUBLISH_ONLY=1; do NOT re-issue.
RENEWED_LINEAGE="$HMDM_LINEAGE" RENEWED_DOMAINS="$DOMAIN" "$DEPLOY_HOOK"
HOOK_STATUS=$?

# Set only by the status-2 branch below, and consumed after the connector advice.
NEEDS_RECOVERY_RESTART=0

if [ "$HOOK_STATUS" -eq 2 ]; then
    echo "WARNING: certificate published, but the deploy hook could not signal every consumer" >&2
    NEEDS_RECOVERY_RESTART=1
elif [ "$HOOK_STATUS" -ne 0 ]; then
    echo "FATAL: deploy hook failed (status $HOOK_STATUS) -- certificate issued but NOT published;" >&2
    echo "       /etc/hmdm/tls/current still points at the previous generation." >&2
    echo "       The lineage IS on disk at $HMDM_LINEAGE. Once the cause is fixed," >&2
    echo "       retry publication WITHOUT issuing another certificate:" >&2
    echo "         HMDM_REPUBLISH_ONLY=1 $0" >&2
    exit 3
fi

echo "The certificates have been published here: /etc/hmdm/tls/current/"
echo "Please add / uncomment the following section in $TOMCAT_HOME/conf/server.xml:"
echo "<Connector port=\"8443\" protocol=\"org.apache.coyote.http11.Http11NioProtocol\""
echo "           maxThreads=\"150\" SSLEnabled=\"true\">"
echo "    <SSLHostConfig>"
echo "        <Certificate certificateKeyFile=\"/etc/hmdm/tls/current/privkey.pem\""
echo "                     certificateFile=\"/etc/hmdm/tls/current/cert.pem\""
echo "                     certificateChainFile=\"/etc/hmdm/tls/current/chain.pem\" />"
echo "    </SSLHostConfig>"
echo "</Connector>"

# ===== RECOVERY RESTART (conditional) =====================================
# This runs ONLY when the deploy hook returned 2 -- the certificate was published,
# but a consumer was not signalled. Tomcat rewrites the Artemis pemcfg at startup,
# so restarting is the recovery for that exceptional state. Successful renewals do
# not restart Tomcat; its built-in TLSCertificateReloadListener reloads HTTPS.
#
# The restart is verified rather than assumed: a failed recovery restart must not
# report success, or an operator learns nothing from a zero exit.
#
# Its status is 2, deliberately NOT 1. The two are different states needing
# different action: 1 means nothing was published and the previous certificate is
# still live, so re-issuing is the correct next step; 2 means the certificate IS
# published and only the consumer signal is missing, so re-issuing spends a Let's
# Encrypt rate-limit slot to obtain something the host already has. Callers key
# their advice off that difference -- hmdm_install.sh offers HMDM_SKIP_ISSUANCE=1
# for status 2 and a plain re-run for everything else.
if [ "$NEEDS_RECOVERY_RESTART" -eq 1 ]; then
    echo "Restarting $TOMCAT_SERVICE to complete the reload..." >&2
    if /usr/sbin/service "$TOMCAT_SERVICE" restart; then
        echo "Recovery restart of $TOMCAT_SERVICE completed." >&2
        RECOVERY_EXIT=0
    else
        echo "FATAL: recovery restart of $TOMCAT_SERVICE FAILED. The certificate is published," >&2
        echo "       but the MQTT broker is still using the previous material, and Tomcat may" >&2
        echo "       be DOWN. Restart it by hand and check /var/lib/hmdm/mqtt/mqtt-broker.pemcfg." >&2
        RECOVERY_EXIT=2
    fi
fi
# ===== end RECOVERY RESTART ================================================

# Non-zero only if the recovery restart was attempted and failed. A published
# certificate whose consumer never picked it up must not report success.
#
# The full exit contract, which hmdm_install.sh branches on:
#   0  published, every consumer signalled, redirect restored
#   1  NOTHING published; the previous certificate, if any, is still serving.
#      Re-issuing is the correct next step.
#   2  published, but the recovery restart failed -- Tomcat may be DOWN.
#      Do NOT re-issue; fix Tomcat and resume.
#   3  ISSUED but NOT published; the lineage is on disk. Retry with
#      HMDM_REPUBLISH_ONLY=1. Do NOT re-issue. This also covers a port-80
#      redirect that could not be restored AFTER certbot succeeded: the
#      unpublished certificate is the more consequential half, and the manual
#      iptables command has already been printed. (A redirect failure with no
#      successful issuance behind it is still 1.)
#   4  RESERVED -- not reachable in the current flow. The EXIT trap upgrades a
#      zero status to 4 when the redirect is still down, but the redirect is
#      restored and CHECKED before publication: a failure there has already
#      exited 1 or 3, and a success sets REDIRECT_REMOVED=0 so the trap's own
#      call returns immediately. No zero-status path can reach the trap with the
#      redirect removed. The upgrade stays as a safety net in case a later edit
#      introduces an exit path ahead of that check.
exit "${RECOVERY_EXIT:-0}"
