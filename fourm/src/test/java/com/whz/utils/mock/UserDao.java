package com.whz.utils.mock;

public interface UserDao {

    public User queryUserByid(long id);

    public int update(User user);
}