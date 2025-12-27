# CardKeeper

Securely manage your cards and documents.

[Documentation](https://evijayan2.github.io/CardKeeper/) | [Releases](https://github.com/evijayan2/CardKeeper/releases)

---

CardKeeper is an Android application designed to securely store and manage various types of card and bank information.

## Prerequisites

- **JDK 17** or higher (required for AGP 8.0+).
- **Android Studio** (Koala or newer recommended).
- **Android SDK** (API 34/35 recommended).

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

CardKeeper includes a powerful search feature accessible via the search icon on the home screen. You can search in two ways:

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
| `identity`, `id`, `document`, `documents` | All identity documents |
| `finance`, `financial`, `bank`, `card`, `cards` | All financial accounts (credit/debit/ATM cards) |
| `rewards`, `reward`, `library` | All rewards & library cards |

> [!TIP]
> Search requires at least 2 characters. Results are displayed with type badges for easy identification.

## Project Structure

- `app/`: Main application module containing source code and resources.
- `build.gradle.kts`: Project-level build configuration.
- `app/build.gradle.kts`: App-module build configuration.
