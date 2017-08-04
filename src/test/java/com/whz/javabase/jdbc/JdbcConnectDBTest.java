package com.whz.javabase.jdbc;

import org.junit.Test;

import java.sql.*;
import java.util.Properties;

/**
 * Created by wb-whz291815 on 2017/8/2.
 */
public class JdbcConnectDBTest {
    String driver = "com.mysql.jdbc.Driver";
    String url = "jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8";
    String username = "root";
    String password = "123456";


    @Test
    public void introduceJDBC() throws SQLException, ClassNotFoundException {
        // 根据JDBC驱动名加载对应的数据库驱动
        Class.forName(driver);
        // 加载完驱动后，就可以使用驱动管理器创建Connection对象了
        Connection connection = DriverManager.getConnection(url,username,password);
        // 创建sql执行对象
        Statement statement = connection.createStatement();

        // 将SQL语句提交到数据库,并且返回执行结果
        ResultSet resultSet = statement.executeQuery("SELECT * FROM USER");
        while(resultSet.next()){
            System.out.println("name: " + resultSet.getString("name"));
        }

        //关闭相关的对象
        resultSet.close();
        statement.close();
        connection.close();

    }


    // 测试：DriverManager.getConnection(url,username,password)
    @Test
    public void testConnection1() throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,username,password);
        connection.close();
    }
    // 测试： DriverManager.getConnection(url, info)
    @Test
    public void testConnection2() throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        Properties info = new Properties();
        info.put( "user", username);
        info.put( "password", password);
        Connection connection = DriverManager.getConnection(url, info);
        connection.close();
    }
    // 测试：DriverManager.getConnection("jdbc:mysql://localhost:3306/test?user=root&password=123456")
    @Test
    public void testConnection3() throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?user=root&password=123456");
        connection.close();
    }



    // 测试statement.executeQuery()方法，
    @Test
    public void testStatement1() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,username,password);
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM USER");
        while(resultSet.next()){
            System.out.println("name: " + resultSet.getString("name"));
        }

        resultSet.close();
        statement.close();
        connection.close();

    }
    // 测试statement.executeUpdate()方法，
    @Test
    public void testStatement2() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,username,password);
        Statement statement = connection.createStatement();

        int count = statement.executeUpdate("CREATE TABLE student(sid INT PRIMARY KEY,sname VARCHAR(20),age INT)");
        System.out.println("影响了" + count + "条记录");

        statement.close();
        connection.close();
    }



    // 调用 preparedStatement.executeQuery()方法执行查询操作
    @Test
    public void testPreparedStatement1() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,username,password);

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM USER WHERE age=? AND sex=?");
        preparedStatement.setInt(1, 20);
        preparedStatement.setString(2, "男");
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            System.out.println("name: " + resultSet.getString("name"));
        }

        //关闭相关的对象
        resultSet.close();
        preparedStatement.close();
        connection.close();

    }
    // 调用 preparedStatement.executeUpdate() 方法执行插入操作
    @Test
    public void testPreparedStatement2() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,username,password);

        PreparedStatement preparedStatement = connection.prepareStatement("insert into user(name,age,sex) values(?,?,?)");
        preparedStatement.setString(1,"李四");
        preparedStatement.setInt(2, 20);
        preparedStatement.setString(3, "男");
        int num = preparedStatement.executeUpdate();
        System.out.println(num + "条记录受到了影响");

        //关闭相关的对象
        preparedStatement.close();
        connection.close();

    }



    // 测试 CallableStatement 执行调用的存储过程
    @Test
    public void test() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,username,password);

        CallableStatement callableStatement = connection.prepareCall("CALL pro_findById(1);");
        ResultSet resultSet = callableStatement.executeQuery();
        while(resultSet.next()){
            System.out.println("name: " + resultSet.getString("name"));
        }

        resultSet.close();
        callableStatement.close();
        connection.close();

    }



    /**
        参数 int type
        ResultSet.TYPE_FORWORD_ONLY 结果集的游标只能向下滚动。
        ResultSet.TYPE_SCROLL_INSENSITIVE 结果集的游标可以上下移动，当数据库变化时，当前结果集不变。
        ResultSet.TYPE_SCROLL_SENSITIVE 返回可滚动的结果集，当数据库变化时，当前结果集同步改变。

        参数 int concurrency
        ResultSet.CONCUR_READ_ONLY 不能用结果集更新数据库中的表。
        ResultSet.CONCUR_UPDATETABLE 能用结果集更新数据库中的表。

        resultSetHoldability 参数：表示在结果集提交后结果集是否打开，取值有两个：
        ResultSet.HOLD_CURSORS_OVER_COMMIT：表示修改提交时ResultSet不关闭
        ResultSet.CLOSE_CURSORS_AT_COMMIT：表示修改提交时ResultSet关闭
     */
    @Test
    public void testResultSet() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,username,password);

        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

        ResultSet resultSet = statement.executeQuery("SELECT * FROM USER");
        while(resultSet.next()){
            System.out.println("name: " + resultSet.getString("name"));
        }

        resultSet.close();
        statement.close();
        connection.close();

    }


}
