<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ page session="false" %>
<div ng-controller="SignInController">
    <tiles:insertTemplate template="/WEB-INF/layouts/components/box.jsp">
        <tiles:putAttribute name="content">
            <form class="form-horizontal" method="POST" action="j_spring_security_check">
                <div class="panel">
                    <fieldset>
                        <legend><h2>Sign In</h2></legend>


                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="j_username">E-Mail:</label>

                            <div class="controls"><input class="input-xlarge" id="j_username" name="j_username"
                                                         type="text" ng-model="user.email" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the e-mail. </span>
                            </div>
                        </div>
                        <div class="control-group ${cgClass}">
                            <label class="control-label" for="j_password">Password:</label>

                            <div class="controls">
                                <input class="input-xlarge" id="j_password" name="j_password" type="password"
                                       ng-model="user.password" required="required"/>
                                <span ng-show="${error}" class="help-inline"> Please provide a valid value for the password. </span>
                            </div>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary" ng-model-instant>
                                Sign In
                            </button>

                            <span>
                                Don't have an account?
                                <a href="${pageContext.request.contextPath}/crm/register.html">Register now!</a>  or
                                <%-- todo make this button real and make it actually do something using Spring
                                     todo   Social if Spring Social is available (which it's not from within China)--%>
                                <a href="#">Sign In With Facebook</a>
                            </span>
                        </div>

                    </fieldset>
                </div>
            </form>

        </tiles:putAttribute>

    </tiles:insertTemplate>


</div>




