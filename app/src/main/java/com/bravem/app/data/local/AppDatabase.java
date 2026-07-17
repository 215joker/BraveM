package com.bravem.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.bravem.app.model.ChatMessage;
import com.bravem.app.model.Course;
import com.bravem.app.model.Degree;
import com.bravem.app.model.Friendship;
import com.bravem.app.model.Notification;
import com.bravem.app.model.PastPaper;
import com.bravem.app.model.SearchLog;
import com.bravem.app.model.User;

@Database(entities = {User.class, Degree.class, Course.class, PastPaper.class, Notification.class, SearchLog.class, Friendship.class, ChatMessage.class}, version = 23, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract DegreeDao degreeDao();
    public abstract CourseDao courseDao();
    public abstract PaperDao paperDao();
    public abstract NotificationDao notificationDao();
    public abstract SearchLogDao searchLogDao();
    public abstract FriendshipDao friendshipDao();
    public abstract ChatMessageDao chatMessageDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "bravem_db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                                        AppDatabase database = getInstance(context);
                                        seedInitialData(database);
                                    });
                                }

                                @Override
                                public void onOpen(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    // Robust check: seed if degrees table is empty
                                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                                        AppDatabase database = getInstance(context);
                                        if (database.degreeDao().getAll().isEmpty()) {
                                            seedInitialData(database);
                                        }
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedInitialData(AppDatabase db) {
        String eden = "Eden University";
        long now = System.currentTimeMillis();

        // Seed Admin User
        db.userDao().insert(new com.bravem.app.model.User(
                "admin_uid", "BraveM Admin", "admin@bravem.com", "admin123",
                eden, null, null, null, com.bravem.app.model.User.ROLE_ADMIN, now
        ));

        // School of Medicine and Health Sciences
        String somhs = "School of Medicine and Health Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_m1", "Master in Public Health", somhs, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_m2", "Bachelor of Medicine and Surgery (MBChB)", somhs, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_m3", "Bachelor of Science in Environmental Health", somhs, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_m4", "Bachelor of Science in Clinical Medicine", somhs, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_m5", "Distance Bachelor of Science in Environmental Health", somhs, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_m6", "Distance Bachelor of Science in Clinical Medicine", somhs, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_m7", "Diploma in Clinical Medicine", somhs, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_m8", "Diploma in Environmental Health", somhs, eden, now));

        // School of Pharmacy
        String sop = "School of Pharmacy";
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_p1", "Bachelor of Pharmacy", sop, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_p2", "Diploma in Pharmacy", sop, eden, now));

        // School of Nursing and Midwifery Sciences
        String sonms = "School of Nursing and Midwifery Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_n1", "Bachelor of Science in Nursing", sonms, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_n2", "Diploma in Registered Nursing", sonms, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_n3", "Diploma in Registered Midwifery", sonms, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_n4", "Public Health Registered Nursing (Diploma)", sonms, eden, now));

        // School of Natural Sciences
        String sons = "School of Natural Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_ns1", "Natural Science (foundation programmes feeding into Medicine, Pharmacy, Nursing, Clinical Medicine, Environmental Health, and Fire Engineering)", sons, eden, now));

        // School of Law and Business Studies
        String solbs = "School of Law and Business Studies";
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_l1", "Bachelor of Laws (LLB)", solbs, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_l2", "Bachelor of Business Management", solbs, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_l3", "Bachelor of Business Management with Accounting and Finance", solbs, eden, now));

        // School of Education, Humanities, and Social Sciences
        String sehss = "School of Education, Humanities, and Social Sciences";
        
        // Dept of ICT
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_i1", "Bachelor of Science in Information and Communications Technology (ICT)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_i2", "BSc in Digital Marketing and E-commerce (ICT)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_i3", "BSc Cyber Security and Network Defense (ICT)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_i4", "BSc Data Science and Big Data Analytics (ICT)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_i5", "Bachelor of ICT & Entrepreneurship (ICT)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_i6", "MSc Cloud Computing and DevOps (ICT)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_i7", "MSc Artificial Intelligence (ICT)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_i8", "MSc Cyber Security and Digital Forensics (ICT)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_i9", "MSc Data Science and Analytics (ICT)", sehss, eden, now));

        // Dept of Humanities and Social Sciences
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h1", "Bachelor of Social Work (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h2", "Bachelor of Arts in Developmental Studies (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h3", "Bachelor of Arts in Sociology (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h4", "Bachelor of Science in Public Administration and Political Science (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h5", "Bachelor of Science in Hospitality and Hotel Management (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h6", "Bachelor of Science in Hospitality, Tourism and Travel (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h7", "Bachelor of Science in Interior Design (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h8", "Bachelor of Science in Disability Studies (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h9", "Bachelor of Science in Event Planning (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h10", "Bachelor of Arts in Peace, Security and Intelligence Management (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h11", "Bachelor of Arts in Sports Management (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h12", "Bachelor of Science in Psychology (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h13", "Master of Science in Sports Management (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h14", "Master of Science in Psychology (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h15", "Master of Arts in Sociology (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h16", "Master of Science in Social Work (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h17", "Master of Arts in Peace, Security and Intelligence Management (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h18", "Master of Arts in Peace and Conflict Resolution (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h19", "Master of Science in Disability Studies (Humanities)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_h20", "Master of Arts in Special Education (Humanities)", sehss, eden, now));

        // Dept of Education
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e1", "Bachelor of Business Administration and Entrepreneurship with Education (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e2", "Bachelor of Arts in Guidance and Counselling with Education (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e3", "Bachelor of Science with Education – Secondary (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e4", "Bachelor of Arts with Education – Primary (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e5", "Bachelor of Arts with Education – Secondary (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e6", "Diploma in Secondary Teaching (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e7", "Diploma in Primary Teaching (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e8", "Diploma in Early Childhood Education (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e9", "Diploma in Teaching Methodology (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e10", "Master of Education Management and Administration (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e11", "Master of Guidance & Counselling (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e12", "Master of Education – Specializations (Education)", sehss, eden, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("eden_e13", "Postgraduate Diploma in Teaching Methodology (Education)", sehss, eden, now));

        // University of Zambia (UNZA)
        String unza = "University of Zambia (UNZA)";

        // School of Agricultural Sciences
        String unzaAgri = "School of Agricultural Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ag1", "Bachelor of Agricultural Sciences (Animal Science)", unzaAgri, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ag2", "Bachelor of Agricultural Sciences (Land Management)", unzaAgri, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ag3", "Bachelor of Agricultural Sciences (Plant Science)", unzaAgri, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ag4", "Bachelor of Food Science and Technology", unzaAgri, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ag5", "Bachelor of Science in Agricultural Economics", unzaAgri, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ag6", "Bachelor of Science in Agricultural Extension", unzaAgri, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ag7", "Bachelor of Science in Agronomy", unzaAgri, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ag8", "Bachelor of Science in Human Nutrition", unzaAgri, unza, now));

        // School of Education
        String unzaEdu = "School of Education";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed1", "Bachelor of Adult Education", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed2", "Bachelor of Agricultural Science with Education", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed3", "Bachelor of Arts in Records and Archives Management", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed4", "Bachelor of Arts with Education", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed5", "Bachelor of Arts with Library and Information Studies", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed6", "Bachelor of Community Education", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed7", "Bachelor of Cultural Studies", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed8", "Bachelor of Education (Early Childhood Education)", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed9", "Bachelor of Education (Educational Administration and Management)", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed10", "Bachelor of Education (Educational Psychology)", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed11", "Bachelor of Education (Environmental Education and Management)", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed12", "Bachelor of Education (Guidance and Counselling)", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed13", "Bachelor of Education (Literacy and Language)", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed14", "Bachelor of Education – Secondary (Mathematics and Science)", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed15", "Bachelor of Education (Primary Education)", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed16", "Bachelor of Education (Sociology of Education)", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed17", "Bachelor of Education (Special Education)", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed18", "Bachelor of Education in Chinese Language Teaching", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed19", "Bachelor of Education in Commerce and Entrepreneurship", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed20", "Bachelor of Information and Communication Technologies in Education", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed21", "Bachelor of Science with Education", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed22", "Bachelor of Youth Development and Leadership", unzaEdu, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ed23", "Diploma in Information and Communication Technologies (ICTs) (Fast Track)", unzaEdu, unza, now));

        // School of Engineering
        String unzaEng = "School of Engineering";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_en1", "Bachelor of Engineering (Agricultural Engineering)", unzaEng, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_en2", "Bachelor of Engineering (Civil and Environmental Engineering)", unzaEng, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_en3", "Bachelor of Engineering (Electrical and Electronic Engineering)", unzaEng, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_en4", "Bachelor of Engineering (Geomatic Engineering)", unzaEng, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_en5", "Bachelor of Engineering (Mechanical Engineering)", unzaEng, unza, now));

        // Graduate School of Business
        String unzaGsb = "Graduate School of Business";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_gs1", "Bachelor of Science in Accounting and Finance (Fulltime and Evening)", unzaGsb, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_gs2", "Bachelor of Science in Business Management (Fulltime and Evening)", unzaGsb, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_gs3", "Bachelor of Science in Entrepreneurship and Innovation Management (Fulltime and Evening)", unzaGsb, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_gs4", "Bachelor of Science in Human Resource Management (Fulltime and Evening)", unzaGsb, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_gs5", "Bachelor of Science in Marketing (Fulltime and Evening)", unzaGsb, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_gs6", "Bachelor of Science in Logistics and Transport Management (Fulltime and Evening)", unzaGsb, unza, now));

        // School of Health Sciences
        String unzaHealth = "School of Health Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hs1", "Bachelor of Pharmacy", unzaHealth, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hs2", "Bachelor of Science in Biomedical Science", unzaHealth, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hs3", "Bachelor of Science in Physiotherapy", unzaHealth, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hs4", "Bachelor of Science in Radiography", unzaHealth, unza, now));

        // School of Humanities and Social Sciences
        String unzaHum = "School of Humanities and Social Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss1", "Bachelor of Arts in Applied Ethics and Management", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss2", "Bachelor of Arts in Archaeology", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss3", "Bachelor of Arts in Chinese Language and Linguistics", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss4", "Bachelor of Arts in Criminology and Security Studies", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss5", "Bachelor of Arts in Demography", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss6", "Bachelor of Arts in Development Studies", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss7", "Bachelor of Arts in Economics", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss8", "Bachelor of Arts in English Language and Linguistics", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss9", "Bachelor of Arts in French", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss10", "Bachelor of Arts in Gender Studies", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss11", "Bachelor of Arts in History", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss12", "Bachelor of Arts in Human Resource Management", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss13", "Bachelor of Arts in Intangible Cultural Heritage", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss14", "Bachelor of Arts in International Relations", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss15", "Bachelor of Arts in Linguistics and African Languages", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss16", "Bachelor of Arts in Local Government Administration", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss17", "Bachelor of Arts in Peace, Security, and Conflict Resolution", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss18", "Bachelor of Arts in Philosophy and Applied Ethics", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss19", "Bachelor of Arts in Political Science", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss20", "Bachelor of Arts in Project Management", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss21", "Bachelor of Arts in Psychology", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss22", "Bachelor of Arts in Public Administration", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss23", "Bachelor of Arts in Rural Sociology and Community Development", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss24", "Bachelor of Arts in Sociology", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss25", "Bachelor of Business Administration", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss26", "Bachelor of Media and Journalism Studies", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss27", "Bachelor of Public Relations and Advertising", unzaHum, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_hss28", "Bachelor of Social Work", unzaHum, unza, now));

        // School of Law
        String unzaLaw = "School of Law";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_lw1", "Bachelor of Law", unzaLaw, unza, now));

        // School of Medicine
        String unzaMed = "School of Medicine";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_md1", "Bachelor of Medicine and Surgery (Regular and Parallel)", unzaMed, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_md2", "Bachelor of Science in Clinical Medicine for Clinical Officers", unzaMed, unza, now));

        // School of Mines
        String unzaMines = "School of Mines";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_mi1", "Bachelor of Engineering in Geotechnical Engineering", unzaMines, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_mi2", "Bachelor of Engineering in Metallurgical Engineering", unzaMines, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_mi3", "Bachelor of Engineering in Mining Engineering", unzaMines, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_mi4", "Bachelor of Science in Geology", unzaMines, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_mi5", "Bachelor of Science in Occupational Safety, Health and Environment", unzaMines, unza, now));

        // School of Natural and Applied Sciences
        String unzaNat = "School of Natural and Applied Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns1", "Bachelor of Computer Science in Computer Science", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns2", "Bachelor of Computer Science in Computer Systems Engineering", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns3", "Bachelor of Computer Science in Networking and Information Security", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns4", "Bachelor of Computer Science in Software Engineering", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns5", "Bachelor of Science in Actuarial Science", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns6", "Bachelor of Science in Biological Sciences", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns7", "Bachelor of Science in Chemical and Biological Sciences", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns8", "Bachelor of Science in Chemistry", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns9", "Bachelor of Science in Chemistry/Biology", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns10", "Bachelor of Science in Chemistry/Geology", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns11", "Bachelor of Science in Ecology and Wildlife Management", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns12", "Bachelor of Science in Environmental and Natural Resource Management", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns13", "Bachelor of Science in Mathematics", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns14", "Bachelor of Science in Microbiology", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns15", "Bachelor of Science in Molecular Biology and Genetics", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns16", "Bachelor of Science in Parasitology", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns17", "Bachelor of Science in Physics", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns18", "Bachelor of Science in Physics/Mathematics", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns19", "Bachelor of Science in Physics/Geology", unzaNat, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ns20", "Bachelor of Science in Statistics", unzaNat, unza, now));

        // School of Nursing Sciences
        String unzaNurse = "School of Nursing Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_nu1", "Bachelor of Science in Mental Health Nursing", unzaNurse, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_nu2", "Bachelor of Science in Midwifery", unzaNurse, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_nu3", "Bachelor of Science in Nursing", unzaNurse, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_nu4", "Bachelor of Science in Oncology Nursing", unzaNurse, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_nu5", "Bachelor of Science in Public Health Nursing", unzaNurse, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_nu6", "Diploma in Registered Midwifery", unzaNurse, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_nu7", "Diploma in Registered Nursing", unzaNurse, unza, now));

        // School of Public Health
        String unzaPh = "School of Public Health";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ph1", "Bachelor in Health Promotion", unzaPh, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ph2", "Bachelor of Science in Environmental Health", unzaPh, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ph3", "Bachelor of Science in Health Services Management and Planning", unzaPh, unza, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_ph4", "Bachelor of Science in Public Health", unzaPh, unza, now));

        // School of Veterinary Medicine
        String unzaVet = "School of Veterinary Medicine";
        db.degreeDao().insert(new com.bravem.app.model.Degree("unza_vt1", "Bachelor of Veterinary Medicine", unzaVet, unza, now));

        // Copperbelt University (CBU)
        String cbu = "Copperbelt University (CBU)";

        // School of the Built Environment
        String cbuBuilt = "School of the Built Environment";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_be1", "Bachelor of Architecture", cbuBuilt, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_be2", "Bachelor of Science in Construction Management", cbuBuilt, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_be3", "Bachelor of Science in Quantity Surveying", cbuBuilt, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_be4", "Bachelor of Science in Real Estate Studies", cbuBuilt, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_be5", "Bachelor of Science in Urban & Regional Planning", cbuBuilt, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_be6", "Master of Urban and Regional Planning (MURP)", cbuBuilt, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_be7", "MSc in Real Estate Finance and Development", cbuBuilt, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_be8", "MPhil in Real Estate Studies", cbuBuilt, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_be9", "PhD in Real Estate Studies", cbuBuilt, cbu, now));

        // School of Business
        String cbuBus = "School of Business";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu1", "Bachelor of Science in Economics", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu2", "Bachelor of Science in Business and Project Management", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu3", "Bachelor of Science in Transport and Logistics Management", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu4", "Bachelor of Science in Public Procurement", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu5", "Bachelor of Science in Purchasing and Supply Chain", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu6", "Bachelor of Science in Production and Operations Management", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu7", "Bachelor of Accountancy (Full-Time, Evening and Distance)", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu8", "Bachelor of Science in Banking and Finance (Full-Time, Evening and Distance)", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu9", "Bachelor of Business Administration", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu10", "Bachelor of Science in Marketing", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu11", "Bachelor of Human Resources Management", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu12", "Master of Science in Accounting and Finance (Full-Time, Evening and Distance)", cbuBus, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_bu13", "MBA Finance (Full-Time, Evening and Distance)", cbuBus, cbu, now));

        // Dag Hammarskjöld Institute for Peace and Conflict Studies
        String cbuPeace = "Dag Hammarskjöld Institute for Peace and Conflict Studies";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_pe1", "Master of Arts in Peace and Conflict Studies", cbuPeace, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_pe2", "Doctor of Philosophy in Peace and Conflict Studies", cbuPeace, cbu, now));

        // School of Engineering
        String cbuEng = "School of Engineering";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_en1", "Bachelor of Engineering (Hons) in Civil Engineering", cbuEng, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_en2", "Diploma in Civil Engineering", cbuEng, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_en3", "Diploma in Construction", cbuEng, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_en4", "Bachelor of Engineering (Hons) in Electrical and Electronics", cbuEng, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_en5", "Bachelor of Engineering (Hons) in Telecommunications", cbuEng, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_en6", "Bachelor of Engineering (Hons) in Mechanical Engineering", cbuEng, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_en7", "Bachelor of Engineering (Hons) in Mechatronics Engineering", cbuEng, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_en8", "Bachelor of Aeronautical Engineering", cbuEng, cbu, now));

        // School of Mathematics and Natural Sciences
        String cbuNat = "School of Mathematics and Natural Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns1", "Bachelor of Science in Physics", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns2", "Bachelor of Science in Chemistry", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns3", "Bachelor of Science in Mathematics", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns4", "Bachelor of Science in Biotechnology", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns5", "Bachelor of Science in Entomology", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns6", "Bachelor of Science in Microbiology", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns7", "Bachelor of Science in Biodiversity and Ecology", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns8", "Bachelor of Science in Mathematics Education", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns9", "Bachelor of Science in Biology Education", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns10", "Bachelor of Science in Physics Education", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns11", "Bachelor of Science in Chemistry Education", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns12", "Master of Science in Physics", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns13", "Master of Science in Physics Education", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns14", "Master of Science in Chemistry", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns15", "Master of Science in Biotechnology", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns16", "Master of Science in Entomology", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns17", "Master of Science in Microbiology", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns18", "Master of Science in Biodiversity and Ecology", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns19", "Master of Science in Biology Education", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns20", "Master of Science in Chemistry Education", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns21", "Master of Science in Mathematics Education", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns22", "Postgraduate Diploma in Education (PGDE)", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns23", "MPhil in Biological Sciences", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns24", "PhD in Mathematics and Science Education", cbuNat, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ns25", "PhD in Physics", cbuNat, cbu, now));

        // School of Medicine
        String cbuMed = "School of Medicine";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_md1", "Bachelor of Dental Medicine", cbuMed, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_md2", "Bachelor of Medicine and Surgery (MBChB)", cbuMed, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_md3", "Bachelor of Science in Clinical Medicine", cbuMed, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_md4", "Doctor of Science Degree", cbuMed, cbu, now));

        // School of Mines and Mineral Sciences
        String cbuMines = "School of Mines and Mineral Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi1", "Bachelor of Engineering in Chemical Engineering", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi2", "Bachelor of Science in Mining and Exploration Geology", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi3", "Bachelor of Engineering in Geomatics Engineering", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi4", "Bachelor of Engineering in Environmental Engineering", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi5", "Bachelor of Engineering in Mining Engineering (Mine Planning / Rock Mechanics Options)", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi6", "Bachelor of Engineering in Minerals/Metallurgical Engineering", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi7", "Diploma in Chemical Technology", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi8", "Diploma in Geomatics Engineering", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi9", "Diploma in Environmental Technology", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi10", "Diploma in Mining", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi11", "Diploma in Mine Ventilation", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi12", "Diploma in Small Scale Mining", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi13", "Diploma in Metallurgy Technology", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi14", "MSc/MPhil in Chemical Engineering", cbuMines, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_mi15", "Doctor of Science Degree", cbuMines, cbu, now));

        // School of Natural Resources
        String cbuRes = "School of Natural Resources";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_nr1", "Bachelor of Science in Wood Science and Technology", cbuRes, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_nr2", "Bachelor of Science in Bioenergy Science", cbuRes, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_nr3", "Bachelor of Science in Plant and Environmental Sciences", cbuRes, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_nr4", "Bachelor of Science in Sustainable Natural Resources Management and Climate Change", cbuRes, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_nr5", "Bachelor of Science in Forestry", cbuRes, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_nr6", "Bachelor of Science in AgroForestry", cbuRes, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_nr7", "Bachelor of Science in Wildlife Management", cbuRes, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_nr8", "Bachelor of Science in Fisheries and Aquaculture", cbuRes, cbu, now));

        // School of Information and Communication Technology
        String cbuIct = "School of Information and Communication Technology";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_it1", "Bachelor of Science in Computer Science", cbuIct, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_it2", "Bachelor of Information Technology", cbuIct, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_it3", "Bachelor of Computer Engineering", cbuIct, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_it4", "Diploma in Information Technology", cbuIct, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_it5", "Master of Science in Computer Science", cbuIct, cbu, now));

        // Distance Education / Open and Distance Learning (DDEOL)
        String cbuDist = "Distance Education / Open and Distance Learning (DDEOL)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de1", "Bachelor of Science in Agroforestry", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de2", "Bachelor of Science in Forestry", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de3", "Bachelor of Science in Natural Resource Management and Climate Change", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de4", "Bachelor of Science in Bioenergy", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de5", "Bachelor of Science in Fisheries Management", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de6", "Bachelor of Science in Sustainable Aquaculture", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de7", "Bachelor of Science in Biomedical Science", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de8", "Bachelor of Business Studies with Education", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de9", "Bachelor of Science in Biology Education", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de10", "Bachelor of Science in Chemistry Education", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de11", "Bachelor of Science in Physics Education", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de12", "Bachelor of Science in Mathematics Education", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de13", "Bachelor of Science in ICT with Education", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de14", "Bachelor of Engineering in Chemical Engineering", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de15", "Bachelor of Engineering in Mining Engineering", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de16", "Bachelor of Engineering in Metallurgical Engineering", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de17", "Bachelor of Accountancy", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de18", "Bachelor of Business Administration", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de19", "Bachelor of Science in Marketing", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de20", "Bachelor of Science in Banking and Finance", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de21", "Bachelor of Science in Purchasing and Supply", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de22", "Bachelor of Science in Business and Project Management", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de23", "Bachelor of Science in Human Resource Management", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de24", "Bachelor of Arts in Peace and Conflict Studies", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de25", "Bachelor of Local Government Administration", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de26", "Diploma in Local Government Administration", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de27", "Diploma in Paralegal Studies", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de28", "Diploma in Metallurgy", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de29", "Diploma in Mining", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de30", "Diploma in Chemical Technology", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de31", "Diploma in Mine Ventilation", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de32", "Diploma in Teaching Methodology", cbuDist, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_de33", "Postgraduate Diploma in Teaching and Learning", cbuDist, cbu, now));

        // CBU eCampus (Online)
        String cbuOnline = "CBU eCampus (Online)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec1", "Bachelor of Law", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec2", "MBA General", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec3", "MBA in Finance", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec4", "MA in Economics", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec5", "Master of Arts in Economic Policy", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec6", "Master of Public Health", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec7", "MSc in Economics", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec8", "MSc in Accounting and Finance", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec9", "MSc in Project Management", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec10", "Masters in Peace and Conflict", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec11", "Postgraduate Diploma in Educational Leadership", cbuOnline, cbu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cbu_ec12", "Postgraduate Diploma in Teaching Methodology", cbuOnline, cbu, now));

        // Mulungushi University (MU)
        String mu = "Mulungushi University";

        // School of Agriculture and Natural Resources (SANR)
        String muSanr = "School of Agriculture and Natural Resources (SANR)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sanr1", "BSc in Agriculture (Fulltime only)", muSanr, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sanr2", "BSc in Land and Water Resources Management (Fulltime only)", muSanr, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sanr3", "BSc in Climatology (Fulltime only)", muSanr, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sanr4", "BSc in Environmental Studies (Fulltime only)", muSanr, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sanr5", "Bachelor of Agricultural Business Management (Fulltime & Distance)", muSanr, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sanr6", "Master in Transformative Community Development (Distance)", muSanr, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sanr7", "Master in Disaster Studies (Distance)", muSanr, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sanr8", "Master in Climate Change and Sustainable Development (Distance)", muSanr, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sanr9", "MSc in Agribusiness (Fulltime & Distance)", muSanr, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sanr10", "MSc in Agricultural Risk Management (Fulltime & Distance)", muSanr, mu, now));

        // School of Business Studies (SBS)
        String muSbs = "School of Business Studies (SBS)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs1", "Bachelor of Laws (LLB)", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs2", "Bachelor of Commerce – Accounting and Finance", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs3", "Bachelor of Commerce – Marketing", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs4", "Bachelor of Commerce – Business Management", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs5", "Bachelor of Commerce – Economics", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs6", "Bachelor of Business Administration and Entrepreneurship", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs7", "Bachelor of Banking and Finance", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs8", "Bachelor of Accounting and Finance", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs9", "Bachelor of Purchasing and Supply Management", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs10", "Bachelor of Human Resource Management", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs11", "Bachelor of Tourism and Hospitality Management", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs12", "Bachelor of Labour and Employment Relations", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs13", "Bachelor of Arts in Labour and Human Resource Management", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs14", "Bachelor of Marketing", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs15", "Bachelor of Marketing and Advertising", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs16", "Bachelor of Communications in Public Relations", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs17", "Bachelor of Communications in Journalism", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs18", "PG Diploma in Labour and Employment Relations (Distance)", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs19", "PG Diploma in Quality Management (Distance)", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs20", "Master of Business Administration (MBA) (Distance)", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs21", "Master of Labour and Employment Relations (Distance)", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs22", "Master of Marketing (Distance)", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs23", "MSc in Human Resource Management (Distance)", muSbs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sbs24", "MSc in Banking and Finance (Distance)", muSbs, mu, now));

        // School of Education (SOE)
        String muSoe = "School of Education (SOE)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe1", "Diploma in Teaching Methodology (Distance only)", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe2", "BSc in ICT with Education", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe3", "BSc in Mathematics with Education", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe4", "BSc in Agriculture with Education", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe5", "BSc in Mathematics and ICT with Education", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe6", "Bachelor of Business Administration & Entrepreneurship with Education", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe7", "BSc with Education (Biology, Chemistry, Physics, Mathematics)", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe8", "Bachelor of Business Studies with Education", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe9", "Bachelor of Arts with Education (English Language, Zambian Languages, French, Chinese, Civic Education, Religious Studies, Physical Education & Sports, History, Geography)", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe10", "Master of Arts in Linguistic Sciences (Distance)", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe11", "Master of Arts in History (Distance)", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe12", "Master of Arts in Sociology of Education (Distance)", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe13", "Master of Arts in Civic Education and Transformational Leadership (Distance)", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe14", "Master of Arts in Educational Administration and Leadership (Distance)", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe15", "Master of Arts in Educational Policy, Planning and Management (Distance)", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe16", "Master of Education – Assessment, Measurement and Evaluation (Distance)", muSoe, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_soe17", "Master of Education in Curriculum Studies (Distance)", muSoe, mu, now));

        // School of Medicine and Health Sciences (SoMHS)
        String muSomhs = "School of Medicine and Health Sciences (SoMHS)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_somhs1", "Bachelor of Medicine and Bachelor of Surgery (MBChB)", muSomhs, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_somhs2", "Master of Medicine in General Surgery", muSomhs, mu, now));

        // School of Science, Engineering and Technology (SSET)
        String muSset = "School of Science, Engineering and Technology (SSET)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset1", "BSc in Computer Science (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset2", "BSc in Information Technology (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset3", "BSc in Statistics (Fulltime & Distance)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset4", "BSc in Mathematics and Statistics (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset5", "BSc in Demography (Fulltime & Distance)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset6", "BSc in Biological Sciences (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset7", "BSc in Bio-Chemistry (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset8", "BSc in Chemistry (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset9", "BSc in Physics (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset10", "BSc in Physics and Mathematics (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset11", "Bachelor of Engineering in Civil Engineering (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset12", "Bachelor of Engineering in Mechanical Engineering (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset13", "Bachelor of Engineering in Agricultural Engineering (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset14", "Bachelor of Engineering in Electronics and Electrical Engineering – Control Systems (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset15", "Bachelor of Engineering in Electronics and Electrical Engineering – Energy Systems (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset16", "Bachelor of Engineering in Electronics and Electrical Engineering – Communication and Network Systems (Fulltime only)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset17", "MSc in Computer Studies (Fulltime & Distance)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset18", "Master of Engineering in Project Management (Distance)", muSset, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sset19", "PhD in Computing Sciences", muSset, mu, now));

        // School of Social Sciences (SSS)
        String muSss = "School of Social Sciences (SSS)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss1", "BSc in Economics and Statistics (Fulltime only)", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss2", "Bachelor of Economics (Fulltime & Distance)", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss3", "Bachelor of Psychology (Fulltime & Distance)", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss4", "Bachelor of Industrial Psychology (Fulltime & Distance)", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss5", "Bachelor of Development Studies (Fulltime & Distance)", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss6", "Bachelor of International Relations and Development (Fulltime & Distance)", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss7", "Bachelor of Local Government Administration (Fulltime & Distance)", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss8", "Bachelor of Public Administration (Fulltime & Distance)", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss9", "Bachelor of Social Work (Fulltime & Distance)", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss10", "Master of Public Administration", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss11", "Master of International Relations and Development", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss12", "Master of Social Work", muSss, mu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("mu_sss13", "Master of Local Government Administration", muSss, mu, now));

        // Kwame Nkrumah University (KNU)
        String knu = "Kwame Nkrumah University";

        // School of Business Studies
        String knuBus = "School of Business Studies";
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs1", "Bachelor of Accountancy", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs2", "Bachelor of Accounting & Finance", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs3", "Bachelor of Arts in Entrepreneurship", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs4", "Bachelor of Arts in Banking and Finance", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs5", "Bachelor of Arts in Economics", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs6", "Bachelor of Arts in Economics and Finance", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs7", "Bachelor of Arts in Economics and Statistics", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs8", "Bachelor of Arts in Human Resource Management", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs9", "Bachelor of Arts in International Relations and Security", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs10", "Bachelor of Arts in Public Administration", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs11", "Bachelor of Banking and Finance", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs12", "Bachelor of Business Administration", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs13", "Bachelor of Business Administration and Entrepreneurship", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs14", "Bachelor of Digital Marketing", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs15", "Bachelor of Laws", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs16", "Bachelor of Science in Accounting and Finance", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs17", "Bachelor of Science in Marketing", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs18", "Bachelor of Science in Procurement and Supply Chain Management", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs19", "Bachelor of Science in Transport and Logistics", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs20", "Bachelor of Transport and Logistics", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs21", "ZIM Diploma in Marketing", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs22", "Master of Business Administration – Executive", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs23", "Master of Business Administration – Finance", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs24", "Master of Business Administration – General", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs25", "Master of Business Studies", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs26", "ZIPS Postgraduate Diploma in Procurement and Supply Chain Management", knuBus, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_bs27", "ZIM Postgraduate Diploma in Marketing", knuBus, knu, now));

        // School of Humanities and Social Sciences
        String knuHum = "School of Humanities and Social Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs1", "Bachelor of Arts in International Relations and Security", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs2", "Bachelor of Arts in Public Administration", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs3", "Bachelor of Science in Disaster Risk Reduction and Management", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs4", "Bachelor of Science in Environmental Studies and Management", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs5", "Bachelor of Sports Management", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs6", "French for Specific Purposes", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs7", "Master of Arts in Civic Education", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs8", "Master of Arts in General Linguistics", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs9", "Master of Arts in History", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs10", "Master of Arts in Human Resource Management", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs11", "Master of Arts in Linguistic Science", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs12", "Master of Arts in Religious Studies", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs13", "Master of Science in Geography", knuHum, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_hs14", "Postgraduate Diploma in Peace and Ethnic Studies", knuHum, knu, now));

        // School of Natural Sciences
        String knuNat = "School of Natural Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ns1", "Bachelor of Information Technology", knuNat, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ns2", "Bachelor of Science in Laboratory Technology", knuNat, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ns3", "Bachelor of Science in Mathematics", knuNat, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ns4", "Bachelor of Science in Agriculture Science Education", knuNat, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ns5", "Bachelor of Technology in Mathematics and Computer Science", knuNat, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ns6", "Diploma in Agriculture Science Education", knuNat, knu, now));

        // School of Education
        String knuEdu = "School of Education";
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed1", "Bachelor of Arts with Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed2", "Bachelor of Business Studies with Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed3", "Bachelor of Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed4", "Bachelor of Education – Educational Psychology", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed5", "Bachelor of Education in Early Childhood Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed6", "Bachelor of Education in Education Management", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed7", "Bachelor of Education in Primary Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed8", "Bachelor of Education in Sociology of Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed9", "Bachelor of Education – Physical Education and Sport", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed10", "Bachelor of Education – Special Needs Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed11", "Bachelor of ICT with Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed12", "Bachelor of Science with Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed13", "Diploma in Early Childhood Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed14", "Diploma in Registered Nursing", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed15", "Master of Education in Educational Psychology", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed16", "Master of Education in Mathematics Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed17", "Master of Education in Sociology of Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed18", "Master of Education in Special Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed19", "Master of Science in Biology Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed20", "Master of Science in Chemistry Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed21", "Master of Science in Mathematics Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed22", "Master of Science in Physics Education", knuEdu, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_ed23", "Postgraduate Diploma in Teaching Methodology", knuEdu, knu, now));

        // Short Courses (School of Business Studies)
        String knuShort = "Short Courses (School of Business Studies)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_sc1", "Cisco Networking Short Courses", knuShort, knu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("knu_sc2", "Various professional short courses", knuShort, knu, now));

        // Chalimbana University (ChaU)
        String chau = "Chalimbana University";

        // School of Business Studies
        String chauBus = "School of Business Studies";
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_bs1", "Bachelor of Business Studies with Education", chauBus, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_bs2", "Bachelor of Business Administration in Marketing", chauBus, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_bs3", "Bachelor of Business Administration in Entrepreneurship", chauBus, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_bs4", "Bachelor of Business Administration in Human Resource Management", chauBus, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_bs5", "Bachelor of Business Administration in Accounting and Finance", chauBus, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_bs6", "Bachelor of Science in Transport and Logistics", chauBus, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_bs7", "Bachelor of Science in Banking and Finance", chauBus, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_bs8", "Bachelor of Science in Procurement and Supply Chain Management", chauBus, chau, now));

        // School of Music, Sports, Performing and Fine Arts
        String chauArts = "School of Music, Sports, Performing and Fine Arts";
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_msp1", "Bachelor of Science in Education and Sports", chauArts, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_msp2", "Bachelor of Science in Sports Management", chauArts, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_msp3", "Bachelor of Arts in Music", chauArts, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_msp4", "Bachelor of Music with Education", chauArts, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_msp5", "Bachelor of Arts in Fine Arts with Education", chauArts, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_msp6", "Bachelor of Arts in Fine Arts", chauArts, chau, now));

        // School of Early Childhood Studies
        String chauEcs = "School of Early Childhood Studies";
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ecs1", "Bachelor of Early Childhood Education", chauEcs, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ecs2", "Bachelor of Science in Child Care and Development", chauEcs, chau, now));

        // School of Vocational and Practical Skills Education
        String chauVoc = "School of Vocational and Practical Skills Education";
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_vps1", "Bachelor of Hospitality and Tourism Management", chauVoc, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_vps2", "Bachelor of Design and Technology with Education (Secondary)", chauVoc, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_vps3", "Bachelor of Home Economics with Education (Secondary)", chauVoc, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_vps4", "Bachelor of Food Science and Nutrition", chauVoc, chau, now));

        // School of Education
        String chauEdu = "School of Education";
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed1", "Bachelor of Education – Secondary", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed2", "Bachelor of Education – Primary", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed3", "Bachelor of Guidance and Counselling with Education", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed4", "Bachelor of Community Education and Development with a Teaching Subject", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed5", "Bachelor of Special Education with a Teaching Subject", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed6", "Bachelor of Information and Communication Technology with Education", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed7", "Diploma in Teaching Methodology (Distance Only)", chauEdu, chau, now));

        // Secondary Subject Combinations
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed8", "B.Ed Secondary - English / Zambian Languages", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed9", "B.Ed Secondary - English / Civic Education", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed10", "B.Ed Secondary - English / Geography", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed11", "B.Ed Secondary - English / History", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed12", "B.Ed Secondary - English / Religious Education", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed13", "B.Ed Secondary - English / Physical Education", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed14", "B.Ed Secondary - English / Music", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed15", "B.Ed Secondary - English / Art and Design", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed16", "B.Ed Secondary - Zambian Languages / English", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed17", "B.Ed Secondary - Zambian Languages / Civic Education", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed18", "B.Ed Secondary - Zambian Languages / Geography", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed19", "B.Ed Secondary - Zambian Languages / History", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed20", "B.Ed Secondary - Zambian Languages / Religious Education", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed21", "B.Ed Secondary - Civic Education / English", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed22", "B.Ed Secondary - Civic Education / Zambian Languages", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed23", "B.Ed Secondary - Civic Education / Geography", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed24", "B.Ed Secondary - Civic Education / History", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed25", "B.Ed Secondary - Civic Education / Religious Education", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed26", "B.Ed Secondary - Religious Education / English", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed27", "B.Ed Secondary - Religious Education / Zambian Languages", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed28", "B.Ed Secondary - Religious Education / Geography", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed29", "B.Ed Secondary - Religious Education / Civic Education", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed30", "B.Ed Secondary - History Major / English Minor", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed31", "B.Ed Secondary - History Major / Zambian Languages Minor", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed32", "B.Ed Secondary - History Major / Civic Education Minor", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed33", "B.Ed Secondary - History Major / Geography Minor", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed34", "B.Ed Secondary - History Major / Religious Education Minor", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed35", "B.Ed Secondary - Civic Education Major / Mathematics Minor", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed36", "B.Ed Secondary - Geography Major / Mathematics Minor", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed37", "B.Ed Secondary - Geography Major / Music Minor", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed38", "B.Ed Secondary - Geography Major / Art and Design Minor", chauEdu, chau, now));

        // ICT Subject Combinations
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed39", "Bachelor of ICT with Education - ICT / Biology", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed40", "Bachelor of ICT with Education - Biology / ICT", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed41", "Bachelor of ICT with Education - ICT / Physics", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed42", "Bachelor of ICT with Education - Physics / ICT", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed43", "Bachelor of ICT with Education - ICT / Chemistry", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed44", "Bachelor of ICT with Education - Chemistry / ICT", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed45", "Bachelor of ICT with Education - ICT / Mathematics", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed46", "Bachelor of ICT with Education - Mathematics / ICT", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed47", "Bachelor of ICT with Education - ICT / Psychology", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed48", "Bachelor of ICT with Education - Psychology / ICT", chauEdu, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_ed49", "Bachelor of ICT with Education - ICT Single Major", chauEdu, chau, now));

        // School of Technology, Science and Mathematics Education
        String chauTech = "School of Technology, Science and Mathematics Education";
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm1", "Bachelor of Science with Education", chauTech, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm2", "Bachelor of Agriculture Science with Education", chauTech, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm3", "Bachelor of Agriculture Science (General)", chauTech, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm4", "Bachelor of Science in Laboratory Technology", chauTech, chau, now));

        // BSc with Education Subject Combinations
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm5", "BSc with Education - Biology / Chemistry", chauTech, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm6", "BSc with Education - Chemistry / Biology", chauTech, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm7", "BSc with Education - Mathematics / Physics", chauTech, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm8", "BSc with Education - Physics / Mathematics", chauTech, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm9", "BSc with Education - Chemistry / Physics", chauTech, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm10", "BSc with Education - Physics / Chemistry", chauTech, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm11", "BSc with Education - Mathematics / Chemistry", chauTech, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_tsm12", "BSc with Education - Chemistry / Mathematics", chauTech, chau, now));

        // School of Humanities and Social Sciences
        String chauHum = "School of Humanities and Social Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_hss1", "Bachelor of Arts in Gender Studies", chauHum, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_hss2", "Bachelor of Arts in Psychology", chauHum, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_hss3", "Bachelor of Arts in Policing and Security Studies", chauHum, chau, now));

        // School of Leadership, Management and Governance
        String chauLmg = "School of Leadership, Management and Governance";
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_lmg1", "Bachelor of Education Leadership and Management", chauLmg, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_lmg2", "Bachelor of Traditional Leadership and Governance", chauLmg, chau, now));

        // Directorate of Postgraduate Studies
        String chauPost = "Directorate of Postgraduate Studies";
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg1", "Master of Education Leadership and Management", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg2", "Master of Music Education", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg3", "Master of Business Administration – Generic", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg4", "Master of Business Studies with Education", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg5", "Master of Business Administration – Human Resource Management", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg6", "Master of Business Administration – Entrepreneurship and Innovation", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg7", "Master of Business Administration – Marketing", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg8", "MSc in Procurement and Supply Chain Management", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg9", "Master of Education in Early Childhood Education", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg10", "Master of Education – Primary Education", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg11", "Master of Arts in Applied Linguistics", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg12", "Master of Arts in Gender Studies", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg13", "Master of Arts in Fine Arts", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg14", "Master of Arts in Literature", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg15", "Master of Arts in Linguistic Science", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg16", "Master of Arts in Religious Studies", chauPost, chau, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("chau_pg17", "Doctor of Philosophy (with Specialization)", chauPost, chau, now));

        // Robert Makasa University
        String kmu = "Robert Makasa University";

        // School of Applied Sciences - Dept of ICT
        String kmuIct = "School of Applied Sciences (ICT)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict1", "BSc in Cyber Security", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict2", "BSc in Information and Communication Technologies", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict3", "BSc in Health Informatics", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict4", "BSc in Network Engineering", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict5", "BSc in Software Engineering", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict6", "BSc in Information Systems and Business Analytics", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict7", "BSc in Data Science", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict8", "BSc in ICT with Education (Agriculture Science minor)", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict9", "BSc in ICT with Education (Chemistry minor)", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict10", "BSc in ICT with Education (Mathematics minor)", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict11", "BSc in ICT with Education (Biology minor)", kmuIct, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ict12", "BSc in ICT with Education (Physics minor)", kmuIct, kmu, now));

        // Dept of Agriculture and Aquatic Sciences
        String kmuAgri = "School of Applied Sciences (Agriculture)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ag1", "BSc in Fisheries and Aquaculture", kmuAgri, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ag2", "BSc in Fisheries Management", kmuAgri, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ag3", "BSc in Sustainable Agriculture", kmuAgri, kmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ag4", "BSc in Animal Science", kmuAgri, kmu, now));

        // Dept of Education and Open Learning
        String kmuEdu = "Department of Education and Open Learning";
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_ed1", "Education programmes (open and distance learning mode)", kmuEdu, kmu, now));

        // Short Courses
        String kmuShort = "Short Courses";
        db.degreeDao().insert(new com.bravem.app.model.Degree("kmu_sc1", "Various ICT and Agriculture short courses", kmuShort, kmu, now));

        // Levy Mwanawasa Medical University (LMMU)
        String lmmu = "Levy Mwanawasa Medical University";

        // School of Medicine and Clinical Sciences
        String lmmuMed = "School of Medicine and Clinical Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m1", "Bachelor of Medicine and Bachelor of Surgery (MBChB) – 6 years", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m2", "Bachelor of Dental Surgery (BDS) – 6 years", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m3", "BSc in Clinical Anaesthesia", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m4", "BSc in Mental Health and Psychiatry", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m5", "BSc in Clinical Ophthalmology", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m6", "BSc in Optometry", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m7", "BSc in Clinical Sciences (Clinical Medicine)", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m8", "Advanced Diploma in Clinical Anaesthesia", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m9", "Advanced Diploma in Clinical Ophthalmology", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m10", "Diploma in Dental Therapy", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m11", "Diploma in Dental Technology", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m12", "Diploma in Optometry", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m13", "Diploma in Clinical Sciences – General", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m14", "Diploma in Clinical Sciences – Psychiatry", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m15", "Diploma in Emergency Medical Care", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m16", "Certificate in Emergency Medical Care", lmmuMed, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_m17", "Certificate in Dental Assisting", lmmuMed, lmmu, now));

        // School of Nursing
        String lmmuNurse = "School of Nursing";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_n1", "BSc in Nursing", lmmuNurse, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_n2", "BSc in Midwifery", lmmuNurse, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_n3", "BSc in Public Health Nursing", lmmuNurse, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_n4", "BSc in Ophthalmic Nursing", lmmuNurse, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_n5", "BSc in Mental Health Nursing", lmmuNurse, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_n6", "Diploma in Nursing", lmmuNurse, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_n7", "Diploma in Midwifery", lmmuNurse, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_n8", "Diploma in Public Health Nursing", lmmuNurse, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_n9", "Diploma in Mental Health Nursing", lmmuNurse, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_n10", "Diploma in Oncology Nursing", lmmuNurse, lmmu, now));

        // Institute of Basic and Biomedical Sciences (IBBS)
        String lmmuIbbs = "Institute of Basic and Biomedical Sciences (IBBS)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_i1", "BSc in Biomedical Science", lmmuIbbs, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_i2", "BSc in Medical Microbiology (in-service only)", lmmuIbbs, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_i3", "Diploma in Biomedical Sciences", lmmuIbbs, lmmu, now));

        // School of Public Health and Environmental Sciences
        String lmmuPub = "School of Public Health and Environmental Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_p1", "BSc in Public Health", lmmuPub, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_p2", "BSc in Environmental Health", lmmuPub, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_p3", "BSc in Public Health Nutrition", lmmuPub, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_p4", "Diploma in Public Health", lmmuPub, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_p5", "Diploma in Environmental Health", lmmuPub, lmmu, now));

        // School of Health Sciences
        String lmmuHealth = "School of Health Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_h1", "Bachelor of Pharmacy (B-Pharm)", lmmuHealth, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_h2", "BSc in Clinical Nutrition and Dietetics", lmmuHealth, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_h3", "BSc in Radiography", lmmuHealth, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_h4", "BSc in Physiotherapy", lmmuHealth, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_h5", "BA in General Counselling", lmmuHealth, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_h6", "Diploma in General Counselling", lmmuHealth, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_h7", "Diploma in Prosthetics and Orthotics", lmmuHealth, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_h8", "Certificate in Psychosocial Counselling", lmmuHealth, lmmu, now));

        // Postgraduate Studies and Research
        String lmmuPost = "Postgraduate Studies and Research";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg1", "MSc in Health Professions' Education", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg2", "MSc in Clinical Ophthalmology", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg3", "MSc in Optometry", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg4", "MSc in Infectious Diseases", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg5", "Master of Public Health (MPH)", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg6", "MMed in Paediatrics and Child Health", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg7", "MMed in Obstetrics and Gynaecology", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg8", "MMed in Internal Medicine", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg9", "MMed in General Surgery", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg10", "MMed in Infectious Diseases", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg11", "MMed in Ophthalmology", lmmuPost, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_pg12", "Doctor of Philosophy (PhD) in Health Professions Education", lmmuPost, lmmu, now));

        // Open, Distance and Blended Learning (ODL / E-Learning)
        String lmmuOdl = "Open, Distance and Blended Learning (ODL / E-Learning)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl1", "Certificate in Community Health Assisting (HIV Medics)", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl2", "Diploma in HIV Nurse Practitioner (in-service only)", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl3", "Diploma in Clinical Medical Science – General", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl4", "Diploma in Clinical Medical Science – Psychiatry", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl5", "Diploma in General Counselling", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl6", "BSc in Public Health (In-Service)", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl7", "BSc in Environmental Health (In-Service)", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl8", "BSc in Public Health Nutrition (In-Service)", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl9", "BSc in Nursing (In-Service)", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl10", "BSc in Mental Health Nursing (In-Service)", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl11", "BSc in Midwifery (In-Service)", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl12", "BSc in Public Health Nursing (In-Service)", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl13", "BSc in Mental Health and Clinical Psychiatry (In-Service)", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl14", "BA in General Counselling", lmmuOdl, lmmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_odl15", "Master of Public Health (MPH)", lmmuOdl, lmmu, now));

        // Kabwe Campus
        String lmmuKabwe = "Kabwe Campus";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lmmu_kb1", "Diploma in Clinical Medical Sciences – General", lmmuKabwe, lmmu, now));

        // Palabana University (PU)
        String pu = "Palabana University";

        // School of Agricultural Sciences
        String puAgri = "School of Agricultural Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_ag1", "Bachelor of Science in Animal Science (Full-time & Distance)", puAgri, pu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_ag2", "Bachelor of Aquaculture and Fisheries Science (Full-time & Distance)", puAgri, pu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_ag3", "Bachelor of Agriculture with Education (Distance)", puAgri, pu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_ag4", "Diploma in Animal Production (Full-time & Distance)", puAgri, pu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_ag5", "Certificate in Livestock Production – TEVETA (Full-time)", puAgri, pu, now));

        // Short Courses
        String puShort = "Short Courses (Palabana University)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_sc1", "Dairy Farming", puShort, pu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_sc2", "Livestock Production", puShort, pu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_sc3", "Poultry Management", puShort, pu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_sc4", "Crop Production", puShort, pu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_sc5", "Vegetable Production", puShort, pu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_sc6", "Aquaculture Management", puShort, pu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pu_sc7", "Agribusiness and Entrepreneurship", puShort, pu, now));

        // Paul Mambo University (PMU)
        String pmu = "Paul Mambo University (PMU)";
        String pmuPlanned = "Planned / Early Programmes";
        db.degreeDao().insert(new com.bravem.app.model.Degree("pmu_p1", "Diploma in Technical Education", pmuPlanned, pmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pmu_p2", "Diploma in Vocational Education", pmuPlanned, pmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pmu_p3", "BSc in Technical and Vocational Education", pmuPlanned, pmu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("pmu_p4", "BSc in Applied Sciences", pmuPlanned, pmu, now));

        // Cavendish University Zambia (CUZ)
        String cuz = "Cavendish University Zambia (CUZ)";
        
        // School of Business and Information Technology (BIT)
        String cuzBit = "School of Business and Information Technology (BIT)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b1", "Bachelor of Business Administration (BBA)", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b2", "Bachelor of Accounting (BAC)", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b3", "Bachelor of Arts – Banking and Finance (BA BF)", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b4", "Bachelor of Arts – Economics (BA ECON)", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b5", "Bachelor of Arts – Purchasing and Supply (BA PS)", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b6", "Bachelor of Science in Procurement and Supply Chain Management (BSc PSCM)", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b7", "Bachelor of Science in Project Management (BSc PM)", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b8", "Bachelor of Science in Computing (BSc COM)", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b9", "Master of Business Administration – MBA Generic", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b10", "Master of Business Administration MBA – Finance", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b11", "Master of Business Administration MBA – Human Resource Management", cuzBit, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_b12", "Master of Science in Project Management (MSc PM)", cuzBit, cuz, now));

        // School of Arts, Education and Social Sciences (AESS)
        String cuzAess = "School of Arts, Education and Social Sciences (AESS)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a1", "Bachelor of Arts in Education – English and Civic Education (BAE-EC)", cuzAess, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a2", "Bachelor of Arts in Education – Mathematics and ICT (BAE-MI)", cuzAess, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a3", "Bachelor of Arts in Education (BA Ed) with Information Technology", cuzAess, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a4", "Bachelor of Arts in Mass Communication and Public Relations (BMCPR)", cuzAess, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a5", "Bachelor of Arts in Politics and International Relations", cuzAess, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a6", "Bachelor of Development Studies (BDS)", cuzAess, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a7", "Bachelor of Social Work", cuzAess, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a8", "Master of Science in Public Relations (MA PR)", cuzAess, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a9", "Master of Development Studies (MDS)", cuzAess, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a10", "Master of Social Work", cuzAess, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_a11", "Master of International Relations and Diplomacy", cuzAess, cuz, now));

        // School of Medicine (SoM)
        String cuzSom = "School of Medicine (SoM)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_m1", "Bachelor of Medicine and Bachelor of Surgery (MBChB)", cuzSom, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_m2", "Bachelor of Nursing and Midwifery Sciences (B.N.Mw.Sc)", cuzSom, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_m3", "Bachelor of Clinical Sciences (BSc CS)", cuzSom, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_m4", "Diploma in Registered Nursing Programme (DRN)", cuzSom, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_m5", "Medicine School Foundation Programme (MSFP)", cuzSom, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_m6", "Master of Public Health (MPH)", cuzSom, cuz, now));

        // School of Law (SoL)
        String cuzSol = "School of Law (SoL)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_l1", "Bachelor of Laws (LLB)", cuzSol, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_l2", "Master of Laws in International Business Law / MPhil", cuzSol, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_l3", "Master of Laws in Constitutionalism and Human Rights Law / MPhil", cuzSol, cuz, now));

        // Postgraduate Diplomas (All Schools)
        String cuzPg = "Postgraduate Diplomas (All Schools)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_pg1", "Postgraduate Diploma in Monitoring and Evaluation (PGDM&E)", cuzPg, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_pg2", "Postgraduate Diploma in Teaching Methods and Learning Resources (PGD-TMLR)", cuzPg, cuz, now));

        // Short Courses
        String cuzShort = "Short Courses";
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_sc1", "Robotics Short Course", cuzShort, cuz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("cuz_sc2", "Remote Work Programme Course", cuzShort, cuz, now));

        // Texila American University Zambia (TAU-Z)
        String tauz = "Texila American University Zambia (TAU-Z)";
        String tauzMed = "School of Medicine";
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_m1", "Health Professions Foundation Programme (HPFP) – 1 year", tauzMed, tauz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_m2", "Bachelor of Medicine and Bachelor of Surgery (MBChB) – 5 years", tauzMed, tauz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_m3", "Bachelor of Science in Public Health (BPH)", tauzMed, tauz, now));
        String tauzBit = "School of Business and Information Technology";
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_b1", "Bachelor of Business Administration (BBA)", tauzBit, tauz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_b2", "Bachelor of Science in Project Management", tauzBit, tauz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_b3", "Bachelor of Science in Marketing", tauzBit, tauz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_b4", "Bachelor of Science in Finance and Accounting", tauzBit, tauz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_b5", "Bachelor of Information Technology (BIT)", tauzBit, tauz, now));
        String tauzPg = "Postgraduate";
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_pg1", "Master of Business Administration (MBA)", tauzPg, tauz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_pg2", "Master of Public Health (MPH)", tauzPg, tauz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_pg3", "Master of Science in Project Management", tauzPg, tauz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_pg4", "Master of Science in Banking and Insurance", tauzPg, tauz, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("tauz_pg5", "Doctor of Philosophy (PhD) – selected fields", tauzPg, tauz, now));

        // Lusaka Apex Medical University (LAMU)
        String lamu = "Lusaka Apex Medical University (LAMU)";
        String lamuPre = "Faculty of Premedical Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_pre1", "Pre-Medical Sciences Programme (1 year)", lamuPre, lamu, now));
        String lamuMed = "Faculty of Medicine";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_m1", "Bachelor of Medicine and Bachelor of Surgery (MBChB) – 5 years", lamuMed, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_m2", "Bachelor of Science in Clinical Anaesthesia (BSc.CA) – 4 years", lamuMed, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_m3", "Diploma in Clinical Medicine (General) – 3 years", lamuMed, lamu, now));
        String lamuPhar = "Faculty of Pharmacy and Nutrition and Dietetics";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_p1", "Bachelor of Pharmacy Degree (BPharm) – 4 years", lamuPhar, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_p2", "Bachelor of Science in Nutrition and Dietetics (BSc.ND) – 4 years", lamuPhar, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_p3", "Diploma in Pharmacy – 3 years", lamuPhar, lamu, now));
        String lamuHealth = "Faculty of Health Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h1", "BSc in Biology with Education (BSc.BE) – 3 years", lamuHealth, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h2", "Bachelor of Science in Physiotherapy – Full Time – 4 years", lamuHealth, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h3", "Bachelor of Science in Physiotherapy – Blended Learning (In-Service) – 3 years", lamuHealth, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h4", "Bachelor of Science in Environmental Health (Env.H) – Full Time – 4 years", lamuHealth, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h5", "Bachelor of Science in Environmental Health – Blended Learning (In-Service) – 3 years", lamuHealth, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h6", "Bachelor of Science in Public Health (BS.PH) – 4 years", lamuHealth, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h7", "Physiotherapy Diploma – 3 years", lamuHealth, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h8", "Diploma in Environmental Health (Dip.Env.H) – 3 years", lamuHealth, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h9", "Master of Public Health (MPH) – 2 years", lamuHealth, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h10", "Master of Science in Hospital and Healthcare Management – 2 years", lamuHealth, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_h11", "Postgraduate Diploma in Hospital and Healthcare Management – 1 year", lamuHealth, lamu, now));
        String lamuRad = "Faculty of Medical Radiation and Ultrasound";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_r1", "Bachelor of Science in Diagnostic Radiography (Diag.Rad) – 4 years", lamuRad, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_r2", "Bachelor of Science in Diagnostic Medical Ultrasound (BSc.DMU) – 3 years", lamuRad, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_r3", "Diploma in Radiography – 3 years", lamuRad, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_r4", "Diploma in Diagnostic Ultrasound – 2 years", lamuRad, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_r5", "Master of Medicine in Radiology (M.Med) – 4 years", lamuRad, lamu, now));
        String lamuNurse = "Faculty of Nursing and Midwifery";
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_n1", "Bachelor of Science in Nursing (BSc.N) – 4 years", lamuNurse, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_n2", "Diploma in Nursing – 3 years", lamuNurse, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_n3", "Diploma in Midwifery – 3 years", lamuNurse, lamu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("lamu_n4", "Diploma in Midwifery (In-Service) – 1.5 years", lamuNurse, lamu, now));

        // Zambian Open University (ZAOU)
        String zaou = "Zambian Open University (ZAOU)";
        String zaouEdu = "School of Education";
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e1", "Bachelor of Education in Adult Education", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e2", "Bachelor of Education in Primary Education", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e3", "Bachelor of Education in Secondary Education", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e4", "Bachelor of Education in Early Childhood Education", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e5", "Bachelor of Education in Guidance and Counselling", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e6", "Bachelor of Education in Special Education", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e7", "Bachelor of Theatre Arts", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e8", "Master of Education in Literacy and Development", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e9", "Master of Education in Adult Education", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e10", "Master of Education in Curriculum Studies", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e11", "Master of Education in Educational Management and Administration", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e12", "Master of Education in Early Childhood Education", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e13", "Master of Education in Special Education", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e14", "Master of Education in Guidance and Counselling", zaouEdu, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_e15", "PhD in Education", zaouEdu, zaou, now));
        String zaouLaw = "School of Law and Social Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l1", "Bachelor of Laws (LLB)", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l2", "Bachelor of Arts in Development Studies", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l3", "Bachelor of Arts in Public Administration", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l4", "Bachelor of Arts in Criminology and Security Studies", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l5", "Bachelor of Arts in Sociology", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l6", "Bachelor of Arts in Political Science", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l7", "Bachelor of Arts in Psychology", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l8", "Bachelor of Arts in Economics", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l9", "Bachelor of Social Work", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l10", "Diploma in Policing and Security Studies", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l11", "Master of Arts in Criminal Justice", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l12", "Master of Arts in Development Studies", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l13", "Master of Laws (LLM)", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l14", "PhD in Law", zaouLaw, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_l15", "PhD in Social Sciences", zaouLaw, zaou, now));
        String zaouBus = "School of Business Studies";
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_b1", "Bachelor of Business Administration (Accounting)", zaouBus, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_b2", "Bachelor of Business Administration (Human Resource Management)", zaouBus, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_b3", "Bachelor of Business Administration (Marketing)", zaouBus, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_b4", "Bachelor of Business Administration (Finance)", zaouBus, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_b5", "Bachelor of Business Administration (Entrepreneurship)", zaouBus, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_b6", "Master of Business Administration (MBA)", zaouBus, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_b7", "PhD in Business Administration", zaouBus, zaou, now));
        String zaouHum = "School of Humanities and Social Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_h1", "Bachelor of Fine Arts", zaouHum, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_h2", "Bachelor of Arts in Linguistics and African Languages", zaouHum, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_h3", "Bachelor of Arts in History", zaouHum, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_h4", "Bachelor of Arts in Religious Studies", zaouHum, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_h5", "Bachelor of Arts in Philosophy", zaouHum, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_h6", "Bachelor of Arts in Gender Studies", zaouHum, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_h7", "Master of Arts in Linguistics", zaouHum, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_h8", "Master of Arts in History", zaouHum, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_h9", "Master of Arts in Religious Studies", zaouHum, zaou, now));
        String zaouAgri = "School of Agricultural Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_a1", "Bachelor of Science in Agriculture", zaouAgri, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_a2", "Bachelor of Science in Fisheries and Aquaculture", zaouAgri, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_a3", "Bachelor of Science in Environmental Science", zaouAgri, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_a4", "Bachelor of Science in Natural Resources Management", zaouAgri, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_a5", "Master of Science in Animal Breeding and Genetics", zaouAgri, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_a6", "Master of Science in Crop Science", zaouAgri, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_a7", "Master of Science in Agricultural Economics", zaouAgri, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_a8", "PhD in Agricultural Sciences", zaouAgri, zaou, now));
        String zaouIct = "School of ICT and Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_i1", "Bachelor of Science in Information Technology", zaouIct, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_i2", "Bachelor of Science in Computer Science", zaouIct, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_i3", "Bachelor of Science in Mathematics", zaouIct, zaou, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zaou_i4", "Master of Science in Information Technology", zaouIct, zaou, now));

        // ZCAS University
        String zcas = "ZCAS University";
        String zcasBus = "School of Business";
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b1", "Bachelor of Accounting", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b2", "Bachelor of Accounting and Finance", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b3", "Bachelor of Accounting with Education", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b4", "Bachelor of Arts in Financial Services", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b5", "Bachelor of Business Administration", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b6", "Bachelor of Business Entrepreneurship", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b7", "Bachelor of Management Accounting", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b8", "Bachelor of Science in Banking and Finance", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b9", "Bachelor of Science in Accountancy", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b10", "Bachelor of Science in Accounting", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b11", "Bachelor of Science in Accounting and Finance", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b12", "Bachelor of Science in Finance and Investment Management", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b13", "Bachelor of Science in Marketing Management", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b14", "Bachelor of Science in Procurement and Supplies", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b15", "Bachelor of Arts (Hons) in Business Purchasing and Supply Chain Management – University of Greenwich", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b16", "Bachelor of Arts (Hons) in Business Studies – University of Greenwich", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b17", "Bachelor of Arts (Hons) in Business Studies, Finance – University of Greenwich", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b18", "Bachelor of Arts (Hons) in Business Studies, Logistics – University of Greenwich", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b19", "Bachelor of Arts (Hons) in Business Studies, Marketing – University of Greenwich", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b20", "Master of Business Administration (Finance)", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b21", "Master of Business Administration (Generic)", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b22", "Master of Science in Accounting and Finance", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b23", "Master of Business Administration in Business Leadership and Management Strategy", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b24", "Master of Science in Financial Services", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b25", "MBA International Business – University of Greenwich", zcasBus, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_b26", "Doctor of Business Administration", zcasBus, zcas, now));
        String zcasSocial = "School of Social Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s1", "Bachelor of Arts in Economics", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s2", "Bachelor of Arts in Human Resource Management", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s3", "Bachelor of Arts in Public Administration and Management", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s4", "Bachelor of Economics and Finance", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s5", "Bachelor of Laws (LLB Commercial Law)", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s6", "Bachelor of Laws (LLB Generic)", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s7", "Bachelor of Science in Development Finance", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s8", "Bachelor of Science in Monitoring and Evaluation", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s9", "Master of Laws in Corporate and Commercial Laws", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s10", "Master of Laws in Taxation and Investment", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s11", "Master of Science in Economics and Finance", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s12", "Master of Science in Procurement and Logistics", zcasSocial, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_s13", "Master of Science in Project Management", zcasSocial, zcas, now));
        String zcasIct = "School of Computing, Technology and Applied Sciences (SOCTAS)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i1", "Bachelor of Science in Business Computing", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i2", "Bachelor of Science in Business Mathematics with Computing", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i3", "Bachelor of Science in Computer Science", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i4", "Bachelor of Science (Honours) in Computing – University of Greenwich", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i5", "Bachelor of Science in Cyber Security", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i6", "Bachelor of Science in Information Technology", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i7", "Bachelor of Science in Internet Technology and Security", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i8", "Bachelor of Science in Mobile Computing", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i9", "Bachelor of Science in Network Engineering", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i10", "Bachelor of Science in Security and Crime Science", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i11", "Bachelor of Science in Software Engineering", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i12", "NCC Level 3 Diploma in Computing", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i13", "NCC Level 4 Diploma in Computing", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i14", "NCC Level 5 Diploma in Computing", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i15", "NCC Education Diploma in Cyber Security", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i16", "Master of Science in Computer Science", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i17", "Master of Science in Information Technology", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i18", "PhD in Computer Science", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i19", "PhD in Computing and Data Science", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i20", "PhD in Information Systems", zcasIct, zcas, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("zcas_i21", "PhD in Information Technology", zcasIct, zcas, now));

        // Information and Communications University (ICU)
        String icu = "Information and Communications University (ICU)";
        String icuEng = "School of Engineering (ICT & Technology)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e1", "Bachelor of Information and Communications Technology (BSc ICT)", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e2", "Bachelor of ICT in Information Systems", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e3", "Bachelor of ICT in Network Technology", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e4", "Bachelor of ICT in Software Engineering", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e5", "Bachelor of ICT in Systems Engineering", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e6", "Bachelor of ICT in Technology Management", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e7", "Bachelor of ICT in IT Business Management", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e8", "Bachelor of Information Security and Computer Forensics", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e9", "Bachelor of Mobile Communications", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e10", "Bachelor of Science in Electrical and Electronics Engineering", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e11", "Bachelor of Science in Architecture", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e12", "Bachelor of Science in Agriculture", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e13", "Bachelor of Science in Agriculture with Education", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e14", "Bachelor of ICT with Education", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e15", "Bachelor of Science in Environmental Management System", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e16", "Master in Information and Communications Technology", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e17", "Master of Science in Plant and Soil Science", icuEng, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_e18", "Master of Design and Technology", icuEng, icu, now));
        String icuBus = "School of Business";
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_b1", "Bachelor of Arts in Economics", icuBus, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_b2", "Bachelor of Economics and Finance", icuBus, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_b3", "Bachelor of Business Administration", icuBus, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_b4", "Master of Arts in Economics", icuBus, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_b5", "Master in Business Administration", icuBus, icu, now));
        String icuHum = "School of Humanities";
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h1", "Bachelor of Arts in Development Studies", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h2", "Bachelor of Human Resource Management", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h3", "Bachelor of Arts in Journalism", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h4", "Bachelor of Arts in Mass Communication", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h5", "Bachelor of Arts in Public Administration", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h6", "Bachelor of Arts in Project Management", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h7", "Bachelor of Social Work Practice and Development", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h8", "Bachelor of Fine Art in Acting and Film Production", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h9", "Master in Development Studies", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h10", "Master in Project Planning Management", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h11", "Master in Social Work", icuHum, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_h12", "Master of Public Administration", icuHum, icu, now));
        String icuEdu = "School of Education";
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_ed1", "Bachelor of Education in Business Studies", icuEdu, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_ed2", "Bachelor of Design and Technology with Education", icuEdu, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_ed3", "Master of Education", icuEdu, icu, now));
        String icuPg = "Postgraduate (All Schools)";
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_pg1", "Postgraduate Diploma in Monitoring and Evaluation", icuPg, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_pg2", "Postgraduate Diploma in Project Planning and Management", icuPg, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_pg3", "Postgraduate Diploma in Computer Hacking Forensic Investigation", icuPg, icu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("icu_pg4", "Postgraduate Diploma in Management Information Systems", icuPg, icu, now));

        // Rusangu University (RU)
        String ru = "Rusangu University (RU)";
        String ruBus = "School of Business";
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_b1", "Bachelor of Business Administration in Accounting", ruBus, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_b2", "Bachelor of Business Administration in Computers and Management Information Systems", ruBus, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_b3", "Bachelor of Business Administration in Human Resource Management", ruBus, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_b4", "Bachelor of Business Administration in Marketing", ruBus, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_b5", "Bachelor of Business Administration in Banking and Finance", ruBus, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_b6", "Bachelor of Science in Information Technology", ruBus, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_b7", "Master of Business Administration (MBA)", ruBus, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_b8", "Master of Science in Software Engineering", ruBus, ru, now));
        String ruEdu = "School of Education";
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_e1", "Bachelor of Arts with Education (various subject combinations)", ruEdu, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_e2", "Bachelor of Science with Education (Mathematics, Biology, Chemistry, Physics)", ruEdu, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_e3", "Bachelor of Business Studies with Education", ruEdu, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_e4", "Post Graduate Diploma in Education", ruEdu, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_e5", "Master of Arts in Psychology with Education", ruEdu, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_e6", "Master of Arts in Applied Linguistics with Education", ruEdu, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_e7", "Master of Arts in Educational Administration", ruEdu, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_e8", "Master of Science in Agriculture with Education", ruEdu, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_e9", "PhD in Education", ruEdu, ru, now));
        String ruHealth = "School of Health Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_h1", "Bachelor of Science in Nursing", ruHealth, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_h2", "Bachelor of Science in Environmental Health", ruHealth, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_h3", "Bachelor of Science in Biomedical Sciences", ruHealth, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_h4", "Diploma in Nursing", ruHealth, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_h5", "Diploma in Environmental Health", ruHealth, ru, now));
        String ruTheo = "School of Theology and Religious Studies";
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_t1", "Diploma in Theology", ruTheo, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_t2", "Diploma in Divinity", ruTheo, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_t3", "Bachelor of Arts in Theology", ruTheo, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_t4", "Master of Theology (MTh) – in collaboration with University of the Free State, South Africa", ruTheo, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_t5", "Master of Education in Religious Studies – in collaboration with UNZA", ruTheo, ru, now));
        String ruAgri = "School of Agriculture";
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_a1", "Bachelor of Science in Agriculture", ruAgri, ru, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("ru_a2", "Master of Science in Agriculture", ruAgri, ru, now));

        // DMI-St. Eugene University (DMISEU)
        String dmiseu = "DMI-St. Eugene University (DMISEU)";
        String dmiseuComp = "Department of Computer Science and IT";
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_c1", "Certificate in Computer Science", dmiseuComp, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_c2", "Diploma in Computer Science", dmiseuComp, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_c3", "Diploma in Hardware and Networking", dmiseuComp, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_c4", "Diploma in Information Technology", dmiseuComp, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_c5", "Diploma in Animation and Multimedia", dmiseuComp, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_c6", "Bachelor of Computer Science", dmiseuComp, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_c7", "Master of Science in Computer Science", dmiseuComp, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_c8", "PhD in Computer Science", dmiseuComp, dmiseu, now));
        String dmiseuMgmt = "Department of Management Studies and Commerce";
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_m1", "Diploma in Business Administration", dmiseuMgmt, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_m2", "Diploma in Sales and Marketing", dmiseuMgmt, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_m3", "Diploma in Banking and Finance", dmiseuMgmt, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_m4", "Diploma in Accounting and Finance", dmiseuMgmt, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_m5", "Diploma in Monitoring and Evaluation", dmiseuMgmt, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_m6", "Bachelor of Business Administration (BBA)", dmiseuMgmt, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_m7", "Bachelor of Commerce", dmiseuMgmt, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_m8", "Master of Business Administration (MBA)", dmiseuMgmt, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_m9", "PhD in Business Administration", dmiseuMgmt, dmiseu, now));
        String dmiseuEdu = "Department of Education";
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_e1", "Diploma in Secondary Education", dmiseuEdu, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_e2", "Bachelor of Science in Secondary Education", dmiseuEdu, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_e3", "Master of Arts in Education", dmiseuEdu, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_e4", "PhD in Education", dmiseuEdu, dmiseu, now));
        String dmiseuEng = "Department of Engineering and Technology";
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_en1", "Bachelor of Science in Electronics and Communication", dmiseuEng, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_en2", "Bachelor of Science in Renewable Energy", dmiseuEng, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_en3", "Bachelor of Science in Graphics and Multimedia", dmiseuEng, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_en4", "Bachelor of Science in Visual Communication", dmiseuEng, dmiseu, now));
        String dmiseuSocial = "Department of Social Work";
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_s1", "Bachelor of Social Work", dmiseuSocial, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_s2", "Master of Social Work", dmiseuSocial, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_s3", "PhD in Social Work", dmiseuSocial, dmiseu, now));
        String dmiseuApp = "Department of Applied Sciences";
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_a1", "Bachelor of Science in Food and Nutrition", dmiseuApp, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_a2", "Bachelor of Science in Home Science", dmiseuApp, dmiseu, now));
        db.degreeDao().insert(new com.bravem.app.model.Degree("dmiseu_a3", "Bachelor of Science in Journalism and Mass Communication", dmiseuApp, dmiseu, now));
    }
}
