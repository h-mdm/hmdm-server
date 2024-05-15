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