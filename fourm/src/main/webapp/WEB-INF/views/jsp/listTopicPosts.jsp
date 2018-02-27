<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="baobaotao" tagdir="/WEB-INF/tags" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<title>${topic.topicTitle}</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

	<link rel="stylesheet" type="text/css" href="<c:url value="/static/components/layui/css/layui.css"/>"/>
	<script type="text/javascript" src="<c:url value='/static/js/jquery-1.9.1.min.js'/>"></script>
	<script type="text/javascript" src="<c:url value="/static/components/layui/layui.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/static/js/jquery.tablesorter.js"/>"></script>

</head>
<body>
	<%@ include file="includeTop.jsp" %>

	<div style="float:right;"><a href="#replyZone">回复</a></div>

	<table class="layui-table" style="border: none;" width="100%">
		<c:forEach var="post" items="${pagedPost.result}">
			<tr style="background-color: beige;"><td colspan="2">${post.postTitle}</td></tr>
			<tr style="background-color: #5FB878"><td colspan="10">${post.postText}</td></tr>
			<tr style="background-color: #5FB878">
				<td width="180px">用户：${post.user.userName}</td>
				<td width="80px">积分：${post.user.credit}</td>
				<td width="">时间：<fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${post.createTime}"/></td>
			</tr>
		</c:forEach>
	</table>

	<baobaotao:PageBar pageUrl="/board/listTopicPosts-${topicId}.html" pageAttrKey="pagedPost"/>

	<form class="layui-form" action="<c:url value="/board/addPost.html"/>" method="post">
		<i class="layui-icon" style="font-size: 30px; color: #1E9FFF;">回复&#xe63a;</i>

		<div class="layui-form-item">
			<label class="layui-form-label">标&nbsp;题</label>
			<div class="layui-input-block">
				<input type="text" name="postTitle" required  lay-verify="required" placeholder="请输入标题" autocomplete="off" class="layui-input">
			</div>
		</div>

		<div class="layui-form-item layui-form-text">
			<label class="layui-form-label">文本域</label>
			<div class="layui-input-block">
				<textarea id="textArea" name="postText" placeholder="请输入内容" class="layui-textarea"></textarea>
			</div>
		</div>

		<div class="layui-form-item">
			<div class="layui-input-block">
				<button type="submit" class="layui-btn" lay-submit lay-filter="formDemo">立即提交</button>
				<button type="reset" class="layui-btn layui-btn-primary">重置</button>
				<input type="hidden" name="boardId" value="${topic.boardId}"/>
				<input type="hidden" name="topic.topicId" value="${topic.topicId}"/>
			</div>
		</div>
	</form>

	<script>
		layui.use('layedit', function(){
			var layedit = layui.layedit;
			layedit.build('textArea'); //建立编辑器
		});

		layui.use('form', function(){
			var form = layui.form();

			//监听提交
			form.on('submit(formDemo)', function(data){
//				layer.msg(JSON.stringify(data.field));
				mySubmit();
			});
		});


		function mySubmit(){
			with(document){
				var postTitle = getElementById("post.postTitle");
				if(postTitle.value != null && postTitle.value.length > 50){
					layer.msg("帖子标题最大长度不能超过50个字符，请调整.");
					postTitle.focus();
					return false;
				}
				var postText = getElementById("post.postText");
				if(postText.value == null || postText.value.length < 10){
					layer.msg("回复帖子内容不能小于10个字符。");
					postText.focus();
					return false;
				}
				return true;
			}
		}
	</script>

</body>
</html>

