package com.whz.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DAO基类，其它DAO可以直接继承这个DAO，不但可以复用共用的方法，还可以获得泛型的好处。
 * 说明：BaseDao<T>使用了JDK 5.0的泛型技术，这是为了让子类可以使用泛型的技术绑定特定类型的PO类，避免强制类型转换带来的麻烦，通过扩展这个基类，
 * 子Dao类仅需声明泛型对应的PO类并实现那些非通用性的方法即可，大大减少子Dao类的代码量。早期开发者通过扩展JdbcDaoSupport定义自己的Dao(JdbcDaoSupport
 * 内部定义了JdbcTemplate的成员变量)，但随着Bean的注解配置逐渐成为主流配置方式，这种做法就显得不合时宜了，因为直接继承JdbcDaoSupport无法对
 * JdbcTemplate成员变量用@Autowired注解，所以我们推荐的做法是你自己定义一个BaseDao，在BaseDao中定义JdbcTemplate成员变量，并使用@Autowired注解。
 *
 */
public class BaseDao<T>{
	// DAO的泛型类，即子类所指定的T所对应的类型
	private Class<T> entityClass;
	@Autowired
	private HibernateTemplate hibernateTemplate;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	// 通过反射获取子类确定的泛型类
	public BaseDao() {
		Class clazz = this.getClass();
		Type genType = clazz.getGenericSuperclass();//获得带有泛型的父类,Type是 Java 编程语言中所有类型的公共高级接口。它们包括原始类型、参数化类型、数组类型、类型变量和基本类型。
		ParameterizedType parameterizedType = (ParameterizedType)genType;//ParameterizedType参数化类型，即泛型
		Type[] params = parameterizedType.getActualTypeArguments();//getActualTypeArguments获取参数化类型的数组，泛型可能有多个
		entityClass = (Class) params[0];
	}

	public Session getSession() {
		return SessionFactoryUtils.getSession(hibernateTemplate.getSessionFactory(),true);
	}

	// 根据ID加载PO实例 返回相应的持久化PO实例
	public T loadById(Serializable id) {
		return (T) getHibernateTemplate().load(entityClass, id);
	}
	// 根据ID获取PO实例
	public T getById(Serializable id) {
		return (T) getHibernateTemplate().get(entityClass, id);
	}
	// 获取PO的所有对象
	public List<T> loadAllEntity() {
		return getHibernateTemplate().loadAll(entityClass);
	}
	// 保存PO
	public void saveEntity(T entity) {
		getHibernateTemplate().save(entity);
	}
	// 删除PO
	public void removeEntity(T entity) {
		getHibernateTemplate().delete(entity);
	}
	// 更改PO
	public void updateEntity(T entity) {
		getHibernateTemplate().update(entity);
	}
	// 执行HQL查询
	public List findByHql(String hql) {
		return this.getHibernateTemplate().find(hql);
	}
	// 执行带参的HQL查询 */
	public List findByHql(String hql, Object... params) {
		return this.getHibernateTemplate().find(hql,params);
	}
	// 对延迟加载的实体PO执行初始化 */
	public void initializeEntity(Object entity) {
		this.getHibernateTemplate().initialize(entity);
	}


	// 分页查询函数，使用hql.移除orderBy @param pageNo 页号,从1开始.
	public com.whz.dao.Page pagedQuery(String hql, int pageNo, int pageSize, Object... values) {
		Assert.hasText(hql);
		Assert.isTrue(pageNo >= 1, "pageNo should start from 1");
		// Count查询
		String countQueryString = " select count (*) " + removeWithSelectStart(removeOrderbyClause(hql));
		List countlist = getHibernateTemplate().find(countQueryString, values);
		long totalCount = (Long) countlist.get(0);

		if (totalCount < 1)
			return new com.whz.dao.Page();
		// 实际查询返回分页对象
		int startIndex = com.whz.dao.Page.getStartOfPage(pageNo, pageSize);
		Query query = createQuery(hql, values);
		query.setFirstResult(startIndex).setMaxResults(pageSize);
		List list = query.list();

		return new Page(startIndex, totalCount, pageSize, list);
	}
	// 去除hql的select 子句，未考虑union的情况
	private static String removeWithSelectStart(String hql) {
		Assert.hasText(hql);
		int beginPos = hql.toLowerCase().indexOf("from");
		Assert.isTrue(beginPos != -1, " hql : " + hql + " must has a keyword 'from'");
		return hql.substring(beginPos);
	}
	// 去除hql的orderby 子句
	private static String removeOrderbyClause(String hql) {
		Assert.hasText(hql);
		Pattern p = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(hql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		return sb.toString();
	}
	/**
	 * 创建Query对象. 对于需要first,max,fetchsize,cache,cacheRegion等诸多设置的函数,可以在返回Query后自行设置.
	 * 留意可以连续设置,如下：
	 * <pre>
	 * dao.getQuery(hql).setMaxResult(100).setCacheable(true).list();
	 * </pre>
	 * 调用方式如下：
	 * <pre>
	 *        dao.createQuery(hql)
	 *        dao.createQuery(hql,arg0);
	 *        dao.createQuery(hql,arg0,arg1);
	 *        dao.createQuery(hql,new Object[arg0,arg1,arg2])
	 * </pre>
	 *
	 * @param values 可变参数.
	 */
	public Query createQuery(String hql, Object... values) {
		Assert.hasText(hql);
		Query query = getSession().createQuery(hql);
		for (int i = 0; i < values.length; i++) {
			query.setParameter(i, values[i]);
		}
		return query;
	}

	//getter and setter ...
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	
}