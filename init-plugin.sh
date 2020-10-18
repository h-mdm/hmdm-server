#!/bin/bash
#
# This utility adds a new plugin to the project code
#
if [ -z "$1" ]; then
    echo "This utility adds the plugin dependency to Headwind MDM code."
    echo "Use it to add optional plugins to your project."
    echo ""
    echo "Usage: init-plugin.sh plugin"
    echo ""
    echo "The plugin code should be placed to the plugins directory first."
    exit 1
fi

RESULT=$(grep -F "<artifactId>$1</artifactId>" server/pom.xml)
if [ -z "$RESULT" ]; then
    # Plugin not exist, let's add it
    cp server/pom.xml server/pom.xml.bak
    sed "s|    </dependencies>|        <dependency><groupId>com.hmdm.plugin</groupId><artifactId>$1</artifactId><version>0.1.0</version><scope>runtime</scope></dependency>\n    </dependencies>|g" server/pom.xml > server/pom.xml.new
    mv server/pom.xml.new server/pom.xml
    echo "server/pom.xml updated"

    cp plugins/pom.xml plugins/pom.xml.bak
    sed "s|    </modules>|        <module>%~1</module>\n    </modules>|g" plugins/pom.xml > plugins/pom.xml.new
    mv plugins/pom.xml.new plugins/pom.xml
    echo "plugins/pom.xml updated"
else
    echo "Plugin $1 already added to the code, nothing to do!"
fi
