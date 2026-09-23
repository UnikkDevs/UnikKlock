# UnikKlock ⚡⏰

An intelligent, challenge-driven alarm clock and time utility app for Android built with **Kotlin** and **Jetpack Compose (Material 3)**. UnikKlock ensures you actually wake up on time by requiring you to complete brain-stimulating challenges before dismissing your alarm, backed by reliable alarm scheduling and foreground service audio.

---

## 📱 Features

### 🧠 Smart Wake-Up Challenges
Say goodbye to accidental snoozes! Alarms can be configured to require one of several interactive challenges to dismiss:
- **Math Master**: Solve addition, subtraction, multiplication, or multi-step equations across Easy, Medium, and Hard difficulties.
- **Memory Sequence**: Remember and repeat back numeric sequences under pressure.
- **Memory Card Match**: Flip and match pairs of icons to test spatial memory.
- **Pattern Recognition**: Identify geometric or mathematical patterns.
- **Word Scramble**: Unscramble energetic morning keywords.
- **Reaction Time**: Tap the screen the instant the indicator turns green.
- **Speed Tap**: Rapidly tap the target counter before the timer runs out.
- **Randomizer**: Let the app pick a random challenge every morning.

### ⏰ Advanced Alarm System
- **Exact Alarms**: Accurate scheduling with `AlarmManager.setExactAndAllowWhileIdle()` and automatic reschedule on reboot (`BOOT_COMPLETED`).
- **Full-Screen Wake Intent**: Displays an interactive wake screen directly over the lock screen.
- **Gradual Volume Crescendo**: Gentle ramp-up of alarm volume over configurable durations.
- **Built-in Tones & Audio Engine**: High-fidelity procedural audio synthesizer providing multiple wake melodies and frequencies.
- **Configurable Snooze**: Set snooze duration and maximum allowed snoozes to prevent oversleeping.

### ⏱️ Comprehensive Clock Utilities
- **Sleep Cycle Calculator**: Calculate ideal bedtimes and wake times based on natural 90-minute REM sleep cycles.
- **World Clock**: Monitor current times and day/night statuses across major international timezones.
- **Stopwatch**: Precision millisecond stopwatch with lap tracking and split times.
- **Timer**: Easy-to-use countdown timer with presets and pause/reset controls.

### 🎨 Modern Material 3 UI
- Clean, aesthetic dark-mode first design with neon cyan-to-purple accent gradients.
- Custom adaptive launcher icon with glowing neon finish.
- Smooth Compose animations and edge-to-edge layout support.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material Design 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) + StateFlow & Coroutines
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room)
- **Audio & Services**: Android Foreground Service (`mediaPlayback`), `AudioTrack` PCM tone generator, and `Vibrator`
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`libs.versions.toml`)
- **CI/CD**: GitHub Actions workflow for automatic APK building

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / Iguana or later
- **JDK**: Version 17
- **Android SDK**: `minSdk = 26` (Android 8.0), `compileSdk = 35` (Android 15)

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/<your-username>/UnikKlock.git
cd UnikKlock

# Build the Debug APK using Gradle
gradle :app:assembleDebug
```
The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📦 Download Prebuilt APK (GitHub Actions)

Every push or pull request to the `main` branch automatically triggers a GitHub Actions workflow that compiles and publishes the latest debug APK:

1. Navigate to the **Actions** tab of this repository.
2. Select the latest **Build UnikKlock APK** workflow run.
3. Scroll down to **Artifacts** and click **UnikKlock-debug-apk** to download the installable `.apk` directly to your Android device.

---

## 🔒 Permissions & Safety

| Permission | Purpose |
| :--- | :--- |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Ensures alarms trigger at exact minute boundaries. |
| `RECEIVE_BOOT_COMPLETED` | Restores scheduled alarms automatically if the device restarts. |
| `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Keeps the alarm audio playing reliably when the screen is locked. |
| `USE_FULL_SCREEN_INTENT` | Displays the challenge screen directly when the alarm rings. |
| `VIBRATE` | Provides haptic vibration alert alongside audio. |
| `WAKE_LOCK` | Wakes up the CPU to trigger alarm notification and audio playback. |

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — feel free to modify and use it in your own projects!
