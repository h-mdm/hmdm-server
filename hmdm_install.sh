#!/bin/bash
#
# Headwind MDM installer script
# Tested on Ubuntu Linux 18.04 - 24.04, Ubuntu 22.04 is recommended
#
REPOSITORY_BASE=https://h-mdm.com/files
CLIENT_VERSION=5.19
DEFAULT_SQL_HOST=localhost
DEFAULT_SQL_PORT=5432
DEFAULT_SQL_BASE=hmdm
DEFAULT_SQL_USER=hmdm
DEFAULT_SQL_PASS=
DEFAULT_LOCATION="/opt/hmdm"
DEFAULT_SCRIPT_LOCATION="/opt/hmdm"
TOMCAT_HOME=$(ls -d /var/lib/tomcat* | tail -n1)
TOMCAT_SERVICE=$(echo $TOMCAT_HOME | awk '{n=split($1,A,"/"); print A[n]}')
TOMCAT_ENGINE="Catalina"
TOMCAT_HOST="localhost"
DEFAULT_PROTOCOL=https
DEFAULT_BASE_DOMAIN=
DEFAULT_BASE_PATH="ROOT"
DEFAULT_PORT=""
TEMP_DIRECTORY="/tmp"
TEMP_SQL_FILE="$TEMP_DIRECTORY/hmdm_init.sql"
TOMCAT_USER=$(ls -ld $TOMCAT_HOME/webapps | awk '{print $3}')

ADMIN_EMAIL=
SMTP_HOST=
SMTP_PORT=
SMTP_SSL=0
SMTP_STARTTLS=0
SMTP_USERNAME=
SMTP_PASSWORD=
SMTP_FROM=

install_soft() {
    read -e -p "Install missing package(s) automatically? (Y/n)?" -n 1 -r
    echo
    if [[ ! "$REPLY" =~ ^[Yy]$ ]]; then
        echo "Please run: apt install $1"
        exit 1
    fi
    apt update
    apt install -y aapt tomcat10 postgresql vim
    TOMCAT_HOME=$(ls -d /var/lib/tomcat* | tail -n1)
    TOMCAT_USER=$(ls -ld $TOMCAT_HOME/webapps | awk '{print $3}')
}

# Use sandbox directory for tomcat 10
if [ "$TOMCAT_HOME" == "/var/lib/tomcat10" ]; then
    DEFAULT_LOCATION="/var/lib/tomcat10/work"
fi

