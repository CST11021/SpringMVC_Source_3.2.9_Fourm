package com.whz.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by wb-whz291815 on 2017/8/2.
 */
public class UserServiceImpl implements UserService {

    private JdbcTemplate jdbcTemlate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemlate = new JdbcTemplate(dataSource);
    }

    @Override
    public void save(User user) {
        jdbcTemlate.update("insert into user(name,age,sex) values(?,?,?)",
                new Object[] {user.getName(), user.getAge(), user.getSex()},
                new int[] {java.sql.Types.VARCHAR,java.sql.Types.INTEGER,java.sql.Types.VARCHAR});
    }

    @Override
    public List<User> getUsers() {
        List<User> list = jdbcTemlate.query("select * from user",new UserRowMapper());
        return list;
    }
}
