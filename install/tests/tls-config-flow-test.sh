#!/bin/bash
# Test the actual template-install block without certbot, root or systemd.
set -eu
INSTALL_DIR=$(cd "$(dirname "$0")/.." && pwd)
TEST_DIR=$(mktemp -d)
trap 'rm -rf "$TEST_DIR"' EXIT

# Extract only the automatic configuration block, not installer side effects.
BLOCK=$(sed -n '/^        SERVER_XML=/,/^        AUTO_CONFIGURED=1/p' "$INSTALL_DIR/../hmdm_install.sh")
[ -n "$BLOCK" ]
for scenario in success restart_failure invalid_template; do
    CASE_DIR="$TEST_DIR/$scenario"
    mkdir -p "$CASE_DIR/conf"
    # Broken old XML must not prevent replacing the configuration.
    printf '%s\n' 'old broken XML' > "$CASE_DIR/conf/server.xml"
    cp "$INSTALL_DIR/server_template.xml" "$CASE_DIR/template.xml"
    if [ "$scenario" = invalid_template ]; then
        printf '%s\n' '<Server>' > "$CASE_DIR/template.xml"
    fi
    status=0
    (
        TOMCAT_HOME="$CASE_DIR"
        TOMCAT_SERVICE=test-tomcat
        SERVER_XML_TEMPLATE="$CASE_DIR/template.xml"
        # GNU ownership/reference options are not available on macOS.
        chown() { return 0; }
        chmod() { return 0; }
        service() {
            printf '%s\n' "$*" >> "$CASE_DIR/restarts"
            [ "$scenario" != restart_failure ]
        }
        eval "$BLOCK"
        [ "$AUTO_CONFIGURED" = 1 ]
    ) > "$CASE_DIR/output" 2>&1 || status=$?
    [ "$(< "$CASE_DIR/conf/server.xml~")" = 'old broken XML' ]
    if [ "$scenario" = invalid_template ]; then
        [ "$status" != 0 ]
        [ ! -e "$CASE_DIR/restarts" ]
        [ "$(< "$CASE_DIR/conf/server.xml")" = 'old broken XML' ]
    else
        cmp "$CASE_DIR/template.xml" "$CASE_DIR/conf/server.xml"
        [ "$(wc -l < "$CASE_DIR/restarts" | tr -d ' ')" = 1 ]
        if [ "$scenario" = success ]; then
            [ "$status" = 0 ]
        else
            [ "$status" != 0 ]
        fi
    fi
done
echo "TLS configuration flow checks passed."
