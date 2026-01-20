# VaultX Bank Management System

VaultX is a premium, enterprise-grade banking management system built with **Java 22**, **Swing**, and **Firebase**.

## Features
- **Modern UI**: Dark-mode first design using FlatLaf.
- **Role-Based Access**: Separate dashboards for Admins and Customers.
- **Secure Authentication**: Firebase Auth with BCrypt hashing.
- **Real-time Banking**: Balance management, transfers, and transaction history.
- **Analytics**: Beautifully rendered charts using JFreeChart.

## Setup Instructions for NetBeans

1. **Open Project**:
   - Launch NetBeans.
   - Go to `File` -> `Open Project`.
   - Select the `VaultX2` folder (ensure it's recognized as a Maven project).

2. **Firebase Setup**:
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Create a new project named `VaultX`.
   - Go to `Project Settings` -> `Service accounts`.
   - Click `Generate new private key`.
   - Rename the downloaded JSON file to `serviceAccountKey.json`.
   - Place this file in `src/main/resources/`.

3. **Run Application**:
   - Right-click the project in NetBeans and select **Run**.
   - The `VaultXApp` main class will launch the Login Screen.

## Testing Credentials (Mock Mode)
- **Admin**: Enter `admin` in the username field to bypass auth and see the Admin Dashboard.
- **Customer**: Enter anything else (or leave blank) and click login to see the Customer Dashboard.

## Tech Stack
- **UI**: Java Swing + FlatLaf + MigLayout
- **Backend**: Firebase Firestore & Admin SDK
- **Security**: BCrypt + OTP logic
- **Charts**: JFreeChart
- **Icons**: Ikonli (Material Design)
