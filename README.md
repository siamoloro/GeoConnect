# GeoConnect 📍

GeoConnect is a location-based mobile platform connecting customers with verified freelancers and local small businesses. The application features real-time geolocation mapping, automated provider verification statuses, dynamic profile management, and direct booking workflows.

---

## 📸 Core User Journeys & Screen Flow

```
[ Welcome / Sign In ]
       │
       ├──► [ Customer Registration ] ──► [ Customer Dashboard ]
       │                                         │
       │                                         ├──► [ Dynamic Market Discovery (Map / List) ]
       │                                         └──► [ Provider Details & Booking ]
       │
       └──► [ Freelancer Registration ] ──► [ Freelancer Profile ]
                                                 │
                                                 ├──► [ Edit Profile / Rates / Bio ]
                                                 └──► [ Settings & Account Operations ]
```

---

## 🛠️ Technology Stack & Architecture

* **Mobile Client**: Native Android (Java / Kotlin, Android SDK)
* **UI Framework**: Material Design Components, ConstraintLayout, Dynamic System Bar Insets (`EdgeToEdge`)
* **Backend Infrastructure**: Google Firebase (Serverless)
  * **Firebase Authentication**: Email/Password account security & credential management
  * **Cloud Firestore**: Real-time NoSQL database supporting dual-model user data
* **Location & Services**: Google Maps SDK for Android (`SupportMapFragment`, Dynamic `GeoPoint` markers)

---

## 🗄️ Database Schema & Structure (Cloud Firestore)

The application maintains four primary collections in Cloud Firestore:

```
cloud_firestore/
├── users/                       # Global authentication routing table
│   └── {userID}/
│       ├── email: "user@example.com"
│       ├── type: "Freelancer" | "Business" | "Customer"
│       └── created_at: Timestamp
│
├── freelancers/                 # Extended provider profiles
│   └── {userID}/
│       ├── name: "Tee Smith"
│       ├── category: "Plumbing"
│       ├── approvalStatus: "Pending" | "Approved"
│       ├── location: GeoPoint(-25.7479, 28.2293)
│       └── rating: 5.0
│
├── businesses/                  # Commercial local business profiles
│   └── {businessID}/
│       ├── businessName: "Apex Repairs"
│       └── location: GeoPoint(-26.2041, 28.0473)
│
└── cases/                       # Service booking requests & job management
    └── {caseID}/
        ├── customerID: "{userID}"
        ├── providerID: "{userID}"
        └── status: "Pending" | "Accepted" | "Completed"
```

---

## ⚙️ Step-by-Step Installation & Setup Guide

### 1. Prerequisites Checklist
* **Android Studio**: Iguana (2023.2) or higher installed.
* **JDK**: Java Development Kit 17 or higher configured.
* **Device / Emulator**: Android API Level 24 (Android 7.0 Nougat) or higher.
* **Firebase Account**: Access to [Firebase Console](https://console.firebase.google.com/).
* **Google Cloud Console**: Active account with **Maps SDK for Android** enabled.

---

### 2. Cloning the Repository
Open your terminal or command prompt and execute:

```bash
git clone https://github.com/your-team-org/GeoConnect.git
cd GeoConnect
```

---

### 3. Firebase Backend Configuration

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a project named **GeoConnect**.
2. Register a new **Android App** using the exact package name:
   ```text
   com.example.geoconnect
   ```
3. Download the generated **`google-services.json`** configuration file.
4. Move `google-services.json` into the project's **`app/`** directory:

```text
GeoConnect/
├── app/
│   ├── google-services.json  ◄── [ Place file here ]
│   ├── build.gradle.kts
│   └── src/
├── build.gradle.kts
└── local.properties
```

5. In Firebase Console, enable:
   * **Authentication** $\rightarrow$ Sign-in method $\rightarrow$ **Email/Password**.
   * **Cloud Firestore** $\rightarrow$ Create Database in **Test Mode**.

---

### 4. Google Maps API Key Setup

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Enable the **Maps SDK for Android**.
3. Generate an API key under **Credentials**.
4. In your project root directory, open or create the **`local.properties`** file.
5. Append your API key as shown below:

```properties
# local.properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=AIzaSyYourActualApiKeyHere
```

> ⚠️ **Security Note**: Never commit `local.properties` or `google-services.json` to public repositories.

---

### 5. Building and Running the App

1. Launch **Android Studio** and click **File $\rightarrow$ Open**, then select the `GeoConnect` folder.
2. Allow Gradle to perform the initial sync. If prompted, click **Sync Project with Gradle Files** (🐘 icon).
3. Select your target device/emulator in the top toolbar.
4. Run the project:
   ```bash
   # Windows
   gradlew.bat assembleDebug
   
   # macOS / Linux
   ./gradlew assembleDebug
   ```
5. Press **Run** (`Shift + F10`) to deploy the app.

---

## 🧪 Testing & Verification Walkthrough

| Test Case | Steps | Expected Outcome |
| :--- | :--- | :--- |
| **New Provider Signup** | 1. Select "Sign Up as Freelancer"<br>2. Fill details and submit | Account created in Firebase Auth; dual documents created in `users` & `freelancers` collections. |
| **Profile & Status Check** | 1. Log in as a Freelancer<br>2. Open Profile screen | Profile displays stats and defaults to `"Pending"` approval badge until updated in Firestore. |
| **Map Discovery** | 1. Log in as Customer<br>2. Go to Market $\rightarrow$ Map View | Map loads and places pins (`Marker`) at the exact `GeoPoint` coordinates of registered providers. |
| **Provider Settings** | 1. Go to Profile<br>2. Click Settings $\rightarrow$ Sign Out | Firebase session clears, and app redirects safely to `SigninActivity`. |

---

## 📂 Project Directory Architecture

```
com.example.geoconnect/
│
├── 📱 Activities / Screens
│   ├── CustomerDashboardActivity.java  # Customer landing & service browser
│   ├── DiscoverActivity.java           # Market screen (Google Maps & List toggles)
│   ├── FreelancerProfileActivity.java  # Profile details & approval status display
│   ├── FreelancerSignupActivity.java   # Provider onboarding & Firestore write
│   ├── SettingsActivity.java          # Account options & sign-out workflow
│   └── DetailsActivity.java           # Individual provider view
│
└── 🛠️ Utilities
    └── FreelancerUi.java              # Standard UI inset & navigation helper
```

---

## 👥 Contributors & Support

Developed by **Team GeoConnect**. For technical inquiries, bug reports, or evaluation queries, please contact the development lead via the project repository.
