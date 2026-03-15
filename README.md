# Ear Sensei - Android App

## Overview
Ear Sensei is a hearing protection app that monitors your headphone/speaker volume and tracks daily exposure to prevent noise-induced hearing loss (NIHL).

## Features
✅ Real-time volume monitoring
✅ ISO 1999 compliant hearing damage calculation
✅ Daily exposure tracking (shows % of safe limit)
✅ Background monitoring service
✅ Smart alerts when approaching danger levels
✅ SQLite database for historical tracking

## How to Build the APK

### Step 1: Install Android Studio
1. Download Android Studio from: https://developer.android.com/studio
2. Install it on your Windows machine
3. During installation, make sure to install:
   - Android SDK
   - Android SDK Platform
   - Android Virtual Device (optional, for testing)

### Step 2: Open the Project
1. Launch Android Studio
2. Click "Open" or "Open an Existing Project"
3. Navigate to the `ear-sensei-android` folder
4. Click OK

### Step 3: Let Android Studio Sync
1. Android Studio will automatically detect it's a Gradle project
2. It will download required dependencies (this takes 5-10 minutes first time)
3. Wait for "Gradle sync finished" message in the bottom status bar

### Step 4: Build the APK
**Option A: Debug APK (for testing)**
1. Go to menu: Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Wait for build to complete (~2-5 minutes)
3. Click "locate" in the notification that appears
4. You'll find the APK at: `app/build/outputs/apk/debug/app-debug.apk`

**Option B: Release APK (for distribution)**
1. Go to menu: Build → Generate Signed Bundle / APK
2. Select APK, click Next
3. If you don't have a keystore:
   - Click "Create new..."
   - Choose a location and password
   - Fill in the details
4. Click Next, select "release", click Finish
5. Find APK at: `app/build/outputs/apk/release/app-release.apk`

### Step 5: Install on Phone
**Method 1: USB Cable**
1. Enable "Developer Options" on your Android phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable "USB Debugging" in Developer Options
3. Connect phone via USB
4. Copy the APK to phone and tap to install
5. You may need to allow "Install from Unknown Sources"

**Method 2: File Transfer**
1. Email the APK to yourself
2. Open email on phone
3. Download and install the APK
4. Allow "Install from Unknown Sources" when prompted

## System Requirements
- **Minimum Android Version:** Android 8.0 (Oreo) - API 26
- **Target Android Version:** Android 14 - API 34
- **Recommended:** Android 10+ for best experience

## How to Use the App

### First Launch
1. Open Ear Sensei
2. Grant permissions when prompted:
   - Modify audio settings (to read volume)
   - Post notifications (for alerts)
3. Tap "Start Monitoring"
4. The app will now run in the background

### Understanding the Dashboard
- **Daily Exposure**: Shows % of safe listening limit (100% = 8 hours at 85 dB)
- **Listening Time**: Total time spent listening today
- **Current Volume**: Your current device volume level

### Alert Levels
- **Green (0-50%)**: Safe - continue normally
- **Yellow (50-75%)**: Moderate - consider taking breaks
- **Orange (75-100%)**: High risk - reduce volume or take a break
- **Red (100%+)**: Daily limit exceeded - stop listening immediately

### Background Monitoring
- The app monitors volume every 10 seconds
- It runs as a foreground service (persistent notification)
- Data is saved locally in SQLite database
- You can close the app and it keeps monitoring

## Technical Details

### Hearing Damage Calculation
Uses NIOSH (National Institute for Occupational Safety and Health) formula:
- 85 dB = safe for 8 hours
- Every +3 dB = halve safe time
- Example: 91 dB = safe for 2 hours, 100 dB = safe for 15 minutes

### Volume to dB Estimation
Since we can only read system volume (not actual acoustic output), we estimate:
- 0% volume ≈ 50 dB
- 100% volume ≈ 100 dB
- Linear interpolation between these points

**Note:** Actual dB depends on:
- Headphone model
- Seal/fit quality
- Audio source quality
This is an approximation for awareness, not a medical device.

### Data Storage
- All data stored locally on device
- No internet connection required
- No data shared with servers
- Database location: `/data/data/com.organsensei.earsensei/databases/ear_sensei.db`

## Troubleshooting

### Build Errors
**"SDK not found"**
- Go to Tools → SDK Manager
- Install Android SDK Platform 34
- Install Build Tools 34.0.0

**"Gradle sync failed"**
- File → Invalidate Caches / Restart
- Try again after restart

**"Kotlin not configured"**
- Should auto-configure, but if not:
- Tools → Kotlin → Configure Kotlin in Project

### App Crashes
**On first launch:**
- Make sure to grant all permissions
- Check Android version is 8.0+

**Background service stops:**
- Disable battery optimization for Ear Sensei
- Settings → Apps → Ear Sensei → Battery → Don't optimize

### Notifications Not Showing
- Go to Settings → Apps → Ear Sensei → Notifications
- Enable all notification channels
- Make sure "Do Not Disturb" is off

## Privacy & Permissions

### Why We Need Each Permission:
- **MODIFY_AUDIO_SETTINGS**: To read current volume levels
- **POST_NOTIFICATIONS**: To alert you about hearing risks
- **FOREGROUND_SERVICE**: To monitor in background
- **WAKE_LOCK**: To keep monitoring even when screen off

**We do NOT:**
- Record audio
- Access microphone
- Connect to internet
- Share any data
- Track location

## Future Enhancements (Roadmap)
- [ ] Headphone model database for accurate dB mapping
- [ ] Weekly/monthly exposure reports
- [ ] Integration with Spotify/YouTube to detect what you're listening to
- [ ] Machine learning to detect risky usage patterns
- [ ] Export data to CSV
- [ ] Multi-user profiles
- [ ] Integration with health apps (Google Fit)

## License
© 2026 OrganSensei. All rights reserved.
For personal use only. Not a medical device.

## Support
For issues or questions, this is an MVP/prototype version.
Built with care for your hearing health 🎧❤️

---

**Important Disclaimer:**
Ear Sensei is an awareness tool, not a medical device. For professional hearing assessment, consult an audiologist. Estimated dB levels are approximations and may not reflect actual acoustic exposure.
