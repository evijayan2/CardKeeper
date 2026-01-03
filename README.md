# Kards

**Your Secure Digital Vault for Personal Documents.**

[Documentation](https://evijayan2.github.io/CardKeeper/) | [Releases](https://github.com/evijayan2/CardKeeper/releases)

---

## About Kards

Kards is a privacy-first Android application designed to securely store and manage your most sensitive personal documents and financial information. In a world where digital identity is fragmented across emails and photos, Kards provides a single, encrypted secure enclave on your device.

It allows you to digitize, organize, and quickly access your **Financial Accounts** (Credit/Debit/Bank), **Identity Documents** (Driver Licenses, Aadhar, PAN), and **Travel Documents** (Passports, Green Cards).

## Why Kards? (Benefits)

> [!IMPORTANT]
> **Data Privacy Promise**: Your personal information **never** leaves your device. While the app uses internet access to fetch UI assets like institution logos and flags, it **never** uploads, syncs, or transmits your sensitive data to any server.

*   **🔒 Uncompromised Security**: Every byte of your sensitive data is stored in a local, AES-256 encrypted vault, protected by hardware-backed keys.
*   **⚡ Instant Access**: Stop digging through your gallery or emails. Find your passport number or bank routing number in seconds.
*   **� Private by Design**: Your database is strictly local. No reliance on cloud storage means no risk of remote data breaches or account hijacking.
*   **🧠 Intelligent Integration**: Automatically scans and extracts data from Passports, Driver Licenses, and Aadhar Cards, reducing manual entry errors.

## Release Notes

View the full history of changes:

- [v1.0.0-alpha.3](releases/v1.0.0-alpha.3.md) - Rebranding to Kards, Aadhar & Green Card Support, Global Search.
- [v1.0.0-alpha.2](releases/v1.0.0-alpha.2.md) - CI/CD Fixes.
- [v1.0.0-alpha.1](releases/v1.0.0-alpha.1.md) - Initial Release with Financial & Identity support.

## Key Features

-   **Biometric Authentication**: Secure access via Fingerprint, Face Unlock, or Device PIN (Android 11+).
-   **Smart Scanning**:
    -   **MRZ Scanner**: Instantly reads Machine Readable Zones on Passports and Green Cards.
    -   **Barcode/QR Scanner**: Decodes PDF417 barcodes on US Driver Licenses, QR codes on Aadhar cards, and various retail barcodes.
    -   **PAN Card Support**: Text recognition for Indian PAN Cards.
-   **Expiration Alerts**: Receive proactive notifications for expiring documents (30 days, 7 days, 1 day) and stay organized with app icon badges.
-   **Customizable Settings**: Personalize your experience with Theme selection (Light/Dark/System) and flexible Date Format options.
-   **Unified Search**: Deep search across all documents. Find cards by institution, last 4 digits, holder name, or document type.
-   **Quick Copy**: One-tap copying for sensitive fields like Gift Card codes and PINs.
-   **Masking & Privacy**: Sensitive fields (like Account Numbers) are masked by default in the UI to prevent shoulder-surfing.
-   **Visual Reference**: Store high-quality front and back images of your physical cards for visual verification.

## Security Architecture

Kards is built with a "Security by Design" philosophy:

1.  **Hardware-Backed Encryption**: The Master Encryption Key is generated and stored in the **Android Keystore System** (a hardware-enforced secure container).
2.  **User Authentication Binding**: This Master Key is cryptographically bound to your biometrics. It **cannot** be used to decrypt your data unless you have successfully authenticated with your fingerprint or face.
3.  **Full Database Encryption**: The application uses **SQLCipher** to encrypt the entire SQLite database (accessed via SQLDelight) with 256-bit AES encryption.
4.  **Zero-Knowledge Architecture**: The developer (or anyone else) has zero access to your data. There is no telemetry, no tracking, and no outbound data collection.
5.  **Strict Data Residency**: Your sensitive documents stay exactly where you put them—on your phone. Internet access is utilized solely for enhancing the UI (e.g., downloading institution logos and flags) and never for transmitting user data.
6.  **Encrypted Image Vault**: All card images (scanning results, photos) are encrypted on disk using AES-GCM with unique 12-byte IVs. They are decrypted only in memory using a custom, hardened decoder pipeline that bypasses standard system caches to prevent data leakage.

---

## Supported Documents

| Type | Features |
| :--- | :--- |
| **Financial Accounts** | Debit/Credit Cards, Bank Accounts, Wire Info |
| **Identity** | Driver Licenses (US), Aadhar Cards (India), PAN Cards (India), Voter IDs |
| **Immigration** | Green Cards (US Permanent Resident Cards) |
| **Travel** | Passports (Global MRZ Support) |
| **Gift Cards** | Store Card Numbers and PINs with Quick Copy |
| **Other** | Loyalty/Rewards Cards, Library Cards |

---

## Prerequisites

- **JDK 17** or higher (required for AGP 8.0+).
- **Android Studio** (Koala or newer recommended).
- **Android SDK** (API 34/35 recommended).

## Android Compatibility

Kards is optimized for modern Android devices.

| Android Version | Compatibility Level | Notes |
| :--- | :--- | :--- |
| **Android 11+** (API 30+) | **Full Support** | Recommended for the best experience. Supports Biometrics and PIN/Pattern fallback. |
| **Android 8.0 - 10** | **Partial Support** | Requires a fingerprint sensor. Device PIN/Pattern fallback is not supported on these versions. |
| **Android 13+** (API 33+) | **Full Support** | Requires runtime notification permission for expiration reminders. |

## Building the Code

To build the project from the command line, use the Gradle wrapper.

### macOS / Linux
```bash
./gradlew build
```

### Windows
```cmd
gradlew.bat build
```

This command will compile the code, run lint checks, and run unit tests.

## Generating an APK

To generate a debug APK:

### macOS / Linux
```bash
./gradlew assembleDebug
```

### Windows
```cmd
gradlew.bat assembleDebug
```

The output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

To generate a signed release APK, you will need to configure your signing keys in `build.gradle.kts` and run:

```bash
./gradlew assembleRelease
```

## Starting the Emulator

### Via Android Studio (Recommended)
1. Open Android Studio.
2. Go to **Device Manager** (icon in the toolbar or `Tools > Device Manager`).
3. Click the **Play** button next to your virtual device.

### Via Command Line
If you have the Android SDK `emulator` tool in your path:

1. List available AVDs:
   ```bash
   emulator -list-avds
   ```
2. Start an emulator:
   ```bash
   emulator -avd <YOUR_AVD_NAME>
   ```

## Running on a Physical Device (e.g., Pixel 9 Pro XL)

> [!NOTE]
> This app is configured with Android Gradle Plugin 8.5.1+ to support 16KB memory page sizes, which is required for Android 15 devices like the Pixel 9 Pro and Pixel 10 series.

To run the app on your Pixel 9 Pro XL or any other Android device:

1.  **Enable Developer Options**:
    *   Go to **Settings > About phone**.
    *   Scroll down to **Build number** and tap it 7 times until you see "You are now a developer!".
2.  **Enable USB Debugging**:
    *   Go to **Settings > System > Developer options**.
    *   Enable **USB debugging**.
3.  **Connect Device**:
    *   Connect your Pixel 9 Pro XL to your computer via USB cable.
    *   Accept the "Allow USB debugging?" prompt on your phone screen.
4.  **Run the App**:
    *   In Android Studio, select your "Google Pixel 9 Pro XL" from the device dropdown.
    *   Click **Run**.

## Running the App

### Using Android Studio
1. Open the project in Android Studio.
2. Select your device (emulator or physical phone) from the device dropdown.
3. Click the green **Run** arrow (or press `Shift + F10` on Windows/Linux, `Control + R` on macOS).

### Using Command Line
Make sure your device is connected or the emulator is running (`adb devices` should show it).

```bash
./gradlew installDebug
```

Then launch the app on the device manually, or use `adb shell am start` if you know the main activity name (usually `.MainActivity`).

## Debugging

### Via Android Studio
1. Set breakpoints in your code by clicking the gutter area next to line numbers.
2. Click the **Debug** icon (bug with a play button) in the toolbar (or press `Shift + F9`).
3. The app will launch in debug mode and pause at your breakpoints.

### Attaching Debugger to Running Process
If the app is already running:
1. Click **Attach Debugger to Android Process** in the toolbar.
2. Select your device and the `com.cardkeeper` (or your app package name) process.
3. Click **OK**.

## Search Functionality

Kards includes a powerful search feature accessible via the search icon on the home screen. You can search in two ways:

### Field-Based Search
Type any text to search across all stored items by their fields:
- **Financial Accounts**: Institution name, account name, holder name, account number
- **Identity Documents**: Holder name, document number, issuing authority
- **Passports**: Passport number, surname, given names
- **Green Cards**: USCIS number, surname, given name

### Type-Based Search
Search by category to see all items of a specific type:

| Search Term | Returns |
|-------------|---------|
| `green card`, `greencard`, `gc` | All green cards |
| `passport`, `passports` | All passports |
| `identity`, `id`, `document`, `driver`, `license`, `dl`, `driver license` | All identity documents |
| `finance`, `financial`, `bank`, `credit`, `debit`, `atm`, `credit card`, `debit card` | All financial accounts |
| `rewards`, `reward`, `library` | All rewards & library cards |
| `gift card`, `giftcard`, `gift` | All gift cards |

> [!TIP]
> Search requires at least 2 characters. Results are displayed with type badges for easy identification.

## Project Structure

- `app/`: Android-specific application module.
  - `src/main/java/`: Main Android source code including Activities, WorkManager workers, and platform-specific Scanning implementations.
  - `src/main/res/`: Android resources and XML layouts.
- `shared/`: Kotlin Multiplatform (KMP) module containing core app logic.
  - `src/commonMain/`: Shared UI components (Compose), Data repositories, SQLDelight database, and ViewModels.
- `releases/`: Detailed release notes for each version.
- `docs/`: Project specifications and manual testing documentation.
- `build.gradle.kts`: Project-level build configuration.
