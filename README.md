# BraveM Application Documentation

## 1. Project Overview
**BraveM** is a comprehensive educational platform designed to empower university students by providing easy access to past examination papers and fostering a collaborative academic community. The app follows an **offline-first approach**, ensuring that students can access critical resources even with limited connectivity.

*   **Current Version**: 1.2.3
*   **Target Platform**: Android (Min SDK 24, Target SDK 35)
*   **Key Paradigm**: Repository Pattern with Local-First Synchronization.

---

## 2. Technology Stack
*   **Language**: Java (Modern Android standards)
*   **UI Framework**: XML with Material Design 3 (Edge-to-Edge support)
*   **Local Database**: Room Persistence Library
*   **Cloud Backend**: Firebase (Realtime Database, Authentication, Storage)
*   **Architecture**: Repository Pattern for clean data abstraction.
*   **Background Processing**: WorkManager (for cloud-local data sync)
*   **Utilities**: ViewBinding, GSON, PDF Viewer (oothp), Firebase Crashlytics.

---

## 3. Core Features

### 3.1 Authentication & Profile Management
*   **Multi-Step Registration**: Includes full name, university selection, and degree/intake assignment.
*   **Role-Based Access**: Distinguishes between `student` and `admin` users.
*   **Session Management**: Cached locally via `SessionManager` for instant UI rendering.
*   **Version Tracking**: Every user profile tracks their `appVersion` (v1.2.3) to ensure compatibility.

### 3.2 Academic Content (Past Papers)
*   **Discovery**: Browse papers by university, degree, course, or intake.
*   **Upload**: Students can upload PDF or Word documents. Includes a **Bulk Upload Queue** for multiple files.
*   **Viewing**: Integrated PDF viewer for seamless reading within the app.
*   **Downloading**: Integration with Android `DownloadManager` for saving files to device storage.
*   **Personalization**: "Pin" important papers for quick access on the dashboard.

### 3.3 Community & Networking
*   **Recommendations**: Suggests students within the same university to foster networking.
*   **Friendship System**: Send, receive, and accept friend requests.
*   **Real-time Messaging**: Private chat system with support for file attachments and unread message badges.
*   **Notifications**: Real-time alerts for new uploads, friend requests, and messages.

---

## 4. Architecture & Data Flow

### 4.1 Repository Pattern
The app abstracts data sources through specialized repositories:
*   **`AuthRepository`**: Manages Firebase Auth and user profile sync.
*   **`PaperRepository`**: Handles metadata in Realtime DB and files in Firebase Storage.
*   **`CommunityRepository`**: Manages user discovery and friendship status.
*   **`ChatRepository`**: Handles real-time message streams and unread counts.

### 4.2 Synchronization (`SyncManager`)
The app uses a `SyncWorker` (WorkManager) that runs every 15 minutes to:
1.  Push local unsynced changes to the cloud.
2.  Update the user's `appVersion` in the database.
3.  Ensure consistency between the local Room DB and Firebase.

---

## 5. Security & Data Integrity

### 5.1 Realtime Database Rules
The app uses strict security rules to ensure data privacy:
*   **User Profiles**: Readable by all authenticated users; writable only by the owner or admins.
*   **Messaging/Notifications**: Strictly locked to the participants involved.
*   **Papers**: Publicly readable; writable only by the original uploader or admins.

### 5.2 Storage Rules
*   **Size Limit**: Enforces a 25MB maximum per file.
*   **MIME Types**: Only accepts PDF and Word documents.
*   **Privacy**: Requires authentication for any file access.

---

## 6. Development & Maintenance

### 6.1 Versioning Policy
The app maintains a versioning node in the database (`app_metadata`). 
*   **Latest Version**: 1.2.3
*   **Minimum Supported Version**: 1.2.0
This allows the backend to notify users when an update is required to maintain functionality.

### 6.2 Error Handling
*   **Firebase Persistence**: Enabled in `BraveMApp` to prevent data loss during offline usage.
*   **Crashlytics**: Integrated to report production crashes while being disabled in debug mode for developer convenience.
*   **Logcat Monitoring**: All repositories include logging for tracking Firebase communication issues (e.g., 404 Storage errors).

---

## 7. Setup Instructions
1.  **Google Services**: Ensure `google-services.json` is placed in the `app/` directory.
2.  **Database URL**: Point to the Firebase Realtime Database instance.
3.  **Storage Bucket**: Ensure the bucket URL matches the one defined in `PaperRepository`.
4.  **Admin Setup**: To create an admin, register a user with an email containing the keyword "admin" or manually change the `role` field in the database.
