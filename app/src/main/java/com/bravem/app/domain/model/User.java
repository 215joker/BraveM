package com.bravem.app.domain.model;

public class User implements java.io.Serializable {
    private final String uid;
    private final String fullName;
    private final String email;
    private final String university;
    private final String degreeId;
    private final String degreeName;
    private final String intake;
    private final String role;
    private final String profilePicture;
    private final boolean isSuspended;
    private final long deletionRequestedAt;

    public User(String uid, String fullName, String email, String university, 
                String degreeId, String degreeName, String intake, String role, 
                String profilePicture, boolean isSuspended, long deletionRequestedAt) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.university = university;
        this.degreeId = degreeId;
        this.degreeName = degreeName;
        this.intake = intake;
        this.role = role;
        this.profilePicture = profilePicture;
        this.isSuspended = isSuspended;
        this.deletionRequestedAt = deletionRequestedAt;
    }

    // Getters
    public String getUid() { return uid; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getUniversity() { return university; }
    public String getDegreeId() { return degreeId; }
    public String getDegreeName() { return degreeName; }
    public String getIntake() { return intake; }
    public String getRole() { return role; }
    public String getProfilePicture() { return profilePicture; }
    public boolean isSuspended() { return isSuspended; }
    public long getDeletionRequestedAt() { return deletionRequestedAt; }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isSupervisor() {
        return "supervisor".equalsIgnoreCase(role) || "developer".equalsIgnoreCase(role);
    }
}
