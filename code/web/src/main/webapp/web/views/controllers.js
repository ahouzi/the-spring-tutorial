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
        accessToken:function () {
            return jso_getToken(oauthResource, scopes);
        },
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
        buildBaseUserApiUrl:function (userId) {
            return ajaxUtils.url(usersCollectionEntryUrl + userId);
        },
        updateUserById:function (userId, username, pw, fn, ln, callback) {
            var updateUrl = this.buildBaseUserApiUrl(userId);
            var user = {username:username, password:pw, firstname: fn, lastname: ln, id:userId };
            ajaxUtils.oauthPut(updateUrl, user, callback);
        },
        getUserById:function (userId, callback) {
            ajaxUtils.oauthGet(ajaxUtils.url(usersCollectionEntryUrl + userId), {}, callback);
        }
    };
})
;


/***
 * used for editing the profile and handling the uploaded photo.
 *
 * @constructor
 */
function ProfileController($rootScope, $scope, ajaxUtils, userService) {

    var profilePhotoUploadedEvent = 'profilePhotoUploadedEvent';  // broadcast when the profile photo's been changed
    var userLoadedEvent = 'userLoadedEvent'; // broadcast when the user being edited is loaded
    var profilePhotoNode = $('#profilePhoto');

    function loadUser(userId) {
        userService.getUserById(userId, function (u) {
            $scope.$apply(function () {
                $scope.user = u;
                $rootScope.$broadcast(userLoadedEvent, $scope.user.id);
            });
        });
    }

    function setupFileDropZone() {

        var photoUrl = ajaxUtils.url('/api/users/photo');// well use the endpoint that takes the <CODE>userId</CODE> as a request param

        console.log('using request URL ' + photoUrl + ', which should require OAuth credentials to work.');

        // todo do we even need to transmit this? it seems to work with just the HTTP Session Cookie
        var accessTokenHeaderToSendInRequestForOauth = ajaxUtils.accessToken();
        var authHeaderForOauth = "Authorization: Authorization " + accessTokenHeaderToSendInRequestForOauth;

        console.log('the oauth header is ' + authHeaderForOauth + '.');

        profilePhotoNode.filedrop({
            dataType:'json',
            maxfilesize:20, /* in MB */
            url:photoUrl,
            paramname:'file',
            data:{
                // todo how come this works? we should be required to send along the OAuth headers and so on
                userId:function () {
                    return $scope.user.id;
                },
                name:'file'
            },
            error:function (err, file) {
                console.log(JSON.stringify(err) + ' caught when trying to upload ' + JSON.stringify(file));
                switch (err) {
                    case 'BrowserNotSupported':
                        alert('browser (usually Safari and IE) do not support html5 drag and drop')
                        break;
                    case 'TooManyFiles':
                        // user uploaded more than 'maxfiles'
                        break;
                    case 'FileTooLarge':
                        // program encountered a file whose size is greater than 'maxfilesize'
                        // FileTooLarge also has access to the file which was too large
                        // use file.name to reference the filename of the culprit file
                        break;
                    default:
                        break;
                }
            },
            dragOver:function () {
            },
            dragLeave:function () {
            },
            docOver:function () {
            },
            docLeave:function () {
            },
            drop:function (e) {
                console.log('drop()');
            },
            uploadStarted:function (i, file, len) {
                console.log('started uploading file ' + i + ' of ' + len + ' ' + file);
            },
            uploadFinished:function (i, file, response, time) {
                console.log('uploadFinished: ' + i + ',' + JSON.stringify(file) + ',' + JSON.stringify(response) + ', ' + JSON.stringify(time));
                $scope.$apply(function () {
                    $rootScope.$broadcast(profilePhotoUploadedEvent, $scope.user.id);
                })
            },
            progressUpdated:function (i, file, progress) {
                console.log('progressUpdated: ' + i + ',' + JSON.stringify(file) + ',' + progress);
            },
            speedUpdated:function (i, file, speed) {
                console.log('speedUpdated: ' + i + ',' + JSON.stringify(file) + ',' + speed);
            },
            rename:function (name) {
                console.log('rename: ' + name);
            },
            beforeEach:function (file) {
                console.log('beforeEach: ' + JSON.stringify(file));
            },
            afterAll:function () {
                console.log('finished uploading (afterAll()). ' +
                    'The file data has been uploaded.');
            }
        });
    }

    function reRenderUserProfilePhoto(userId) {
        var userPhotoUrl = ajaxUtils.url('/api/users/' + userId + '/photo');
        if ($scope.user.profilePhotoImported != true) {
            return;
        }
        var cacheBustingUrl = userPhotoUrl + '?r=' + Math.random();
        var html = '<img id= "photoImage" width="300"  src="' + cacheBustingUrl + '"/>'; // todo this needs to be sexier
        console.debug('html for uploaded photo is ' + html);
        profilePhotoNode.html(html);
    }

    $rootScope.$on(profilePhotoUploadedEvent, function (evt, userId) {
        loadUser(crmSession.getUserId());
    });

    $rootScope.$on(userLoadedEvent, function (evt, userId) {
        console.debug('user loaded event passed for user ID# ' + userId);
        reRenderUserProfilePhoto(userId);
    });

    $scope.saveProfileData = function () {
        userService.updateUserById($scope.user.id, $scope.user.username, $scope.user.password, $scope.user.firstName, $scope.user.lastName, function (u) {
            $scope.$apply(function () {
                $scope.user = u;
            });
        });
    };

    setupFileDropZone();

    loadUser(crmSession.getUserId());


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
function SignInController($scope, ajaxUtils) {


    /// todo remove this and introduce Spring Security's RememberMe service !


    jso_wipe();

    $scope.signinWithFacebook = function(){
        alert('signing in with facebook')
    };
}

