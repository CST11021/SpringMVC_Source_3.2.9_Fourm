package com.whz.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "t_topic")
public class Topic extends BaseDomain {

	public static final int DIGEST_TOPIC = 1;/*精华主题帖子*/
	public static final int NOT_DIGEST_TOPIC = 0;/*普通的主题帖子*/

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "topic_id")
	private int topicId;/*主键*/

	@Column(name = "topic_title")
	private String topicTitle;/*标题*/

	@ManyToOne
	@JoinColumn(name = "user_id")//@JoinColumn 注释的是另一个表(User对象所指向的表)指向本表的外键
	private User user;/*发帖用户*/

	@Column(name = "board_id")
	private int boardId;/*所属板块编号*/

	@Column(name = "last_post")
	private Date lastPost = new Date();/*最后回复时间*/

	@Column(name = "create_time")
	private Date createTime = new Date();/*创建时间*/

	@Column(name = "topic_views")
	private int views;/*浏览数*/

	@Column(name = "topic_replies")
	private int replies;/*帖子回复数*/

	private int digest = NOT_DIGEST_TOPIC;/*是否为精华话题*/
	@Transient//@Transient表示该属性并非一个到数据库表的字段的映射,ORM框架将忽略该属性.
	private MainPost mainPost = new MainPost();

	/*getter and setter...*/
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public int getDigest() {
		return digest;
	}
	public void setDigest(int digest) {
		this.digest = digest;
	}
	public int getBoardId() {
		return boardId;
	}
	public void setBoardId(int boardId) {
		this.boardId = boardId;
	}
	public Date getLastPost() {
		return lastPost;
	}
	public void setLastPost(Date lastPost) {
		this.lastPost = lastPost;
	}
	public int getReplies() {
		return replies;
	}
	public void setReplies(int replies) {
		this.replies = replies;
	}
	public int getTopicId() {
		return topicId;
	}
	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}
	public String getTopicTitle() {
		return topicTitle;
	}
	public void setTopicTitle(String topicTitle) {
		this.topicTitle = topicTitle;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public int getViews() {
		return views;
	}
	public void setViews(int views) {
		this.views = views;
	}
	public MainPost getMainPost() {
		return mainPost;
	}
	public void setMainPost(MainPost mainPost) {
		this.mainPost = mainPost;
	}

	// public Set<MainPost> getMainPosts()
	// {
	// return mainPosts;
	// }
	//
	// public void setMainPosts(Set<MainPost> mainPosts)
	// {
	// this.mainPosts = mainPosts;
	// }

}
