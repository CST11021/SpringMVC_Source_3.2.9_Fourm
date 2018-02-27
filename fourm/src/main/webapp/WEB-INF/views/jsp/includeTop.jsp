<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<a href="<c:url value="/index.jsp"/>" style="color: #2D93CA;">首页</a>
&nbsp;&nbsp;
<c:if test="${!empty sessionScope.CURRENT_ONLINE_USER}">
   ${sessionScope.CURRENT_ONLINE_USER.userName}(${sessionScope.CURRENT_ONLINE_USER.credit}),欢迎您的到来,<a href="<c:url value="/login/doLogout.html"/>" style="color: #2D93CA;">注销</a>
</c:if>
&nbsp;&nbsp;
<c:if test="${empty sessionScope.CURRENT_ONLINE_USER}">
   <a href="<c:url value="/login.jsp"/>" style="color: #2D93CA;">登录</a>&nbsp;&nbsp;
   <a href="<c:url value="/register.jsp"/>" style="color: #2D93CA;">注册</a>
</c:if>
<c:if test="${sessionScope.CURRENT_ONLINE_USER !=null && sessionScope.CURRENT_ONLINE_USER.userType == 2}">
   <a href="<c:url value="/forum/addBoardPage.html"/>" style="color: #2D93CA;">新建论坛版块</a>&nbsp;&nbsp;
   <a href="<c:url value="/forum/setBoardManagerPage.html"/>" style="color: #2D93CA;">论坛版块管理员</a>&nbsp;&nbsp;
   <a href="<c:url value="/forum/userLockManagePage.html"/>" style="color: #2D93CA;">用户锁定/解锁</a>
</c:if>
