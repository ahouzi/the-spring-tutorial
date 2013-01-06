<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page session="false" %>

<div ng-controller="SignUpController">
    <tiles:insertTemplate template="/WEB-INF/layouts/components/box.jsp">
        <tiles:putAttribute name="content">
            <form class="form-horizontal">

                <div class="panel">
                    <fieldset>
                        <legend>
                            <h2> Sign Up </h2>
                        </legend>


                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="username">User Name:</label>

                            <div class="controls"><input class="input-xlarge"
                                                         id="username"
                                                         type="text"
                                                         ng-model="user.username"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the e-mail.   </span>
                                <span ng-show="usernameTaken"> This user name is already taken. Please choose a unique name. </span>
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
                            <label class="control-label" for="firstName">First Name:</label>

                            <div class="controls"><input class="input-xlarge" id="firstName" type="text"
                                                         ng-model="user.firstName" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the first name. </span>
                            </div>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="lastName">Last Name:</label>

                            <div class="controls"><input class="input-xlarge" id="lastName" type="text"
                                                         ng-model="user.lastName" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the last name. </span>
                            </div>
                        </div>


                        <div class="form-actions">
                            <a ng-click="saveProfileData()" href="javascript:void(0);">Sign Up </a>
                                <%--    <button type="submit" ng-click="saveProfileData()" onclick="javascript:void(0);" class="btn btn-primary" ng-model-instant> Create Profile</button>--%>
                            or
                              <span>
                                     <a ng-click="signinWithFacebook()" href="javascript:void(0);">Sign Up with
                                         Facebook</a>

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




