<div align="center">

# 🎯 ZeroMQ Pub-Sub Studio

### A modern JavaFX desktop application for exploring real-time Publish–Subscribe messaging with ZeroMQ

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

## ⚡ Overview

**ZeroMQ Pub-Sub Studio** is a modern JavaFX desktop application built to demonstrate the **Publish–Subscribe (PUB/SUB) messaging pattern** using **ZeroMQ**.

The application provides an interactive environment for publishing and receiving topic-based messages in real time while visualizing important distributed-system concepts through a modern desktop interface.

It is designed for:

* Learning distributed messaging
* Understanding PUB/SUB communication
* Exploring topic-based subscriptions
* Demonstrating asynchronous communication
* Teaching distributed computing concepts
* Experimenting with real-time notification systems

> Built as part of **COMP438 — Distributed Computing**

---

# 🧠 Core Concept

The application demonstrates the classic ZeroMQ **Publisher–Subscriber** communication model.

```text
                    ┌──────────────────┐
                    │    PUBLISHER     │
                    │                  │
                    │  COURSE          │
                    │  EXAM            │
                    │  EVENT           │
                    │  NEWS            │
                    └────────┬─────────┘
                             │
                             │ ZeroMQ PUB
                             ▼
                    ┌──────────────────┐
                    │   ZeroMQ Layer  │
                    │   Topic Routing  │
                    └────────┬─────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
        ┌───────────┐  ┌───────────┐  ┌───────────┐
        │SUBSCRIBER │  │SUBSCRIBER │  │SUBSCRIBER │
        │  COURSE   │  │   NEWS    │  │ EXAM/EVENT│
        └───────────┘  └───────────┘  └───────────┘
```

Each subscriber can subscribe to one or more topics and receives messages matching its active subscriptions.

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

| Capability             | Description                                 |
| ---------------------- | ------------------------------------------- |
| 🎯 Multi-subscription  | Subscribe to multiple topics simultaneously |
| ❌ Quick unsubscribe    | Remove active subscriptions instantly       |
| 🔔 Sound notifications | Optional notification sound                 |
| 🎨 Topic highlighting  | Visually distinguish different topics       |
| 📊 Live statistics     | Track received messages                     |
| 📋 Clipboard support   | Copy received messages                      |
| ⤓ Inbox export         | Save received messages                      |
| ⏱️ Timestamp control   | Toggle message timestamps                   |

---

# 🎨 Interface

ZeroMQ Pub-Sub Studio uses a modern **Aurora-inspired dark interface** designed around clarity and real-time interaction.

### UI principles

* 🌑 Dark developer-oriented visual language
* 💜 Color-coded topics
* 🎛️ Modern controls and status indicators
* ✨ Smooth interaction feedback
* 📊 Real-time statistics
* 🔎 Searchable activity
* 📐 Responsive JavaFX layouts
* ⚡ Fast and lightweight desktop experience

The interface is designed to feel like a practical developer tool rather than a traditional classroom demonstration.

---

# 🏗️ Architecture

The application combines JavaFX for the presentation layer with ZeroMQ for distributed messaging.

```text
┌─────────────────────────────────────────┐
│               JavaFX UI                 │
│                                         │
│    ┌─────────────┐   ┌─────────────┐    │
│    │  Publisher  │   │  Subscriber │    │
│    └──────┬──────┘   └──────┬──────┘    │
└───────────┼──────────────────┼──────────┘
            │                  │
            ▼                  ▼
       ┌─────────┐        ┌─────────┐
       │   PUB   │        │   SUB   │
       │ Socket  │        │ Socket  │
       └────┬────┘        └────┬────┘
            │                  │
            └────────┬─────────┘
                     ▼
              ┌──────────────┐
              │    ZeroMQ    │
              │ Messaging    │
              └──────────────┘
```

---

# 🔄 Message Flow

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
Build Message Payload
      │
      ▼
ZeroMQ Publisher
      │
      ▼
PUB/SUB Transport
      │
      ▼
Topic Filtering
      │
      ▼
