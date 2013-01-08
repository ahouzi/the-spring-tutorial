<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ page session="false" %>
<div ng-controller="CustomerController">
    <tiles:insertTemplate template="/WEB-INF/layouts/components/box.jsp">
        <tiles:putAttribute name="content">
            <style type="text/css">
                    /*http://meyerweb.com/eric/articles/webrev/200006b.html*/

                .cell {
                    display: inline-block;
                    height: 30px;
                    vertical-align: baseline;
                }

                .id {
                    width: 50px;
                    text-align: right;
                    padding-right: 10px;
                }

                .ln {
                    display: inline-block;
                    width: 230px;
                }

                .hr > span {
                    padding-top: 8px;
                }

                .tr {
                    display: block;
                    height: 45px;
                    font-weight: bold;
                }

                .tr > * > input {
                    margin-top: 8px;
                }

                .fn {
                    display: inline-block;
                    width: 230px;
                }

                .btns {
                    width: 200px
                }
            </style>


            <fieldset>
                <legend><h2> Customer Data </h2></legend>
            </fieldset>
            <DIV>
                <DIV class="tr hr">

                          <span class="cell id  ">
                               ID#
                        </span>
                        <span class="cell fn ">
                         First Name
                        </span>
                    <span class=" cell ln  ">  Last Name</span>
                    <span class="cell btns "></span>


                </DIV>
                <div class="tr" ng-repeat="customer in customers">


                          <span class="cell id ">
                               <span style="color:gray">{{'#'+customer.id}}</span>
                        </span>
                        <span class="cell fn "> <input class="input-large" type="text" ng-model="customer.firstName"
                                                       size="30"/> </span>
                        <span class="cell ln "><input class="input-large" type="text" ng-model="customer.lastName"
                                                      size="30"/> </span>
                        <span class="cell btns ">
                            <a ng-click="updateCustomer( customer.id )" class="btn   btn-small"><i
                                    class="icon-check"></i></a>
                            <a ng-click="deleteCustomer ( customer.id )" class="btn   btn-small"><i
                                    class="icon-trash"></i></a>
                        </span>
                </DIV>
                <div class="tr">


                    <span class="cell id "> &nbsp; </span>
                    <span class="cell  fn"> <input class="input-large" type="text" ng-model="firstName"
                                                   size="30"/> </span>
                    <span class="cell  ln"> <input class="input-large" type="text" ng-model="lastName"
                                                   size="30"/> </span>
                        <span class="cell  btns">
                             <a ng-click="addCustomer()" class="btn btn-small"><i class="icon-plus"></i></a>
                        </span>
                </DIV>
            </DIV>


        </tiles:putAttribute></tiles:insertTemplate>


</div>
