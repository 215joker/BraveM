# BraveM — Setup Instructions

BraveM is a native Android app written in **Java** (business logic) and **XML**
(layouts/resources), using **Firebase** (Auth + Firestore + Storage) as its backend,
with **JSON** used for data interchange (Firestore documents, the seed-data file,
and Gradle/Firebase config files).

Follow these steps before the app will build and run.

## 1. Prerequisites

- Android Studio (Hedgehog 2023.1.1 or newer recommended)
- A free Firebase account: https://console.firebase.google.com
- An Android device or emulator running API 24 (Android 7.0) or higher

## 2. Create your Firebase project

1. Go to https://console.firebase.google.com and click **Add project**.
2. Name it (e.g. "BraveM") and finish the wizard.
3. In the project, click **Add app > Android**.
4. Use package name: `com.bravem.app` (must match exactly).
5. Download the generated **google-services.json** file.
6. Replace the placeholder file at `app/google-services.json` in this project
   with the one you just downloaded.

## 3. Enable Firebase services

In the Firebase Console, for your new project:

1. **Authentication** -> Sign-in method -> enable **Email/Password**.
2. **Firestore Database** -> Create database -> start in **production mode**
   (or test mode while developing) -> choose a region close to your users.
3. **Storage** -> Get started -> accept the default bucket.

## 4. Deploy the security rules

Two rules files are included at the project root:
- `firestore.rules`
- `storage.rules`

Easiest way: open each file, copy its contents, and paste it into
**Firebase Console -> Firestore Database -> Rules** (and **Storage -> Rules**)
respectively, then click **Publish**.

(Alternatively, if you have the Firebase CLI installed: `firebase deploy --only firestore:rules,storage`.)

## 5. Create your first admin account

The app has no public "become admin" button by design. To create an admin:

1. Run the app and **Register** a normal account as you normally would.
2. In Firebase Console -> Firestore Database -> `users` collection, find your
   new user document (its ID is your Firebase Auth UID).
3. Edit the `role` field from `"student"` to `"admin"`.
4. Log out and log back in (or just relaunch the app) — you'll now land on
   the Admin Dashboard instead of the student Dashboard.

From the Admin Dashboard you can add Degrees and Courses, which is required
before students can select a degree or upload papers (a quick reference list
of example degrees/courses is in `seed-data/sample_degrees_courses.json`).

## 6. Open and run the project

1. Open Android Studio -> **Open** -> select the `BraveM` folder (this project's root, containing `settings.gradle`).
2. Let Gradle sync (it will download dependencies the first time — requires internet).
3. Choose a device/emulator and click **Run**.

## 7. Typical first-run flow

1. Admin logs in -> adds 2-3 Degrees -> adds Courses under each Degree.
2. A student registers a new account -> selects their Degree on the onboarding
   screen -> lands on their personalized Dashboard.
3. Student taps the amber **Upload** button (bottom-right) -> picks a PDF or
   Word file -> fills in title/degree/course/year/semester -> submits.
4. Any student with the same degree can now see that paper on their Dashboard,
   browse to it via Degree -> Course -> Past Papers, or find it via Search.
5. Admin can moderate uploads from Admin Dashboard -> Manage Papers
   (approve/unapprove or delete).

## Notes on file viewing

PDF in-app preview uses the `android-pdf-viewer` library, which requires a
local file rather than a streaming URL. The current build downloads via the
system Download Manager when the user taps **Download**; wiring up an
automatic "download-to-cache-then-preview" step is flagged with a comment in
`PaperViewerActivity.java` as the natural next enhancement. Word documents
(.docx/.doc) are not rendered in-app (no Android library does this reliably)
— the viewer screen shows a placeholder and prompts the user to download and
open the file with a Word app instead.

## Project structure

```
BraveM/
├── app/
│   ├── google-services.json      <- REPLACE with your real Firebase config
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/bravem/app/
│       │   ├── BraveMApp.java
│       │   ├── model/            <- User, Degree, Course, PastPaper
│       │   ├── data/             <- Firebase repositories (Auth/Degree/Course/Paper)
│       │   ├── adapter/          <- RecyclerView adapters
│       │   ├── utils/            <- SessionManager, FileUtils
│       │   └── ui/
│       │       ├── auth/         <- Splash, Login, Register, SelectDegree
│       │       ├── dashboard/    <- Dashboard, Profile
│       │       ├── papers/       <- CourseList, PaperList, PaperViewer, Search
│       │       ├── upload/       <- UploadPaper
│       │       └── admin/        <- AdminDashboard, ManageDegrees, ManageCourses, ManagePapers
│       └── res/                  <- layouts, drawables, values (all XML)
├── firestore.rules
├── storage.rules
├── seed-data/sample_degrees_courses.json
└── settings.gradle
```
