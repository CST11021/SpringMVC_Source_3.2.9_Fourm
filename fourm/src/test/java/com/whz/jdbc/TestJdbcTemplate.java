package com.whz.jdbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.sql.*;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/com/whz/jdbc/spring-jdbc.xml"})
public class TestJdbcTemplate{
    /*
    注意：
        JdbcTemplate 中所有查询、插入、更新和删除，以及创建数据库对象，执行存储过程的方法最终都是通过调用以上四种类型的回调
    接口来实现，而这四种类型的回调是Spring基于底层JDBC API进一步封装的，它使用模板模式和接口回调的设计方法，将一些相同的数据
    访问流程代码固化到模板类中，然后将变化的部分通过回调接口开放出来，用于定义具体数据访问和结果返回的操作。这样，我们只要
    编写好回调接口，并调用模板类进行数据访问，就可以得到预想的结果
  */


    private final String SQL_QUERYUSER = "SELECT * FROM USER WHERE id=1";
    // 测试调用存储过程SQL
    private final String SQL_PRO_FINDBYID = "CALL pro_findById(1);";

    @Resource
    private JdbcTemplate jdbcTemplate;

    // 测试 ConnectionCallback 回调接口
    @Test
    public void testConnectionCallback() {
        User user = jdbcTemplate.execute(new ConnectionCallback<User>() {
            @Override
            public User doInConnection(Connection con) throws SQLException, DataAccessException {
                Statement statement = con.createStatement();
                ResultSet resultSet = statement.executeQuery(SQL_QUERYUSER);
                User u = null;
                while(resultSet.next()){
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    String sex = resultSet.getString("sex");
                    u = new User(id,name,age,sex);
                }
                return u;
            }
        });
        System.out.println(user.toString());
    }


    // 测试 StatementCallback 回调接口
    @Test
    public void testStatementCallback() {
        User user = jdbcTemplate.execute(new StatementCallback<User>() {
            @Override
            public User doInStatement(Statement stmt) throws SQLException, DataAccessException {
                ResultSet resultSet = stmt.executeQuery(SQL_QUERYUSER);
                User u = null;
                while(resultSet.next()){
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    String sex = resultSet.getString("sex");
                    u = new User(id,name,age,sex);
                }
                return u;
            }
        });
        System.out.println(user.toString());
    }


    // 测试 PreparedStatementCallback 回调接口
    @Test
    public void testPreparedStatementCallback1() {
        class PreparedStatementCreatorImpl implements PreparedStatementCreator {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                return con.prepareStatement(SQL_QUERYUSER);
            }
        }
        class PreparedStatementCallbackImpl implements PreparedStatementCallback<User> {
            @Override
            public User doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ResultSet resultSet = ps.executeQuery();
                User u = null;
                while(resultSet.next()){
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    String sex = resultSet.getString("sex");
                    u = new User(id,name,age,sex);
                }
                return u;
            }
        }
        User user = jdbcTemplate.execute(new PreparedStatementCreatorImpl() , new PreparedStatementCallbackImpl());
        System.out.println(user.toString());
    }
    @Test
    public void testPreparedStatementCallback2() {
        User user = jdbcTemplate.execute(SQL_QUERYUSER,new PreparedStatementCallback<User>() {
            @Override
            public User doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ResultSet resultSet = ps.executeQuery();
                User u = null;
                while(resultSet.next()){
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    String sex = resultSet.getString("sex");
                    u = new User(id,name,age,sex);
                }
                return u;
            }
        });
        System.out.println(user.toString());
    }


    // 测试 CallableStatementCallback 回调接口
    @Test
    public void testCallableStatementCallback1() {
        class CallableStatementCreatorImpl implements CallableStatementCreator {
            @Override
            public CallableStatement createCallableStatement(Connection con) throws SQLException {
                return con.prepareCall(SQL_PRO_FINDBYID);
            }
        }
        class CallableStatementCallbackImpl implements CallableStatementCallback<User> {
            @Override
            public User doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
                ResultSet resultSet = cs.executeQuery();
                User u = null;
                while(resultSet.next()){
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    String sex = resultSet.getString("sex");
                    u = new User(id,name,age,sex);
                }
                return u;
            }
        }
        User user = jdbcTemplate.execute(new CallableStatementCreatorImpl() , new CallableStatementCallbackImpl());
        System.out.println(user.toString());
    }
    @Test
    public void testCallableStatementCallback2() {
        class CallableStatementCallbackImpl implements CallableStatementCallback<User> {
            @Override
            public User doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
                ResultSet resultSet = cs.executeQuery();
                User u = null;
                while(resultSet.next()){
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    String sex = resultSet.getString("sex");
                    u = new User(id,name,age,sex);
                }
                return u;
            }
        }
        User user = jdbcTemplate.execute(SQL_PRO_FINDBYID,new CallableStatementCallbackImpl());
        System.out.println(user.toString());
    }


    @Test
    public void test1() {

    }

}
