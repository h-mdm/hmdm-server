#!/bin/bash
#
# Headwind MDM installer script
# Tested on Ubuntu Linux 18.04 LTS
#
TOMCAT_BASE=/var/lib/tomcat8
REPOSITORY_BASE=https://h-mdm.com/files
CLIENT_VERSION=2.17
DEFAULT_SQL_HOST=localhost
DEFAULT_SQL_PORT=5432
DEFAULT_SQL_BASE=hmdm
DEFAULT_SQL_USER=hmdm
DEFAULT_SQL_PASS=
DEFAULT_LOCATION="/opt/hmdm"
TOMCAT_HOME="/var/lib/tomcat8"
TOMCAT_ENGINE="Catalina"
TOMCAT_HOST="localhost"
DEFAULT_PROTOCOL=http
DEFAULT_BASE_HOST=
DEFAULT_BASE_PATH="/hmdm"
TEMP_DIRECTORY="/tmp"
TEMP_SQL_FILE="$TEMP_DIRECTORY/hmdm_init.sql"
INSTALL_FLAG_FILE="$TEMP_DIRECTORY/hmdm_install_flag"
TOMCAT_USER="tomcat"
INSTALL_FILES=("wifi-manager-4-2-7-220.apk")

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
    echo "Please run: apt install aapt"
    exit 1
fi

# Check PostgreSQL installation
if ! which psql > /dev/null; then
    echo "PostgreSQL is not installed!"
    echo "Please run: apt install postgresql"
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

# Search for the WAR
SERVER_WAR=./server/target/server-0.1.0.war
if [ ! -f $SERVER_WAR ]; then
    SERVER_WAR=$(ls hmdm*.war | tail -1)
fi
if [ ! -f $SERVER_WAR ]; then
    echo "FAILED to find the WAR file of Headwind MDM!"
    echo "Did you compile the project?"
    exit 1
fi

#read -p "Are you installing an open-source version? (Y/n)? " -n 1 -r
#echo
#if [[ $REPLY =~ ^[Yy]$ ]]; then
    CLIENT_VARIANT="os"
#else
#    CLIENT_VARIANT="master"
#fi

CLIENT_APK="hmdm-$CLIENT_VERSION-$CLIENT_VARIANT.apk"
INSTALL_FILES+=("$CLIENT_APK")

read -e -p "Please choose the installation language (en/ru) [en]: " -i "en" LANGUAGE
echo

echo "PostgreSQL database setup"
echo "========================="
echo "Make sure you've installed PostgreSQL and created the database:"
echo "# CREATE USER hmdm WITH PASSWORD 'topsecret';"
echo "# CREATE DATABASE hmdm WITH OWNER=hmdm;"
echo

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

echo
echo "File storage setup"
echo "=================="
echo "Please choose where the files uploaded to Headwind MDM will be stored"
echo "If the directory doesn't exist, it will be created"
echo

read -e -p "Headwind MDM directory [$DEFAULT_LOCATION]: " -i "$DEFAULT_LOCATION" LOCATION

# Create directories
if [ ! -d $LOCATION ]; then
    mkdir -p $LOCATION || exit 1
fi
if [ ! -d $LOCATION/files ]; then
    mkdir $LOCATION/files
fi
if [ ! -d $LOCATION/plugins ]; then
    mkdir $LOCATION/plugins
fi
chown $TOMCAT_USER:$TOMCAT_USER $LOCATION/files || exit 1
chown $TOMCAT_USER:$TOMCAT_USER $LOCATION/plugins || exit 1

echo
echo "Web application setup"
echo "====================="
echo "Headwind MDM requires access from Internet"
echo "Please assign a public domain name to this server"
echo

read -e -p "Protocol (http|https) [$DEFAULT_PROTOCOL]: " -i "$DEFAULT_PROTOCOL" PROTOCOL
read -e -p "Domain name or public IP (e.g. example.com): " -i "$DEFAULT_BASE_HOST" BASE_HOST
read -e -p "Project path on server [$DEFAULT_BASE_PATH]: " -i "$DEFAULT_BASE_PATH" BASE_PATH
read -e -p "Tomcat virtual host [$TOMCAT_HOST]: " -i "$TOMCAT_HOST" TOMCAT_HOST
echo
echo "Ready to install!"
echo "Location on server: $LOCATION"
echo "URL: $PROTOCOL://$BASE_HOST:8080$BASE_PATH"
read -p "Is this information correct [Y/n]? " -n 1 -r
echo

