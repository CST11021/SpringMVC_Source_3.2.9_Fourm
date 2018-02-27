package com.whz.javabase.lock.demo;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


// 乐观锁
/*
    乐观锁这样定义，假设有很少概率出现同时两个用户更新同样的记录，在这种情况下如果万一发生，提供一种健壮的检测方式。你可
加入一个额外的列如版本号 "version"到数据表结构中，每次update-sql 语句执行时，附加条件"where version = X" 限制. 此外，
每次你更新一行记录，你需要逐个增加版本号，以表明这行记录已经更新了。这里有一小技巧：JDBC驱动包会在你使用update语句时返回
你真正进行update了多少行，但是如果有其他人在我之前更新了同样的记录呢？看看代码：
 */
public class OptimisticLockingTest {

    // 第一个人更新成功了一条记录，而第二个人使用错误的版本后进行了更新，因此无法成功。
    @Test(expected = OptimisticLockingException.class)
    public void optimistic_locking_exercise() throws SQLException {
        try (Connection conn1 = getConnection()) {
            conn1.setAutoCommit(false);

            // 插入一条数据，current_date - 100 模拟之前就存在这条数据，此时version=0
            conn1.createStatement().execute(
                    "insert into items (name, release_date, version) values ('CTU Field Agent Report', current_date() - 100, 0)");

            // 现在有人对这条数据做了一次更新，每次更新 release_date 和 version 都会改变，此时version=1
            int updatedRows = conn1.createStatement().executeUpdate(
                    "update items set release_date = current_date(), version = version + 1 where name = 'CTU Field Agent Report' and version = 0");

            System.out.println("Rows updated by Jack Bauer: " + updatedRows);
            conn1.commit();
        }

        // 同时, 另一个人试图设置release_date为今天+10，但是他试图使用版本为0的记录进行这种更新
        try (Connection conn2 = getConnection()) {
            conn2.setAutoCommit(false);
            int updatedRows = conn2.createStatement().executeUpdate(
                    "update items set release_date = current_date() + 10, version = version + 1 where name = 'CTU Field Agent Report' and version = 0");

            System.out.println("Rows updated by Habib Marwan: " + updatedRows);
            conn2.commit();

            // 这一行是很多Java框架进行乐观锁检测的方法，检测真正更新的记录数
            if (updatedRows == 0) {
                System.out.println("抛出异常...");
                throw new OptimisticLockingException();
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/test?user=root&password=123456");
    }

    public static class OptimisticLockingException extends RuntimeException {}


}