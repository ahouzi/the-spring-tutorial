<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page session="false" %>


<div ng-controller="SignUpController">
    <tiles:insertTemplate template="/WEB-INF/layouts/components/box.jsp">
        <tiles:putAttribute name="content">
            <form name="form" class="form-horizontal">

                <div class="panel">
                    <fieldset>
                        <legend>
                            <h2> Sign Up </h2>
                        </legend>


                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="username">User Name:</label>

                            <div class="controls"><input class="input-xlarge"
                                                         id="username"
                                                         name="username"
                                                         value="${username}"
                                                         type="text"
                                                         ui-validate="{ validUsername : isUsernameValid }"
                                                         required="required"
                                                         ng-model="user.username"/>
                                <span ng-show="${error}" class="help-block"> Please provide a valid value for the e-mail.   </span>
                                <span ng-show="form.username.$error.validUsername" class="help-block">

                                     <spring:message code="profile.email.error.invalid"/>
                                </span>
                                <span ng-show="usernameTaken" class="help-block"> <spring:message code="profile.email.error.taken"/>  </span>
                            </div>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="password">Password:</label>

                            <div class="controls">
                                <input class="input-xlarge" id="j_password" id="password" type="password"
                                       ng-model="user.password" required="required"/>
                                <span ng-show="${error}" class="help-inline">  <spring:message code="signup.password.error.invalid"/> </span>
                            </div>
                        </div>
                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="password"><spring:message code="profile.password-confirm"/>:</label>

                            <div class="controls">
                                <input class="input-xlarge" ui-validate="{ confirmPassword : confirmPasswordMatches }"
                                       type="password"
                                       name="passwordConfirmation"
                                       ng-model="user.passwordConfirmation" required="required"/>
                                <span ng-show="form.passwordConfirmation.$error.confirmPassword" class="help-block">            <spring:message code="profile.passwords.dont-match"/>  </span>
                                <span ng-show="${error}" class="help-inline">
                                  <spring:message code="profile.passwords.prompt"/>
                                </span>
                            </div>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="firstName"><spring:message code="signup.first-name"/> :</label>

                            <div class="controls"><input value="${firstName}" class="input-xlarge" id="firstName"
                                                         type="text"
                                                         ng-model="user.firstName" required="required"/>
                                <span ng-show="${error}" class="help-inline">
                                     <spring:message code="profile.first-name.prompt"/>
                                     </span>
                            </div>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="lastName">      <spring:message code="profile.last-name"/>:</label>

                            <div class="controls"><input value="${lastName}" class="input-xlarge" id="lastName"
                                                         type="text"
                                                         ng-model="user.lastName" required="required"/>
                                <span ng-show="${error}" class="help-inline">  <spring:message code="profile.last-name.prompt"/>  </span>
                            </div>
                        </div>


                        <div class="form-actions">


                            <div ng-show="!isFacebookSignup()">
                                <button type="submit" ng-disabled="form.$invalid" ng-click="saveProfileData()"
                                        href="javascript:void(0);" class="btn btn-primary" name="action" value="signin"
                                        ng-model-instant>
                                    Sign Up
                                </button>
                                or
                              <span>
                        <%-- todo restore this    <a ng-click="signinWithFacebook()" href="javascript:void(0);">Sign Up with Facebook</a>--%>

                            </span>

                            </div>

                         <%--   <div ng-show="isFacebookSignup()">

                                <button type="submit" ng-disabled="form.$invalid" ng-click="continueSigninWithFacebook()"
                                        href="javascript:void(0);" class="btn btn-primary" name="action" value="signin"
                                        ng-model-instant>
                                    Continue Signing Up With Facebook
                                </button>

                            </div>
--%>
                        </div>

                    </fieldset>
                </div>
            </form>

         <%--   <c:url var="signinWithProvider" value="/signin/facebook"/>

            <form method="POST" id="signinWithFacebook" action="${signinWithProvider}">
            </form>
--%>

        </tiles:putAttribute>

    </tiles:insertTemplate>


</div>




