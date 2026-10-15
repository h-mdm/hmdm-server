#!/bin/sh
#
# Headwind MDM certbot deploy hook.
#
# Installed by hmdm_install.sh to /etc/letsencrypt/renewal-hooks/deploy/hmdm-tls.
# Certbot runs every executable in that directory ONCE PER SUCCESSFULLY RENEWED
# CERTIFICATE ON THE HOST -- not once per certbot invocation -- so the first thing
# this script does is check that the renewal it was handed is actually ours.
#
# What it does, in order:
#   1. stage the new PEMs into a fresh generation directory
#   2. verify the private key and the certificate are a matching pair
#   3. apply root-owned, Tomcat-group-readable ownership
#   4. publish atomically by swapping a single symlink
#   5. rewrite the Artemis pemcfg so the running broker notices the new material
#   6. prune old generations, keeping the previous one for rollback
#
# Any failure before the symlink swap leaves the previous generation published
# and intact. The whole run holds an exclusive lock so that two overlapping
# renewals cannot corrupt each other; see the comment above LOCK_FILE.
#
# Exit status -- the caller acts on the difference, so it is part of the contract:
#   0  published and every consumer signalled
#   1  NOT published; the previous generation is still live and serving
#   2  published, but a consumer was not signalled (typically the Artemis pemcfg).
#      The certificate is correct and live; the running broker has not picked it
#      up. A Tomcat restart resolves it, since Tomcat rewrites the pemcfg at
#      startup. Never report this as a failed publication.
#
set -eu

# Written by the installer. Defines HMDM_LINEAGE (the exact certbot live path),
# TOMCAT_USER and TOMCAT_GROUP. Root-only: it names the certificate lineage.
ENV_FILE=/etc/hmdm/tls/hook.env
[ -r "$ENV_FILE" ] || { echo "hmdm-tls: $ENV_FILE missing" >&2; exit 1; }
. "$ENV_FILE"
: "${HMDM_LINEAGE:?hook.env must define HMDM_LINEAGE}"
: "${TOMCAT_GROUP:?hook.env must define TOMCAT_GROUP}"
: "${TOMCAT_USER:?hook.env must define TOMCAT_USER}"

TLS_DIR=/etc/hmdm/tls
PEMCFG=/var/lib/hmdm/mqtt/mqtt-broker.pemcfg
PEM_FILES="privkey.pem fullchain.pem cert.pem chain.pem"

# Serializes concurrent hook runs against each other. Publishing and pruning both
# mutate state shared by every run in this directory, and a run that is still
# staging owns a directory the pruner must not touch, so the whole run is
# serialized rather than just the tail of it.
LOCK_FILE="$TLS_DIR/.publish.lock"

# Ignore renewals for any lineage that is not ours. Exact match, no globbing:
# on a host serving several domains an unrelated renewal must not overwrite
# HMDM's published certificate with a stranger's.
[ "${RENEWED_LINEAGE:-}" = "$HMDM_LINEAGE" ] || exit 0

log() { echo "hmdm-tls: $*"; }

# True when "current" already resolves to this run's generation -- i.e. the swap
# has happened, whatever any bookkeeping says. This is the single source of truth
# for "did we publish?", used by the signal handler, the abort path and the staging
# cleanup alike.
#
# A flag would be wrong here. Setting one cannot be made atomic with the rename, so
# a signal landing between the two would read a stale flag: it would report a live
# certificate as un-published, and the caller would skip the recovery restart. It
# also removes any dependence on trap ordering, and on which /bin/sh the host has --
# shells disagree about whether an EXIT trap runs after an unhandled signal.
#
# ${STAMP:-} because this is reachable before STAMP is assigned: a lock failure
# calls fail() during setup. Unset STAMP simply cannot match, which is the correct
# answer that early -- nothing has been published yet.
is_published() {
    [ -n "${STAMP:-}" ] && [ "$(readlink "$TLS_DIR/current" 2>/dev/null || true)" = "gen-$STAMP" ]
}

# Aborts. Exit 1 means nothing was published and the previous generation is still
# live -- the caller relies on that to tell an operator what state the host is in.
# Exit 2 if the swap already happened: a future edit that reaches fail() after
# publication must not report a live certificate as un-published.
fail() {
    if is_published; then
        echo "hmdm-tls: $* -- NOTE: the new certificate IS already published" >&2
        exit 2
    fi
    echo "hmdm-tls: $*" >&2
    exit 1
}

