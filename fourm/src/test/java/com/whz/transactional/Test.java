package com.whz.transactional;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.*;

public class Test {

    User zhangsanUser = new User("张三",20,"男");

    // 不使用事务管理
    @org.junit.Test
    public void testSaveWithoutTransaction() throws Exception {
        ApplicationContext app = new ClassPathXmlApplicationContext("/com/whz/transactional/spring-dataSource.xml");
        UserService userService = (UserService) app.getBean("userService");
        // 执行出错了，但是数据仍然会保持到数据库中
        userService.saveWithoutTransaction(zhangsanUser);
    }


    // 使用JDBC最原始的事务操作
    @org.junit.Test
    public void test() throws ClassNotFoundException{
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8";
        String username = "root";
        String password = "123456";

        Class.forName(driver);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(false);// JDBC默认是自动提交

            statement = connection.createStatement();
            statement.executeUpdate("insert into user(name,age,sex) values(\'张三\',20,\'男\')");
            if(true) throw new RuntimeException("抛出异常，测试事务回滚");

            connection.commit();
        } catch(Exception e) {
            try{
                connection.rollback();
                System.out.println("事务回滚了");
            }catch(SQLException sqlex){
                sqlex.printStackTrace();
            }
            e.printStackTrace();
        }finally{
            try{
                statement.close();
                connection.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }


    /*----------------------------------- Spring 编程式事务实现 -----------------------------------------------*/

    // 使用Spring的编程式事务操作：使用 DefaultTransactionDefinition 实现
    @org.junit.Test
    public void testSaveByDefaultTransactionDefinition() throws Exception {

        ApplicationContext app = new ClassPathXmlApplicationContext("/com/whz/transactional/spring-dataSource.xml");
        UserService userService = (UserService) app.getBean("userService");
        DataSource datasource = (DataSource) app.getBean("dataSource");

        PlatformTransactionManager tran = new DataSourceTransactionManager(datasource);// 事务管理器
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();// 事务定义类
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);// 设置事务传播级别
        TransactionStatus status = tran.getTransaction(def);// 返回事务对象
        try {
            userService.saveWithoutTransaction(zhangsanUser);
            tran.commit(status);
        } catch (Exception ex) {
            tran.rollback(status);
            System.out.println("出错了，事务回滚...");
        }

    }
    // 使用Spring的编程式事务操作：使用 TransactionTemplate 实现
    @org.junit.Test
    public void testSaveByTransactionTemplate() {
        ApplicationContext app = new ClassPathXmlApplicationContext("/com/whz/transactional/spring-dataSource.xml");
        UserService userService = (UserService) app.getBean("userService");
        DataSource datasource = (DataSource) app.getBean("dataSource");

        PlatformTransactionManager tran = new DataSourceTransactionManager(datasource);
        TransactionTemplate trantemplate = new TransactionTemplate(tran);
        trantemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                try {
                    userService.saveWithoutTransaction(zhangsanUser);
                    throw new RuntimeException("抛出异常，测试事务回滚");
                } catch (Exception ex) {
                    System.out.println("出错了，事务回滚...");
                    ex.printStackTrace();
                    status.setRollbackOnly();
                }
                return null;
            }
        });
    }



    /*----------------------------------- Spring 声明式事务实现 -----------------------------------------------*/

    // 使用Spring 注解方式的声明式事务操作：将事务相关的配置交由Spring配置
    @org.junit.Test
    public void testSaveBySpringTransaction() throws Exception {
        // 将事务的模板类代码抽到出来交由Spring配置实现
        ApplicationContext app = new ClassPathXmlApplicationContext("/com/whz/transactional/spring-annoTransaction.xml");
        UserService userService = (UserService) app.getBean("userService");
        userService.saveByAnno(zhangsanUser);
    }
    // 使用Spring早期的事务配置方式
    @org.junit.Test
    public void testSaveBySpringTransaction1() throws Exception {
        // 将事务的模板类代码抽到出来交由Spring配置实现
        ApplicationContext app = new ClassPathXmlApplicationContext("/com/whz/transactional/spring-xmlTransaction.xml");
        UserService userService = (UserService) app.getBean("userServiceTarget");
        userService.saveByConfg1(zhangsanUser);
    }
    @org.junit.Test
    public void testSaveBySpringTransaction2() throws Exception {
        // 将事务的模板类代码抽到出来交由Spring配置实现
        ApplicationContext app = new ClassPathXmlApplicationContext("/com/whz/transactional/spring-xmlTransaction.xml");
        UserService userService = (UserService) app.getBean("userServiceTarget");
        userService.saveByConfg2(zhangsanUser);
    }

}
