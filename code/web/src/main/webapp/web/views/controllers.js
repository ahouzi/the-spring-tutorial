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

    if (crmSession.isLoggedIn()) {

        var resources = {};
        resources[oauthResource] = {
            client_id:crmSession.getUserId() + '',
            isDefault:true,
            redirect_uri:window.location.href + '',
            authorization:'/oauth/authorize',
            scopes:['read', 'write'],
            callback:function () {
            }
        };
        jso_configure(resources);
    }

    var baseUrl = (function () {
        var defaultPorts = {"http:":80, "https:":443};
        return window.location.protocol + "//" + window.location.hostname
            + (((window.location.port)
            && (window.location.port != defaultPorts[window.location.protocol]))
            ? (":" + window.location.port) : "");
    })();

    console.debug('the base URL is ' + baseUrl);


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
        },
        oauthPut:function (url, data, cb) {


            data['_method'] = 'PUT';

            $.oajax(this.enrichRequestArguments({
                type:'POST',
                url:url,
                headers:{'_method':'PUT'},
                data:data, // the object to send
                cache:false,
                dataType:dataType,
                contentType:contentType,
                success:cb,
                error:errorCallback
            }));
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
    return {

        updateUserById:function (userId, email, pw, callback) {
            // call the update method
            // call as a method of type PUT
            var updateUrl = ajaxUtils.url('/api/user/' + userId)

            var user = {id:413, email:'josh@joshlong.com', password:'password01' ,_method:'PUT' };


            var contentType = 'application/json; charset=utf-8' ,
                dataType = 'json',
                oauthResource = appName,
                errorCallback = function (e) {
                    alert('error trying to connect to ');
                };


            function enrichRequestArguments(args) {
                var a = args || {};
                a['jso_provider'] = oauthResource;
                a['jso_scopes'] = ["read", 'write'];
                a['jso_allowia'] = true;
                return a;
            }

            // data['_method'] = 'PUT';

            $.oajax(enrichRequestArguments({
                type:'POST',
                url:updateUrl,
                headers:{'_method':'PUT'},
                data:{email:email, password:pw, _method:'PUT' ,id: userId }, // the object to send
                cache:false,
                dataType:dataType,
               // contentType:contentType,
                success:callback,
                error:errorCallback
            }));


            //ajaxUtils.oauthPut(updateUrl, user, callback)

        },
        getUserById:function (userId, callback) {
            ajaxUtils.oauthGet(ajaxUtils.url('/api/users/' + userId), {}, callback);
        }
    };
});


/***
 * used for editing the profile and handling the uploaded photo.
 *
 * @constructor
 */
function ProfileController($scope, userService) {

    console.log('inside ProfileController.');

    userService.getUserById(crmSession.getUserId(), function (usr) {
        $scope.$apply(function () {
            $scope.user = usr;
        })
    });

    userService.updateUserById(413, 'josh@joshlong.com', 'password12132', function (updatedUsr) {
        window.alert(JSON.stringify(updatedUsr))
    });

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

