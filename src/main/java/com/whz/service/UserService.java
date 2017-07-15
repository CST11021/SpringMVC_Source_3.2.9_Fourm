package com.whz.service;

import com.whz.dao.LoginLogDao;
import com.whz.dao.UserDao;
import com.whz.domain.LoginLog;
import com.whz.domain.User;
import com.whz.exception.UserExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 用户管理服务器，负责查询用户、注册用户、锁定用户等操作
 *
 */
@Service
public class UserService {
	@Autowired
	private UserDao userDao;
	@Autowired
	private LoginLogDao loginLogDao;

	// 注册一个新用户,如果用户名已经存在此抛出UserExistException的异常
	public void register(User user) throws UserExistException{
		User u = this.getUserByUserName(user.getUserName());
		if(u != null){
		    throw new UserExistException("用户名已经存在");
		}else{
		    user.setCredit(100);
            user.setUserType((byte) 1);
            userDao.saveEntity(user);
		}
	}

	// 更新用户
    public void update(User user){
        userDao.updateEntity(user);
    }

	// 根据用户名查询 User对象
    public User getUserByUserName(String userName){
        return userDao.getUserByUserName(userName);
    }

	// 根据userId加载User对象
	public User getUserById(int userId){
		return userDao.getById(userId);
	}

	// 将用户锁定，锁定的用户不能够登录
	// @param userName 锁定目标用户的用户名
	public void lockUser(String userName){
		User user = userDao.getUserByUserName(userName);
		user.setLocked((byte) User.USER_LOCK);
	    userDao.updateEntity(user);
	}

	// 解除用户的锁定
	// @param userName 解除锁定目标用户的用户名
	public void unlockUser(String userName){
		User user = userDao.getUserByUserName(userName);
		user.setLocked((byte) User.USER_UNLOCK);
		userDao.updateEntity(user);
	}

	// 根据用户名为条件，执行模糊查询操作
	// @param userName 查询用户名
	// @return 所有用户名前导匹配的userName的用户
	public List<User> queryUserByUserName(String userName){
		return userDao.queryUserByUserName(userName);
	}
	
	// 获取所有用户
	public List<User> getAllUsers(){
		return userDao.loadAllEntity();
	}

	// 登陆成功
	public void loginSuccess(User user) {
		user.setCredit( 5 + user.getCredit());
		LoginLog loginLog = new LoginLog();
		loginLog.setUser(user);
		loginLog.setIp(user.getLastIp());
		loginLog.setLoginDate(new Date());
        userDao.updateEntity(user);
        loginLogDao.saveEntity(loginLog);
	}	
	
}
