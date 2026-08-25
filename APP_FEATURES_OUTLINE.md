# BraveM Application Feature & Flow Outline

This document provides a detailed map of the features within the BraveM application, their layout, user flow, and internal connections.

---

## 1. Authentication & Onboarding Flow
The entry point of the application ensures that users are authenticated and their academic profile is complete.

*   **1.1 Splash Screen (`SplashActivity`)**
    *   **Function**: Determines if a user session exists.
    *   **Connection**: Redirects to **LoginActivity** (if not logged in) or **DashboardActivity** (if logged in).
*   **1.2 Login (`LoginActivity`)**
    *   **Function**: Firebase Email/Password authentication.
    *   **Connection**: Leads to **RegisterActivity** for new users or **DashboardActivity** upon success.
*   **1.3 Registration (`RegisterActivity`)**
    *   **Function**: Captures basic user details (Name, Email).
    *   **Connection**: Transitions to the multi-step profile setup.
*   **1.4 University Selection (`SelectUniversityActivity`)**
    *   **Function**: Users select their institution from a predefined list.
    *   **Connection**: Proceeds to **SelectDegreeActivity**.
*   **1.5 Degree & Intake Selection (`SelectDegreeActivity`)**
    *   **Function**: Users pick their specific course and graduation intake.
    *   **Connection**: Finalizes the profile and enters the **DashboardActivity**.

---

## 2. Core Feature: Academic Content (Past Papers)
The primary value proposition of the app is discovering and viewing academic resources.

*   **2.1 Discovery (The Search/Browse Path)**
    *   **Search Hub (`SearchActivity`)**: Global search for papers by title, course, or metadata.
    *   **Browsing Hierarchy**: 
        *   **Dashboard** -> **Browse Universities** -> **Select Degree** -> **Course List** -> **Paper List**.
*   **2.2 Viewing & Interaction**
    *   **Paper Viewer (`PaperViewerActivity`)**: Integrated PDF rendering engine.
    *   **Offline Access**: Integration with Android `DownloadManager` for local storage.
    *   **Personalization**: Users can "Pin" papers, which then appear in their Dashboard for quick access.
*   **2.3 Contribution (`UploadPaperActivity`)**
    *   **Function**: Allows students to upload PDF/Word documents.
    *   **Flow**: Select file -> Assign metadata (Degree, Course, Intake) -> Background Upload.

---

## 3. Core Feature: Community & Networking
Fosters collaboration between students of the same university.

*   **3.1 Discovery (`CommunityActivity`)**
    *   **Recommendations**: Smart suggestions of students in the same degree/university.
    *   **Search**: Find specific students by name.
    *   **Connection**: Tapping a student opens their profile or a chat.
*   **3.2 Real-time Messaging (`ChatActivity`)**
    *   **Function**: Private 1-to-1 messaging.
    *   **Features**: Image attachments, real-time message status, and unread badges on the dashboard.
*   **3.3 Social System**
    *   **Friendship**: Send and accept friend requests to unlock full messaging capabilities.

---

## 4. User Dashboard & Management
The central hub for the student experience.

*   **4.1 Main Dashboard (`DashboardActivity`)**
    *   **Greeting & Profile Summary**: Displays university and degree info.
    *   **Recent/Recommended Papers**: Horizontal or vertical list based on user's degree.
    *   **Notification Center**: Real-time alerts for new papers, friend requests, or messages.
*   **4.2 User Profile (`ProfileActivity`)**
    *   **Function**: View and edit personal details, change profile picture, and view account stats.
    *   **Session Management**: Logout functionality.

---

## 5. Admin Management System
A specialized interface for "admin" role users to maintain the platform.

*   **5.1 Admin Dashboard (`AdminDashboardActivity`)**
    *   **Overview**: Real-time stats on total users, papers, and institutions.
*   **5.2 Content Management**
    *   **`ManageUniversitiesActivity` / `ManageDegreesActivity` / `ManageCoursesActivity`**: CRUD operations on the academic structure.
    *   **`ManagePapersActivity`**: Review, edit, or delete student-uploaded papers.
*   **5.3 User & Security**
    *   **`ManageUsersActivity`**: Ban or promote users to admin status.
    *   **`SearchLogsActivity`**: Monitor application usage and search trends.
    *   **`ClearTempFilesActivity`**: Maintenance tool for cleaning up storage.
*   **5.4 Support System**
    *   **`AdminChatListActivity`**: A global view of all active chats to provide support or monitoring.

---

## 6. System Supervisor / Developer Dashboard
A high-level infrastructure dashboard for the "Developer" role, focused on system stability and maintenance without access to sensitive user data.

*   **6.1 Supervisor Overview (`SupervisorDashboardActivity`)**
    *   **System Health**: Real-time status indicators for Firebase services, API availability, and database connectivity.
    *   **Performance Metrics**: Aggregate data on app latency, crash-free session percentages, and active background worker status.
*   **6.2 Infrastructure Management**
    *   **`FeatureToggleActivity` (Remote Config)**: Globally enable or disable app features (e.g., maintenance mode, new upload engine) without requiring a store update.
    *   **`VersionControlActivity`**: Manage mandatory update flags and deprecated API versions.
*   **6.3 Resource & Traffic Monitoring**
    *   **Storage Metrics**: Track Cloud Storage quotas and CDN bandwidth usage.
    *   **Database Load**: Monitor read/write operations and identify high-latency queries.
*   **6.4 System Maintenance Tools**
    *   **`GlobalCacheManagerActivity`**: Trigger server-side cache invalidation or CDN purges.
    *   **`BackupLogsActivity`**: View system-level logs, server errors, and automated deployment status (anonymized).
    *   **`WorkerMonitorActivity`**: Inspect the health and retry-history of `SyncWorker` and other scheduled tasks.

---

## 7. Architecture & Data Flow Connectivity

*   **Repository Layer**: Every UI component connects to a specialized repository (e.g., `AuthRepository`, `PaperRepository`) which abstracts Firebase and Room logic.
*   **Synchronization**: The `SyncWorker` runs in the background to keep the local Room database updated with the Firebase Realtime Database.
*   **Global Access**: `SessionManager` provides shared preferences-backed data for immediate UI rendering without network calls.
