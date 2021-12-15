#!/bin/bash
# 
# LetsEncrypt renewal script for Headwind MDM
#

# Set this parameter to 1 if you're redirecting port 80 to 8080 to be able to run Headwind MDM on port 80
HTTP_REDIRECT=0
DOMAIN=your-domain.com
TOMCAT_HOME=$(ls -d /var/lib/tomcat* | tail -n1)
TOMCAT_USER=$(ls -ld $TOMCAT_HOME/webapps | awk '{print $3}')
TOMCAT_SERVICE=$(echo $TOMCAT_HOME | awk '{n=split($1,A,"/"); print A[n]}')
SSL_DIR=$TOMCAT_HOME/ssl
PASSWORD=123456

if [ "$DOMAIN" = "your-domain.com" ]; then
    echo "Please edit this script and update HTTP_REDIRECT and DOMAIN variables!"
    exit 1
fi

# Remove HTTP redirection to tomcat so certbot could verify the domain
if [ "$HTTP_REDIRECT" = "1" ]; then
	/sbin/iptables -D PREROUTING -t nat -p tcp -m tcp --dport 80 -j REDIRECT --to-ports 8080
fi

if [ ! -d $SSL_DIR ]; then
    mkdir -p $SSL_DIR
fi

certbot certonly --standalone --force-renewal -d $DOMAIN

# Add the HTTP rule back
if [ "$HTTP_REDIRECT" = "1" ]; then
	/sbin/iptables -A PREROUTING -t nat -p tcp -m tcp --dport 80 -j REDIRECT --to-ports 8080
fi

# TODO: here we should check that certbot actually renewed the certificate!

CERTBOT_DIR=/etc/letsencrypt/live/$DOMAIN
openssl pkcs12 -export -out $SSL_DIR/$DOMAIN.p12 -inkey $CERTBOT_DIR/privkey.pem -in $CERTBOT_DIR/cert.pem -certfile $CERTBOT_DIR/fullchain.pem -password pass:$PASSWORD
keytool -importkeystore -destkeystore $SSL_DIR/$DOMAIN.jks -srckeystore $SSL_DIR/$DOMAIN.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD -deststorepass $PASSWORD -noprompt

chown -R $TOMCAT_USER:$TOMCAT_USER $SSL_DIR

echo "The certificates should be stored here: $SSL_DIR/$DOMAIN.jks"
echo "Please add / uncomment the following section in $TOMCAT_HOME/conf/server.xml:"
echo "<Connector port=\"8443\" protocol=\"org.apache.coyote.http11.Http11NioProtocol\""
echo "           maxThreads=\"150\" SSLEnabled=\"true\">"
echo "    <SSLHostConfig>"
echo "        <Certificate certificateKeystoreFile=\"$SSL_DIR/$DOMAIN.jks\""
echo "                     type=\"RSA\" certificateKeystorePassword=\"$PASSWORD\" />"
echo "    </SSLHostConfig>"
echo "</Connector>"

# This line is required when you refresh the certificates because Tomcat needs
# to be restarted to load a new certificate.
# Here we assume the service has the same name as the Tomcat directory
# (e.g. tomcat9)
/usr/sbin/service $TOMCAT_SERVICE restart

