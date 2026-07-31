#!/bin/bash

set -e

echo "Setting IP tables into IPS mode..."

sudo iptables -F
sudo iptables -P FORWARD ACCEPT

sudo iptables -I FORWARD -p tcp --dport 22 -m conntrack --ctstate NEW -m recent --name SSHBRUTE --set
sudo iptables -I FORWARD -p tcp --dport 22 -m conntrack --ctstate NEW -m recent --name SSHBrute --update --seconds 10 --hitcount 4 -j DROP

sudo iptables -I FORWARD -p tcp -m conntrack --ctstate NEW -m recent --name PORTSCAN --set
sudo iptables -I FORWARD -p tcp -m conntrack --ctstate NEW -m recent --name PORTSCAN --update --seconds 5 --hitcount 20 -j DROP

sudo iptables -A FORWARD -j NFQUEUE --queue-num 0 --queue-bypass

sudo iptables -L FORWARD -n -v --line-numbers

echo "IP tables set to IPS mode. Starting Suricata in IPS mode."

sudo suricata -c /etc/suricata/suricata_ips.yaml -q 0
