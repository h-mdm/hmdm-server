#!/bin/bash
#
# Web application update script
TOMCAT_HOME=$(ls -d /var/lib/tomcat* | tail -n1)
TOMCAT_SERVICE=$(echo $TOMCAT_HOME | awk '{n=split($1,A,"/"); print A[n]}')
TOMCAT_USER=$(ls -ld $TOMCAT_HOME/webapps | awk '{print $3}')
FILES_DIRECTORY=$TOMCAT_HOME/work/files
WAR_FILE=$TOMCAT_HOME/webapps/ROOT.war
MANIFEST_FILE=$FILES_DIRECTORY/hmdm_web_update_manifest.txt

if [ ! -f $MANIFEST_FILE ]; then
    echo "No updates found. Select 'admin - Check for updates' in the web panel"
    exit 1
fi

NEW_WAR_FILE=$(cat $MANIFEST_FILE)

if [ ! -f $NEW_WAR_FILE ]; then
    echo "$NEW_WAR_FILE is not found."
    echo " Select 'admin - Check for updates - Get updates' in the web panel"
    exit 1
fi


# The server switched from log4j to logback. The logging configuration is taken from the
# 'logback.config' context parameter; installations made before the switch declare the obsolete
# 'log4j.config' parameter instead. This script only replaces ROOT.war, so such an installation
# would end up with no file appenders at all after the update.
CONTEXT_FILE=$TOMCAT_HOME/conf/Catalina/localhost/ROOT.xml

if [ -f $CONTEXT_FILE ] && grep -q "log4j.config" $CONTEXT_FILE; then
    BASE_DIRECTORY=$(sed -n 's|.*name="log4j.config".*value="file://\(.*\)/log4j-hmdm.xml".*|\1|p' $CONTEXT_FILE | head -n1)
    if [ -z "$BASE_DIRECTORY" ]; then
        BASE_DIRECTORY="<your Headwind MDM base directory>"
    fi
    echo
    echo "**********************************************************************"
    echo "*                            WARNING                                 *"
    echo "*      LOGGING CONFIGURATION MUST BE MIGRATED: log4j -> logback      *"
    echo "**********************************************************************"
    echo
    echo "$CONTEXT_FILE still declares the obsolete 'log4j.config' parameter."
    echo "The version being installed uses logback and reads 'logback.config'."
    echo
    echo "UNTIL YOU MIGRATE, FILE LOGGING IS OFF: logs/hmdm.log and logs/audit.log"
    echo "are not written any more and everything goes to catalina.out."
    echo
    echo "To migrate, after this update:"
    echo
    echo " 1. Create $BASE_DIRECTORY/logback-hmdm.xml from the install/logback_template.xml"
    echo "    file shipped with the Headwind MDM installation package:"
    echo
    echo "      sed \"s|_BASE_DIRECTORY_|$BASE_DIRECTORY|g\" ./install/logback_template.xml > $BASE_DIRECTORY/logback-hmdm.xml"
    echo "      chown $TOMCAT_USER:$TOMCAT_USER $BASE_DIRECTORY/logback-hmdm.xml"
    echo
    echo "    Do NOT just rename the old $BASE_DIRECTORY/log4j-hmdm.xml file: it is written in"
    echo "    the log4j format, which logback cannot read. The old file is no longer used and"
    echo "    may be deleted once the new one is in place."
    echo
    echo " 2. In $CONTEXT_FILE, rename the parameter:"
    echo
    echo "      <Parameter name=\"log4j.config\" value=\"file://$BASE_DIRECTORY/log4j-hmdm.xml\"/>"
    echo "    to"
    echo "      <Parameter name=\"logback.config\" value=\"file://$BASE_DIRECTORY/logback-hmdm.xml\"/>"
    echo
    echo " 3. service $TOMCAT_SERVICE restart"
    echo
    echo "**********************************************************************"
    echo
fi

echo "Version to install: $NEW_WAR_FILE"
echo "Destination: $WAR_FILE"
read -p "Update web panel? [Y/n]? " -n 1 -r
echo

if [[ ! "$REPLY" =~ ^[Yy]$ ]]; then
    exit 1
fi

mv $NEW_WAR_FILE $WAR_FILE
chmod 644 $WAR_FILE
service $TOMCAT_SERVICE restart
rm -f $MANIFEST_FILE

echo "Update successful. Please check the web panel version in 'admin - About'."

if [ -f $CONTEXT_FILE ] && grep -q "log4j.config" $CONTEXT_FILE; then
    echo
    echo "REMINDER: file logging is OFF until you replace the 'log4j.config' parameter in"
    echo "$CONTEXT_FILE with 'logback.config' (see the warning above)."
fi