# Network IDS Simulation

A JavaFX-based Network Intrusion Detection System (IDS) simulator that analyzes packet captures and identifies potentially malicious network activity using multiple detection heuristics.

Originally developed as a graduate cybersecurity project, the application was expanded beyond the assignment requirements into an interactive desktop application capable of replaying packet captures, classifying events, and visualizing alerts in real time.

---

## Features

- Import packet captures exported from Wireshark/TShark
- Replay network traffic in real time or accelerated playback
- Detect known malicious IP addresses using a configurable blacklist
- Identify suspicious network behaviors including:
  - Known malicious actors
  - High packet-rate activity
  - Horizontal scanning
  - Connections to sensitive service ports
- Classify events by severity:
  - INFO
  - WARNING
  - ALERT
  - CRITICAL
- JavaFX graphical interface with sortable event tables
- Color-coded alerts for rapid analysis

---

## Technologies

- Java 21
- JavaFX
- Maven
- Jackson JSON
- Wireshark / TShark
- PCAP packet analysis

---

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── sim/
│   │       ├── IDS_simulation.java
│   │       └── PcapJsonParser.java
│   └── resources/
│       ├── ids_simulator.fxml
│       ├── ips.txt
│       ├── packet.json
│       └── example.pcap

assignment/
├── CSCI6450FA2025Project2.pdf
└── assignment_blacklist.txt

report/
└── Project2_Writeup.pdf

export-pcap-to-json.txt
```

---

## Running

Clone the repository and execute

```bash
mvn javafx:run
```

The application will load the included sample packet capture and blacklist automatically.

---

## Blacklist

The file

```
src/main/resources/ips.txt
```

contains known malicious IP addresses used during detection.

Any packet whose source or destination matches an address in this file is classified as a **CRITICAL** event.

---

## Packet Capture

The included packet capture has already been exported to JSON for convenience.

If you wish to analyze a different capture, use the command contained in

```
tools/export-pcap-to-json.txt
```

to generate a compatible JSON file from a PCAP using TShark.

---

## Detection Logic

Each packet is evaluated against several independent heuristics:

- Blacklisted source/destination IPs
- Packet rate thresholds
- Horizontal scanning behavior
- Sensitive destination ports
- Basic protocol inspection

Events are assigned one of four severity levels:

| Severity | Description |
|----------|-------------|
| INFO | Normal network activity |
| WARNING | Potentially suspicious behavior |
| ALERT | High-confidence suspicious activity |
| CRITICAL | Known malicious indicators or multiple triggered heuristics |

---

## Educational Purpose

This project is intended as an educational simulation of a Network Intrusion Detection System.

It demonstrates how signature-based and heuristic detection techniques can be combined to identify suspicious traffic while emphasizing that an IDS **detects and alerts** rather than actively blocking network traffic.

---

## Future Improvements

Potential enhancements include:

- Live packet capture support
- Additional IDS signatures
- Rule configuration interface
- Exportable alert reports
- Geographic IP lookup
- Threat intelligence feed integration
- Statistical dashboards
- Machine-learning-based anomaly detection

---

## License

This project was developed for educational purposes as part of graduate coursework in cybersecurity.