package com.bravem.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Lightweight secure local cache of user session data.
 */
public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREFS_NAME = "secure_bravem_session";
    private static final String KEY_UID = "uid";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UNIVERSITY = "university";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_DEGREE_ID = "degree_id";
    private static final String KEY_DEGREE_NAME = "degree_name";
    private static final String KEY_INTAKE = "intake";
    private static final String KEY_THEME = "app_theme";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            prefs = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences", e);
            // Fallback to regular prefs if encryption fails (though better to handle strictly)
            prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public void saveSession(String uid, String email, String fullName, String role, String university, String degreeId, String degreeName, String intake) {
        prefs.edit()
                .putString(KEY_UID, uid)
                .putString(KEY_EMAIL, email)
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

    public String getUid() { return prefs.getString(KEY_UID, null); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, null); }
    public String getFullName() { return prefs.getString(KEY_FULL_NAME, null); }
    public String getRole() { return prefs.getString(KEY_ROLE, "student"); }
    public String getUniversity() { return prefs.getString(KEY_UNIVERSITY, null); }
    public String getDegreeId() { return prefs.getString(KEY_DEGREE_ID, null); }
    public String getDegreeName() { return prefs.getString(KEY_DEGREE_NAME, null); }
    public String getIntake() { return prefs.getString(KEY_INTAKE, null); }

    public void setUniversity(String university) {
        prefs.edit().putString(KEY_UNIVERSITY, university).apply();
    }

    public void setTheme(int themeMode) {
        prefs.edit().putInt(KEY_THEME, themeMode).apply();
    }

    public int getTheme() {
        return prefs.getInt(KEY_THEME, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
