# Project Specification: CardKeeper (Kards)

## 1. Project Overview
**App Name:** Kards (Internal Project Name: CardKeeper)
**Platform:** Android (Min SDK: 26, Target SDK: 36)
**Purpose:** A secure, offline-first personal data management application designed to store sensitive documents, financial details, and identity cards locally on the user's device.

## 2. Key Features & User Flows

### 2.1 Security & Authentication (Mandatory)
*   **Biometric / Device Lock:** The app enforces strict security. It REQUIRES the device to have a secure lock screen (PIN, Pattern, or Password) or Biometrics enrolled.
    *   **First Launch:** User sees a "Welcome" screen prompting to "Register Biometrics/Security".
    *   **Subsequent Launches:** App immediately prompts for Biometric or Device Credential authentication before showing any data.
    *   **Data Encryption:** All data is stored in a `SQLCipher` encrypted database. The encryption key is secured via Android Keystore.
    *   **Background Protection:** App clears sensitive keys from memory when backgrounded.

### 2.2 Dashboard (Home Screen)
The dashboard is organized into four main tabs:
1.  **Finance:** Lists Bank Accounts, Credit/Debit Cards.
2.  **Identity:** Lists Driver Licenses, Aadhar Cards, Green Cards.
3.  **Passports:** Lists Passports.
4.  **Rewards:** Lists Rewards Cards and Gift Cards.

**FAB (Floating Action Button):** The central "+" button opens a menu to add new items:
*   Credit/Debit Card
*   Bank Account
*   Rewards Card
*   Gift Card
*   Driver License
*   Passport
*   Green Card
*   Aadhaar Card
*   Other Identity

### 2.3 Item Management & Scanning
The app leverages **Google ML Kit** and **CameraX** for intelligent data entry.

*   **Financial Cards:** Supports scanning card details (Number, Expiry) using text recognition.
*   **Driver License:** Supports scanning PDF417 barcodes on the back of US licenses to auto-fill data.
*   **Passport:** Supports scanning the MRZ (Machine Readable Zone) to auto-fill identity details.
*   **Aadhar Card:** Supports scanning the QR code. Validates signature using UIDAI public certificates.
*   **Green Card:** Supports MRZ scanning (back side) for data extraction.
*   **Manual Entry:** All fields can be edited manually.
*   **Images:** Users can capture Front and Back images of physical cards/docs. These are stored locally.

### 2.4 Search
*   **Universal Search:** Accessible from the Home Screen top bar.
*   **Capabilities:** Searches across all stored item types (Names, Numbers, Providers).

### 2.5 Settings
*   **Appearance:** Light / Dark / System Default themes.
*   **General:** Date Format configuration (App Default, System Default, Custom).
*   **Notifications:**
    *   **Item Expiration:** Toggle to enable/disable alerts for expiring items (e.g., Passport, Credit Card).
    *   **Reminder Schedule:** Configurable days before expiration (e.g., 30, 7, 1 day prior).

## 3. Technical Stack & Dependencies (For Tester Awareness)
*   **Storage:** Room Database with SQLCipher (Encryption).
*   **Camera/Scanning:** CameraX, ML Kit (Text Recognition, Barcode Scanning, Document Scanner), ZXing.
*   **Security:** Android Biometric API.
*   **Image Loading:** Coil.
*   **Architecture:** MVVM with Jetpack Compose.

## 4. Test Scenarios for Manual Testing

### 4.1 Installation & Onboarding
1.  **Fresh Install:** Verify "Welcome Screen" appears.
2.  **No Security:** If device has NO lock screen set, verify app shows "Device Security Required" dialog and prevents usage.
3.  **Registration:** Verify "Register Biometrics" flow works and leads to Dashboard.

### 4.2 Authentication
1.  **Cold Start:** Kill app and relaunch. Verify Biometric prompt appears immediately.
2.  **Backgrounding:** minimize app and restore. Verify if auth is required (depending on implementation specifics, immediate or timeout). *Note: Current implementation clears keys on background, enforcing re-auth.*
3.  **Failure:** Fail biometrics multiple times and verify fallback to Device PIN/Pattern.

### 4.3 Adding Items
1.  **Credit Card:** Use "Scan Card" feature. Verify Number and Expiry are detected. Save and verify in "Finance" tab.
2.  **Passport:** Scan MRZ. Verify Name, DOB, Expiry, Passport Number are populated correctly.
3.  **Aadhar:** Scan QR. Verify data poulation.
4.  **Images:** Capture Front/Back photos for an item. Verify they appear in the Detail View.

### 4.4 Settings & Notifications
1.  **Theme:** Switch to Dark Mode. Verify UI updates immediately.
2.  **Date Format:** Change format. Verify dates on Dashboard/Detail screens reflect change.
3.  **Expirations:** Add an item expiring tomorrow. Verify if a notification triggers (requires `WorkManager` background job trigger logic to be tested, usually runs periodically).

### 4.5 Data Persistence
1.  **Restart:** Add items, kill app, relaunch, auth. Verify items persists.
2.  **Images:** Verify images load correctly after restart (ensures path saving is correct).
