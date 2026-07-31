#!/bin/bash

set -e

echo "Setting IP tables to IDP mode..."

sudo iptables -F
sudo iptables -P FORWARD ACCEPT

sudo iptables -A FORWARD -j NFQUEUE --queue-num 0 --queue-bypass
sudo iptables -L FORWARD -n -v --line-numbers

echo "IP tables set to IDP/Logging mode. Starting Suricata in IDS mode..."

sudo suricata -c /etc/suricata/suricata_ids.yaml -q 0