# Signal handling needs to be explicit. $? inside a handler is not the signal's
# status, so without these a signal can exit 0 and be read as success. And a
# signal arriving after the symlink swap must report 2, not 130/143: the caller
# treats any other non-zero as "nothing was published", which would be false and
# would skip the recovery restart.
signal_exit() {
    if is_published; then
        echo "hmdm-tls: interrupted AFTER publishing gen-$STAMP -- the certificate IS live" >&2
        exit 2
    fi
    echo "hmdm-tls: interrupted before publishing; previous generation left in place" >&2
    exit "$1"
}

# ---------------------------------------------------------------------------
# 0. Take the lock
#
# certbot runs deploy hooks once per renewed lineage, and its own systemd timer
# can fire while letsencrypt-ssl.sh is running ours, so two copies of this script
# really can overlap. Held for the whole run: the pruner would otherwise be free
# to delete a generation another run had staged but not yet published.
#
# 9>: the lock lives as long as this descriptor stays open, and the kernel
# releases it when the process exits, however it exits -- no unlock path to miss.
# ---------------------------------------------------------------------------
LOCKED=0
if command -v flock >/dev/null 2>&1; then
    exec 9>"$LOCK_FILE" || fail "could not open $LOCK_FILE"
    if flock -w 120 -x 9; then
        LOCKED=1
    else
        fail "another hmdm-tls hook held $LOCK_FILE for over 120s -- refusing to publish concurrently"
    fi
else
    # Publishing stays safe without the lock because the staging symlink carries
    # "-$$" (see below). Pruning does not, so it is skipped rather than risk
    # deleting a concurrent run's work.
    echo "hmdm-tls: WARNING flock not found; running unserialized and skipping the prune" >&2
fi

# ---------------------------------------------------------------------------
# 1. Stage
#
# The "-$$" is load-bearing: the timestamp has one-second resolution, so two hook
# runs inside the same second would otherwise target the same directory and one
# would clobber the other's half-copied contents. letsencrypt-ssl.sh passes
# --no-directory-hooks so our own path cannot double-fire, but "certbot renew"
# from certbot's own systemd timer is outside our control.
# ---------------------------------------------------------------------------
STAMP="$(date -u +%Y%m%dT%H%M%SZ)-$$"
GEN_DIR="$TLS_DIR/gen-$STAMP"

# Plain mkdir, deliberately not -p: this must fail if the directory somehow exists.
mkdir "$GEN_DIR" || fail "could not create $GEN_DIR"

# From here until the symlink swap, any failure removes the half-built generation
# and leaves the previously published one untouched. Guarded by is_published(), so
# a signal arriving after the swap can never delete the generation "current" now
# points at -- see the rationale above that function.
cleanup_staging() {
    if is_published; then
        return 0    # live: never delete
    fi
    rm -rf "$GEN_DIR"
}
trap 'cleanup_staging' EXIT
trap 'signal_exit 130' INT
trap 'signal_exit 143' TERM

for f in $PEM_FILES; do
    [ -r "$RENEWED_LINEAGE/$f" ] || fail "$RENEWED_LINEAGE/$f is missing or unreadable"
    cp "$RENEWED_LINEAGE/$f" "$GEN_DIR/$f" || fail "could not copy $f"
done

# ---------------------------------------------------------------------------
# 2. Validate the pair BEFORE publishing
#
# Publishing a key that does not match its certificate takes the site down as
# surely as publishing nothing, and it does so silently until the next handshake.
# ---------------------------------------------------------------------------
key_pub="$(openssl pkey -in "$GEN_DIR/privkey.pem" -pubout 2>/dev/null)" \
    || fail "could not read the private key -- refusing to publish"
cert_pub="$(openssl x509 -in "$GEN_DIR/cert.pem" -pubkey -noout 2>/dev/null)" \
    || fail "could not read the certificate -- refusing to publish"

if [ "$key_pub" != "$cert_pub" ]; then
    fail "private key does not match the certificate -- refusing to publish; previous generation left in place"
fi

# ---------------------------------------------------------------------------
# 3. Ownership: root-owned, Tomcat-group-readable.
#
# Never Tomcat-owned: a Tomcat-owned private key could be rewritten by a
# compromised application.
# ---------------------------------------------------------------------------
chown root:"$TOMCAT_GROUP" "$GEN_DIR" || fail "could not chown $GEN_DIR"
chmod 0750 "$GEN_DIR" || fail "could not chmod $GEN_DIR"
for f in $PEM_FILES; do
    chown root:"$TOMCAT_GROUP" "$GEN_DIR/$f" || fail "could not chown $f"
    chmod 0640 "$GEN_DIR/$f" || fail "could not chmod $f"
done

