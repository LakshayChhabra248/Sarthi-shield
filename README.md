# 🛡️ Sarthi Shield

<p align="center">
  <b>AI-IoT safety + fair-earnings intelligence for India’s gig riders.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Architecture-AI%20%2B%20IoT-blue" alt="AI IoT" />
</p>

---

## 🚀 Overview

**Sarthi Shield** ek AI-IoT ecosystem hai jo gig economy (Zomato, Swiggy, Uber-style operations) mein kaam karne wale delivery/driver partners ke liye bana hai.  
Ye app sirf distance track nahi karta — ye **road effort, risk, safety events, aur fair earning context** ko combine karta hai using **Computer Vision + IMU + GPS + voice safety signals**.

In short: smarter safety, better risk awareness, and more transparent rider effort intelligence.

---

## ✨ Key Highlights

- 🧠 **AI + IoT Fusion:** multi-sensor pipeline combining camera vision, IMU motion data, and live location.
- 👁️ **Computer Vision Hazard Detection:** pothole/speed-breaker style hazard detection stream (`vision/*`).
- 📳 **Sensor Fusion Engine:** correlates visual detections with physical impact anomalies (`fusion/DataFusionEngine.kt`).
- 🗺️ **Geo-aware Rider Alerts:** proximity-based hazard warnings with voice alerting.
- 🎙️ **Voice Safety Layer:** aggressive voice/noise spike monitoring for risky doorstep interactions.
- 📈 **Effort & Wage Intelligence:** Dynamic Difficulty Index + live wage/fair-pay logic (`wage/*`, mission flow).
- 🛡️ **Rating/ID Protection Concepts:** safety incidents integrated with protection workflows (`shield/*`).
- 📱 **Unified Rider Cockpit UI:** Jetpack Compose screens for trip, map, shield, voice safety, and Vision-IMU views.

---

## 💡 Why Sarthi Shield?

Gig riders real world mein sirf kilometers nahi cover karte — woh **risk, fatigue, poor roads, unsafe interactions, and unfair compensation pressure** handle karte hain.

**Sarthi Shield helps by:**
- turning invisible road stress into measurable signals,
- giving earlier hazard warnings,
- creating stronger safety evidence trails,
- and supporting a fairer way to understand rider effort.

---

## 🧩 High-Level Workflow

```mermaid
flowchart LR
    A[Camera Vision] --> E[Data Fusion Engine]
    B[IMU Sensors] --> E
    C[GPS/Location] --> E
    E --> F[Hazard Store (Room DB)]
    E --> G[Audio Alerts / TTS]
    D[Voice Safety Monitor] --> H[Incident & Safety Signals]
    H --> I[Shield / Mission Logic]
    F --> J[Compose UI Screens]
    I --> J
    K[Wage/DDI Engine] --> J
```

### Codebase modules (observed)
- `app/src/main/java/com/example/vision` – visual hazard detection
- `app/src/main/java/com/example/sensor` – IMU processing and features
- `app/src/main/java/com/example/fusion` – vision + IMU correlation
- `app/src/main/java/com/example/location` – location tracking/distance logic
- `app/src/main/java/com/example/audio` – alerts and voice safety
- `app/src/main/java/com/example/wage` – DDI and wage calculations
- `app/src/main/java/com/example/shield` – rider protection logic
- `app/src/main/java/com/example/data/local` – Room entities/DAO/database
- `app/src/main/java/com/example/ui` – Compose screens/components/theme

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Platform:** Android
- **UI:** Jetpack Compose + Material 3
- **Architecture style:** ViewModel + reactive `StateFlow`
- **Data:** Room (local persistence)
- **Camera/Vision:** CameraX pipeline
- **Location:** Google Play Services Location
- **Networking:** Retrofit + OkHttp + Moshi
- **Async:** Kotlin Coroutines

---

## ⚙️ Setup & Run (Conservative)

> This repository is an Android Kotlin project with module `:app`.

### 1) Prerequisites
- Android Studio (latest stable recommended)
- JDK 11 (project targets Java 11)
- Android SDK (minSdk 24, targetSdk 36)

### 2) Open project
1. Clone the repo.
2. Open the repository root in Android Studio.
3. Let Gradle sync complete.

### 3) Environment/Secrets
- A sample `.env.example` file exists.
- If needed for local runs, create `.env` from `.env.example` and fill required values.

### 4) Run app
- Select the `app` configuration in Android Studio.
- Run on emulator/device.

### 5) Optional CLI checks (if Gradle CLI/wrapper is available in your environment)
```bash
# Build debug APK
./gradlew :app:assembleDebug

# Unit tests
./gradlew :app:testDebugUnitTest
```

If `./gradlew` is not present in your local copy, run through Android Studio or use your installed Gradle setup.

---

## 🗺️ Future Scope / Roadmap

- [ ] Production-grade on-device model integration for hazard vision
- [ ] Better multi-modal confidence scoring and calibration
- [ ] Real-time incident escalation workflows (fleet + emergency contacts)
- [ ] Deeper earnings fairness analytics and partner-facing reports
- [ ] Cloud sync/dashboard layer for city-scale safety intelligence
- [ ] Improved multilingual voice guidance for riders

---

## 🤝 Contribution

Contributions are welcome!

1. Fork the repo
2. Create a feature branch
3. Make focused changes
4. Add/update tests where relevant
5. Open a pull request with clear context

---

## 📄 License

License file is currently not defined in this repository.  
Please add a `LICENSE` file (for example MIT/Apache-2.0) to formalize usage terms.
