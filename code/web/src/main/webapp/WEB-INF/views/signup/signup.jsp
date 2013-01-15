<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                                <span ng-show="form.username.$error.validUsername" class="help-block"> The username you specified is not valid.
                                    Please
                                ensure that it is a minimum of 8 characters and that it has no spaces or any non alpha numeric characters (a-z, A-Z and 1-10).
                                </span>
                                <span ng-show="usernameTaken" class="help-block"> This user name is already taken. Please choose a unique name. </span>
                            </div>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="password">Password:</label>

                            <div class="controls">
                                <input class="input-xlarge" id="j_password" id="password" type="password"
                                       ng-model="user.password" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the password. </span>
                            </div>
                        </div>
                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="password">Confirm Password:</label>

                            <div class="controls">
                                <input class="input-xlarge" ui-validate="{ confirmPassword : confirmPasswordMatches }"
                                       type="password"
                                       name="passwordConfirmation"
                                       ng-model="user.passwordConfirmation" required="required"/>
                                <span ng-show="form.passwordConfirmation.$error.confirmPassword" class="help-block"> Your passwords do not match </span>
                                <span ng-show="${error}" class="help-inline">  Please confirm your password </span>
                            </div>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="firstName">First Name:</label>

                            <div class="controls"><input value="${firstName}" class="input-xlarge" id="firstName"
                                                         type="text"
                                                         ng-model="user.firstName" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the first name. </span>
                            </div>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="lastName">Last Name:</label>

                            <div class="controls"><input value="${lastName}" class="input-xlarge" id="lastName"
                                                         type="text"
                                                         ng-model="user.lastName" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the last name. </span>
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
                             <a ng-click="signinWithFacebook()" href="javascript:void(0);">Sign Up with Facebook</a>

                            </span>

                            </div>

                            <div ng-show="isFacebookSignup()">
                             <%--   <a ng-click="continueSigninWithFacebook()" href="javascript:void(0);">

                                </a>--%>
                                <button type="submit" ng-disabled="form.$invalid" ng-click="continueSigninWithFacebook()"
                                        href="javascript:void(0);" class="btn btn-primary" name="action" value="signin"
                                        ng-model-instant>
                                    Continue Signing Up With Facebook
                                </button>

                            </div>

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




