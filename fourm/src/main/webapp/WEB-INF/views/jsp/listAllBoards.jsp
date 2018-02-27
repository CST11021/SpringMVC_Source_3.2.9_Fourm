<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<title>论坛首页</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

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
				}
			});
		});
	</script>
</head>
<body>
	<%@ include file="includeTop.jsp" %>

	<table class="layui-table tablesorter" width="100%">
		<thead>
			<tr><td colspan="3">所有论坛版块</td></tr>
			<tr bgcolor="#EEEEEE">
			   <c:if test="${isAdmin}">
				 <th>
					<script>
						function switchSelectBox(){
							var selectBoxs = document.all("boardIds");
							if(!selectBoxs) return ;
							if(typeof(selectBoxs.length) == "undefined"){
								selectBoxs.checked = event.srcElement.checked;
							}else{
							  for(var i = 0 ; i < selectBoxs.length ; i++){
								 selectBoxs[i].checked = event.srcElement.checked;
							  }
							}
						}
					</script>
					<input type="checkbox" onclick="switchSelectBox()"/>
				 </th>
			   </c:if>
				<th>版块名称</th>
				<th onclick="return;">版块简介</th>
				<th style="cursor: pointer;">主题帖数</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="board" items="${boards}">
				<tr>
					<c:if test="${isAdmin}">
						<td><input type="checkbox" name="boardIds" value="${board.boardId}"/></td>
					</c:if>
					<td><a href="<c:url value="/board/listBoardTopics-${board.boardId}.html"/>" style="color: #2e8ded;">${board.boardName}</a></td>
					<td>${board.boardDesc}</td>
					<td>${board.topicNum}</td>
				</tr>
			</c:forEach>
		</tbody>

	</table>

	<c:if test="${isAdmin || isboardManager}">
		<script>
			function getSelectedBoardIds(){
				var selectBoxs = document.all("boardIds");
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
			function deleteBoards(){
			   var ids = getSelectedBoardIds();
			   if(ids){
				  var url = "<c:url value='/board/removeBoard.html'/>?boardIds=" + ids + "";
				  alert(url);
				  location.href = url;
			   }
			}
	    </script>
		<input type="button" value="删除" onclick="deleteBoards()">
		<a href="<c:url value="/test.html"/>" >test</a>
		<a href="<c:url value="/board/listBoardTopics-111.html"/>" >test111</a>
	</c:if>


</body>
</html>
