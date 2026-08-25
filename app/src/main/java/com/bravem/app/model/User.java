package com.bravem.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Represents a BraveM user stored locally in Room.
 */
@Entity(tableName = "users")
public class User implements Serializable {

    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_SUPERVISOR = "supervisor";

    @PrimaryKey
    @NonNull
    private String uid;
    private String fullName;
    private String email;
    private String password; // Added for local auth
    private String university;
    private String degreeId;
    private String degreeName;
    private String intake; // Added for paper recommendations
    private String profilePicture;
    private String role;
    private long createdAt;
    private boolean deletionRequested;
    private long deletionRequestedAt;
    private boolean isSynced;
    private boolean isSuspended;
    private long suspendedUntil;
    private String appVersion;

    public User() {
    }

    public User(@NonNull String uid, String fullName, String email, String password, String university, String degreeId, String degreeName, String intake, String role, long createdAt) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.university = university;
        this.degreeId = degreeId;
        this.degreeName = degreeName;
        this.intake = intake;
        this.role = role;
        this.createdAt = createdAt;
        this.deletionRequested = false;
        this.deletionRequestedAt = 0;
        this.isSynced = false;
        this.appVersion = "1.2.4";
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getDegreeId() {
        return degreeId;
    }

    public void setDegreeId(String degreeId) {
        this.degreeId = degreeId;
    }

    public String getDegreeName() {
        return degreeName;
    }

    public void setDegreeName(String degreeName) {
        this.degreeName = degreeName;
    }

    public String getIntake() {
        return intake;
    }

    public void setIntake(String intake) {
        this.intake = intake;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeletionRequested() {
        return deletionRequested;
    }

    public void setDeletionRequested(boolean deletionRequested) {
        this.deletionRequested = deletionRequested;
    }

    public long getDeletionRequestedAt() {
        return deletionRequestedAt;
    }

    public void setDeletionRequestedAt(long deletionRequestedAt) {
        this.deletionRequestedAt = deletionRequestedAt;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    public void setSuspended(boolean suspended) {
        isSuspended = suspended;
    }

    public long getSuspendedUntil() {
        return suspendedUntil;
    }

    public void setSuspendedUntil(long suspendedUntil) {
        this.suspendedUntil = suspendedUntil;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }
}
