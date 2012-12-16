<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ page session="false" %>
<div>
    <tiles:insertTemplate template="/WEB-INF/layouts/components/box.jsp">
        <tiles:putAttribute name="content">
            Welcome to the Spring CRM.
        </tiles:putAttribute>
    </tiles:insertTemplate>

</div>




