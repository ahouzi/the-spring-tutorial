<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page session="false" %>
<div ng-controller="SignInController">
    <tiles:insertTemplate template="/WEB-INF/layouts/components/box.jsp">
        <tiles:putAttribute name="content">
            <form:form class="form-horizontal" id="form" name="form" method="POST" commandName="signInAttempt"   action="${pageContext.request.contextPath}/crm/signin.html">
                <div class="panel">
                    <fieldset>

                        <legend><h2>
                            <spring:message code="login.signin"/>
                        </h2></legend>


                        <div class="control-group error">
                            <UL>
                                <form:errors element="li" path="*"/>
                            </UL>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="username">
                                <spring:message code="login.email"/>:</label>

                            <div class="controls">
                                <input class="input-xlarge" id="username" name="username" type="text"
                                       ng-model="user.username" required="required"/>
                                <span ng-show="${error}" class="help-inline">  <spring:message
                                        code="login.email.prompt"/>  </span>
                            </div>
                        </div>
                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="password"> <spring:message
                                    code="login.password"/>:</label>

                            <div class="controls">
                                <input class="input-xlarge" id="password" name="password" type="password"
                                       ng-model="user.password" required="required"/>
                                <span ng-show="${error}" class="help-inline">  <spring:message
                                        code="login.password.prompt"/> </span>
                            </div>
                        </div>

                        <div class="form-actions">

                            <button type="submit" ng-disabled="form.$invalid" class="btn btn-primary" name="action"
                                    value="signin" ng-model-instant>
                                <spring:message code="login.signin"/>
                            </button>

                            <span>

                                <spring:message code="login.buttons.dontHaveAccount"/>



                                <a href="${pageContext.request.contextPath}/crm/signup.html">
                                    <spring:message code="login.buttons.register"/>
                                </a>

                            </span>


                        </div>
                    </fieldset>
                </div>
            </form:form>


        </tiles:putAttribute>

    </tiles:insertTemplate>


</div>




