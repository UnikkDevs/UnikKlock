# Privacy Policy for UnikKlock

**Effective Date:** September 24, 2026  
**Last Updated:** September 24, 2026  

---

### Overview
**UnikKlock** ("we", "our", or "the app") is designed as an offline-first smart alarm clock and time utility application. Your privacy is paramount. **We do not collect, transmit, store, sell, or share any personal information, device identifiers, or telemetry with external servers or third parties.**

---

### 1. Information Collection and Use
UnikKlock does **not** collect any personal data:
- No account registration or login required.
- No collection of names, email addresses, phone numbers, location data, or payment information.
- No third-party analytics (e.g. Firebase Analytics, Google Analytics).
- No third-party advertising SDKs or tracking cookies.

### 2. Local Device Storage
All data created in the app is stored locally on your device in an encrypted Room SQLite database:
- **Alarms:** Configured alarm times, labels, repeat schedules, volume, and challenge preferences.
- **Wake Statistics:** Historical completion times, snooze counts, and challenge completion duration.
- **App Settings:** Theme preference (Dark / Light / System) and audio settings.

This data never leaves your device. If you uninstall the app or clear app data via Android Settings, this local data is permanently deleted.

### 3. Android Permissions
The app requests only standard Android permissions strictly necessary for alarm functionality:
- **`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`**: Ensures alarms ring precisely at the exact minute set.
- **`POST_NOTIFICATIONS`**: Displays heads-up notifications for upcoming alarms, snooze countdowns, and timers.
- **`FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PLAYBACK`**: Keeps the audio engine running continuously when an alarm fires until you complete the wake challenge.
- **`VIBRATE`**: Triggers device vibration alongside the alarm tone.
- **`WAKE_LOCK`**: Wakes the screen when an alarm is triggered.
- **`RECEIVE_BOOT_COMPLETED`**: Automatically restores and reschedules active alarms after device reboots.

### 4. Third-Party Services
UnikKlock does not share data with any third-party services, data brokers, or cloud platforms.

### 5. Children’s Privacy
UnikKlock does not collect personal data from anyone, including children under 13. The application is suitable for users of all ages.

### 6. Contact Us
If you have any questions about this Privacy Policy, please contact:
- **Email:** nikhilkathait666@gmail.com
- **Developer:** UnikKlock Team