# Check if we are root
CURRENTUSER=$(whoami)
if [[ "$EUID" -ne 0 ]]; then
    echo "It is recommended to run the installer script as root."
    read -p "Proceed as $CURRENTUSER (Y/n)? " -n 1 -r
    echo
    if [[ ! "$REPLY" =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Check if there's an install folder
if [ ! -d "./install" ]; then
    echo "Cannot find installation directory (install)"
    echo "Please cd to the installation directory before running script!"
    exit 1
fi

# --- TLS-only resume mode: decided FIRST ----------------------------------
# This has to be settled before any prerequisite check runs. Recovery is about a
# certificate on a host where the application is ALREADY installed, while the
# checks below are about building and deploying it: they install packages, and
# two of them exit outright. A host without psql, or a checkout without a
# compiled WAR, would abort the recovery run before it ever learned it was one --
# so the advice every failure branch prints would still be unfollowable.
#
# Only the detection lives here. The prompts it drives come later, after
# TOMCAT_USER and TOMCAT_HOME have been resolved, because the TLS work needs both.
TLS_RECOVERY_ONLY=0
if [ "${HMDM_SKIP_ISSUANCE:-}" = "1" ] || [ "${HMDM_REPUBLISH_ONLY:-}" = "1" ]; then
    TLS_RECOVERY_ONLY=1
fi

# aapt, PostgreSQL and the WAR are build/deploy prerequisites. TLS-only mode
# touches none of them: no APK is repackaged, no database is opened, nothing is
# deployed. The Tomcat account and $TOMCAT_HOME checks further down are NOT
# skipped -- the certificate work needs both.
if [ "$TLS_RECOVERY_ONLY" != "1" ]; then
    # Check if there's aapt tool installed
    if ! which aapt > /dev/null; then
        echo "Android App Packaging Tool is not installed!"
        install_soft aapt
    fi

    # Check PostgreSQL installation
    if ! which psql > /dev/null; then
        echo "PostgreSQL is not installed!"
        install_soft postgresql
        exit 1
    fi
fi      # end of the build/deploy prerequisites skipped by TLS-only mode

# Check if tomcat user exists
getent passwd $TOMCAT_USER > /dev/null
if [ "$?" -ne 0 ]; then
    # Try tomcat8
    TOMCAT_USER="tomcat8"
    getent passwd $TOMCAT_USER >/dev/null
    if [ "$?" -ne 0 ]; then
        echo "Tomcat is not installed! User tomcat not found."
        echo "If you're running Tomcat as different user,"
        echo "please edit this script and update the TOMCAT_USER variable."
        exit 1
    fi
fi


# Search for the WAR. Skipped by TLS-only mode: nothing is deployed there, and
# requiring a compiled WAR would make certificate recovery depend on a build.
if [ "$TLS_RECOVERY_ONLY" != "1" ]; then
    SERVER_WAR=./server/target/launcher.war
    if [ ! -f $SERVER_WAR ]; then
        SERVER_WAR=$(ls hmdm*.war | tail -1)
    fi
    if [ ! -f $SERVER_WAR ]; then
        echo "FAILED to find the WAR file of Headwind MDM!"
        echo "Did you compile the project?"
        exit 1
    fi
fi

# Check the Tomcat base folder
if [ ! -d "$TOMCAT_HOME" ]; then
    read -e -p "Enter the Tomcat base directory: " TOMCAT_HOME
    if [ ! -d "$TOMCAT_HOME" ]; then
        echo "The directory $TOMCAT_HOME does not exist."
        echo "Headwind MDM installer requires this directory to install the WAR file!"
        exit 1
    fi
fi

#read -p "Are you installing an open-source version? (Y/n)? " -n 1 -r
#echo
#if [[ $REPLY =~ ^[Yy]$ ]]; then
    CLIENT_VARIANT="os"
#else
#    CLIENT_VARIANT="master"
#fi

CLIENT_APK="hmdm-$CLIENT_VERSION-$CLIENT_VARIANT.apk"

# --- TLS-only resume mode: the prompts ------------------------------------
# TLS_RECOVERY_ONLY itself was decided near the top, before the build prerequisites.
# HMDM_SKIP_ISSUANCE and HMDM_REPUBLISH_ONLY are recovery entry points: the
# operator is resuming a run whose certificate work failed AFTER the application
# was already installed. Re-running the FULL installer for that is not merely
# wasteful -- on a host whose database is already populated it stops at the
# "type erase to continue" prompt, and that prompt has exactly two outcomes:
# DESTROY the database, or abort with "installation aborted". There was no third
# path, so the resume command every failure branch below prints could not
# actually be followed on the hosts that need it most.
#
# In this mode the installer asks only for the values the TLS section consumes
# and skips database setup, file storage, SMTP, WAR deployment and the APK sync.
# Nothing outside /etc/hmdm/tls, the renewal script and server.xml is touched.
if [ "$TLS_RECOVERY_ONLY" = "1" ]; then
    echo
    echo "======================================"
    echo "TLS-ONLY MODE: resuming certificate setup on an existing installation."
    echo "The database, the file storage, SMTP settings and the deployed"
    echo "application are left exactly as they are. Nothing is erased, nothing is"
    echo "redeployed, and you will NOT be asked to clear the database."
    echo "======================================"
    echo

    # These must match the original run: the domain selects the certificate
    # lineage, and the two paths decide where the renewal script lives and which
    # URL the closing banner prints.
    read -e -p "Headwind MDM scripts directory [$DEFAULT_SCRIPT_LOCATION]: " -i "$DEFAULT_SCRIPT_LOCATION" SCRIPT_LOCATION
    if [ ! -d "$SCRIPT_LOCATION" ]; then
        mkdir -p "$SCRIPT_LOCATION" || exit 1
    fi
    while [ -z "$BASE_DOMAIN" ]; do
        read -e -p "Domain name or public IP (e.g. example.com): " -i "$DEFAULT_BASE_DOMAIN" BASE_DOMAIN
        if [ -z "$BASE_DOMAIN" ]; then
            echo "Please enter a non-empty domain name"
        fi
    done
    read -e -p "Project path on server (e.g. /hmdm) or ROOT: " -i "$DEFAULT_BASE_PATH" BASE_PATH
    if [ "$BASE_PATH" == "ROOT" ]; then
        BASE_PATH=""
    fi
    # The closing banner builds its URL from these. HTTPS on the default port is
    # the only thing this mode can produce, so they are not prompted for.
    PROTOCOL=https
    BASE_HOST="$BASE_DOMAIN"
fi

# Everything from here to the HTTPS prompt is the full installation. TLS-only
# mode skips the whole span rather than guarding each block, so the code inside
# is unchanged and a reviewer sees only this one condition.
if [ "$TLS_RECOVERY_ONLY" != "1" ]; then

read -e -p "Please choose the installation language (en/ru) [en]: " -i "en" LANGUAGE
echo

echo "PostgreSQL database setup"
echo "========================="
echo "Make sure you've installed PostgreSQL and created the database."
echo "If you didn't create a database yet, please click Ctrl-C to break,"
echo "then execute the following commands:"
echo "-------------------------"
echo "su postgres"
echo "psql"
echo "CREATE USER hmdm WITH PASSWORD 'topsecret';"
echo "CREATE DATABASE hmdm WITH OWNER=hmdm;"
echo "\q"
echo "exit"
echo "-------------------------"

read -e -p "PostgreSQL host [$DEFAULT_SQL_HOST]: " -i "$DEFAULT_SQL_HOST" SQL_HOST
read -e -p "PostgreSQL port [$DEFAULT_SQL_PORT]: " -i "$DEFAULT_SQL_PORT" SQL_PORT
read -e -p "PostgreSQL database [$DEFAULT_SQL_BASE]: " -i "$DEFAULT_SQL_BASE" SQL_BASE
read -e -p "PostgreSQL user [$DEFAULT_SQL_USER]: " -i "$DEFAULT_SQL_USER" SQL_USER
read -e -p "PostgreSQL password: " -i "$DEFAULT_SQL_PASS" SQL_PASS

PSQL_CONNSTRING="postgresql://$SQL_USER:$SQL_PASS@$SQL_HOST:$SQL_PORT/$SQL_BASE"

# Check the PostgreSQL access
echo "SELECT 1" | psql $PSQL_CONNSTRING > /dev/null 2>&1
if [ "$?" -ne 0 ]; then
    echo "Failed to connect to $SQL_HOST:$SQL_PORT/$SQL_BASE as $SQL_USER!"
    echo "Please make sure you've created the database!"
    exit 1
fi

TABLE_EXISTS=$(echo "\dt users" | psql $PSQL_CONNSTRING 2>&1 | grep public)
if [ ! -z "$TABLE_EXISTS" ]; then
    echo "The database is already setup."
    echo "To re-deploy Headwind MDM, the database needs to be cleared."
    echo "Clear the database? ALL DATA WILL BE LOST!"
    read -e -p "Type \"erase\" to clear the database and continue setup: " RESPONSE
    if [ "$RESPONSE" == "erase" ]; then
        echo "DROP TABLE IF EXISTS applicationfilestocopytemp, applications, applicationversions, applicationversionstemp, configurationapplicationparameters, configurationapplications, configurationapplicationsettings, configurationfiles, configurations, customers, databasechangelog, databasechangeloglock, deviceapplicationsettings, devicegroups, devices, devicestatuses, groups, icons, pendingpushes, pendingsignup, permissions, plugin_apuppet_data, plugin_apuppet_settings, plugin_audit_log, plugin_deviceinfo_deviceparams, plugin_deviceinfo_deviceparams_device, plugin_deviceinfo_deviceparams_gps, plugin_deviceinfo_deviceparams_mobile, plugin_deviceinfo_deviceparams_mobile2, plugin_deviceinfo_deviceparams_wifi, plugin_deviceinfo_settings, plugin_devicelocations_history, plugin_devicelocations_latest, plugin_devicelocations_settings, plugin_devicelog_log, plugin_devicelog_setting_rule_devices, plugin_devicelog_settings, plugin_devicelog_settings_rules, plugin_devicereset_status, plugin_knox_rules, plugin_messaging_messages, plugin_openvpn_defaults, plugin_photo_photo, plugin_photo_photo_places, plugin_photo_places, plugin_photo_settings, plugin_push_messages, plugin_push_schedule, plugin_urlfilter_lists, plugins, pluginsdisabled, pushmessages, settings, trialkey, uploadedfiles, usagestats, userconfigurationaccess, userdevicegroupsaccess, userhints, userhinttypes, userrolepermissions, userroles, userrolesettings, users CASCADE" |  psql $PSQL_CONNSTRING >/dev/null 2>&1
	echo "Database has been cleared."
    else
        echo "Headwind MDM installation aborted"
	exit 1
    fi
fi

echo
echo "File storage setup"
echo "=================="
echo "Please choose where the files uploaded to Headwind MDM will be stored"
echo "If the directory doesn't exist, it will be created"
echo "##### FOR TOMCAT 10, USE SANDBOXED DIR: /var/lib/tomcat10/work #####"
echo

read -e -p "Headwind MDM storage directory [$DEFAULT_LOCATION]: " -i "$DEFAULT_LOCATION" LOCATION

# Create directories
if [ ! -d $LOCATION ]; then
    mkdir -p $LOCATION || exit 1
    chown $TOMCAT_USER:$TOMCAT_USER $LOCATION || exit 1
fi
if [ ! -d $LOCATION/files ]; then
    mkdir $LOCATION/files
    chown $TOMCAT_USER:$TOMCAT_USER $LOCATION/files || exit 1
fi
if [ ! -d $LOCATION/plugins ]; then
    mkdir $LOCATION/plugins
    chown $TOMCAT_USER:$TOMCAT_USER $LOCATION/plugins || exit 1
fi
if [ ! -d $LOCATION/logs ]; then
    mkdir $LOCATION/logs
    chown $TOMCAT_USER:$TOMCAT_USER $LOCATION/logs || exit 1
fi

INSTALL_FLAG_FILE="$LOCATION/hmdm_install_flag"

# Logger configuration
cat ./install/logback_template.xml | sed "s|_BASE_DIRECTORY_|$LOCATION|g" > $LOCATION/logback-hmdm.xml
chown $TOMCAT_USER:$TOMCAT_USER $LOCATION/logback-hmdm.xml

echo
echo "Please choose the directory where supply scripts will be located."
echo
read -e -p "Headwind MDM scripts directory [$DEFAULT_SCRIPT_LOCATION]: " -i "$DEFAULT_SCRIPT_LOCATION" SCRIPT_LOCATION
if [ ! -d $SCRIPT_LOCATION ]; then
    mkdir -p $SCRIPT_LOCATION || exit 1
fi

echo
echo "Web application setup"
echo "====================="
echo "Headwind MDM requires access from Internet"
echo "Please assign a public domain name to this server"
echo

read -e -p "Protocol (http|https) [$DEFAULT_PROTOCOL]: " -i "$DEFAULT_PROTOCOL" PROTOCOL
while [ -z $BASE_DOMAIN ]; do
    read -e -p "Domain name or public IP (e.g. example.com): " -i "$DEFAULT_BASE_DOMAIN" BASE_DOMAIN
    if [ -z $BASE_DOMAIN ]; then
        echo "Please enter a non-empty domain name"
    fi
done
read -e -p "Port (e.g. 8080, leave empty for default ports 80 or 443): " -i "$DEFAULT_PORT" PORT
read -e -p "Project path on server (e.g. /hmdm) or ROOT: " -i "$DEFAULT_BASE_PATH" BASE_PATH

# Nobody changes it!
# read -e -p "Tomcat virtual host [$TOMCAT_HOST]: " -i "$TOMCAT_HOST" TOMCAT_HOST

# HTTPS via LetsEncrypt
echo
echo "To enable password recovery function, Headwind MDM must be connected to SMTP."
echo "Password recovery is an optional but recommended feature."
read -e -p "Setup SMTP credentials [Y/n]?: " -i "Y" REPLY

if [[ "$REPLY" =~ ^[Yy]$ ]]; then
    read -e -p "E-mail of the admin account: " ADMIN_EMAIL
    read -e -p "SMTP host (e.g. smtp.gmail.com): " SMTP_HOST
    read -e -p "SMTP port (e.g. 25, 465, or 587): " SMTP_PORT
    read -e -p "Use SSL (1 - use, 0 - not use): " -i "0" SMTP_SSL
    read -e -p "Use STARTTLS (1 - use, 0 - not use): " -i "0" SMTP_STARTTLS
    read -e -p "SMTP username (leave empty if no auth required): " SMTP_USERNAME
    read -e -p "SMTP password (leave empty if no auth required): " SMTP_PASSWORD
    read -e -p "Sender e-mail address: " SMTP_FROM
fi

TOMCAT_DEPLOY_PATH=$BASE_PATH
if [ "$BASE_PATH" == "ROOT" ]; then
    BASE_PATH=""
fi

if [[ ! -z "$PORT" ]]; then
    BASE_HOST="$BASE_DOMAIN:$PORT"
else
    BASE_HOST="$BASE_DOMAIN"
fi

echo
echo "Ready to install!"
echo "Location on server: $LOCATION"
echo "URL: $PROTOCOL://$BASE_HOST$BASE_PATH"
read -p "Is this information correct [Y/n]? " -n 1 -r
echo

if [[ ! "$REPLY" =~ ^[Yy]$ ]]; then
    exit 1
fi

# Prepare the XML config
if [ ! -f ./install/context_template.xml ]; then
    echo "ERROR: Missing ./install/context_template.xml!"
    echo "The package seems to be corrupted!"
    exit 1
fi

# Removing old application if required
if [ -d $TOMCAT_HOME/webapps/$TOMCAT_DEPLOY_PATH ]; then
    rm -rf $TOMCAT_HOME/webapps/$TOMCAT_DEPLOY_PATH > /dev/null 2>&1
    rm -f $TOMCAT_HOME/webapps/$TOMCAT_DEPLOY_PATH.war > /dev/null 2>&1
    echo "Waiting for undeploying the previous version"
    for i in {1..10}; do
        echo -n "."
        sleep 1
    done
    echo
fi

TOMCAT_CONFIG_PATH=$TOMCAT_HOME/conf/$TOMCAT_ENGINE/$TOMCAT_HOST
if [ ! -d $TOMCAT_CONFIG_PATH ]; then
    mkdir -p $TOMCAT_CONFIG_PATH || exit 1
    chown root:$TOMCAT_USER $TOMCAT_CONFIG_PATH
    chmod 755 $TOMCAT_CONFIG_PATH
fi
cat ./install/context_template.xml | sed "s|_SQL_HOST_|$SQL_HOST|g; s|_SQL_PORT_|$SQL_PORT|g; s|_SQL_BASE_|$SQL_BASE|g; s|_SQL_USER_|$SQL_USER|g; s|_SQL_PASS_|$SQL_PASS|g; s|_BASE_DIRECTORY_|$LOCATION|g; s|_PROTOCOL_|$PROTOCOL|g; s|_BASE_HOST_|$BASE_HOST|g; s|_BASE_DOMAIN_|$BASE_DOMAIN|g; s|_BASE_PATH_|$BASE_PATH|g; s|_INSTALL_FLAG_|$INSTALL_FLAG_FILE|g; s|_SMTP_HOST_|$SMTP_HOST|g; s|_SMTP_PORT_|$SMTP_PORT|g;  s|_SMTP_SSL_|$SMTP_SSL|g; s|_SMTP_STARTTLS_|$SMTP_STARTTLS|g; s|_SMTP_USERNAME_|$SMTP_USERNAME|g; s|_SMTP_PASSWORD_|$SMTP_PASSWORD|g; s|_SMTP_FROM_|$SMTP_FROM|g;" > $TOMCAT_CONFIG_PATH/$TOMCAT_DEPLOY_PATH.xml
if [ "$?" -ne 0 ]; then
    echo "Failed to create a Tomcat config file $TOMCAT_CONFIG_PATH/$TOMCAT_DEPLOY_PATH.xml!"
    exit 1
fi
echo "Tomcat config file created: $TOMCAT_CONFIG_PATH/$TOMCAT_DEPLOY_PATH.xml"
chmod 644 $TOMCAT_CONFIG_PATH/$TOMCAT_DEPLOY_PATH.xml
cp $TOMCAT_CONFIG_PATH/$TOMCAT_DEPLOY_PATH.xml $TOMCAT_CONFIG_PATH/$TOMCAT_DEPLOY_PATH.xml~

echo "Deploying $SERVER_WAR to Tomcat: $TOMCAT_HOME/webapps/$TOMCAT_DEPLOY_PATH.war"
rm -f $INSTALL_FLAG_FILE > /dev/null 2>&1
cp $SERVER_WAR $TOMCAT_HOME/webapps/$TOMCAT_DEPLOY_PATH.war
chmod 644 $TOMCAT_HOME/webapps/$TOMCAT_DEPLOY_PATH.war

# Waiting until the end of deployment
SUCCESSFUL_DEPLOY=0
for i in {1..120}; do
    if [ -f $INSTALL_FLAG_FILE ]; then
        if [[ $(< $INSTALL_FLAG_FILE) == "OK" ]]; then
            SUCCESSFUL_DEPLOY=1
        else
            SUCCESSFUL_DEPLOY=0
        fi
        break
    fi
    echo -n "."
    sleep 1
done
echo
rm -f $INSTALL_FLAG_FILE > /dev/null 2>&1
if [ $SUCCESSFUL_DEPLOY -ne 1 ]; then
    echo "ERROR: failed to deploy WAR file!"
    echo "Please check $TOMCAT_HOME/logs/catalina.out for details."
    exit 1
fi
echo "Deployment successful, initializing the database..."

# Initialize database
cat ./install/sql/hmdm_init.$LANGUAGE.sql | sed "s|_HMDM_BASE_|$LOCATION|g; s|_HMDM_VERSION_|$CLIENT_VERSION|g; s|_HMDM_APK_|$CLIENT_APK|g; s|_ADMIN_EMAIL_|$ADMIN_EMAIL|g;" > $TEMP_SQL_FILE
cat $TEMP_SQL_FILE | psql $PSQL_CONNSTRING > /dev/null 2>&1
if [ "$?" -ne 0 ]; then
    echo "ERROR: failed to execute SQL script!"
    echo "See $TEMP_SQL_FILE for details."
    exit 1
fi
rm -f $TEMP_SQL_FILE > /dev/null 2>&1

echo
echo "======================================"
echo "Minimal installation of Headwind MDM has been done!"
echo "At this step, you can open in your web browser:"
echo "http://$BASE_DOMAIN:8080$BASE_PATH"
echo "Login: admin:admin"
echo "======================================"
echo

fi      # end of the full-installation span skipped by TLS-only mode

# HTTPS via LetsEncrypt
if [ "$TLS_RECOVERY_ONLY" = "1" ]; then
    # Certificate work is the entire purpose of this mode, so the prompt would
    # only offer a dead end: answering "n" would skip straight to the closing
    # banner having done nothing at all.
    REPLY=Y
else
    read -e -p "Setup HTTPS via LetsEncrypt [Y/n]?: " -i "Y" REPLY
fi

if [[ "$REPLY" =~ ^[Yy]$ ]]; then
    if ! which certbot > /dev/null; then
        apt update
        apt install -y certbot
    fi
    # --- certbot deploy hook: publish PEMs where Tomcat and the MQTT broker can read them ---
    #
    # Certbot creates /etc/letsencrypt/{live,archive} root-only, so neither Tomcat's
    # HTTPS connector nor the embedded broker can read the key there. A deploy hook
    # stages each renewal into /etc/hmdm/tls instead. An ACL on /etc/letsencrypt was
    # rejected as the alternative: it would grant the Tomcat account read access to
    # the private key of every domain on the host.
    #
    # This must be installed by hmdm_install.sh rather than by letsencrypt-ssl.sh,
    # because the renewal script runs from $SCRIPT_LOCATION (and from cron), where
    # the checkout that contains the hook may not exist at all.

    # Explicit certificate name. Pinning it makes the lineage path deterministic:
    # --cert-name targets one specific lineage, and reusing the name updates that
    # lineage instead of creating a colliding <domain>-0001.
    #
    # Domain-qualified, NOT a bare "hmdm": --cert-name modifies whatever certificate
    # already bears that name, so a generic name could silently repurpose an
    # unrelated lineage on a shared host.
    CERT_NAME="${CERT_NAME:-hmdm-$BASE_DOMAIN}"

    # --- recovery identity check: BEFORE anything persistent is rewritten ----
    # In TLS-only mode the operator retypes the domain, and CERT_NAME is derived
    # from it. Everything downstream rewrites persistent renewal identity --
    # hook.env and the installed letsencrypt-ssl.sh. A mistyped domain would
    # therefore repoint this host's renewal configuration at a lineage that does
    # not exist and leave it that way even if a later configuration step aborts.
    # The next unattended cron renewal would then fail against the wrong name.
    #
    # Recovery is by definition resuming an existing setup, so the installed
    # hook.env is the AUTHORITY, not an optional cross-check. The installer writes
    # it before it ever calls letsencrypt-ssl.sh, so any run that got as far as
    # requesting a certificate has one; its absence means no previous run reached
    # that point, and recovery is the wrong mode. Making it conditional left open
    # exactly the gap it was added to close: validation skipped when the file is
    # missing, and the renewal identity overwritten regardless a few hundred
    # lines later.
    if [ "$TLS_RECOVERY_ONLY" = "1" ]; then
        if [ ! -r /etc/hmdm/tls/hook.env ]; then
            echo "======================================"
            echo "ERROR: /etc/hmdm/tls/hook.env is missing or unreadable, so this host has"
            echo "       no renewal identity to resume. The installer writes that file"
            echo "       before it ever requests a certificate, so its absence means no"
            echo "       previous run got that far."
            echo
            echo "Nothing has been modified. Unset HMDM_SKIP_ISSUANCE / HMDM_REPUBLISH_ONLY"
            echo "and run the installer normally to set this host up."
            echo "======================================"
            exit 1
        fi

        # Read in a subshell with the names unset first, so a hook.env that omits
        # one cannot silently return OUR value and make the comparison vacuous.
        EXISTING_CERT_NAME=$(unset CERT_NAME; . /etc/hmdm/tls/hook.env 2>/dev/null; echo "$CERT_NAME")
        EXISTING_LINEAGE=$(unset HMDM_LINEAGE; . /etc/hmdm/tls/hook.env 2>/dev/null; echo "$HMDM_LINEAGE")

        # BOTH must be present. An empty value is not "nothing to check against";
        # it is a hook.env that cannot drive a renewal at all, and
        # letsencrypt-ssl.sh refuses to run against one for the same reason.
        if [ -z "$EXISTING_CERT_NAME" ] || [ -z "$EXISTING_LINEAGE" ]; then
            echo "======================================"
            echo "ERROR: /etc/hmdm/tls/hook.env does not define both CERT_NAME and"
            echo "       HMDM_LINEAGE, so this host's renewal identity cannot be confirmed:"
            echo "         CERT_NAME=${EXISTING_CERT_NAME:-<empty>}"
            echo "         HMDM_LINEAGE=${EXISTING_LINEAGE:-<empty>}"
            echo
            echo "Nothing has been modified. Repair that file, or remove it and run the"
            echo "installer normally to regenerate it."
            echo "======================================"
            exit 1
        fi

        if [ "$EXISTING_CERT_NAME" != "$CERT_NAME" ]; then
            echo "======================================"
            echo "ERROR: this host's renewal configuration is for the certificate"
            echo "         $EXISTING_CERT_NAME"
            echo "       but the domain you entered produces"
            echo "         $CERT_NAME"
            echo
            echo "Nothing has been modified. Re-run and enter the domain this host was"
            echo "originally set up with, or unset HMDM_SKIP_ISSUANCE / HMDM_REPUBLISH_ONLY"
            echo "and run the installer normally to configure a different domain."
            echo "======================================"
            exit 1
        fi

        # The lineage recorded in hook.env must agree with the name, exactly as
        # letsencrypt-ssl.sh requires of it. A disagreement means hook.env was
        # hand-edited inconsistently, and rewriting it here would quietly repair
        # the symptom while leaving whatever caused it unexamined. Unconditional
        # now -- an empty lineage was rejected above, so nothing skips this.
        if [ "$EXISTING_LINEAGE" != "/etc/letsencrypt/live/$CERT_NAME" ]; then
            echo "======================================"
            echo "ERROR: /etc/hmdm/tls/hook.env is internally inconsistent:"
            echo "         CERT_NAME=$EXISTING_CERT_NAME"
            echo "         HMDM_LINEAGE=$EXISTING_LINEAGE"
            echo "       Nothing has been modified. Fix that file before resuming."
            echo "======================================"
            exit 1
        fi
    fi

    # Resolve the Tomcat group deterministically. A username is not its primary group.
    TOMCAT_GROUP=$(id -gn "$TOMCAT_USER") || { echo "cannot resolve group for $TOMCAT_USER"; exit 1; }

    # Key material: root-owned, Tomcat-group-readable.
    # Check installation inputs before issuing or publishing a certificate.
    SERVER_XML_TEMPLATE="./install/server_template.xml"
    # Configuration semantics are covered by install/tests/server-template-test.sh.
    if ! command -v xmllint >/dev/null 2>&1; then
        echo "Installing libxml2-utils (xmllint), required to check XML syntax..."
        apt install -y libxml2-utils
    fi
    if ! command -v xmllint >/dev/null 2>&1; then
        echo "======================================"
        echo "ERROR: xmllint is required and could not be installed."
        echo "Install it with: apt install libxml2-utils"
        echo "No certificate was issued or published by this run."
        echo "======================================"
        exit 1
    fi

    if ! xmllint --noout "$SERVER_XML_TEMPLATE"; then
        echo "ERROR: $SERVER_XML_TEMPLATE is missing or invalid XML."
        echo "Restore the template and re-run. No certificate was issued or published by this run."
        exit 1
    fi
    # The old file must be readable for backup, but may contain broken XML.
    if [ ! -r "$TOMCAT_HOME/conf/server.xml" ]; then
        echo "ERROR: $TOMCAT_HOME/conf/server.xml is missing or unreadable."
        echo "No certificate was issued or published by this run."
        exit 1
    fi

    install -d -o root -g "$TOMCAT_GROUP" -m 0750 /etc/hmdm/tls
    # Runtime state: Tomcat-owned, because the application's pemcfg writer creates
    # its temp file HERE and so needs write permission on the directory itself.
    # This is why the pemcfg does not live beside the keys.
    install -d -o "$TOMCAT_USER" -g "$TOMCAT_GROUP" -m 0750 /var/lib/hmdm/mqtt
    mkdir -p /etc/letsencrypt/renewal-hooks/deploy

    # Write hook.env COMPLETE, before the hook exists on disk. All values are known
    # now: the account and group from id(1), the name we chose, the lineage derived
    # from it. Writing it in two stages would leave a window in which the hook is
    # installed but cannot resolve HMDM_LINEAGE.
    #
    # hook.env is the single source of CERT_NAME -- it is not sed-substituted into
    # the renewal script. One file defines the name, the lineage, the account and
    # the group together, and both the hook and the renewal script source it. Two
    # mechanisms could drift apart; one cannot.
    ( umask 077
      cat > /etc/hmdm/tls/hook.env <<EOF
CERT_NAME=$CERT_NAME
TOMCAT_USER=$TOMCAT_USER
TOMCAT_GROUP=$TOMCAT_GROUP
HMDM_LINEAGE=/etc/letsencrypt/live/$CERT_NAME
EOF
    )
    chown root:root /etc/hmdm/tls/hook.env && chmod 0600 /etc/hmdm/tls/hook.env

    # Only now install the hook, from the checkout, where it exists.
    install -o root -g root -m 0755 ./install/hmdm-tls-deploy-hook.sh \
            /etc/letsencrypt/renewal-hooks/deploy/hmdm-tls

    sed "s/DOMAIN=your-domain.com/DOMAIN=$BASE_DOMAIN/" ./letsencrypt-ssl.sh > $SCRIPT_LOCATION/letsencrypt-ssl.sh
    chmod +x $SCRIPT_LOCATION/letsencrypt-ssl.sh

    # Resume after publication without requesting another certificate.
    if [ "${HMDM_SKIP_ISSUANCE:-}" = "1" ]; then
        echo
        echo "HMDM_SKIP_ISSUANCE=1: reusing the certificate already published at"
        echo "/etc/hmdm/tls/current -- certbot will NOT run and nothing is re-issued."

        # Publication validation belongs to the deploy hook, and Tomcat validates
        # and loads the configured PEM files when the staged server.xml is activated.
        # Do not duplicate certificate parsing, SAN/expiry checks, key matching or
        # Tomcat-user readability checks here.
        LE_STATUS=0
    else
        $SCRIPT_LOCATION/letsencrypt-ssl.sh
        LE_STATUS=$?
    fi

    # Preserve the renewal script's distinct recovery outcomes.
    case "$LE_STATUS" in
        0) ;;
        2)
            echo "ERROR: certificate published, but $TOMCAT_SERVICE recovery restart failed."
            echo "Check: journalctl -u $TOMCAT_SERVICE -n 50"
            echo "After fixing Tomcat, resume without issuance: HMDM_SKIP_ISSUANCE=1 $0"
            exit 1
            ;;
        3)
            echo "ERROR: certificate available at /etc/letsencrypt/live/$CERT_NAME, but publication failed."
            echo "Check the deploy-hook error above and: ls -l /etc/hmdm/tls/"
            echo "Retry publication without issuance: HMDM_REPUBLISH_ONLY=1 $0"
            exit 1
            ;;
        *)
            echo "ERROR: $SCRIPT_LOCATION/letsencrypt-ssl.sh failed (status $LE_STATUS)."
            echo "Check the errors above and inspect the certificate state:"
            echo "  ls -l /etc/hmdm/tls/current /etc/letsencrypt/live/$CERT_NAME"
            echo "Recovery options: README.md (TLS recovery). Do not re-issue blindly."
            exit 1
            ;;
    esac

    echo
    echo "Automatic setup REPLACES server.xml with $SERVER_XML_TEMPLATE."
    echo "Use this only for a dedicated, uncustomized Tomcat installation."
    echo "Custom connectors, virtual hosts and realms will NOT be preserved."
    echo "Choose n to merge the TLS settings manually instead."
    echo "If Tomcat won't work after update, please revert the config back:"
    echo "cp $TOMCAT_HOME/conf/server.xml~ $TOMCAT_HOME/conf/server.xml"
    echo "======================================"
    echo

    # Tracks configuration and restart, not HTTPS readiness.
    AUTO_CONFIGURED=0

    read -e -p "Replace Tomcat config with the HMDM template [y/N]?: " -i "N" REPLY
    if [[ "$REPLY" =~ ^[Yy]$ ]]; then
        SERVER_XML="$TOMCAT_HOME/conf/server.xml"
        SERVER_XML_BACKUP="$TOMCAT_HOME/conf/server.xml~"
        SERVER_XML_STAGED="$TOMCAT_HOME/conf/.server.xml.hmdm.$$"

        # Stage changes; leave the live configuration untouched until replacement.
        tls_config_abort() {
            rm -f "$SERVER_XML_STAGED"
            echo "======================================"
            echo "ERROR: $*"
            echo
            echo "$SERVER_XML was NOT modified and Tomcat was NOT restarted."
            echo "The certificate is published at /etc/hmdm/tls/current/ and is unaffected."
            echo "Fix the cause, then resume without re-issuing it:"
            echo "  HMDM_SKIP_ISSUANCE=1 $0"
            echo "======================================"
            exit 1
        }

        if ! cp -p "$SERVER_XML" "$SERVER_XML_BACKUP"; then
            tls_config_abort "could not back up $SERVER_XML to $SERVER_XML_BACKUP"
        fi
        # cp -p first so the staging file inherits server.xml's mode and ownership.
        # A redirection into an existing file rewrites its contents and changes
        # neither, so the file that is finally renamed into place carries the
        # permissions Tomcat expects.
        if ! cp -p "$SERVER_XML" "$SERVER_XML_STAGED"; then
            tls_config_abort "could not create the staging file $SERVER_XML_STAGED"
        fi

        # The template owns the connector and listener declarations. No XML
        # insertion or matching against a distribution's commented examples.
        if ! cat "$SERVER_XML_TEMPLATE" > "$SERVER_XML_STAGED"; then
            tls_config_abort "could not stage $SERVER_XML_TEMPLATE"
        fi

        if ! xmllint --noout "$SERVER_XML_STAGED"; then
            tls_config_abort "the staged server.xml is not well-formed XML"
        fi

        # Preserve the live configuration's ownership and permissions.
        if ! chown --reference="$SERVER_XML" "$SERVER_XML_STAGED" \
                || ! chmod --reference="$SERVER_XML" "$SERVER_XML_STAGED"; then
            tls_config_abort "could not set ownership/mode on $SERVER_XML_STAGED"
        fi

        # One rename, and only now. Everything above this line left the running
        # configuration untouched.
        if ! mv -f "$SERVER_XML_STAGED" "$SERVER_XML"; then
            tls_config_abort "could not install the verified server.xml"
        fi

        # Leave a failed configuration in place for diagnosis; recovery is manual.
        if ! service "$TOMCAT_SERVICE" restart; then
            echo "ERROR: $TOMCAT_SERVICE failed to restart. It may be unavailable."
            echo "The new configuration remains at $SERVER_XML."
            echo "Check: journalctl -u $TOMCAT_SERVICE -n 50"
            echo "Previous configuration saved at $SERVER_XML_BACKUP (not automatically restored)."
            echo "The published certificate is unchanged. After fixing the cause, resume:"
            echo "  HMDM_SKIP_ISSUANCE=1 $0"
            exit 1
        fi
        AUTO_CONFIGURED=1
    fi

    # Read the renewal state BEFORE the banner that describes it. The cron entry
    # is inspected further down, but the banner asserts something about it first,
    # and on a recovery run against a host that already has the entry the old
    # wording ("not configured yet") was simply false.
    CERTBOT_RENEWAL=$(crontab -l 2>/dev/null | grep letsencrypt-ssl.sh)

    echo
    echo "======================================"
    # Tomcat can keep running after an HTTPS connector fails. A successful service
    # restart alone does not verify HTTPS; leave that check explicit for the operator.
    if [ "$AUTO_CONFIGURED" = "1" ]; then
        echo "Tomcat configuration installed and service restart completed."
        echo "HTTPS has NOT been verified. Open this URL to confirm it works:"
        echo "https://$BASE_DOMAIN:8443$BASE_PATH"
        echo "If it does not work, check: journalctl -u $TOMCAT_SERVICE -n 50"
    else
        echo "Certificate provisioning is complete, but MANUAL HTTPS SETUP REMAINS."
        echo "The certificate is published at /etc/hmdm/tls/current/."
        echo "This run has not configured or restarted Tomcat."
        echo
        echo "Merge the HTTPS Connector and TLS reload Listener from $SERVER_XML_TEMPLATE into"
        echo "  $TOMCAT_HOME/conf/server.xml"
        echo "then restart Tomcat:"
        echo "  service $TOMCAT_SERVICE restart"
        echo "Then verify: https://$BASE_DOMAIN:8443$BASE_PATH"
        echo
        # State the renewal situation as it actually is, read above. The old
        # wording asserted "not configured yet" before anything had looked.
        if [ -n "$CERTBOT_RENEWAL" ]; then
            echo "Automatic renewal is already configured in cron and stays as it is."
        else
            echo "Automatic renewal is NOT configured yet -- it is the next question this"
            echo "installer asks, and it only happens if you accept there."
        fi
    fi
    echo
    echo "Notice: if Tomcat starts slowly:"
    echo "Open the Java security config, e.g. /etc/java-21-openjdk/security/java.security"
    echo "Replace securerandom.source=file:/dev/random"
    echo "to securerandom.source=file:/dev/urandom"
    echo "and restart Tomcat."
    echo "======================================"
    echo

    # CERTBOT_RENEWAL was read above, before the banner that describes it.
    if [ -z "$CERTBOT_RENEWAL" ]; then
        read -e -p "Setup regular HTTPS certificate renewal [Y/n]?: " -i "Y" REPLY
        if [[ "$REPLY" =~ ^[Yy]$ ]]; then
            crontab -l > /tmp/current-crontab
            echo "0 5 * * 1 $SCRIPT_LOCATION/letsencrypt-ssl.sh" >> /tmp/current-crontab
	    crontab /tmp/current-crontab
	    rm /tmp/current-crontab
        fi
    fi