if [[ ! "$REPLY" =~ ^[Yy]$ ]]; then
    exit 1
fi

# Copy files from the installer or download them from the web
for APK in ${INSTALL_FILES[@]}; do
    if [ -f "$DEFAULT_LOCATION/files/$APK" ]; then
        continue
    fi
    if [ ! -f "./install/files/$APK" ]; then
        wget -O "$DEFAULT_LOCATION/files/$APK" "$REPOSITORY_BASE/$APK"
        if [ "$?" -eq 0 ]; then
            echo "$APK downloaded to $DEFAULT_LOCATION/files"
        else
            echo "FAILED to download $REPOSITORY_BASE/$APK"
            rm -f "$DEFAULT_LOCATION/files/$APK"
        fi
    else
        cp "./install/files/$APK" "$DEFAULT_LOCATION/files/$APK" > /dev/null
        if [ "$?" -eq 0 ]; then
            echo "$APK copied to $DEFAULT_LOCATION/files"
        else
            echo "FAILED to copy ./install/files/$APK to $DEFAULT_LOCATION/files"
        fi        
    fi
done

# Prepare the XML config
if [ ! -f ./install/context_template.xml ]; then
    echo "ERROR: Missing ./install/context.xml!"
    echo "The package seems to be corrupted!"
    exit 1
fi

TOMCAT_CONFIG_PATH=$TOMCAT_HOME/conf/$TOMCAT_ENGINE/$TOMCAT_HOST
mkdir -p $TOMCAT_CONFIG_PATH || exit 1
cat ./install/context_template.xml | sed "s|_SQL_HOST_|$SQL_HOST|g; s|_SQL_PORT_|$SQL_PORT|g; s|_SQL_BASE_|$SQL_BASE|g; s|_SQL_USER_|$SQL_USER|g; s|_SQL_PASS_|$SQL_PASS|g; s|_BASE_DIRECTORY_|$LOCATION|g; s|_PROTOCOL_|$PROTOCOL|g; s|_BASE_HOST_|$BASE_HOST|g; s|_BASE_PATH_|$BASE_PATH|g; s|_INSTALL_FLAG_|$INSTALL_FLAG_FILE|g" > $TOMCAT_CONFIG_PATH/$BASE_PATH.xml
if [ "$?" -ne 0 ]; then
    echo "Failed to create a Tomcat config file $TOMCAT_CONFIG_PATH/$BASE_PATH.xml!"
    exit 1
fi 
echo "Tomcat config file created: $TOMCAT_CONFIG_PATH/$BASE_PATH.xml"

echo "Deploying $SERVER_WAR to Tomcat: $TOMCAT_HOME/webapps/$BASE_PATH.war"
rm -f $INSTALL_FLAG_FILE > /dev/null 2>&1
cp $SERVER_WAR $TOMCAT_HOME/webapps/$BASE_PATH.war

# Waiting until the end of deployment
SUCCESSFUL_DEPLOY=
for i in {1..60}; do
    if [ -f $INSTALL_FLAG_FILE ]; then
        SUCCESSFUL_DEPLOY=1
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
cat ./install/sql/hmdm_init.$LANGUAGE.sql | sed "s|_HMDM_BASE_|$LOCATION|g; s|_HMDM_VERSION_|$CLIENT_VERSION|g; s|_HMDM_APK_|$CLIENT_APK|g" > $TEMP_SQL_FILE
cat $TEMP_SQL_FILE | psql $PSQL_CONNSTRING > /dev/null 2>&1
if [ "$?" -ne 0 ]; then
    echo "ERROR: failed to execute SQL script!"
    echo "See $TEMP_SQL_FILE for details."
    exit 1
fi
rm $TEMP_SQL_FILE

echo
echo "======================================"
echo "Headwind MDM has been installed!"
echo "To continue, open in your web browser:"
echo "$PROTOCOL://$BASE_HOST:8080$BASE_PATH"
