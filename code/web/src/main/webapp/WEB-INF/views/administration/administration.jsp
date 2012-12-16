<%@ page import="com.joshlong.blawg.config.SuccessHandlerUtils" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page session="false" %>

<link rel="stylesheet" href="${context}/web/assets/css/administration.css" type="text/css"/>

<script type="text/javascript" src="${context}/web/assets/js/jquery-filedrop.js"></script>
<script type="text/javascript" src="${context}/web/assets/js/jquery-edit-in-place.js"></script>
<script type="text/javascript">
    function loadUserCredentialsId() {
        return <%=request.getSession(false).getAttribute(  SuccessHandlerUtils.USER_CREDENTIALS_ID ) %>;
    }
    function loadCurrentBloggerId() {
        return  <%=request.getSession(false).getAttribute(  SuccessHandlerUtils.USER_CREDENTIALS_BLOGGER_ID ) %>;
    }

    ensureTokens();

</script>


<div style="display: none" id="admin">

<div style=" z-index: -21; float: left;padding-top :10px; padding:10px;min-width: 250px;max-width:300px;   ">
    <div ng-controller="SearchBlogsController">
        <form class="well form-search" ng-submit="search()">
            <div>
                <input type="text" id="search" class="input-medium search-query" ng-model="query"/>
                <a href="#" class="icon-search" ng-submit="search()"></a>


            </div>
            <div style="padding-top: 10px;">
                <div ng-show=" !searchResultsFound()">
                    <span class="no-records">(no results)</span>
                </div>

                <div ng-show=" searchResultsFound()">

                    <div ng-repeat="result in results" class="search-results">
                        <span class="title"> <a ng-click="load(result)">{{result.title}}</a> </span> -
                        <span class="date-published">{{result.published |date:'shortDate'}}</span>

                        <div style="font-weight: normal;font-size: smaller">
                            <a href="${context}/jl/blogPost/{{result.seoUrl}}.html" target="_blank">Permalink</a>
                        </div>
                    </div>

                </div>
            </div>
        </form>


    </div>
</div>


<div class="panel" id="blogEditor">

