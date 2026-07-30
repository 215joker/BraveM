package com.bravem.app.data.local.source;

import com.bravem.app.data.local.UserDao;
import com.bravem.app.model.User;

import java.util.List;

public class UserLocalDataSource {

    private final UserDao userDao;

    public UserLocalDataSource(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUser(String uid) {
        return userDao.getByUid(uid);
    }

    public void saveUser(User user) {
        userDao.insert(user);
    }

    public void deleteUser(User user) {
        userDao.delete(user);
    }

    public List<User> getAllUsers() {
        return userDao.getAll();
    }
}
