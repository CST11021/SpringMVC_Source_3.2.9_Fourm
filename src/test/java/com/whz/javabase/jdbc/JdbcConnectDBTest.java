package com.whz.javabase.jdbc;

import org.junit.Test;

import java.sql.*;

/**
 * Created by wb-whz291815 on 2017/8/2.
 */
public class JdbcConnectDBTest {
    String driver = "com.mysql.jdbc.Driver";
    String url = "jdbc:mysql://localhost:3306/test";
    String username = "root";
    String password = "123456";
    String sql = "SELECT * FROM user";

    @Test
    public void testJDBC() throws SQLException, ClassNotFoundException {
        Class.forName(driver);
        //创建连接对象
        Connection con = DriverManager.getConnection(url,username,password);
        //创建sql执行对象
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while(rs.next()){
            System.out.println("name: " + rs.getString("name"));
        }
        //关闭相关的对象
        rs.close();
        st.close();
        con.close();
    }

}
