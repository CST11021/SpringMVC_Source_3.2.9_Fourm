package com.whz.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "t_board")
public class Board extends BaseDomain {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "board_id")
	private int boardId;/*论坛板块主键*/

	@Column(name = "board_name")
	private String boardName;/*板块名称*/

	@Column(name = "board_desc")
	private String boardDesc;/*板块描述*/

	@Column(name = "topic_num")
	private int topicNum ;/*帖子数据*/
	
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "manBoards", fetch = FetchType.LAZY)
	/*分别是CascadeType.PERSIST(级联新建)，CascadeType.REMOVE（级联删除），CascadeType.REFRESH（级联刷新），CascadeType.MERGE（级联更新），CascadeType.ALL（全部四项）*/

	private Set<User> users = new HashSet<User>();/**/

	/*getter and setter...*/
	public int getTopicNum() {
		return topicNum;
	}
	public void setTopicNum(int topicNum) {
		this.topicNum = topicNum;
	}
	public String getBoardDesc() {
		return boardDesc;
	}
	public void setBoardDesc(String boardDesc) {
		this.boardDesc = boardDesc;
	}
	public int getBoardId() {
		return boardId;
	}
	public void setBoardId(int boardId) {
		this.boardId = boardId;
	}
	public String getBoardName() {
		return boardName;
	}
	public void setBoardName(String boardName) {
		this.boardName = boardName;
	}
	public Set<User> getUsers() {
		return users;
	}
	public void setUsers(Set<User> users) {
		this.users = users;
	}

    

}
