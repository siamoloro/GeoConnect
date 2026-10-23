# GeoConnect

GeoConnect is an Android app that connects people with nearby service providers. Customers can discover freelancers and businesses on a map or in a list, view their details and make booking requests. Freelancers and businesses manage their profiles and incoming requests, and administrators oversee users and support cases.

> **Course project:** replace this line with your module name, institution and submission date.

## Team

| Name            | Role / Focus               |
|------           |--------------              |
| Khensani Baloyi | _e.g. Customer features_   |
| Zoë Qobo        | _e.g. Freelancer features_ |
| Osiame Moloro   | _e.g. Business features_   |
| Bruno Thinya    | _e.g. Admin features       |
| All             | Database implementation     |  

## Features

**Customer**
- Sign up and sign in
- Discover providers on a Google Map or in a list, with search and filters
- View provider details and make booking requests
- Manage profile, security settings and active sessions
- Open support cases

**Freelancer**
- Sign up with name, phone and service category
- Dashboard with availability toggle, new requests and upcoming jobs
- Booking requests with Pending, Accepted and Completed tabs (accept or decline)
- Profile and edit profile screens

**Business**
- Sign up and business dashboard
- Manage profile, offerings and operating hours

**Administrator**
- Separate admin sign-in
- Review and approve freelancer and business registrations
- View users, reports and support cases

## Tech stack

| Area           | Technology |
|------          |-----------|
| Language       | Java |
| Platform       | Android SDK (AndroidX, Material Components) |
| Build system   | Gradle (Kotlin DSL) |
| Authentication | Firebase Authentication (email and password) |
| Database       | Cloud Firestore |
| Maps           | Google Maps SDK for Android |

## Project structure

```
GeoConnect/
├── app/
│   ├── build.gradle.kts            # App module build configuration
│   ├── google-services.json        # Firebase configuration
│   └── src/main/
│       ├── AndroidManifest.xml     # Activity registration and Maps API key
│       ├── java/com/example/geoconnect/   # Activities, adapters and models
│       └── res/                    # Layouts, drawables, colours, string arrays
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties                # Local only, not committed (see setup)
```

## Prerequisites

- **Android Studio** (latest stable version)
- **JDK 17** or newer (bundled with recent Android Studio versions)
- **Git**
- An Android phone with USB debugging, or an Android emulator
- A Google account with access to the project's Firebase console
- A **Google Maps API key** with the Maps SDK for Android enabled

## Setup instructions

### 1. Clone the repository

Clone into a folder that is **not** synced by OneDrive, Dropbox or similar. Cloud-sync tools lock build files and cause Gradle errors on Windows. A short path such as `C:\Dev` works well.

```bash
git clone https://github.com/siamoloro/GeoConnect.git
cd GeoConnect
```

### 2. Open the project

1. Open Android Studio and choose **File > Open**.
2. Select the `GeoConnect` folder and choose **Trust Project**.
3. Wait for the Gradle sync to finish. Android Studio creates `local.properties` with your SDK path automatically.

### 3. Add your Google Maps API key

Open `local.properties` in the project root and add:

```properties
MAPS_API_KEY=your_google_maps_api_key_here
```

`local.properties` is listed in `.gitignore`, so each team member uses their own key. Never commit API keys.

If you leave this out, the build fails with:
`requires a placeholder substitution but no value for <MAPS_API_KEY> is provided`.

To create a key, open the Google Cloud Console, enable **Maps SDK for Android**, and create an API key under **APIs & Services > Credentials**.

### 4. Firebase configuration

The app uses a shared Firebase project.

1. Check that `app/google-services.json` exists. It comes with the repository.
2. In the Firebase console, make sure **Authentication > Sign-in method > Email/Password** is enabled.
3. Make sure **Firestore Database** has been created.

To use your own Firebase project instead, register an Android app with the package name `com.example.geoconnect`, download its `google-services.json` and replace the one in `app/`.

### 5. Firestore security rules

During development the project uses test-mode rules, which **expire on a fixed date** and then block all reads and writes. For anything beyond a demo, use rules that require sign-in:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Publish them under **Firestore Database > Rules**.

### 6. Build and run

**From Android Studio:** select a device and click **Run**.

**From the terminal:**

```bash
./gradlew assembleDebug        # macOS / Linux / PowerShell
gradlew assembleDebug          # Windows Command Prompt
```

The debug APK is created in `app/build/outputs/apk/debug/`.

#### Running on a physical phone (recommended)

1. On the phone, open **Settings > About phone** and tap **Build number** seven times.
2. In **Developer options**, enable **USB debugging**.
3. Connect the phone with a USB cable and tap **Allow** on the phone.
4. Select the phone in Android Studio's device dropdown and click **Run**.

#### Running on an emulator

Use a **Google APIs** system image so that Firebase and Maps work. Emulators need plenty of RAM and disk space (several GB free), so a physical phone is easier on low-spec machines.

## Database overview

Cloud Firestore collections used by the app:

| Collection | Purpose |
|-----------|---------|
| `users` | One document per account (document ID is the Firebase Auth UID). Stores the role in `type` (or `role` for admins). |
| `freelancers` | Freelancer profiles, linked to `users` by the `userID` field. |
| `businesses` | Business profiles. |
| `bookings` | Booking requests with status Pending, Accepted, Declined or Completed. |
| `cases` | Support cases opened by users. |
| `sessions` | Signed-in devices shown on the Active Sessions screen. |

> Keep collection and field names consistent across screens. Firestore names are **case-sensitive**: `freelancers` and `Freelancers` are different collections.

## Demo accounts and roles

Sign-in routes users by the role stored in their `users` document.

| Role | How to get an account |
|------|----------------------|
| Customer | Sign up in the app |
| Freelancer | Sign up in the app |
| Business | Sign up in the app |
| Admin | Create manually (see below) |

**Creating an admin account**

1. In the Firebase console, go to **Authentication > Users** and add a user with an email and password.
2. In **Firestore > users**, create a document whose ID is that user's UID, with fields `email` and `role` set to `admin`.
3. Sign in through the **Admin portal** link on the sign-in screen.

> Do not put real passwords in this README. Share demo credentials with markers privately.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `MAPS_API_KEY` placeholder error | Add the key to `local.properties` (step 3). |
| `Unable to delete directory` or `not a regular file` | The project is in a cloud-synced folder. Move or re-clone it outside OneDrive. |
| Orange file icons or failed Gradle sync | Run **File > Sync Project with Gradle Files**, then **Invalidate Caches / Restart**. |
| `permission denied` on every Firestore call | The test-mode rules have expired. Publish new rules (step 5). |
| Map is blank | Check the Maps API key and that the Maps SDK for Android is enabled. |
| Push rejected (`fetch first`) | Run `git pull`, rebuild, then `git push`. |

## Git workflow

```bash
git pull                       # always pull before starting work
git add app/src
git commit -m "Describe your change"
git push
```

- Do not commit `local.properties`, `.idea/` files or `build/` folders.
- Tell the team before deleting or renaming shared files such as activities and layouts.

## Known limitations

- Revoking an active session removes its Firestore record but does not sign that device out.
- Some profile statistics (rating, jobs done, response rate) only appear when the data exists in Firestore.
- Photo upload is not implemented yet.

## Licence

Created for educational purposes only.
