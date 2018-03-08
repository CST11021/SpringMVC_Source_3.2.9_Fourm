package com.whz.javabase.jdbc.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.Test;

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
    // 测试statement.execute()方法
    @Test
    public void testStatement3() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,username,password);
        Statement statement = connection.createStatement();

        statement.execute("SELECT * FROM USER");
        ResultSet resultSet = statement.getResultSet();
        while(resultSet.next()){
            System.out.println("name: " + resultSet.getString("name"));
        }

        resultSet.close();
        statement.close();
        connection.close();

    }

    /**
        这里介绍一下预编译SQL处理的知识：
            预编译语句PreparedStatement 是Java.sql中的一个接口，它是Statement的子接口。通过Statement对象执行SQL语句时，
            需要将SQL语句发送给DBMS，由DBMS首先进行编译后再执行。预编译语句和Statement不同，在创建PreparedStatement 对象
     时就指定了SQL语句，该语句立即发送给DBMS进行编译。当该编译语句被执行时，DBMS直接运行编译后的SQL语句，而不需要像其他
     SQL语句那样首先将其编译。预编译的SQL语句处理性能稍微高于普通的传递变量的办法。

        预编译语句的作用：

            1、提高效率：当需要对数据库进行数据插入、更新或者删除的时候，程序会发送整个SQL语句给数据库处理和执行。数据库
               处理一个SQL语句，需要完成解析SQL语句、检查语法和语义以及生成代码；一般说来，处理时间要比执行语句所需要的时
               间长。预编译语句在创建的时候已经是将指定的SQL语句发送给了DBMS，完成了解析、检查、编译等工作。因此，当一个
               SQL语句需要执行多次时，使用预编译语句可以减少处理时间，提高执行效率。
            2、提高安全性：防止sql注入。

     */


    // 调用 preparedStatement.executeQuery()方法执行查询操作
    @Test
    public void testPreparedStatement1() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,username,password);

        // 注意这里创建 PreparedStatement 对象的和上面创建 Statement 对象的区别，这里创建的时候就指定了sql语句，创建时该
        // 语句会立即发送给数据库进行编译，而不是等到执行的时候在编译
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
