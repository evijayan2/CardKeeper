# Kards

**Your Secure Digital Vault for Personal Documents.**

[Download Latest Release](https://github.com/evijayan2/CardKeeper/releases)

---

## About Kards

**Kards** is a privacy-first Android application designed to securely store and manage your most sensitive personal documents and financial information. In a world where digital identity is fragmented across emails and photos, Kards provides a single, encrypted secure enclave on your device.

It allows you to digitize, organize, and quickly access your **Financial Accounts** (Credit/Debit/Bank), **Identity Documents** (Driver Licenses, Aadhar), and **Travel Documents** (Passports, Green Cards).

## Why Kards? (Benefits)

*   **🔒 Uncompromised Security**: Your data never leaves your device. It is stored in a local, AES-256 encrypted database.
*   **⚡ Instant Access**: Stop digging through your gallery or emails. Find your passport number or bank routing number in seconds.
*   **📵 Offline First**: Works completely offline. No reliance on cloud servers means no risk of remote data breaches.
*   **🧠 Intelligent Integration**: Automatically scans and extracts data from Passports (MRZ), Driver Licenses (Barcode), and Aadhar Cards (QR), reducing manual entry errors.

## Key Features

-   **Biometric Authentication**: Access is protected by your device's biometric security (Fingerprint/Face Unlock). The app cannot be opened without you.
-   **Smart Scanning**:
    -   **MRZ Scanner**: Instantly reads Machine Readable Zones on Passports and Green Cards.
    -   **Barcode/QR Scanner**: Decodes PDF417 barcodes on US Driver Licenses and QR codes on Aadhar cards.
-   **Unified Search**: deeply search across all your documents. Find a card by its last 4 digits, or find all "Financial" items with a single tap.
-   **Visual Reference**: Store high-quality front and back images of your physical cards for visual verification.
-   **Masking & Privacy**: Sensitive fields (like Account Numbers) are masked by default in the UI to prevent shoulder-surfing.

## Security Architecture

Kards is built with a "Security by Desgin" philosophy:

1.  **Hardware-Backed Encryption**: The Master Encryption Key is generated and stored in the **Android Keystore System** (a hardware-enforced secure container).
2.  **User Authentication Binding**: This Master Key is cryptographically bound to your biometrics. It **cannot** be used to decrypt your data unless you have successfully authenticated with your fingerprint or face.
3.  **Full Database Encryption**: The application uses **SQLCipher** to encrypt the entire SQLite database with 256-bit AES encryption.
4.  **Zero-Knowledge**: The developer has no access to your data. There is no backend server, no tracking, and no analytics.

---

## Supported Documents

| Type | Features |
| :--- | :--- |
| **Financial Accounts** | Debit/Credit Cards, Bank Accounts, Wire Info |
| **Identity** | Driver Licenses (US), Aadhar Cards (India), Voter IDs |
| **Immigration** | Green Cards (US Permanent Resident Cards) |
| **Travel** | Passports (Global MRZ Support) |
| **Other** | Loyalty/Rewards Cards, Library Cards |

---

## Get Started

### Prerequisites

- **JDK 17** or higher.
- **Android Studio**.
- **Android SDK** (API 34/35).

### Installation

Download the latest APK from our [Releases Page](https://github.com/evijayan2/CardKeeper/releases).

### Building from Source

```bash
git clone https://github.com/evijayan2/CardKeeper.git
cd CardKeeper
./gradlew build
```
