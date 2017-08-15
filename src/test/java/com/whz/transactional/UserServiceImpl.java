package com.whz.transactional;

import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

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

    @Override
    public void saveByAnno(User user) throws Exception {
        jdbcTemlate.update("insert into user(name,age,sex) values(?,?,?)",
                new Object[] {user.getName(), user.getAge(), user.getSex()},
                new int[] {java.sql.Types.VARCHAR,java.sql.Types.INTEGER,java.sql.Types.VARCHAR});
        throw new RuntimeException("事务测试...");
    }

}
