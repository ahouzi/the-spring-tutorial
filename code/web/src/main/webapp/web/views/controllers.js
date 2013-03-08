/***
 *
 * controllers that make up the client-side business logic of the application. Handles all the pages through REST services,
 * which - while not obscured - require multi-factor authentication to do anything useful.
 *
 */


$.ajaxSetup({
    cache:false
});

var appName = 'crm';
var module = angular.module(appName, ['ngResource', 'ui']);


module.value('ui.config', {
    // The ui-jq directive namespace
    jq:{
        // The Tooltip namespace
        tooltip:{
            // Tooltip options. This object will be used as the defaults
            placement:'right'
        }
    }
});

// idea try moving the module.run logic into this ajaxUtils object and then try separating this out into a separate object
module.factory('ajaxUtils', function () {
    var contentType = 'application/json; charset=utf-8' ,
        dataType = 'json',
        errorCallback = function (e) {
            alert('error trying to connect to ');
        };



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
        var isPost = (_method || '').toLowerCase() == 'post';

        if (!isPost) {
            d['_method'] = _method;
        }

        var arg = {
            type:'POST',
            url:url,
            data:d,
            cache:false,
            dataType:dataType,
            success:cb,
            error:errorCallback
        };

        if (!isPost) {
            arg['headers'] = {'_method':_method};
        }
        ajaxFunction(argFunc(arg));
    };


    var noopArgsProcessor = function (e) {
        return e;
    };

    return {

        url:function (u) {
            return baseUrl + u;
        },
        enrichRequestArguments:noopArgsProcessor,
        put:function (url, data, cb) {
            sendDataFunction($.ajax, noopArgsProcessor, url, 'PUT', data, cb);
        },
        post:function (url, data, cb) {
            sendDataFunction($.ajax, noopArgsProcessor, url, 'POST', data, cb);
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

module.factory('customerService', function (ajaxUtils) {

    var customersCollectionUrl = '/api/crm/userId/customers';
    return {
        buildBaseUserCustomerApiUrl:function (userId) {
            return ajaxUtils.url(customersCollectionUrl.replace('userId', userId + ''))
        },
        buildBaseCustomerApiUrl:function (userId, customerId) {
            return ajaxUtils.url(customersCollectionUrl.replace('userId', userId + '') + '/' + customerId);
        },
        loadCustomers:function (userId, cb) {
            var u = this.buildBaseUserCustomerApiUrl(userId)
            console.log('loadCustomers url ' + u)
            ajaxUtils.get(u, {}, function (customers) {
                customers.sort(function (a, b) {
                    return a.id - b.id;
                });
                cb(customers)
            });
        },
        getCustomerById:function (userId, customerId, cb) {
            var urlForCustomer = this.buildBaseCustomerApiUrl(userId, customerId);
            ajaxUtils.get(urlForCustomer, {}, cb);
        },
        deleteCustomerById:function (userId, customerId, callback) {
            var urlForCustomer = this.buildBaseCustomerApiUrl(userId, customerId);
            ajaxUtils.delete(urlForCustomer, {}, callback);

        },
        updateCustomerById:function (userId, customerId, fn, ln, callback) {
            var urlForCustomer = this.buildBaseCustomerApiUrl(userId, customerId);
            console.log('called updateCustomerById, the URL is: ' + urlForCustomer);
            ajaxUtils.put(urlForCustomer, { firstName:fn, lastName:ln}, callback);
        },
        addNewCustomer:function (userId, fn, ln, callback) {
            var user = {  firstName:fn, lastName:ln };
            var url = this.buildBaseUserCustomerApiUrl(userId);
            ajaxUtils.post(url, user, callback);
        }

    };

});

module.factory('userService', function (ajaxUtils) {
    var usersCollectionEntryUrl = '/api/users';
    return {
        buildBaseUserApiUrl:function (userId) {
            return ajaxUtils.url(usersCollectionEntryUrl + '/' + userId);
        },
        isUserNameTaken:function (username, cb) {
            var url = ajaxUtils.url(usersCollectionEntryUrl + '/usernames/' + username);
            console.log('url for isUserNameTaken is ' + url);
            ajaxUtils.get(url, {}, cb);
        },
        updateUserById:function (userId, username, pw, fn, ln, callback) {
            var user = {username:username, password:pw, firstname:fn, lastname:ln, id:userId };
            ajaxUtils.put(this.buildBaseUserApiUrl(userId), user, callback);
        },
        getUserById:function (userId, callback) {
            ajaxUtils.get(this.buildBaseUserApiUrl(userId), {}, callback);
        },
        registerNewUser:function (username, pw, fn, ln, imported, callback) {
            var user = {username:username, password:pw, firstname:fn, lastname:ln, imported:imported};
            var url = ajaxUtils.url(usersCollectionEntryUrl);
            ajaxUtils.post(url, user, callback);
        }
    };
})
;


/***
 * used for editing the profile and handling the uploaded photo.
 *
 * @constructor
 */
function ProfileController($rootScope, $scope, $q, $timeout, ajaxUtils, userService) {
    // so that its not null on initial calls before ajax

    var profilePhotoUploadedEvent = 'profilePhotoUploadedEvent';  // broadcast when the profile photo's been changed
    var userLoadedEvent = 'userLoadedEvent'; // broadcast when the user being edited is loaded
    var profilePhotoNode = $('#profilePhoto');

    function setupFileDropZoneForUser(userId) {

        var photoUrl = ajaxUtils.url('/api/users/' + userId + '/photo');// well use the endpoint that takes the <CODE>userId</CODE> as a request param


        profilePhotoNode.filedrop({
            dataType:'json',
            maxfilesize:20, /* in MB */
            url:photoUrl,
            paramname:'file',


            data:{
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
        $scope.loadUser(crmSession.getUserId());
    });

    $rootScope.$on(userLoadedEvent, function (evt, userId) {
        reRenderUserProfilePhoto(userId);
        setupFileDropZoneForUser(userId);
    });

    $scope.isUsernameValid = function (u) {
        var isString = typeof u == typeof '';
        if (!isString) return true;
        var m = u.match(/\w{8,}$/) != null;
        console.log('does [' + u + '] match? ' + m);
        return m;
    };

    $scope.confirmPasswordMatches = function (cpw) {
        if (!$scope.user) {
            return true;
        }
        var pw = $scope.user.password;
        return  pw != null && cpw != null && pw == cpw;
    };

    $scope.originalUsername = null;

    $scope.$watch('user.username', function (username) {
        if (username != null && username != '' && username != $scope.originalUsername) {
            userService.isUserNameTaken(username, function (taken) {
                $scope.$apply(function () {
                    $scope.usernameTaken = taken && !($scope.originalUsername == username);
                });
            });
        } else {
            $scope.usernameTaken = false;
        }
    });

    $scope.loadUser = $scope.loadUser || function (userId) {
        userService.getUserById(userId, function (u) {
            $scope.$apply(function () {
                $scope.user = u;
                $scope.user.passwordConfirmation = u.password; // set it to the be the same thing initially
                $scope.originalUsername = u.username; // so we can see if the 'conflict' is our self
                $rootScope.$broadcast(userLoadedEvent, $scope.user.id);
            });
        });
    };

    $scope.saveProfileData = $scope.saveProfileData || function () {
        userService.updateUserById($scope.user.id, $scope.user.username, $scope.user.password, $scope.user.firstName, $scope.user.lastName, function (u) {
            $scope.$apply(function () {
                $scope.user = u;
            });
        });
    };


//  $scope.loadUser(crmSession.getUserId());


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
function SignInController($scope, $location) {
    /// todo remove this and introduce Spring Security's RememberMe service !
    jso_wipe();

    // 'u' is the username request parameter that we'll expect to preload the username
    // typically after some one's successfully finished the signup flow
    var u = crmSession.getUsername();
    if (u != null) {
        $scope.user = { username:u };
    }

    //$scope.user = { username : 'joshy@joshlong.com',password:'cowbell'}

    $scope.signinWithFacebook = function () {
        var facebookForm = $('#signinWithFacebook');
        facebookForm.submit();
        console.log('signing in with facebook')
    };
}


/**
 * Designed to support the creation and import of customer data into
 * the CRM from LinkedIn using Spring Social. Also, you can enter data
 * in the form directly, if you like.
 *
 * @param $scope
 * @param ajaxUtils
 *
 * @constructor
 *
 */
function CustomerController($scope, customerService, ajaxUtils) {

    // the user id of the currently logged in user
    var userId = crmSession.getUserId();


    $scope.customers = [];

    function resetNewCustomerForm() {
        $scope.firstName = null;
        $scope.lastName = null
    }

    function customerById(id, cb) {
        for (var i = 0; i < $scope.customers.length; i++)
            if ($scope.customers[i].id == id)
                cb($scope.customers[i], i);
    }

    $scope.deleteCustomer = function (id) {
        console.log('delete customer #' + id);
        customerById(id, function (customer, arrIndx) {

            customerService.deleteCustomerById(userId, customer.id, function () {
                $scope.refreshCustomers()
            });
            //$scope.customers[arrIndx].remove();
        });
        console.log('deleteCustomer() does not currently actually delete a resource on the server...');
    };

    $scope.updateCustomer = function (id) {
        console.log('update customer #' + id);
        customerById(id, function (customer, arrIndx) {
            customerService.updateCustomerById(userId, customer.id, customer.firstName, customer.lastName, $scope.refreshCustomers)
        });
    };

    $scope.refreshCustomers = function () {
        customerService.loadCustomers(userId, function (customers) {
            $scope.$apply(function () {
                $scope.customers = customers;
                console.log('refreshed customers result = ' + customers.length)
            });
        })
    }
    ;

    $scope.addCustomer = function () {
        var fn = $scope.firstName, ln = $scope.lastName;

        customerService.addNewCustomer(userId, $scope.firstName, $scope.lastName, function () {
            $scope.refreshCustomers();
            resetNewCustomerForm();
        });
    };
    resetNewCustomerForm();

   // $scope.refreshCustomers();

}


/**
 * a lot of the logic that we want to provide here is already present in the more
 * sophisticated <CODE>ProfileController</code> controller, and <CODE>SignInController</CODE>,
 * so we simply reuse those controllers when building up this object. This is
 * the nice part about angular.js controllers: they compose naturally
 * because they're just functions.
 *
 * @param $rootScope
 * @param $scope
 * @param ajaxUtils
 * @param userService
 * @constructor
 */
function SignUpController($rootScope, $scope, $q, $timeout, ajaxUtils, userService, $location) {
    $scope.user = {username:null, firstName:null, lastName:null };

    $scope.loadUser = function () {
    }; // noop

    $scope.saveProfileData = function () {
        userService.registerNewUser($scope.user.username, $scope.user.password, $scope.user.firstName, $scope.user.lastName, false, function (u) {
            var urlToRedirectTo = ajaxUtils.url('/crm/signin.html?username=' + u.username);
            window.location.href = urlToRedirectTo;
        });
    };
    ProfileController($rootScope, $scope, $q, $timeout, ajaxUtils, userService);
    SignInController($scope, $location);



}