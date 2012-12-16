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
var baseUrl = 'http://localhost:8080'; // todo factor this out so that it's provided by the server somehow


module.factory('userService', function () {
    return {
        userById:function (userId, callback) {
            $.oajax({
                url: baseUrl + '/api/users/' + userId,
                jso_provider: appName,
                jso_scopes:["read", 'write'],
                jso_allowia:true,
                dataType:'json',
                success:function (data) {
                    console.log("Response:");
                    console.log(data);
                    callback(data);
                }
            });

        }
    };
});

module.run(function () {
    // setup jso.js and tell it about our OAuth service

    var resources = {};
    resources[oauthResource] = {
        client_id:crmSession.getUserId() + '',
        isDefault:true,
        redirect_uri:window.location.href,
        authorization:'/oauth/authorize',
        scopes:['read', 'write'],
        callback:function () {
        }};

    jso_configure(resources);

    resources[oauthResource] = resources[oauthResource].scopes;
    jso_ensureTokens(resources);


});

/***
 * used for editing the profile and handling the uploaded photo.
 *
 * @constructor
 */
function ProfileController($scope, userService) {

    console.log('inside ProfileController.');

    userService.userById(crmSession.getUserId(), function (usr) {
        $scope.$apply(function () {
            $scope.user = usr;
        })
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

