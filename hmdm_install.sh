#!/bin/bash
#
# Headwind MDM installer script
# Tested on Ubuntu Linux 18.04 - 20.10, Ubuntu 20.04 is recommended
#
REPOSITORY_BASE=https://h-mdm.com/files
CLIENT_VERSION=4.17
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
    apt install -y aapt tomcat9 postgresql vim
    TOMCAT_HOME=$(ls -d /var/lib/tomcat* | tail -n1)
    TOMCAT_USER=$(ls -ld $TOMCAT_HOME/webapps | awk '{print $3}')
}

# Use sandbox directory for tomcat 9
if [ "$TOMCAT_HOME" == "/var/lib/tomcat9" ]; then
    DEFAULT_LOCATION="/var/lib/tomcat9/work"
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

# Check for Tomcat version in Ubuntu 20.04
OUTDATED_TOMCAT=$(/usr/share/tomcat9/bin/version.sh 2>&1 | grep "Server number" | grep "9.0.31")
if [ ! -z "$OUTDATED_TOMCAT" ]; then
    echo "Tomcat $OUTDATED_TOMCAT requires updating!"
    read -e -p "Update it now [Y/n]?: " -i "Y" REPLY
    if [[ "$REPLY" =~ ^[Yy]$ ]]; then
        # This clause is only for tomcat 9 so do not bother for the major version
        VERSION=9.0.40
        wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.40/bin/apache-tomcat-${VERSION}.tar.gz
        tar -zxf apache-tomcat-${VERSION}.tar.gz
        cd apache-tomcat-${VERSION}
        chmod a+x bin
        chmod a+x lib
        chmod -R a+r bin
        chmod -R a+r lib
        chmod a+x bin/*.sh
        mv /usr/share/tomcat9/bin /usr/share/tomcat9/bin~
        mv /usr/share/tomcat9/lib /usr/share/tomcat9/lib~
        cp -r bin /usr/share/tomcat9
        cp -r lib /usr/share/tomcat9
        service tomcat9 restart
	cd ..
	rm -rf apache-tomcat-${VERSION}
	rm -f apache-tomcat-${VERSION}.tar.gz
    fi
fi

# Search for the WAR
SERVER_WAR=./server/target/launcher.war
if [ ! -f $SERVER_WAR ]; then
    SERVER_WAR=$(ls hmdm*.war | tail -1)
fi
if [ ! -f $SERVER_WAR ]; then
    echo "FAILED to find the WAR file of Headwind MDM!"
    echo "Did you compile the project?"
    exit 1
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
        echo "DROP TABLE IF EXISTS applicationfilestocopytemp, applications, applicationversions, applicationversionstemp, configurationapplicationparameters, configurationapplications, configurationapplicationsettings, configurationfiles, configurations, customers, databasechangelog, databasechangeloglock, deviceapplicationsettings, devicegroups, devices, devicestatuses, groups, icons, pendingpushes, permissions, plugin_apuppet_data, plugin_apuppet_settings, plugin_audit_log, plugin_deviceinfo_deviceparams, plugin_deviceinfo_deviceparams_device, plugin_deviceinfo_deviceparams_gps, plugin_deviceinfo_deviceparams_mobile, plugin_deviceinfo_deviceparams_mobile2, plugin_deviceinfo_deviceparams_wifi, plugin_deviceinfo_settings, plugin_devicelocations_history, plugin_devicelocations_latest, plugin_devicelocations_settings, plugin_devicelog_log, plugin_devicelog_setting_rule_devices, plugin_devicelog_settings, plugin_devicelog_settings_rules, plugin_devicereset_status, plugin_knox_rules, plugin_messaging_messages, plugin_openvpn_defaults, plugin_photo_photo, plugin_photo_photo_places, plugin_photo_places, plugin_photo_settings, plugins, pluginsdisabled, pushmessages, settings, trialkey, uploadedfiles, userconfigurationaccess, userdevicegroupsaccess, userhints, userhinttypes, userrolepermissions, userroles, userrolesettings, users" |  psql $PSQL_CONNSTRING >/dev/null 2>&1
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
echo "##### FOR TOMCAT 9, USE SANDBOXED DIR: /var/lib/tomcat9/work #####"
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
cat ./install/log4j_template.xml | sed "s|_BASE_DIRECTORY_|$LOCATION|g" > $LOCATION/log4j-hmdm.xml
chown $TOMCAT_USER:$TOMCAT_USER $LOCATION/log4j-hmdm.xml

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

# HTTPS via LetsEncrypt
read -e -p "Setup HTTPS via LetsEncrypt [Y/n]?: " -i "Y" REPLY

if [[ "$REPLY" =~ ^[Yy]$ ]]; then
    if ! which certbot > /dev/null; then
        apt update
        apt install -y certbot
    fi
    sed "s/DOMAIN=your-domain.com/DOMAIN=$BASE_DOMAIN/" ./letsencrypt-ssl.sh > $SCRIPT_LOCATION/letsencrypt-ssl.sh
    chmod +x $SCRIPT_LOCATION/letsencrypt-ssl.sh
    $SCRIPT_LOCATION/letsencrypt-ssl.sh

    echo
    echo "======================================"
    echo "The installer can try to update Tomcat config automatically."
    echo "Use this feature with care, ONLY IF YOU DIDN'T TOUCH server.xml"
    echo "If Tomcat won't work after update, please revert the config back:"
    echo "cp $TOMCAT_HOME/conf/server.xml~ $TOMCAT_HOME/conf/server.xml"
    echo "======================================"
    echo
    read -e -p "Update Tomcat config automatically [Y/n]?: " -i "Y" REPLY
    if [[ "$REPLY" =~ ^[Yy]$ ]]; then
        cp $TOMCAT_HOME/conf/server.xml $TOMCAT_HOME/conf/server.xml~
	# EPIC MAGIC!!!
        sed -z -e "s^<\!\-\-\n    <Connector port=\"8443\" protocol=\"org.apache.coyote.http11.Http11NioProtocol\"^<Connector port=\"8443\" protocol=\"org.apache.coyote.http11.Http11NioProtocol\"^" -e "s^\-\->\n    <\!\-\- Define an SSL/TLS HTTP/1.1 Connector on port 8443 with HTTP/2^<\!\-\- Define an SSL/TLS HTTP/1.1 Connector on port 8443 with HTTP/2^" -e "s^certificateKeystoreFile=\"conf/localhost-rsa.jks\"^certificateKeystoreFile=\"/var/lib/tomcat9/ssl/$BASE_DOMAIN.jks\" certificateKeystorePassword=\"123456\"^" $TOMCAT_HOME/conf/server.xml~ > $TOMCAT_HOME/conf/server.xml
	service $TOMCAT_SERVICE restart
    fi

    echo
    echo "======================================"
    echo "Secure installation of Headwind MDM has been done!"
    echo "At this step, you can open in your web browser:"
    echo "https://$BASE_DOMAIN:8443$BASE_PATH"
    echo
    echo "Notice: if Tomcat starts slowly:"
    echo "Open a file /etc/java-11-openjdk/security/java.security"
    echo "Replace securerandom.source=file:/dev/random"
    echo "to securerandom.source=file:/dev/urandom"
    echo "and restart Tomcat."
    echo "======================================"
    echo

    CERTBOT_RENEWAL=$(crontab -l | grep letsencrypt-ssl.sh)
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

# Download required files
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

echo
echo "======================================"
echo "Headwind MDM installation is completed!"
echo "To access your web panel, open in the web browser:"
echo "$PROTOCOL://$BASE_HOST$BASE_PATH"
echo "Login: admin:admin"
echo "======================================"
echo



