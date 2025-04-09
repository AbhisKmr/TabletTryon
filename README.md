# 💍 MirrAR Tablet Try-On App

**MirrAR Tablet Try-On** is an Android-based augmented reality (AR) application that allows users to virtually try on jewelry using real-time camera feed and face tracking. It leverages DeepAR, ML Kit, and MediaPipe for facial detection and rendering.

---

## 🚀 Features

- 📸 Live virtual jewelry try-on via camera
- 🎯 Real-time face detection using **ML Kit**
- 🔮 DeepAR SDK integration for advanced rendering
- 🔍 Algolia-powered search and product listings
- 🧱 Navigation component-based UI architecture
- 🔧 Barcode/QR code scanning using ZXing
- 🔄 Firebase Realtime Database for syncing data
- 📀 Offline storage with Room DB
- 🔥 Crashlytics + Analytics integrated
- 🎨 Shimmer loading animations
- 🌐 Retrofit + Gson for API interaction

---

## 🛠️ Tech Stack

| Component           | Library / Tool                             |
|---------------------|---------------------------------------------|
| Language            | Kotlin                                      |
| UI Framework        | XML + ViewBinding                           |
| Architecture        | MVVM + LiveData + ViewModel                 |
| AR Engine           | [DeepAR SDK](https://docs.deepar.ai/)       |
| Face Detection      | ML Kit + MediaPipe                          |
| Image Loading       | Glide                                       |
| Networking          | Retrofit + OkHttp + Gson                    |
| Search              | Algolia SDK                                 |
| QR/Barcode Scanner  | ZXing Embedded                              |
| DB / Caching        | Room DB                                     |
| Firebase            | Crashlytics, Realtime DB, Analytics         |
| Animation           | Lottie                                      |

---

## 🏠 Project Structure

```
📁 app/
🔝📁 activities/
    ├── MainActivity.kt
    └── DeepARActivity.kt
📁 fragments/
📁 viewmodel/
📁 network/
📁 database/
📁 utils/
```

> UI uses `ViewBinding` for all views, navigation via Navigation Component.

---

## 🔐 Permissions Used

| Permission                      | Purpose                            |
|--------------------------------|------------------------------------|
| `CAMERA`                       | Required for AR camera feed        |
| `RECORD_AUDIO`                 | For capturing audio if needed      |
| `INTERNET` & `ACCESS_NETWORK`  | API communication, Algolia, etc.   |
| `READ_MEDIA_IMAGES`            | Gallery access for image overlays  |
| `WRITE_EXTERNAL_STORAGE`       | Storing images, DeepAR config      |
| `READ_EXTERNAL_STORAGE`        | Reading filters / textures         |

---

## 💠 Build Configuration

### SDK Versions
- `minSdk`: 24
- `targetSdk`: 35
- `compileSdk`: 35

### Build Tools
- Kotlin: `2.0.20`
- Gradle Plugin: `8.8.0`

### Signing
- Release signing config included in `signingConfigs.release`
- Stored keystore reference: `../mirrarykey`

---

## 📦 Key Dependencies

```kotlin
// Core
implementation("androidx.core:core-ktx:1.15.0")
implementation("androidx.appcompat:appcompat:1.7.0")

// Navigation
implementation("androidx.navigation:navigation-fragment-ktx:2.8.6")
implementation("androidx.navigation:navigation-ui-ktx:2.8.6")

// Firebase
implementation("com.google.firebase:firebase-database:21.0.0")
implementation("com.google.firebase:firebase-crashlytics:19.4.1")

// Camera + Face Detection
implementation("androidx.camera:camera-core:1.4.1")
implementation("com.google.mlkit:face-detection:16.1.7")
implementation("com.google.mediapipe:tasks-vision:0.20230731")

// DeepAR
implementation("ai.deepar.ar:DeepAR:5.6.17")

// Search
implementation("com.algolia:algoliasearch-client-kotlin:2.1.3")

// Room
implementation("androidx.room:room-runtime:2.6.1")

// Others
implementation("com.github.bumptech.glide:glide:4.16.0")
implementation("com.google.zxing:core:3.5.2")
```

---

## 📲 How to Run

### Prerequisites
- Android Studio Flamingo or later
- Gradle 8.0+
- Kotlin Plugin 2.0.20
- Minimum Android 7.0 (API 24) device or emulator with camera support

### Steps

```bash
git clone https://github.com/your-org/mirrAR-tablet-tryon.git
cd mirrAR-tablet-tryon
open in Android Studio
```

- Connect an Android device with a working camera
- Run the app

---

## 🪖 Main Entry Point

- `MainActivity.kt`: App launch activity
- `DeepARActivity.kt`: AR rendering screen using DeepAR SDK
- Manifest registered `MainActivity` as launcher

---

## 📄 License

This project is proprietary to **MirrAR Technologies**. All rights reserved.

---

## 🙌 Author

**Abhishek**  
Lead Android Engineer  
📩 akaks09@gmail.com | 📱 +91-7004900663

