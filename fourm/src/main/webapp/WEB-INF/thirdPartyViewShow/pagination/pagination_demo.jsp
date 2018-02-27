<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@page language="java" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

    <style type="text/css">
        .simple-table{ border-collapse:collapse; width:100%;}
        .simple-table th{ font-weight:normal; background:#f0f0f0; color:#000; height:29px; line-height:29px; white-space:nowrap; }
        .simple-table td{text-align:center; height:28px; line-height:28px;color:#757575;border-bottom:1px solid #f0f0f0; white-space:nowrap;}

        /*分页*/
        .pagination ul {
            display: inline-block;
            *display: inline;
            /* IE7 inline-block hack */

            *zoom: 1;
            margin-left: 0;
            margin-bottom: 0;
            -webkit-border-radius: 3px;
            -moz-border-radius: 3px;
            border-radius: 3px;
            -webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
            -moz-box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
            box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
        }
        .pagination li {
            display: inline;
        }
        .pagination a,
        .pagination span {
            float: left;
            padding: 0 14px;
            line-height: 38px;
            text-decoration: none;
            background-color: #ffffff;
            border: 1px solid #dddddd;
            border-left-width: 0;
        }
        .pagination a:hover,
        .pagination .active a,
        .pagination .active span {
            background-color: #f5f5f5;
        }
        .pagination .active a,
        .pagination .active span {
            color: #999999;
            cursor: default;
        }
        .pagination .disabled span,
        .pagination .disabled a,
        .pagination .disabled a:hover {
            color: #999999;
            background-color: transparent;
            cursor: default;
        }
        .pagination li:first-child a,
        .pagination li:first-child span {
            border-left-width: 1px;
            -webkit-border-radius: 3px 0 0 3px;
            -moz-border-radius: 3px 0 0 3px;
            border-radius: 3px 0 0 3px;
        }
        .pagination li:last-child a,
        .pagination li:last-child span {
            -webkit-border-radius: 0 3px 3px 0;
            -moz-border-radius: 0 3px 3px 0;
            border-radius: 0 3px 3px 0;
        }
        /*分页*/
    </style>
    <style type="text/css">
        a:hover,a:active {
            text-decoration: underline;
            color: #FF0000;
        }
    </style>
    <style type="text/css">
        body,h1,h2,h3,h4,h5,h6,hr,p,blockquote,dl,dt,dd,ul,ol,li,pre,form,fieldset,legend,button,input,textarea,th,td
        {
            margin: 0;
            padding: 0;
        }
        body,button,input,select,textarea{ font-family:Verdana, Arial, Helvetica, sans-serif; font-size:12px;color:#333;}
        a:link,a:visited {
            text-decoration: none;
            color: #333;
        }

        a:hover,a:active {
            text-decoration: underline;
            color: #FF0000;
        }
    </style>
    <script type="text/javascript" src="<c:url value="/static/js/jquery-1.9.1.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/static/js/jquery.tablesorter.js"/>"></script>
</head>
<script type="text/javascript">
    $(function() {
        $("#myTable").tablesorter();
    });
</script>
<body>
    <table id="myTable" class="simple-table tablesorter">
        <thead>
            <tr>
                <th>序号</th>
                <th style="cursor:pointer">姓名</th>
                <th style="cursor:pointer">身份证</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${page.result}" var="item" varStatus="s">
                <tr>
                    <td>${item['序号']}</td>
                    <td>${item['姓名']}</td>
                    <td>${item['身份证']}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <tags:pagination page="${page}" paginationSize="${page.pageSize}"/>
</body>
</html>

