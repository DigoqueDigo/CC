# Cumulus

A lightweight, decentralized peer-to-peer (P2P) file sharing system built in Java. It allows multiple clients to share, request, and download files from each other without relying on any central storage server. The system is designed with simplicity, robustness, and scalability in mind, using a custom-built protocol over TCP and UDP for control and data transmission.

---

## 🧪 Features

- Fully decentralized file sharing using a block-based strategy.
- Multithreaded client operations to support concurrent transfers.
- Reliable UDP data transmission using a custom sliding window algorithm.
- Custom-built TCP and UDP packet structures for protocol communication.
- In-house DNS implementation for hostname resolution.

---

## 🏗️ Architecture

- **Client:** shares and downloads files, communicates with the tracker and other clients using custom TCP/UDP protocols.
- **Tracker:** acts as a central coordinator that maps file blocks to client IPs. It does not store the files themselves.
- **DNS Server:** resolves hostnames to IP addresses used by clients and the tracker.

---

## 🛠️ Requirements

- **Java 11** or higher
- **Unix-like system**

---

## ⚙️ Compile

```bash
chmod +x compile.sh
./compile.sh
```

---

## 🚀 Start Components

### DNS Server

```bash
java dns.DNS
```

### Tracker

```bash
java tracker.Tracker
```

### Client

```bash
java client.Main <folder> <tracker_ip> 12345 <dns_ip> 22222
```

- **folder:** directory containing the files to share.
- **tracker_ip:** ip address of the tracker.
- **12345:** port number for tracker communication.
- **dns_ip:** ip address of the DNS server.
- **22222:** port number for DNS queries.
