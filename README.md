<p align="center">
  <img src="assets/app_icon.webp" width="140" height="140" alt="Tuneora Logo">
</p>

<h1 align="center">Tuneora</h1>

<p align="center">
  <b>Elevate your music experience with Tuneora — a premium, high-performance Audio Player for Android.</b><br>
  Built with the cutting-edge Jetpack Compose and the elegance of Material 3.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Language-Kotlin%202.0.21-blue?style=for-the-badge&logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose" alt="Compose">
  <img src="https://img.shields.io/badge/Android-Sdk%2035-3DDC84?style=for-the-badge&logo=android" alt="Android">
</p>

---

## 📝 About Tuneora
Tuneora is a meticulously crafted audio player designed for the modern Android ecosystem. It combines a "Premium Airy" design philosophy — featuring glassmorphism and fluid micro-animations — with a powerful media engine. Tuneora is engineered for music lovers who demand both aesthetic beauty and technical excellence.

---

## 🛠️ Technology Stack
Tuneora utilizes the latest industry-standard libraries to ensure a stable and fluid experience.

| Component | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Core Language** | Kotlin | `2.0.21` | Modern, safe, and powerful. |
| **UI Framework** | Jetpack Compose | `2024.12.01 (BOM)` | Declarative UI for a responsive interface. |
| **Media Engine** | Media3 (ExoPlayer) | `1.5.1` | High-fidelity audio playback. |
| **Image Loading** | Coil | `3.0.4` | Efficient, hardware-accelerated album art decoding. |
| **Database** | Room | `2.6.1` | Reliable persistence for library data. |
| **Dependency Injection** | Hilt | `2.51.1` | Clean and scalable architecture. |
| **Logging** | Timber | `5.0.1` | Streamlined development logging. |

---

## ✨ Key Features

### 🎵 High-Fidelity Playback
*   **Media3 Engine**: Smooth playback of all major audio formats (MP3, FLAC, AAC, etc.).
*   **Volume Boost**: Enhance audio output up to 200% for a richer sound experience.
*   **Gestures Control**: Intuitive swipe controls for volume adjustment and track seeking.
*   **Playback Speed**: Fine-tuned control over playback speed (0.25x to 4.0x).

### 📂 Smart Library Management
*   **Playlists**: Create and manage custom offline playlists.
*   **Folder Browsing**: Automatically groups your music by directory structure.
*   **Universal Search**: Instant, real-time search across songs, artists, and albums.
*   **Search History**: Quickly access your recent searches.
*   **Excluded Folders**: Hide specific directories from your music library for a cleaner view.

### 🎨 Premium Aesthetics
*   **Material 3**: Fully compliant with the latest Material Design guidelines.
*   **Glassmorphic UI**: Modern, translucent design elements that feel light and airy.
*   **Dynamic Accent Colors**: Personalize the interface with curated professional themes.
*   **Smooth Micro-Animations**: Delighting transitions when navigating through your library.

---

## 📸 Screenshots

<table align="center">
  <tr>
    <td align="center"><b>Home Screen</b></td>
    <td align="center"><b>Music Library</b></td>
    <td align="center"><b>Audio Player</b></td>
  </tr>
  <tr>
    <td><img src="assets/Homescreen.png" width="200" style="border-radius: 12px; border: 1px solid #ddd;"></td>
    <td><img src="assets/Library_screen.png" width="200" style="border-radius: 12px; border: 1px solid #ddd;"></td>
    <td><img src="assets/Player_screen.png" width="200" style="border-radius: 12px; border: 1px solid #ddd;"></td>
  </tr>
  <tr>
    <td align="center"><b>Playlists</b></td>
    <td align="center"><b>Listening History</b></td>
    <td align="center"><b>Settings</b></td>
  </tr>
  <tr>
    <td><img src="assets/Playlist_screen.png" width="200" style="border-radius: 12px; border: 1px solid #ddd;"></td>
    <td><img src="assets/History_screen.png" width="200" style="border-radius: 12px; border: 1px solid #ddd;"></td>
    <td><img src="assets/Settings_screen.png" width="200" style="border-radius: 12px; border: 1px solid #ddd;"></td>
  </tr>
</table>

---

## 🚀 Installation & Requirements

### System Requirements
* **OS**: Android 7.0 (Nougat) or higher (Min SDK 24).
* **Architecture**: Supported on `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`.
* **Compiled SDK**: 35.

### Permissions Explained
* **Media Access**: Required to scan and play audio files from your device.
* **All Files Access (Optional)**: Granting this allows for **Fast Delete**, bypassing system confirmation dialogs for a more seamless management experience.
* **Notifications**: For media controls in the notification drawer.

---

## 🏗️ Build from Source
Ensure you have **Android Studio Ladybug** and **JDK 21** configured.

```bash
# Clone the repository
git clone https://github.com/Maheswara660/Tuneora.git

# Enter the project directory
cd Tuneora

# Build the release variant
./gradlew assembleRelease
```

---

## ❤️ Support & Community
Tuneora is a labor of love by a **solo developer**. Your support directly fuels the development of new features!

* ⭐ **Star**: Please give this project a star if you find it useful.
* ☕ **[Buy me a coffee](https://ko-fi.com/Maheswara660)**: Support my work via Ko-fi.
* 🤝 **Contribute**: Check out the [Contributing Guidelines](.github/CONTRIBUTING.md).

---

## 📜 License
Tuneora is open-source software licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for more information.

---

## ✉️ Message from Developer
> "Tuneora was born out of a desire for an audio player that feels professional yet looks beautiful. Every line of code and every UI component has been crafted to provide the best possible experience on Android. I hope Tuneora becomes an essential part of your music experience."
> — **Maheswara660**

<p align="center">
  <b>Tuneora — Music in your pocket.</b><br>
  Made with ❤️ in India
</p>