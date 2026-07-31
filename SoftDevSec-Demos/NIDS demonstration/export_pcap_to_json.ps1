# This is how I extracted the JSON file from the pcap

tshark -r src\main\resources\example.pcap -Y "ip and (tcp or udp)" -c 1000 -T ek -e frame.time_epoch -e ip.src -e ip.dst -e tcp.srcport -e tcp.dstport -e udp.srcport -e udp.dstport -e _ws.col.Protocol > src\main\resources\packet.json