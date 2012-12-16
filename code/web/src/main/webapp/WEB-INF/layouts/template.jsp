<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page session="false" %>
<!doctype html>
<html ng-app="crm">
<head>
    <script type="text/javascript" src="${pageContext.request.contextPath}/web/assets/js/jquery.js"></script>
    <script type="text/javascript">

        var crmSession = {
            isLoggedIn: function(){
                return !(this.getUserId()  == null ) ;
            },
            getUserId:function () {
                var uid = "${userId}";
                var pi;
                if (uid == '' || (pi = parseInt(uid)) == 0) {
                    return null;
                }
                return pi;
            }
        };

        $(function () {
      //      Ajax.setup('${pageContext.request.contextPath}', '${fullUrl}');
        });
    </script>

    <script type="text/javascript" src="${pageContext.request.contextPath}/web/assets/js/jquery-ui.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/web/assets/js/jquery-filedrop.js"></script>

    <script type="text/javascript" src="${pageContext.request.contextPath}/web/assets/js/angular.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/web/assets/js/angular-resource.js"></script>

    <script type="text/javascript" src="${pageContext.request.contextPath}/web/assets/js/jso.js"></script>

    <script type="text/javascript" src="${pageContext.request.contextPath}/web/views/controllers.js"></script>

    <%--      todo plugin in integration.js, rest.js
              todo ping scott on how to do oauth dance with rest.js
              todo check out monty hall example game (i think thats in adrian's keynote for examples on rest.js
              todo
    --%>

    <link href="${pageContext.request.contextPath}/web/assets/bootstrap/bootstrap.css" rel="stylesheet"/>
    <link href="${pageContext.request.contextPath}/web/views/controllers.css" rel="stylesheet"/>

</head>
<body>


<div id="navigation">
    <%--
      Use Spring Security tags to handle conditional display of
      menu items based on whether there's an authenticated principal or not.
    --%>
    <div ng-controller="NavigationController">
        <security:authorize access="isAnonymous()">
            <A href="${pageContext.request.contextPath}/crm/signin.html">Sign In</a>
        </security:authorize>
        <security:authorize access="isAuthenticated()">
            <c:url value="/j_spring_security_logout" var="logoutUrl"/>
            Welcome <strong><security:authentication property="principal.username"/></strong> |
            <span>
                <a href="${pageContext.request.contextPath}/crm/profile.html">My Profile</a> |
                <a href="${pageContext.request.contextPath}/crm/customers.html">My Customers</a> |
                <a ng-click="startLogoutFlow( '${logoutUrl}' )" href="${logoutUrl}">Sign Out</a>
            </span>
        </security:authorize>
    </div>
</div>
<div id="content">
    <tiles:insertAttribute name="content"/>
</div>


<%--
 browsers tend to render css at the point they're encontered,
 which triggers a repaint delay and an awkward pause
 while the file is downloaded for the first time. best to put this
 at the end so that everything appears to instantly snap into place
 rather than render slowly once they've arrived already
--%>

<div id="copyright">
    Brought to you by <A href="http://www.springsource.org">SpringSource</a>.
</div>
</body>
</html>