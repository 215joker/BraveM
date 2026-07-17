package com.bravem.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Represents a past question paper stored locally in Room.
 */
@Entity(tableName = "papers")
public class PastPaper implements Serializable {

    public static final String TYPE_PDF = "pdf";
    public static final String TYPE_DOCX = "docx";

    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String degreeId;
    private String degreeName;
    private String university;
    private String intake;
    private String courseId;
    private String courseName;
    private String courseCode;
    private int year;
    private String semester;
    private String fileType;       // "pdf" or "docx"
    private String fileUrl;        // Locally: this could be a local file path
    private String fileName;
    private long fileSizeBytes;
    private String uploadedByUid;
    private String uploadedByName;
    private long createdAt;
    private boolean approved;
    private boolean pinned;
    private boolean isSynced;

    public PastPaper() {
    }

    public PastPaper(@NonNull String id, String title, String degreeId, String degreeName,
                      String courseId, String courseName, String courseCode,
                      int year, String semester, String fileType, String fileUrl,
                      String fileName, long fileSizeBytes, String uploadedByUid,
                      String uploadedByName, long createdAt, boolean approved) {
        this(id, title, degreeId, degreeName, courseId, courseName, courseCode, year, semester, fileType, fileUrl, fileName, fileSizeBytes, uploadedByUid, uploadedByName, createdAt, approved, false);
    }

    public PastPaper(@NonNull String id, String title, String degreeId, String degreeName,
                      String courseId, String courseName, String courseCode,
                      int year, String semester, String fileType, String fileUrl,
                      String fileName, long fileSizeBytes, String uploadedByUid,
                      String uploadedByName, long createdAt, boolean approved, boolean pinned) {
        this.id = id;
        this.title = title;
        this.degreeId = degreeId;
        this.degreeName = degreeName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.year = year;
        this.semester = semester;
        this.fileType = fileType;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileSizeBytes = fileSizeBytes;
        this.uploadedByUid = uploadedByUid;
        this.uploadedByName = uploadedByName;
        this.createdAt = createdAt;
        this.approved = approved;
        this.pinned = pinned;
        this.isSynced = false;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getIntake() {
        return intake;
    }

    public void setIntake(String intake) {
        this.intake = intake;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getUploadedByUid() {
        return uploadedByUid;
    }

    public void setUploadedByUid(String uploadedByUid) {
        this.uploadedByUid = uploadedByUid;
    }

    public String getUploadedByName() {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