# ---------------------------------------------------------------------------
# 4. Publish atomically
#
# Four independent mv operations cannot atomically publish a key/cert set: a
# reader landing between them observes a mismatched key and chain. Publish the
# whole directory, then swap one pointer with a single rename(2).
#
# The staging symlink carries "-$$" for the same reason the generation directory
# does. With one shared name, run A could overwrite run B's staged link; B's mv
# would then fail with its source already consumed, and B's still-armed EXIT trap
# would delete the very generation A had just published -- leaving "current"
# dangling and TLS dead until the next renewal. Per-process names keep each mv
# independent: both runs publish a complete, valid generation, later one wins.
# This holds even on a host with no flock.
# ---------------------------------------------------------------------------
CURRENT_TMP="$TLS_DIR/.current.tmp.$$"
ln -sfn "gen-$STAMP" "$CURRENT_TMP" || fail "could not stage the current symlink"
mv -T "$CURRENT_TMP" "$TLS_DIR/current" || {
    rm -f "$CURRENT_TMP"
    fail "could not publish the current symlink"
}

# Published. The staged directory is now live, so stop treating it as scratch.
trap - EXIT
log "published gen-$STAMP for ${RENEWED_DOMAINS:-$HMDM_LINEAGE}"

# Exit status once the certificate is live. Everything from here on is a signal to
# a consumer, not the publication itself, so a failure must NOT be reported as
# "nothing was published" -- the caller would then tell an operator the previous
# generation is still current, which is false, and would skip its own recovery.
# 0 = fully done. EXIT_PARTIAL = published, but a consumer was not signalled.
EXIT_PARTIAL=2
STATUS=0

# Post-publication failures are warnings that downgrade STATUS. They must not
# abort: the remaining steps are independent, and the prune still needs to run.
degrade() {
    echo "hmdm-tls: WARNING $*" >&2
    STATUS=$EXIT_PARTIAL
}

# ---------------------------------------------------------------------------
# 5. Signal Artemis
#
# The broker watches the pemcfg file, not the PEMs. Swapping the "current"
# symlink changes no inode that Artemis has open, so rewriting this file is what
# tells it to reload. Written to a temp file in the same directory and moved into
# place so the broker never reads a half-written config.
#
# A failure here leaves a correct, published certificate that the running broker
# has not picked up. Tomcat rewrites this file at startup, so a restart recovers
# it -- which is exactly what EXIT_PARTIAL asks the caller to do.
# ---------------------------------------------------------------------------
PEMCFG_DIR=$(dirname "$PEMCFG")
if [ -d "$PEMCFG_DIR" ]; then
    PEMCFG_TMP="$PEMCFG_DIR/.mqtt-broker.pemcfg.$$"
    if {
        echo "source.key=$TLS_DIR/current/privkey.pem"
        echo "source.cert=$TLS_DIR/current/fullchain.pem"
    } > "$PEMCFG_TMP" 2>/dev/null \
        && chown "$TOMCAT_USER":"$TOMCAT_GROUP" "$PEMCFG_TMP" \
        && chmod 0640 "$PEMCFG_TMP" \
        && mv -f "$PEMCFG_TMP" "$PEMCFG"; then
        # Tomcat rewrites this file itself at startup, so leave it Tomcat-owned.
        log "rewrote $PEMCFG"
    else
        rm -f "$PEMCFG_TMP"
        degrade "could not rewrite $PEMCFG; MQTT will not reload until Tomcat restarts"
    fi
else
    degrade "$PEMCFG_DIR does not exist; MQTT will not reload until Tomcat restarts"
fi

# ---------------------------------------------------------------------------
# 6. Prune, keeping the current generation and the one before it
#
# The previous generation is what a rollback needs; anything older is dead weight
# holding a private key on disk.
#
# Only runs while the lock is held. Unserialized, a run still staging its own
# generation would be indistinguishable from an abandoned one, and pruning it
# would destroy work that is about to be published.
# ---------------------------------------------------------------------------
if [ "$LOCKED" = "1" ]; then
    KEEP=2
    # shellcheck disable=SC2012 # names are generated by this script; no odd characters
    ls -1d "$TLS_DIR"/gen-* 2>/dev/null | sort -r | tail -n +$((KEEP + 1)) | while read -r old; do
        # Never prune the generation just published, whatever the sort produced.
        if [ "$old" = "$GEN_DIR" ]; then
            continue
        fi
        if rm -rf "$old"; then
            log "pruned $(basename "$old")"
        else
            echo "hmdm-tls: WARNING could not prune $old" >&2
        fi
    done
fi

# 0 or EXIT_PARTIAL. A pruning problem does not change it: the certificate is
# published and every consumer signalled; only stale material was left behind.
exit "$STATUS"