<div style="padding-top: 10px;" ng-controller="EditBlogController">


    <form name="blogEditorForm" class="form-horizontal" style="background-color: #ffffff;margin: 0;  ">

        <a style="float: right; padding-right: 10px" class="icon-remove" ng-click="reset()"> </a>


        <h2 style="height: 2em">
            {{blog.title}}
                <span style="font-size: small;">
                <span ng-show="blog.id"> |   (blog # {{blog.id}})</span>
                <span ng-show="!isNewBlog() && blog.published">

                    | <a href="${context}/jl/blogPost/{{blog.seoUrl}}.html">Permalink</a>
                </span>
                    </span>
        </h2>


        <fieldset>

            <div style="padding-right: 10px">
                <div class="control-group">
                    <label class="control-label" for="title">Title:</label>

                    <div class="controls">
                        <input class="input-xlarge" type="text" ng-model="blog.title" name="title" id="title"
                               required="required"/>
                        <span ng-show="blogEditorForm.title.$error.required" class="help-block">You need to provide a value for the title of this blog. </span>
                    </div>

                </div>

                <div class="control-group ">
                    <label class="control-label" for="tags">Tags:</label>

                    <div class="controls">
                        <input class="input-xlarge" type="text" ng-model="blog.tagsString" name="tags" id="tags"
                               required="required"/>
                        <span ng-show="blogEditorForm.tags.$error.required" class="help-block">You need to provide a value for the tags field. </span>
                    </div>
                </div>

                <div class="control-group ">
                    <label class="control-label" for="subject">Subject:</label>

                    <div class="controls">
                        <input class="input-xlarge" type="text" ng-model="blog.subject" name="subject" id="subject"
                               required="required"/>
                        <span ng-show="blogEditorForm.subject.$error.required" class="help-block">You need to provide a value for the subject field. </span>
                    </div>
                </div>


                <div class="control-group">
                    <label class="control-label" for="feed">Blog Feed:</label>

                    <div class="controls">
                        <select ng-model="blog.blogFeedString" id="feed" name="feed"
                                ng-options="i for i in feeds"></select>
                    </div>
                </div>


                <div class="control-group">
                    <label class="control-label" for="seoUrl">Bookmarkable URL:</label>

                    <div class="controls">
                        <input class="input-xlarge" type="text" ng-model="blog.seoUrl" id="seoUrl" name="seoUrl"
                               required="required"/>
                            <span ng-show="blogEditorForm.seoUrl.$error.required" class="help-block">
                                 You need to provide a value for the bookmarkable URL.    <br/>
                                 <span ng-show="blog.title != null"><a ng-click=" useSuggestedSeoUrl()">Use
                                     "{{suggestedSeoUrl}}"?</a></span>
                            </span>

                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="rawBody">Body:</label>

                    <div class="controls">
                        <textarea id="rawBody" name="rawBody" style="height: 300px;" ng-model="blog.rawBody"
                                  required="required"></textarea>
                        <span ng-show="blogEditorForm.rawBody.$error.required" class="help-block">You need to provide a value for the body of this blog. </span>
                    </div>
                </div>

                <div class="control-group">

                    <label class="control-label"> File Attachments:</label>

                    <div ng-show="managedUploads.length==0" class="controls">
                        <span class="no-records">(no managed uploads)</span>
                    </div>

                    <div ng-show="managedUploads.length > 0" class="controls">
                        <div>
                            <span class="mu-col-header mu-id"> ID </span>
                            <span class="mu-col-header mu-source-file-name"> File Name </span>
                            <span class="mu-col-header mu-actions">Tools</span>
                        </div>
                        <div id="managedUploads">
                            <div ng-repeat="mu in managedUploads">

                                <div ng-mouseover="setCurrentManagedUpload(mu)"
                                     id="{{getDropZoneNameForNode(mu)}}" class="managed-upload">

                                    <div class="managed-upload-used-{{mu.used}}">

                                        <span class="mu-id"> {{mu.id}} </span>

                                        <span class="mu-source-file-name"> {{mu.sourceFileName}} </span>

                                        <%--<span class="mu-imported mu-imported-{{mu.imported != null}}"></span>--%>

                                                <span class="mu-actions">
                                                    <span class="mu-action">
                                                        <span class="mu-imported mu-imported-{{mu.imported != null }}"></span>
                                                    </span>


                                                    <span class="mu-action">

                                                        <span ng-show="mu.imported == null">
                                                            <span class="preview-button-false"></span>
                                                        </span>

                                                        <span ng-show="mu.imported != null">

                                                            <a target="_blank"
                                                               href="${context}/api/managedUploads/{{mu.id}}">
                                                                <span class="preview-button-true"></span>
                                                            </a>
                                                        </span>


                                                     </span>

                                                    <span class="mu-action">
                                                            <span ng-show="!mu.used">
                                                               <a ng-click="deleteManagedUpload(mu)">
                                                                   <span class="delete-button-true"></span>
                                                               </a>
                                                            </span>
                                                            <span ng-show="mu.used">
                                                                <span class="delete-button-false"></span>
                                                            </span>
                                                    </span>


                                                </span>

                                    </div>

                                </div>


                            </div>
                        </div>
                    </div>
                </div>
            </div>


            <div class="form-actions">

                        <span ng-show=" !isNewBlog() ">
                            <button id="updateBlog" type="submit" ng-disabled="isFormDisabled()" ng-click="update()"
                                    class="btn btn-primary" ng-model-instant>
                                Update
                            </button>
                        </span>
                        <span ng-show=" isNewBlog() ">
                            <button type="submit" id="createBlog" ng-disabled="isFormDisabled()" ng-click="create()"
                                    class="btn btn-primary" ng-model-instant>
                                Create
                            </button>
                        </span>

                <button ng-disabled="( isFormDisabled() ||  !(blog.id > -1)) " type="submit" id="publishBlog"
                        ng-click="publish()" class="btn" ng-model-instant>
                    Publish
                </button>
                <button type="submit" ng-click="delete()" class="btn" ng-model-instant>
                    Delete
                </button>

                <button type="submit" ng-click="reset()" class="btn" ng-model-instant>
                    Close
                </button>


            </div>
        </fieldset>
    </form>
</div>
</div>
</div>