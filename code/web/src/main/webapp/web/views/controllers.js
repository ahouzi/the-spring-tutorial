/***
 *
 * controllers that make up the client-side business logic of the application.
 *
 * handles all the pages through OAuth secured services,
 * which - while not obscured - require multi-factor authentication to do anything useful.
 */


$.ajaxSetup({
    cache:false
});


var appName = 'crm';
var module = angular.module(appName, ['ngResource']);
var oauthResource = appName;


module.factory('ajaxUtils', function () {


    function getRootUrl() {
        var defaultPorts = {"http:":80,"https:":443};

        return window.location.protocol + "//" + window.location.hostname
            + (((window.location.port)
            && (window.location.port != defaultPorts[window.location.protocol]))
            ? (":"+window.location.port) : "");
    }
    var baseUrl = getRootUrl() ;
    console.debug('the base URL is '+ baseUrl)
//    function getRootUrl(url) {
//        return url.toString().replace(/^(.*\/\/[^\/?#]*).*$/,"$1");
//    }
//

    return {
        url:function (u) {
            return baseUrl + u;
        },
        enrichRequestArguments:function (args) {
            var a = args || {};
            a['jso_provider'] = oauthResource;
            a['jso_scopes'] = ["read", 'write'];
            a['jso_allowia'] = true;
            return a;
        }
    };
});

/**
 * injects a reference to ajaxUtils object, which is provided above.
 */
module.factory('userService', function (ajaxUtils) {
    return {
        userById:function (userId, callback) {
            $.oajax(ajaxUtils.enrichRequestArguments({
                url:ajaxUtils.url('/api/users/' + userId),
                dataType:'json',
                success:function (data) {
                    console.log("Response: " + data);
                    callback(data);
                }
            }));

        }
    };
});

module.run(function () {
    // setup jso.js and tell it about our OAuth service

    if (!crmSession.isLoggedIn())
        return;

    var resources = {};
    resources[oauthResource] = {
        client_id:crmSession.getUserId() + '',
        isDefault:true,
        redirect_uri:window.location.href + '',
        authorization:  '/oauth/authorize',
        scopes:['read', 'write'],
        callback:function () {
        }};
    jso_configure(resources);

});

/***
 * used for editing the profile and handling the uploaded photo.
 *
 * @constructor
 */
function ProfileController($scope, userService) {

    console.log('inside ProfileController.');

    if (crmSession.isLoggedIn()) {
        userService.userById(crmSession.getUserId(), function (usr) {
            $scope.$apply(function () {
                $scope.user = usr;
            })
        });
    }

}

/***
 *
 * Handles various events in the 'navigation' div of the page.
 *
 * @constructor
 */
function NavigationController() {
}

/**
 * handles logging in
 *
 * @param $scope
 * @constructor
 */
function SignInController($scope) {

    /// todo remove this and introduce Spring Security's RememberMe service !
    $scope.user = {email:'josh@joshlong.com', password:'password'};

    //console.debug('inside SignInController ');

    //jso_wipe();  // it'll work on the next page no matter what
}

