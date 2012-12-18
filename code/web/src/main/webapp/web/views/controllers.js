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


// idea try moving the module.run logic into this ajaxUtils object and then try separating this out into a separte object
module.factory('ajaxUtils', function () {
    var contentType = 'application/json; charset=utf-8' ,
        dataType = 'json',
        oauthResource = appName,
        errorCallback = function (e) {
            alert('error trying to connect to ');
        };

    var scopes = ['read', 'write'];
    var resources = {};
    resources[oauthResource] = {
        client_id:crmSession.getUserId() + '',
        isDefault:true,
        redirect_uri:window.location.href + '',
        authorization:'/oauth/authorize',
        scopes:scopes
    };


    if (crmSession.isLoggedIn()) {

        // hack, but this clears out any existing tokens
        for (k in resources)
            localStorage.removeItem("tokens-" + k);

        jso_configure(resources, { debug:true });


        var toEnsure = {};
        toEnsure[oauthResource] = scopes;
        jso_ensureTokens(toEnsure);
    }


    var baseUrl = (function () {
        var defaultPorts = {"http:":80, "https:":443};
        return window.location.protocol + "//" + window.location.hostname
            + (((window.location.port)
            && (window.location.port != defaultPorts[window.location.protocol]))
            ? (":" + window.location.port) : "");
    })();

    var sendDataFunction = function (ajaxFunction, argsProcessor, url, _method, data, cb) {
        var d = data || {};
        var argFunc = argsProcessor || function (a) {
            return a;
        };
        var isPost = (_method || '').toLowerCase() == 'post'; // dont specify the '_method' attribute if the request's a POST. Redundant
        if (!isPost) d['_method'] = _method;
        ajaxFunction(argFunc({
            type:'POST',
            url:url,
            headers:(isPost ? {} : {'_method':_method}),
            data:d,
            cache:false,
            dataType:dataType,
            success:cb,
            error:errorCallback
        })
        );
    };

    return {
        establishOAuthToken:establishOAuthToken,
        url:function (u) {
            return baseUrl + u;
        },
        enrichRequestArguments:function (args) {
            var a = args || {};
            a['jso_provider'] = oauthResource;
            a['jso_scopes'] = scopes;
            a['jso_allowia'] = false;
            return a;
        },
        put:function (url, data, cb) {
            sendDataFunction($.ajax, function () {
            }, url, 'PUT', data, cb);
        },
        oauthPut:function (url, data, cb) {
            sendDataFunction($.oajax, this.enrichRequestArguments, url, 'PUT', data, cb);
        },
        oauthGet:function (url, data, cb) {
            $.oajax(this.enrichRequestArguments({
                type:'GET',
                url:url,
                cache:false,
                dataType:dataType,
                contentType:contentType,
                success:cb,
                error:errorCallback
            }));
        },
        get:function (url, data, cb) {
            $.ajax({
                type:'GET',
                url:url,
                cache:false,
                dataType:dataType,
                contentType:contentType,
                success:cb,
                error:errorCallback
            });
        }


    };
});

module.factory('userService', function (ajaxUtils) {

    var usersCollectionEntryUrl = '/api/users/';
    return {
        updateUserById:function (userId, email, pw, callback) {
            var updateUrl = ajaxUtils.url(usersCollectionEntryUrl + userId);
            var user = {email:email, password:pw, id:userId };
            ajaxUtils.oauthPut(updateUrl, user, callback);
        },
        getUserById:function (userId, callback) {
            ajaxUtils.oauthGet(ajaxUtils.url(usersCollectionEntryUrl + userId), {}, callback);
        }
    };
});


/***
 * used for editing the profile and handling the uploaded photo.
 *
 * @constructor
 */
function ProfileController($scope, ajaxUtils, userService) {

    ajaxUtils.establishOAuthToken();

    // load the current User object into the form on load
    userService.getUserById(crmSession.getUserId(), function (u) {
        $scope.$apply(function () {
            $scope.user = u;
        })
    });

    $scope.saveProfileData = function () {
        userService.updateUserById($scope.user.id, $scope.user.email, $scope.user.password, function (u) {
            $scope.$apply(function () {
                $scope.user = u;
            });
        });
    };


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
    jso_wipe();

}

