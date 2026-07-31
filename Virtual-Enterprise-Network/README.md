# Advanced Network Security Project: Virtual Enterprise Network

## Overview

This project was built for **CSCI 6650 - Advanced Network Security**. 

The objective was to design, implement, and evaluate a segmented enterprise-style virtual network capable of operating in both **Intrusion Detection System (IDS)** and **Intrusion Prevention System (IPS)** modes using Suricata as an inline gateway.

The resulting lab demonstrates attack simulation, packet capture, traffic analysis, and comparative evaluation of detection versus prevention techniques in an isolated virtual environment.

## Repository Structure

```
Network_Security_Lab/
│
├── README.md
│
├── docs/
│   ├── report.pdf
│   ├── assignment.pdf
│
├── topology/
│   ├── logical_topology.png
│   └── logical_topology.pkt
│
├── scripts/
│   ├── startup.sh
│   ├── set_ids.sh
│   ├── set_ips.sh
│   ├── suricata_ids.yaml
│   └── suricata_ips.yaml
│
├── captures/
│   ├── netscan.pcap
│   ├── brute_force.pcap
│   ├── exfil.pcap
│   └── ids_block.pcap
│
├── media/
    ├── normal http.png
    └── live_demonstration.mp4
```

## Features

- Dual-network architecture
- Inline gateway
- Suricata IDS
- Suricata IPS
- iptables enforcement
- Packet capture
- Brute-force detection
- Port-scan detection
- Data exfiltration analysis
- Comparative IDS vs IPS testing

## Architecture

![Topology Diagram](topology/logical%20topology.png)

## Attack Workflow

```
Reconnaissance
      ↓
Nmap Enumeration
      ↓
Hydra SSH Brute Force
      ↓
SSH Authentication
      ↓
File Discovery
      ↓
SCP Data Exfiltration
```

## Defense Workflow

```
Traffic
   ↓
Gateway
   ↓
iptables
   ↓
NFQUEUE
   ↓
Suricata Engine
   ↓
Alert / Drop / Allow
```

## Experimental Validation

[Demonstration](media/live_demonstration.mp4)

The project was demonstrated in a live virtual environment to validate both intrusion detection (IDS) and intrusion prevention (IPS) capabilities using Suricata as an inline gateway between two isolated network segments.

The validation consisted of four primary phases:

### Baseline Connectivity

- Verified normal communication between the attacker and target hosts.
- Confirmed legitimate traffic (HTTP, SSH, etc.) passed successfully through the gateway.

### IDS Evaluation

- Executed reconnaissance and brute-force attacks against the target system while Suricata operated in detection mode.
- Generated alerts and packet captures without interrupting network traffic.

### IPS Evaluation

- Repeated the same attack scenarios with Suricata configured for inline prevention.
- Verified malicious traffic was automatically blocked while legitimate traffic continued to flow.

### Traffic Analysis

- Compared packet captures, Suricata alerts, and system behavior between IDS and IPS modes.
- Evaluated the effectiveness of detection, prevention, and overall network visibility.

The repository includes the supporting scripts, Packet Tracer topology, packet captures, and documentation used throughout the demonstration, while the accompanying report provides a detailed discussion of implementation decisions and experimental results.

Summary:

| Phase | Objective | Evidence |
|-------|-----------|----------|
| Baseline | Verify normal network operations | Packet captures |
| Reconnaissance | Detect Nmap scans | Suricata alerts & PCAPs |
| Brute Force | Detect / block Hydra SSH attack | IDS alerts / IPS blocking |
| Data exfiltration | Detect / block SCP file transfer attempt | IDS alerts / IPS blocking |
| Data Transfer | Validate legitimate traffic | HTTP & SSH packet captures |
| Comparison | Contrast IDS vs IPS behavior | Report & demonstration video |

## Results

| Scenario | IDS | IPS |
|----------|-----|-----|
| Nmap | Detected, not blocked | Blocked |
| Hydra SSH Brute Force | Detected, not blocked | Blocked |
| Legitamate SSH | Allowed | Allowed |
| Exfiltration | Successful | Prevented |
| Normal HTTP Traffic | Allowed | Allowed |

## Skills

- Linux Administration
- Enterprise Networking
- TCP/IP
- Routing
- Suricata IDS/IPS
- iptables / NFQUEUE
- VirtualBox
- Wireshark
- Packet Analysis
- Bash Scripting
- Network Security
- Intrusion Detection & Prevention

## Key Takeaways

This project demonstrates the design and deployment of an enterprise-style virtual network capable of operating in both IDS and IPS configurations. By comparing identical attack scenarios under detection and prevention modes, the lab illustrates the trade-offs between visibility and active defense while providing hands-on experience with network segmentation, packet analysis, intrusion detection, and inline traffic enforcement.