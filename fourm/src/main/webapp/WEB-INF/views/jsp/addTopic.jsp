<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<title>发表新话题</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

	<link rel="stylesheet" type="text/css" href="<c:url value="/static/components/layui/css/layui.css"/>"/>
	<script type="text/javascript" src="<c:url value='/static/js/jquery-1.9.1.min.js'/>"></script>
	<script type="text/javascript" src="<c:url value="/static/components/layui/layui.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/static/js/jquery.tablesorter.js"/>"></script>

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
				var topicTitle = getElementById("topicTitle");
				if(topicTitle.value == null || topicTitle.value.length ==0){
					alert("话题标题不能为空，请填上.");
					topicTitle.focus();
					return false;
				}else if(topicTitle.value.length > 100){
					alert("话题标题最大长度不能超过100个字符，请调整.");
					boardName.focus();
					return false;
				}

				var postText = getElementById("mainPost.postText");
				if(postText.value == null || postText.value.length < 10 ){
					alert("话题的内容必须大于10个字符");
					postText.focus();
					return false;
				}

				return true;
			}

		}
	</script>
</head>
<body>
	<%@ include file="includeTop.jsp" %>

	<form class="layui-form" action="<c:url value="/board/addTopic.html"/>" method="post">
		<div class="layui-form-item">
			<div class="layui-input-block">
				<input type="text" name="topicTitle" required  lay-verify="required" placeholder="请输入标题" autocomplete="off" class="layui-input">
			</div>
		</div>

		<div class="layui-form-item layui-form-text">
			<div class="layui-input-block">
				<textarea id="textArea" name="mainPost.postText" placeholder="请输入内容" class="layui-textarea"></textarea>
			</div>
		</div>

		<div class="layui-form-item">
			<div class="layui-input-block">
				<button type="submit" class="layui-btn" lay-submit lay-filter="formDemo">立即提交</button>
				<button type="reset" class="layui-btn layui-btn-primary">重置</button>
				<input type="hidden" name="boardId" value="${boardId}">
			</div>
		</div>
	</form>

</body>
</html>
