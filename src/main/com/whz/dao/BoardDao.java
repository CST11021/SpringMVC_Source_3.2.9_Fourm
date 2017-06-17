package com.whz.dao;

import java.util.Iterator;

import org.springframework.stereotype.Repository;
import com.whz.domain.Board;

@Repository
public class BoardDao extends com.whz.dao.BaseDao<Board> {
	protected final String GET_BOARD_NUM = "select count(f.boardId) from Board f";
	
	public long getBoardNum() {    
		Iterator iter = getHibernateTemplate().iterate(GET_BOARD_NUM);
        return ((Long)iter.next());
	}
}
