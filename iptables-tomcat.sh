/sbin/iptables -A PREROUTING -t nat -p tcp -m tcp --dport 443 -j REDIRECT --to-ports 8443
/sbin/iptables -A OUTPUT -t nat -o lo -p tcp -m tcp --dport 443 -j REDIRECT --to-ports 8443
