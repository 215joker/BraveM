package com.bravem.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lightweight local cache of the current user's role/degree so the UI can render
 * instantly on app start without waiting for a Firestore round-trip every time.
 * The Firestore profile document remains the source of truth; this is just a cache.
 */
public class SessionManager {

    private static final String PREFS_NAME = "bravem_session";
    private static final String KEY_UID = "uid";
    private static final String KEY_UNIVERSITY = "university";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_DEGREE_ID = "degree_id";
    private static final String KEY_DEGREE_NAME = "degree_name";
    private static final String KEY_INTAKE = "intake";
    private static final String KEY_THEME = "app_theme";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String uid, String fullName, String role, String university, String degreeId, String degreeName, String intake) {
        prefs.edit()
                .putString(KEY_UID, uid)
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_ROLE, role)
                .putString(KEY_UNIVERSITY, university)
                .putString(KEY_DEGREE_ID, degreeId)
                .putString(KEY_DEGREE_NAME, degreeName)
                .putString(KEY_INTAKE, intake)
                .apply();
    }

    public void updateDegree(String degreeId, String degreeName, String intake) {
        prefs.edit()
                .putString(KEY_DEGREE_ID, degreeId)
                .putString(KEY_DEGREE_NAME, degreeName)
                .putString(KEY_INTAKE, intake)
                .apply();
    }

    public String getIntake() {
        return prefs.getString(KEY_INTAKE, null);
    }

    public void setUniversity(String university) {
        prefs.edit().putString(KEY_UNIVERSITY, university).apply();
    }

    public String getUniversity() {
        return prefs.getString(KEY_UNIVERSITY, null);
    }

    public String getUid() {
        return prefs.getString(KEY_UID, null);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "student");
    }

    public boolean isAdmin() {
        return "admin".equals(getRole());
    }

    public String getDegreeId() {
        return prefs.getString(KEY_DEGREE_ID, null);
    }

    public String getDegreeName() {
        return prefs.getString(KEY_DEGREE_NAME, null);
    }

    public boolean hasSelectedDegree() {
        return getDegreeId() != null;
    }

    public void setTheme(int themeMode) {
        prefs.edit().putInt(KEY_THEME, themeMode).apply();
    }

    public int getTheme() {
        // Default to Light (White) theme
        return prefs.getInt(KEY_THEME, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
