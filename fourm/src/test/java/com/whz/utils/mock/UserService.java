package com.whz.utils.mock;

public class UserService {

    private UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean update(long id, String name){

        User user = userDao.queryUserByid(id);
        if(user==null){
            return false;
        }
        user.setName(name);
        userDao.update(user);
        return true;
    }
}