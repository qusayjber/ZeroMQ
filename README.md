<div align="center">

# 🎯 ZeroMQ Pub-Sub Studio

### A modern JavaFX workspace for exploring real-time Publish–Subscribe messaging with ZeroMQ

<br>

[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge\&logo=openjdk\&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge\&logo=openjfx\&logoColor=white)](https://openjfx.io/)
[![ZeroMQ](https://img.shields.io/badge/ZeroMQ-4.x-red?style=for-the-badge\&logo=zeromq\&logoColor=white)](https://zeromq.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

<br>

**A visual, real-time demonstration of distributed Publish–Subscribe communication.**

<br>

[Features](#-features) ·
[Architecture](#-architecture) ·
[Quick Start](#-quick-start) ·
[Usage](#-usage) ·
[Project Structure](#-project-structure)

</div>

---

## ⚡ What is ZeroMQ Pub-Sub Studio?

**ZeroMQ Pub-Sub Studio** is a JavaFX desktop application that demonstrates the **Publisher–Subscriber messaging pattern** using ZeroMQ.

Instead of treating distributed messaging as an abstract concept, the application provides an interactive environment where you can:

* Publish topic-based messages
* Subscribe to multiple topics
* Observe messages arriving in real time
* Experiment with priorities
* Monitor message statistics
* Search communication activity
* Export message logs
* Explore how topic filtering works

The project is designed to make **distributed communication visible and interactive**.

> Built as part of **COMP438 — Distributed Computing**

---

# 🧠 Core Concept

The application demonstrates the classic ZeroMQ PUB/SUB flow:

```text
                 ┌──────────────────┐
                 │    Publisher     │
                 │                  │
                 │  COURSE          │
                 │  EXAM            │
                 │  EVENT           │
                 │  NEWS            │
                 └────────┬─────────┘
                          │
                          │ ZeroMQ
                          │ PUB
                          ▼
                 ┌──────────────────┐
                 │   Message Bus    │
                 └────────┬─────────┘
                          │
              ┌───────────┼───────────┐
              │           │           │
              ▼           ▼           ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │Subscriber│ │Subscriber│ │Subscriber│
        │  COURSE  │ │   NEWS   │ │EXAM/EVENT│
        └──────────┘ └──────────┘ └──────────┘
```

Subscribers receive only the topics they are interested in.

---

# ✨ Features

## 📡 Publisher

| Capability                | Description                                 |
| ------------------------- | ------------------------------------------- |
| 🏷️ Multi-topic messaging | COURSE, EXAM, EVENT, NEWS and custom topics |
| ⚡ Message priority        | Normal, High and Urgent                     |
| 📝 Quick templates        | Ready-to-use message templates              |
| 📊 Live statistics        | Global and per-topic message counters       |
| 🔍 Activity search        | Search published message history            |
| ⤓ Log export              | Export activity logs as `.txt`              |
| ⌨️ Keyboard support       | `Ctrl + Enter` to publish                   |
| ✨ Visual feedback         | Animated publishing indicators              |

---

## 📥 Subscriber

| Capability             | Description                           |
| ---------------------- | ------------------------------------- |
| 🎯 Multi-subscription  | Subscribe to several topics           |
| ❌ Quick unsubscribe    | Remove active subscriptions instantly |
| 🔔 Sound notifications | Optional notification sound           |
| 🎨 Topic highlighting  | Visually distinguish message topics   |
| 📊 Live statistics     | Track received messages               |
| 📋 Clipboard support   | Copy received messages                |
| ⤓ Inbox export         | Save received messages                |
| ⏱️ Timestamp control   | Toggle message timestamps             |

---

# 🎨 Interface

The application uses a dark **Aurora-inspired interface** focused on:

* Clear information hierarchy
* Topic-based color coding
* Modern cards and controls
* Smooth interaction feedback
* Real-time status indicators
* Compact messaging workflows
* Responsive JavaFX layouts

The goal is to make distributed messaging feel like an actual developer tool rather than a traditional classroom demo.

---

# 🏗️ Architecture

The application follows a simple distributed messaging architecture:

```text
┌─────────────────────┐
│     JavaFX UI       │
├─────────────────────┤
│ Publisher │Subscriber│
└──────┬────────┬─────┘
       │        │
       ▼        ▼
   ┌───────┐  ┌───────┐
   │  PUB  │  │  SUB  │
   │Socket │  │Socket │
   └───┬───┘  └───┬───┘
       │          │
       └────┬─────┘
            ▼
      ┌───────────┐
      │  ZeroMQ   │
      │ Messaging │
      └───────────┘
```

### Message Flow

```text
Create Message
      │
      ▼
Select Topic
      │
      ▼
Set Priority
      │
      ▼
Serialize Payload
      │
      ▼
ZeroMQ Publisher
      │
      ▼
Topic Filtering
      │
      ▼
Matching Subscribers
      │
      ▼
JavaFX Inbox
```

---

# 🧩 Technology Stack

| Technology     | Purpose               |
| -------------- | --------------------- |
| **Java 17+**   | Application runtime   |
| **JavaFX 21**  | Desktop UI            |
| **ZeroMQ 4.x** | Distributed messaging |
| **PUB/SUB**    | Messaging pattern     |
| **TCP**        | Transport layer       |
| **Git**        | Version control       |

---

# 🚀 Quick Start

## 1. Clone

```bash
git clone https://github.com/qusayjber/ZeroMQ.git
cd ZeroMQ
```

## 2. Open the project

Open the project using your preferred Java IDE.

Recommended:

* IntelliJ IDEA
* Eclipse
* VS Code with Java extensions

## 3. Configure dependencies

Make sure the project has access to:

* Java 17+
* JavaFX 21
* ZeroMQ / compatible Java binding

## 4. Run

Start the application from the main JavaFX application class.

---

# 🎮 Usage

### Publisher

1. Launch the Publisher interface.
2. Select or create a topic.
3. Enter a message.
4. Select a priority.
5. Publish the message.
6. Observe the activity statistics and log.

### Subscriber

1. Launch the Subscriber interface.
2. Select one or more topics.
3. Activate the subscriptions.
4. Wait for matching messages.
5. Inspect incoming notifications.
6. Copy or export the inbox when needed.

---

# 🧪 Example Message

A conceptual message can be represented as:

```text
TOPIC: EXAM
PRIORITY: HIGH
MESSAGE: Distributed Systems examination starts at 10:00
TIMESTAMP: 2026-09-18 10:00:00
```

The subscriber uses the topic information to determine whether the message belongs to its active subscriptions.

---

# 🔬 What This Project Demonstrates

This project provides a practical environment for understanding:

* Publish–Subscribe architecture
* Topic-based filtering
* Distributed messaging
* Asynchronous communication
* Message producers and consumers
* Socket-based communication
* Real-time event propagation
* Desktop visualization of distributed systems

It can also serve as a foundation for experimenting with larger messaging architectures.

---

# 📁 Project Structure

```text
ZeroMQ/
│
├── src/
│   └── ...
│
├── lib/
│   └── ...
│
├── README.md
├── LICENSE
├── .gitignore
└── ...
```

> The exact source structure may vary depending on the IDE configuration and project organization.

---

# 📸 Screenshots

Add screenshots here when publishing the project:

```text
docs/
├── publisher.png
├── subscriber.png
├── messaging.png
└── statistics.png
```

Example:

![Publisher](docs/publisher.png)

![Subscriber](docs/subscriber.png)

---

# 🛣️ Roadmap

* [x] Publisher interface
* [x] Subscriber interface
* [x] Topic subscriptions
* [x] Message priorities
* [x] Activity logging
* [x] Message statistics
* [x] Export functionality
* [x] Notification feedback
* [ ] Additional messaging patterns
* [ ] Advanced message inspection
* [ ] Configurable broker settings
* [ ] Extended distributed-system experiments

---

# 🎓 Academic Context

**Course:** COMP438 — Distributed Computing

The project was developed to provide a practical visualization of the **Publish–Subscribe communication model** and demonstrate how distributed components can exchange topic-oriented messages through ZeroMQ.

---

# 📄 License

This project is licensed under the **MIT License**.

See [`LICENSE`](LICENSE) for details.

---

<div align="center">

### Built with Java • JavaFX • ZeroMQ

**Explore distributed systems. Visualize communication. Build better systems.**

</div>
