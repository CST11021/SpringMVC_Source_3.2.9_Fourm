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

  <link rel="stylesheet" type="text/css" href="<c:url value="/static/components/layui/css/layui.css"/>"/>
  <script type="text/javascript" src="<c:url value='/static/js/jquery-1.9.1.min.js'/>"></script>
  <script type="text/javascript" src="<c:url value="/static/components/layui/layui.js"/>"></script>

  <script>
    layui.use(['laypage', 'layer'], function(){
      var laypage = layui.laypage
              ,layer = layui.layer;

      laypage({
        cont: 'demo1'
        ,pages: 100 //总页数
        ,groups: 5 //连续显示分页数
      });

      laypage({
        cont: 'demo2'
        ,pages: 100
        ,skin: '#1E9FFF'
      });

      laypage({
        cont: 'demo3'
        ,pages: 100
        ,first: 1
        ,last: 100
        ,prev: '<em><</em>'
        ,next: '<em>></em>'
      });

      laypage({
        cont: 'demo4'
        ,pages: 100
        ,first: false
        ,last: false
      });

      laypage({
        cont: 'demo5'
        ,pages: 100
        ,curr: location.hash.replace('#!fenye=', '') //获取hash值为fenye的当前页
        ,hash: 'fenye' //自定义hash值
      });

      laypage({
        cont: 'demo6'
        ,pages: 5
        ,groups: 0
        ,first: false
        ,last: false
        ,jump: function(obj, first){
          if(!first){
            layer.msg('第 '+ obj.curr +' 页');
          }
        }
      });

      laypage({
        cont: 'demo7'
        ,pages: 100
        ,skip: true
      });


      //将一段数组分页展示

      //测试数据
      var data = [
        '北京',
        '上海',
        '广州',
        '深圳',
        '杭州',
        '长沙',
        '合肥',
        '宁夏',
        '成都',
        '西安',
        '南昌',
        '上饶',
        '沈阳',
        '济南',
        '厦门',
        '福州',
        '九江',
        '宜春',
        '赣州',
        '宁波',
        '绍兴',
        '无锡',
        '苏州',
        '徐州',
        '东莞',
        '佛山',
        '中山',
        '成都',
        '武汉',
        '青岛',
        '天津',
        '重庆',
        '南京',
        '九江',
        '香港',
        '澳门',
        '台北'
      ];

      var nums = 5; //每页出现的数据量

      //模拟渲染
      var render = function(curr){
        //此处只是演示，实际场景通常是返回已经当前页已经分组好的数据
        var str = '', last = curr*nums - 1;
        last = last >= data.length ? (data.length-1) : last;
        for(var i = (curr*nums - nums); i <= last; i++){
          str += '<li>'+ data[i] +'</li>';
        }
        return str;
      };

      //调用分页
      laypage({
        cont: 'demo8'
        ,pages: Math.ceil(data.length/nums) //得到总页数
        ,jump: function(obj){
          document.getElementById('biuuu_city_list').innerHTML = render(obj.curr);
        }
      });

    });
  </script>
</head>

<body>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 20px;">
  <legend>开门见山 ： 默认分页</legend>
</fieldset>
<div id="demo1"></div>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 50px;">
  <legend>自定义主题 ： 赤橙黄绿青蓝紫 神马的，随便设:-O</legend>
</fieldset>
<div id="demo2"></div>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 50px;">
  <legend>自定义文本 ： 上一页、下一页、首页、末页统统被替换</legend>
</fieldset>
<div id="demo3"></div>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 50px;">
  <legend>不显示首页、末页</legend>
</fieldset>
<div id="demo4"></div>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 50px;">
  <legend>开启 URL hash</legend>
</fieldset>
<div id="demo5"></div>

<blockquote class="layui-elem-quote">
  切换分页后看地址栏的变化（#后面的fenye名字可以随便定义），该功能对于单页应用有着极大的帮助
</blockquote>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 50px;">
  <legend>只显示上一页、下一页</legend>
</fieldset>
<div id="demo6"></div>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 50px;">
  <legend>是时候看一下完整功能了！</legend>
</fieldset>
<div id="demo7"></div>

<fieldset class="layui-elem-field layui-field-title" style="margin-top: 50px;">
  <legend>将一段已知数组分页展示</legend>
</fieldset>
<div id="demo8"></div>

<ul id="biuuu_city_list"></ul>

</body>
</html>
