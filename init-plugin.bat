@echo off

grep --version >NUL 2>NUL && goto hasmingw
echo This utility requires mingw64 (grep and sed utilities)!
echo Please install the mingw package! 
exit

:hasmingw

if "%~1"=="" goto blank

echo Adding plugin "%~1" to Headwind MDM code
grep -r ^<artifactId^>%~1^</artifactId^> server\pom.xml >NUL 2>NUL && goto exists

rem Plugin doesn't exist, we need to add it to the code
copy server\pom.xml server\pom.xml.bak >NUL 2>NUL
sed "s|    </dependencies>|        <dependency><groupId>com.hmdm.plugin</groupId><artifactId>%~1</artifactId><version>0.1.0</version><scope>runtime</scope></dependency>\n    </dependencies>|g" server/pom.xml > server/pom.xml.new
move server\pom.xml.new server\pom.xml >NUL 2>NUL
echo server/pom.xml updated

copy plugins\pom.xml plugins\pom.xml.bak >NUL 2>NUL
sed "s|    </modules>|        <module>%~1</module>\n    </modules>|g" plugins/pom.xml > plugins/pom.xml.new
move plugins\pom.xml.new plugins\pom.xml >NUL 2>NUL
echo plugins/pom.xml updated

exit

:exists
echo Plugin "%~1" already added to the code, nothing to do!

exit
:blank
echo This utility adds the plugin dependency to Headwind MDM code.
echo Use it to add optional plugins to your project.
echo.
echo Usage: init-plugin.bat plugin
echo.
echo The plugin code should be placed to the plugins directory first.
exit

