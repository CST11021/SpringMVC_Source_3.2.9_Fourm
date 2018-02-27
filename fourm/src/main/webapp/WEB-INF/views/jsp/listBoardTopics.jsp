<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="baobaotao" tagdir="/WEB-INF/tags" %>
<c:set var="userInfo" value="${CURRENT_ONLINE_USER}" scope="session"/>
<c:set var="isboardManager" value="${false}" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>论坛版块页面</title>

		<link rel="stylesheet" type="text/css" href="<c:url value="/static/components/layui/css/layui.css"/>"/>
		<script type="text/javascript" src="<c:url value='/static/js/jquery-1.9.1.min.js'/>"></script>
		<script type="text/javascript" src="<c:url value="/static/components/layui/layui.js"/>"></script>
		<script type="text/javascript" src="<c:url value="/static/js/jquery.tablesorter.js"/>"></script>

		<script type="text/javascript">
			$(function() {
				$(".tablesorter").tablesorter({
					headers: {
						0: { sorter: false}
						,1: { sorter: false}
						,2: { sorter: false}
					}
				});
			});
		</script>
	</head>
	<body>

		<%@ include file="includeTop.jsp"%>

		<br/>
		<div>
			<table class="layui-table tablesorter" width="100%">
				<thead>
					<tr style="background-color: #eeeeee;">
						<c:if test="${isAdmin || isboardManager}"><th></th></c:if>
						<th>${board.boardName}</th>
						<th align="right"><a href="<c:url value="/board/addTopicPage-${board.boardId}.html"/>" style="color: #2e8ded;">发表新话题</a></th>
					</tr>
					<tr>
						<c:if test="${isAdmin || isboardManager}">
							<th>
								<input type="checkbox" onclick="switchSelectBox()"/>
							</th>
						</c:if>
						<th width="50%">标题</th>
						<th width="10%">发表人</th>
						<th width="10%" style="cursor: pointer;">回复数</th>
						<th width="15%" style="cursor: pointer;">发表时间</th>
						<th width="15%" style="cursor: pointer;">最后回复时间</th>
					</tr>
				</thead>

				<c:forEach items="${userInfo[manBoards].set}" var="manBoard">
					<c:if test="${manBoard.boardId == board.boardId}">
						<c:set var="isboardManager" value="${true}" />
					</c:if>
				</c:forEach>

				<tbody>
					<c:forEach var="topic" items="${pagedTopic.result}">
						<tr>
							<c:if test="${isAdmin || isboardManager}">
								<td><input type="checkbox" name="topicIds" value="${topic.topicId}"/></td>
							</c:if>
							<td>
								<a  href="<c:url value="/board/listTopicPosts-${topic.topicId}.html"/>">
									<c:if test="${topic.digest > 0}"><font color=red>★</font></c:if>
									${topic.topicTitle}
								</a>
							</td>
							<td>${topic.user.userName}<br><br></td>
							<td>${topic.replies}<br><br>
							</td>
							<td><fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${topic.createTime}" /></td>
							<td><fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${topic.lastPost}" /></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
			<baobaotao:PageBar pageUrl="/board/listBoardTopics-${board.boardId}.html" pageAttrKey="pagedTopic"/>
		</div>


	    <c:if test="${isAdmin || isboardManager}">
			<br/>
			<button class="layui-btn" onclick="deleteTopics()">删除</button>
			<button class="layui-btn" onclick="setDefinedTopis()">设为精华帖</button>
			<%--<input type="button" value="删除" onclick="deleteTopics()">--%>
			<%--<input type="button" value="置精华帖" onclick="setDefinedTopis()">--%>
		</c:if>

		<c:if test="${isAdmin || isboardManager}">
			<script>
				function switchSelectBox(){
					var selectBoxs = document.all("topicIds");
					if(!selectBoxs) return ;
					if(typeof(selectBoxs.length) == "undefined"){
						selectBoxs.checked = event.srcElement.checked;
					}else{
						for(var i = 0 ; i < selectBoxs.length ; i++){
							selectBoxs[i].checked = event.srcElement.checked;
						}
					}
				}
				function getSelectedTopicIds(){
					var selectBoxs = document.all("topicIds");
					if(!selectBoxs) return null;
					if(typeof(selectBoxs.length) == "undefined" && selectBoxs.checked){
						return selectBoxs.value;
					}else{//many checkbox ,so is a array
						var ids = "";
						var split = ""
						for(var i = 0 ; i < selectBoxs.length ; i++){
							if(selectBoxs[i].checked){
								ids += split+selectBoxs[i].value;
								split = ",";
							}
						}
						return ids;
					}
				}
				function deleteTopics(){
					var ids = getSelectedTopicIds();
					if(ids){
						var url = "<c:url value="/board/removeTopic.html"/>?topicIds="+ids+"&boardId=${board.boardId}";
						//alert(url);
						location.href = url;
					}
				}
				function setDefinedTopis(){
					var ids = getSelectedTopicIds();
					if(ids){
						var url = "<c:url value="/board/makeDigestTopic.html"/>?topicIds="+ids+"&boardId=${board.boardId}";
						location.href = url;
					}
				}
			</script>
		</c:if>

	</body>
</html>
