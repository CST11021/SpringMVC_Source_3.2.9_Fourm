package com.whz.domain;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *<br>
 * <b>类描述:</b>
 * 
 * <pre>
 * 主题对应的主题帖
 * </pre>
 * 
 * @see
 *@since
 */
@Entity
//@Table(name = "t_post")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
/** 通过@Inheritance注解来指定PO映射继承关系，Hibernate提供三种方式：
 * TABLE_PER_CLASS:每个类一张表
 * JOINED:连接的子类
 * SINGLE_TABLE：每个类层次结构一张表*/
@DiscriminatorColumn(name = "post_type", discriminatorType = DiscriminatorType.STRING)
/** 通过@DiscriminatorColumn 注解定义了辨别符列。对于继承层次结构中的每个类，@DiscriminatorValue注解指定了用来辨别该类的值。discriminatorType指定其类型 */
@DiscriminatorValue("1")

public class MainPost extends Post {

}
