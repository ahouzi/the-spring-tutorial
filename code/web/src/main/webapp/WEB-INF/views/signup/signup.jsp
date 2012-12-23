<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page session="false" %>
<div ng-controller="SignUpController">
    <tiles:insertTemplate template="/WEB-INF/layouts/components/box.jsp">
        <tiles:putAttribute name="content">
            <form class="form-horizontal" ng-submit="saveProfileData()">

                <div class="panel">
                    <fieldset>
                        <legend>
                            <h2>
                                Sign Up for an Account
                            </h2>
                        </legend>


                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="username">User Name:</label>

                            <div class="controls"><input class="input-xlarge" id="username" type="text"
                                                         ng-model="user.username" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the e-mail. </span>
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
                            <label class="control-label" for="firstName">E-Mail:</label>

                            <div class="controls"><input class="input-xlarge" id="firstName" type="text"
                                                         ng-model="user.firstName" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the first name. </span>
                            </div>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="lastName">First Name:</label>

                            <div class="controls"><input class="input-xlarge" id="lastName" type="text"
                                                         ng-model="user.lastName" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the last name. </span>
                            </div>
                        </div>
                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="j_password">Profile Photo:</label>

                            <div class="controls">

                                <div id="profilePhoto">
                                    Drag your profile photo here.
                                </div>


                            </div>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary" ng-model-instant>
                                Create Profile
                            </button>

                            or


                            <a ng-click="signinWithFacebook()" href="javascript:void(0);">Sign in with Facebook</a>

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




