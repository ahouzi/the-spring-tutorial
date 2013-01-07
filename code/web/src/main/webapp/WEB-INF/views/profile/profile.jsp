<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ page session="false" %>
<div ng-controller="ProfileController">
    <tiles:insertTemplate template="/WEB-INF/layouts/components/box.jsp">
        <tiles:putAttribute name="content">
            <form class="form-horizontal" id="form" name="form" ng-submit="saveProfileData()">

                <div class="panel">
                    <fieldset>
                        <legend><h2> Update Your Profile</h2></legend>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="username">User Name:</label>

                            <div class="controls"><input class="input-xlarge"
                                                         id="username"
                                                         name="username"
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
                                <input class="input-xlarge" id="j_password"   id="password" type="password" ng-model="user.password" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the password. </span>
                            </div>
                        </div>
                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="password">Confirm Password:</label>

                            <div class="controls">
                                <input class="input-xlarge" ui-validate="{ confirmPassword : confirmPasswordMatches }"  type="password"
                                       name="passwordConfirmation"
                                       ng-model="user.passwordConfirmation" required="required"/>
                                 <span ng-show="form.passwordConfirmation.$error.confirmPassword" class="help-block"> Your passwords do not match </span>
                                <span ng-show="${error}" class="help-inline">  Please confirm your password </span>
                            </div>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="firstName">First Name:</label>

                            <div class="controls"><input class="input-xlarge"  id="firstName" type="text" ng-model="user.firstName" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the first name. </span>
                            </div>
                        </div>

                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="lastName">Last Name:</label>

                            <div class="controls"><input class="input-xlarge" id="lastName" type="text" ng-model="user.lastName" required="required"/>
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
                            <button type="submit" ng-disabled="form.$invalid" class="btn btn-primary" ng-model-instant>
                               Save Changes
                            </button>


                        </div>

                    </fieldset>
                </div>
            </form>

        </tiles:putAttribute>

    </tiles:insertTemplate>


</div>




