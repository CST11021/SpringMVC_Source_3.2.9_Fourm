/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An interface used by {@link JdbcTemplate} for mapping rows of a
 * {@link java.sql.ResultSet} on a per-row basis. Implementations of this
 * interface perform the actual work of mapping each row to a result object,
 * but don't need to worry about exception handling.
 * {@link java.sql.SQLException SQLExceptions} will be caught and handled
 * by the calling JdbcTemplate.
 *
 * <p>Typically used either for {@link JdbcTemplate}'s query methods
 * or for out parameters of stored procedures. RowMapper objects are
 * typically stateless and thus reusable; they are an ideal choice for
 * implementing row-mapping logic in a single place.
 *
 * <p>Alternatively, consider subclassing
 * {@link org.springframework.jdbc.object.MappingSqlQuery} from the
 * {@code com.whz.spring.jdbc.object} package: Instead of working with separate
 * JdbcTemplate and RowMapper objects, you can build executable query
 * objects (containing row-mapping logic) in that style.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see JdbcTemplate
 * @see RowCallbackHandler
 * @see ResultSetExtractor
 * @see org.springframework.jdbc.object.MappingSqlQuery
 */
// 该接口被作为一个回调接口来使用，用于映射当前结果集中的当前行
// RowMapper是ResultSetExtractor的精简版，功能类似于RowCallbackHandler，也只关注处理单行的结果。不过，处理后的结果会由ResultSetExtractor实现类进行组合。
/*

使用 RowCallbackHandler 接口：
   final List forums = new ArrayList();
   jdbcTemplate.query(sql, new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet resultSet) throws SQLException {
         Forum forum = new Forum();
         forum.setId(resultSet.getInt("id"));
         forum.setName(resultSet.getInt("name"));
         forums.add(forum); // 注意这里的区别
      }
   });

 使用 RowMapper 接口
 jdbcTemplate.query(sql, new RowMapper<Forum>() {
      @Override
      public Forum mapRow(ResultSet resultSet, int i) throws SQLException {
         Forum forum = new Forum();
         forum.setId(resultSet.getInt("id"));
         forum.setName(resultSet.getString("name"));
         return forum;// 仅针对处理当前行的逻辑
      }
   });

	使用RowCallbackHandler接口，我们需要在接口方法processRow(ResultSet rs)中手工将forum添加到List中，
	而在RowMapper<T>接口方法中，我们仅需简单定义结果行集和对象的映射关系即可。创建List<T>对象、将行数据映射对象添加到List中等操作都有JdbcTemplate代劳了
 */
public interface RowMapper<T> {

	/**
	 * Implementations must implement this method to map each row of data in the ResultSet.
	 * This method should not call {@code next()} on the ResultSet; it is only supposed to map values of the current row.
	 * @param rs the ResultSet to map (pre-initialized for the current row)
	 * @param rowNum the number of the current row
	 * @return the result object for the current row
	 * @throws SQLException if a SQLException is encountered getting
	 * column values (that is, there's no need to catch SQLException)
	 */
	// 实现这个方法来映射ResultSet中的每一行数据。这个方法不应该在ResultSet上调用next();它只需要映射当前行的值
	T mapRow(ResultSet rs, int rowNum) throws SQLException;

}
