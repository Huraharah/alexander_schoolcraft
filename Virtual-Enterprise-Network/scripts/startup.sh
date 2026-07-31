#!/bin/bash

echo "net.ipv4.ip_forward=1" | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
cat /proc/sys/net/ipv4/ip_forward

sudo mount -t vboxsf NetSecLabShare /mnt/share

tmux
