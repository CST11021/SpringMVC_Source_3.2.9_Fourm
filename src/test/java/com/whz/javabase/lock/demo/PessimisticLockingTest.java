package com.whz.javabase.lock.demo;

import org.h2.jdbc.JdbcSQLException;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.fail;


// 悲观锁
/*
    当我们使用悲观锁更新或删除一行数据表记录时，数据库会锁住整个一行，排他性地用于读操作和写操作，这能够确保这一行记录不
会同时被其它用户锁住。下面是悲观锁实现代码：
 */
public class PessimisticLockingTest {

    @Test(expected = JdbcSQLException.class)
    public void pessimistic_locking_exercise() throws SQLException {
        // 首先,插入一行记录，current_date - 100 模拟之前就存在的这样一条数据
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            conn.createStatement().execute(
                    "insert into items (name, release_date) values ('CTU Field Agent Report' , current_date() - 100)");
            conn.commit();
        }

        // 第一个人使用这条数据时，对该记录使用了悲观锁，在使用期间，不让其他人修改这个记录
        try (Connection conn1 = getConnection()) {
            conn1.setAutoCommit(false);
            conn1.createStatement().execute(
                    "select * from items where name = 'CTU Field Agent Report' for update");
            System.out.println("锁住这条记录，不让其他人对该记录进行更新");


            // 插入记录后，再修改它，注意上面的SQL语句，使用了"select..for update" SQL语句来排他性锁住了整个记录，直到他
            // 提交了语句也就是释放了这段事务的锁，其他用户才能读取这行曾经被锁住过的记录。在锁住期间，任何其他用户都进入
            // 等待状态，等待该条记录被释放，这就是悲观锁。
            int updatedRows = 0;
            try (Connection conn2 = getConnection()) {
                conn2.setAutoCommit(false);
                updatedRows = conn2.createStatement().executeUpdate(
                        "update items set release_date = current_date() + 10 where name = 'CTU Field Agent Report'");
            }

            // 上面的更新操作会阻塞，程序无法执行到这里
            System.out.println("更新了"+updatedRows+"条数据");

        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/test?user=root&password=123456");
    }


}