Matching Subscribers
      │
      ▼
JavaFX Subscriber UI
```

---

# 🧩 Technology Stack

| Technology     | Purpose                         |
| -------------- | ------------------------------- |
| **Java 17+**   | Application runtime             |
| **JavaFX 21**  | Desktop graphical interface     |
| **ZeroMQ 4.x** | Distributed messaging           |
| **PUB/SUB**    | Messaging communication pattern |
| **TCP**        | Network transport               |
| **Git**        | Version control                 |

---

# 🚀 Quick Start

## Prerequisites

Before running the project, make sure you have:

* Java 17 or newer
* JavaFX 21
* ZeroMQ 4.x
* A compatible Java IDE

Recommended IDEs:

* IntelliJ IDEA
* Eclipse
* Visual Studio Code with Java extensions

---

## Clone the Repository

```bash
git clone https://github.com/qusayjber/ZeroMQ.git
cd ZeroMQ
```

---

## Configure Dependencies

Configure the project with:

```text
Java 17+
JavaFX 21
ZeroMQ 4.x
```

If the project uses local libraries, make sure the required ZeroMQ Java dependencies are available in the project's configured library path.

---

## Run the Application

Open the project in your Java IDE and run the main JavaFX application class.

Once started, launch the Publisher and Subscriber workflow to begin exchanging messages.

---

# 🎮 Usage

## Publisher

1. Open the Publisher interface.
2. Select a topic.
3. Enter the message content.
4. Choose the message priority.
5. Publish the message.
6. Monitor the activity log and statistics.

### Supported Topics

```text
COURSE
EXAM
EVENT
NEWS
CUSTOM
```

---

## Subscriber

1. Open the Subscriber interface.
2. Select the topics you want to receive.
3. Activate your subscriptions.
4. Wait for incoming messages.
5. Inspect received notifications.
6. Copy or export the inbox when needed.

---

# 🧪 Example Message

A message can conceptually contain information such as:

```text
TOPIC: EXAM
PRIORITY: HIGH
MESSAGE: Distributed Systems examination starts at 10:00
TIMESTAMP: 2026-09-18 10:00:00
```

The subscriber uses the topic information to determine whether the message matches an active subscription.

---

# 🔬 What This Project Demonstrates

ZeroMQ Pub-Sub Studio provides a practical demonstration of:

* Publish–Subscribe architecture
* Topic-based message filtering
* Distributed communication
* Asynchronous messaging
* Publisher and subscriber roles
* Socket-based communication
* Real-time event propagation
* Network messaging
* Desktop visualization of distributed systems
* Message monitoring and statistics

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

The exact source structure depends on the project's Java IDE and dependency configuration.

---

# 🛣️ Roadmap

* [x] Publisher interface
* [x] Subscriber interface
* [x] Multi-topic subscriptions
* [x] Custom topics
* [x] Message priorities
* [x] Activity logging
* [x] Message statistics
* [x] Search functionality
* [x] Message export
* [x] Inbox export
* [x] Notification feedback
* [ ] Additional ZeroMQ messaging patterns
* [ ] Advanced message inspection
* [ ] Configurable broker settings
* [ ] Extended distributed-system experiments

---

# 🎓 Academic Context

**Course:** COMP438 — Distributed Computing

The project demonstrates the **Publish–Subscribe communication model** and provides a visual environment for understanding how distributed components communicate through topic-based messaging.

It connects theoretical distributed-computing concepts with a practical desktop implementation using JavaFX and ZeroMQ.

---

# 🤝 Contributing

Contributions, improvements, bug reports, and ideas are welcome.

If you would like to contribute:

```bash
git clone https://github.com/qusayjber/ZeroMQ.git
```

Create a feature branch, make your changes, and open a pull request.

---

# 📄 License

This project is licensed under the **MIT License**.

See [`LICENSE`](LICENSE) for the complete license text.

---

<div align="center">

### Built with ☕ Java · JavaFX · ZeroMQ

**Explore distributed systems. Visualize communication. Build better systems.**

</div>
