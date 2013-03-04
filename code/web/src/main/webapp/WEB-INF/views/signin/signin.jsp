<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page session="false" %>
<div ng-controller="SignInController">
    <tiles:insertTemplate template="/WEB-INF/layouts/components/box.jsp">
        <tiles:putAttribute name="content">
            <form name="form" class="form-horizontal" method="POST" action="${pageContext.request.contextPath}/crm/signin.html">
                <div class="panel">
                    <fieldset>

                        <legend><h2>Sign In</h2></legend>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="username">E-Mail:</label>

                            <div class="controls"><input class="input-xlarge" id="username" name="username"
                                                         type="text" ng-model="user.username" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the e-mail. </span>
                            </div>
                        </div>
                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="pw">Password:</label>

                            <div class="controls">
                                <input class="input-xlarge" id="pw" name="pw" type="password"
                                       ng-model="user.password" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the password. </span>
                            </div>
                        </div>

                        <div class="form-actions">

                            <button type="submit"  ng-disabled="form.$invalid" class="btn btn-primary" name="action" value="signin" ng-model-instant>
                                Sign In
                            </button>

                            <span>
                                Don't have an account? <a href="${pageContext.request.contextPath}/crm/signup.html">Register now!</a>

                            </span>


                        </div>
                    </fieldset>
                </div>
            </form>

            <c:url var="signinWithProvider" value="/signin/facebook"/>
            <form method="POST" id="signinWithFacebook" action="${signinWithProvider}">
            </form>


        </tiles:putAttribute>

    </tiles:insertTemplate>


</div>