fi

# Redirect the ports
IPTABLES_HTTPS_SET=$(/sbin/iptables -t nat --list | grep 8443)
if [ -z "$IPTABLES_HTTPS_SET" ]; then
    read -e -p "Use iptables to redirect port 443 to 8443 [Y/n]?: " -i "Y" REPLY
    if [[ "$REPLY" =~ ^[Yy]$ ]]; then
        cp iptables-tomcat.sh $SCRIPT_LOCATION/iptables-tomcat.sh
	chmod +x $SCRIPT_LOCATION/iptables-tomcat.sh
	$SCRIPT_LOCATION/iptables-tomcat.sh

        IPTABLES_RENEWAL=$(crontab -l | grep iptables-tomcat.sh)
	if [ -z "$IPTABLES_RENEWAL" ]; then
            crontab -l > /tmp/current-crontab
	    echo "@reboot $SCRIPT_LOCATION/iptables-tomcat.sh" >> /tmp/current-crontab
            crontab /tmp/current-crontab
	    rm /tmp/current-crontab
	fi
    fi
fi

# Download required files. Skipped by TLS-only mode: it queries the database and
# writes into $LOCATION/files, and that mode configures neither.
if [ "$TLS_RECOVERY_ONLY" != "1" ]; then
read -e -p "Move required APKs from h-mdm.com to your server [Y/n]?: " -i "Y" REPLY
if [[ "$REPLY" =~ ^[Yy]$ ]]; then
    FILES=$(echo "SELECT url FROM applicationversions WHERE url IS NOT NULL" | psql $PSQL_CONNSTRING 2>/dev/null | tail -n +3 | head -n -2)
    CURRENT_DIR=$(pwd)
    cd $LOCATION/files
    for FILE in $FILES; do
        echo "Downloading $FILE..."
	wget $FILE
    done
    chown $TOMCAT_USER:$TOMCAT_USER *
    echo "UPDATE applicationversions SET url=REPLACE(url, 'https://h-mdm.com', '$PROTOCOL://$BASE_HOST$BASE_PATH') WHERE url IS NOT NULL" | psql $PSQL_CONNSTRING >/dev/null 2>&1
    cd $CURRENT_DIR
fi
fi

echo
echo "======================================"
if [ "$TLS_RECOVERY_ONLY" = "1" ]; then
    # Do not claim an installation happened: this mode deliberately touched only
    # the certificate, the renewal script and server.xml.
    echo "TLS-only run is complete."
    echo "The database, the file storage and the deployed application were not"
    echo "modified. Your existing credentials are unchanged."
    echo "Web panel:"
    echo "$PROTOCOL://$BASE_HOST$BASE_PATH"
else
    echo "Headwind MDM installation is completed!"
    echo "To access your web panel, open in the web browser:"
    echo "$PROTOCOL://$BASE_HOST$BASE_PATH"
    echo "Login: admin:admin"
fi
echo "======================================"
echo
