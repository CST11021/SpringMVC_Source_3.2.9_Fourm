<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <base href="<%=basePath%>">

  <title>My JSP 'index.jsp' starting page</title>
  <meta http-equiv="pragma" content="no-cache">
  <meta http-equiv="cache-control" content="no-cache">
  <meta http-equiv="expires" content="0">
  <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
  <meta http-equiv="description" content="This is my page">

  <%--作为独立组件引入时jQuery要在比layer.js先引入，jQuery要求1.8以上--%>
  <link rel="stylesheet" type="text/css" href="<c:url value="/static/components/layer/skin/default/layer.css"/>"/>
  <script type="text/javascript" src="<c:url value='/static/js/jquery-1.9.1.min.js'/>"></script>
  <script type="text/javascript" src="<c:url value="/static/components/layer/layer.js"/>"></script>

  <%--引入layui模块化组件--%>
  <%--<link rel="stylesheet" type="text/css" href="<c:url value="/components/layui/css/layui.css"/>"/>--%>
  <%--<script type="text/javascript" src="<c:url value='/js/jquery-1.9.1.min.js'/>"></script>--%>
  <%--<script type="text/javascript" src="<c:url value="/components/layui/layui.js"/>"></script>--%>


  <script>
    //如果作为独立组件引入layer.js,可以直接使用layer全局变量
    //		    var layer;
    //		    layui.use('layer', function(){
    //		      layer = layui.layer;
    //		    });

    $(function(){
      $('#T').on('click', function(){
        layer.open({
          type: 0,
          area: ['600px', '360px'],
          shadeClose: true, //点击遮罩关闭
          content: '\<\div style="padding:20px;">自定义内容\<\/div>'
        });
      });
      $('#T1').on('click', function(){
        layer.alert(layer.v);
      });
      $('#T2').on('click', function(){
        layer.msg(layer.v);
      });
      $('#T3').on('click', function(){
        layer.open({
          type: 1,
          area: ['600px', '360px'],
          shadeClose: true, //点击遮罩关闭
          content: '\<\div style="padding:20px;">自定义内容\<\/div>'
        });
      });
      $('#T4').on('click', function(){
        var ii = layer.load();
        //此处用setTimeout演示ajax的回调
        setTimeout(function(){
          layer.close(ii);
        }, 1000);
      });
      $('#T5').on('click', function(){
        layer.tips('Hello tips!', '#T5');
      });
    });

  </script>
</head>

<body>
<a href="javascript:;" id="T">测试:layer</a><br/>
<a href="javascript:;" id="T1">测试:layer.alert</a><br/>
<a href="javascript:;" id="T2">测试:layer.msg</a><br/>
<a href="javascript:;" id="T3">测试:layer.open</a><br/>
<a href="javascript:;" id="T4">测试:layer.load</a><br/>
<a href="javascript:;" id="T5">测试:layer.tips</a><br/>
</body>
</html>
