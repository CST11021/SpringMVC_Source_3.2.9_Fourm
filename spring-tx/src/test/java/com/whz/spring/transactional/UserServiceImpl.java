package com.whz.spring.transactional;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by wb-whz291815 on 2017/8/15.
 */
public class UserServiceImpl implements UserService {

    private JdbcTemplate jdbcTemlate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemlate = new JdbcTemplate(dataSource);
    }

    @Override
    public void saveWithoutTransaction(User user) throws Exception {
        jdbcTemlate.update("insert into user(name,age,sex) values(?,?,?)",
                new Object[] {user.getName(), user.getAge(), user.getSex()},
                new int[] {java.sql.Types.VARCHAR,java.sql.Types.INTEGER,java.sql.Types.VARCHAR});

        // 事务测试，加上这句代码则数据不会保存到数据库中，因为事务会回滚
        throw new RuntimeException("事务测试...");

        // 默认情况下Spring中的事务处理只对RuntimeException方法进行回滚，所以，如果此处将RuntimeException替换成普通的Exception不会产生回滚效果
        //throw new Exception("事务测试...");

    }

    @Override
    public void saveByConfg1(User user) throws Exception {
        jdbcTemlate.update("insert into user(name,age,sex) values(?,?,?)",
                new Object[] {user.getName(), user.getAge(), user.getSex()},
                new int[] {java.sql.Types.VARCHAR,java.sql.Types.INTEGER,java.sql.Types.VARCHAR});
        throw new RuntimeException("事务测试...");
    }

    @Override
    public void saveByConfg2(User user) throws Exception {
        jdbcTemlate.update("insert into user(name,age,sex) values(?,?,?)",
                new Object[] {user.getName(), user.getAge(), user.getSex()},
                new int[] {java.sql.Types.VARCHAR,java.sql.Types.INTEGER,java.sql.Types.VARCHAR});
        throw new RuntimeException("事务测试...");
    }

    /**
     * 关于@Transactional注解一些注意事项：
     *
     * 1、不要在接口上声明@Transactional，而要再具体类的方法上使用@Transactional注解，否则注解可能无效；
     *
     * 2、不要在@Transactional放置在类级别的声明中，放在类声明，会使得所有方法都有事务。故@Transactional应该放在方法级别，
     * 不需要使用事务的方法，就不要放置事务，比如查询方法，否则对性能是有影响的；
     *
     * 3、使用@Transactional方法，对同一个类里面的方法调用，@Transactional无效。比如有一个类Test，它的一个方法A，A再调用
     * Test本类的方法B（不管B是public还是private），但A没有声明注解事务，而B有。则外部调用A之后，B的事务是不会起作用的。（经常在这里错误）
     *
     * 4、使用@Transactional的方法，只能是public，@Transactional注解的方法都是被外部其他类调用才有效，故只能是public。
     * 故在protected、private或package-visible的方法上使用@Transactional注解也不会报错，但事务无效
     *
     * @param user
     * @throws Exception
     */
    @Override
    @Transactional(propagation= Propagation.REQUIRED)
    public void saveByAnno(User user) throws Exception {
        jdbcTemlate.update("insert into user(name,age,sex) values(?,?,?)",
                new Object[] {user.getName(), user.getAge(), user.getSex()},
                new int[] {java.sql.Types.VARCHAR,java.sql.Types.INTEGER,java.sql.Types.VARCHAR});
        throw new RuntimeException("事务测试...");
    }

}
