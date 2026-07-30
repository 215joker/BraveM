package com.bravem.app.data.local.source;

import com.bravem.app.data.local.PaperDao;
import com.bravem.app.model.PastPaper;

import java.util.List;

public class PaperLocalDataSource {

    private final PaperDao paperDao;

    public PaperLocalDataSource(PaperDao paperDao) {
        this.paperDao = paperDao;
    }

    public List<PastPaper> getRecommended(String degreeId, String intake, String currentUid) {
        if (intake != null && intake.contains(".")) {
            try {
                String[] parts = intake.split("\\.");
                int year = Integer.parseInt(parts[0]);
                String semesterQuery = "Semester " + parts[1];
                return paperDao.getRecommended(degreeId, year, semesterQuery, currentUid);
            } catch (Exception e) {
                return paperDao.getByDegreeId(degreeId, currentUid);
            }
        }
        return paperDao.getByDegreeId(degreeId, currentUid);
    }

    public List<PastPaper> getUploadedBy(String uid) {
        return paperDao.getByUploader(uid);
    }

    public List<PastPaper> getByDegreeId(String degreeId, String currentUid) {
        return paperDao.getByDegreeId(degreeId, currentUid);
    }

    public List<PastPaper> getByCourseId(String courseId) {
        return paperDao.getByCourseId(courseId);
    }

    public List<PastPaper> search(String query) {
        return paperDao.search(query);
    }

    public PastPaper getById(String id) {
        return paperDao.getById(id);
    }

    public void savePapers(List<PastPaper> papers) {
        for (PastPaper p : papers) paperDao.insert(p);
    }

    public void savePaper(PastPaper paper) {
        paperDao.insert(paper);
    }

    public void updatePaper(PastPaper paper) {
        paperDao.update(paper);
    }

    public void deletePaper(PastPaper paper) {
        paperDao.delete(paper);
    }

    public int getPinnedCount() {
        return paperDao.getPinnedCount();
    }

    public List<PastPaper> getAll() {
        return paperDao.getAll();
    }
}
