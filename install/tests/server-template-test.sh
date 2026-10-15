#!/bin/bash
# Run from any directory; requires xmllint. Does not invoke the installer.
set -eu
INSTALL_DIR=$(cd "$(dirname "$0")/.." && pwd)
TEMPLATE="$INSTALL_DIR/server_template.xml"
xmllint --noout "$TEMPLATE"

assert_xpath() {
    local actual
    actual=$(xmllint --xpath "$1" "$TEMPLATE")
    if [ "$actual" != "$2" ]; then
        echo "FAIL: $1: expected '$2', found '$actual'" >&2
        exit 1
    fi
}

LISTENER="/Server/Listener[@className='org.apache.catalina.security.TLSCertificateReloadListener']"
CONNECTOR="/Server/Service/Connector[@port='8443']"
assert_xpath "count($LISTENER)" "1"
assert_xpath "count($LISTENER/@checkPeriod | $LISTENER/@daysBefore)" "0"
assert_xpath "count($CONNECTOR)" "1"
assert_xpath "string($CONNECTOR/@SSLEnabled)" "true"
assert_xpath "count($CONNECTOR/SSLHostConfig/Certificate)" "1"
assert_xpath "string($CONNECTOR/SSLHostConfig/Certificate/@certificateKeyFile)" "/etc/hmdm/tls/current/privkey.pem"
assert_xpath "string($CONNECTOR/SSLHostConfig/Certificate/@certificateFile)" "/etc/hmdm/tls/current/cert.pem"
assert_xpath "string($CONNECTOR/SSLHostConfig/Certificate/@certificateChainFile)" "/etc/hmdm/tls/current/chain.pem"
assert_xpath "count($CONNECTOR/SSLHostConfig/Certificate/@type)" "0"
assert_xpath "count(//@*[starts-with(name(), 'certificateKeystore')])" "0"
assert_xpath "count(/Server/Service/Connector[@port='8080' and @redirectPort='8443'])" "1"
assert_xpath "count(/Server/Service/Engine[@name='Catalina' and @defaultHost='localhost']/Host[@name='localhost' and @appBase='webapps'])" "1"
echo "Server template checks passed."
