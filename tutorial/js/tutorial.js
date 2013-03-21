$(function () {

    var mapOfModulesToLinks = {};

    //// global functions

    //
    // module supporting resolution of SpringSource JavaDoc URLs


    // todo handle gists http://developer.github.com/v3/gists/#list-gists

    function GenericModule(self, bu) {
        self.baseUrl = bu;
        self.urlForCodeReference = function (ref, attrs) {
            return self.baseUrl + StringUtils.encodeFullyQualifiedPath(ref) + '.html';
        };
        self.labelForCodeReference = function (ref, attrs) {
            return StringUtils.classForFullyQualifiedClass(ref);
        };
    }

    /* Specifically for SD MongoDB. There's got to be a cleaner way. */
    function SpringDataMongoDbModule() {
        GenericModule(this, 'http://static.springsource.org/spring-data/data-mongodb/docs/current/api/');
    }

    function GitHubProjectModule(mod) {
        var self = this;
        var ghUrl = 'https://github.com/joshlong/the-spring-tutorial/blob/_Q_/code/' + mod + '/src/main/java/';
        console.log('the github url is ' + ghUrl);
        self.urlForCodeReference = function (ref, attrs) {
            var q = attrs['q'];
            var u = ghUrl.replace('_Q_', q) + StringUtils.encodeFullyQualifiedPath(ref) + '.java';
            return u;
        };
        self.labelForCodeReference = function (ref, attrs) {
            return StringUtils.classForFullyQualifiedClass(ref);
        };
    }

    function JavaModule(baseUrl) {
        GenericModule(this, 'http://docs.oracle.com/javase/7/docs/api/');
    }


    function ServletsModule(baseUrl) {
        GenericModule(this, 'http://docs.oracle.com/javaee/6/api/');
    }

    function SpringModule(baseUrl) {
        GenericModule(this, 'http://static.springsource.org/spring/docs/current/javadoc-api/');
    }

    function SpringSecurityModule(baseUrl) {
        GenericModule(this, 'http://static.springsource.org/spring-security/site/docs/current/apidocs/')
    }


    //
    // generic visitor function that can be used to cherry pick DOM elements and
    // return the correct implementations of the DOM elements based on CSS selectors
    function visitElements(css, attrs, callback) {
        $(css).each(function (indx) {
            var node = $(this);
            var classRefValue = node.html().trim();
            var collectedAttributes = {};
            for (var i = 0; i < attrs.length; i++)
                collectedAttributes[ attrs[i]] = node.attr(attrs[i]);
            callback(node, classRefValue, collectedAttributes);
        });

    }

    // todo class-reference && module


    // first lets bootstrap
    visitElements('.class-reference', ['module'], function (node, val, attrs) {
        mapOfModulesToLinks [attrs['module']] = {};
    });

    // then we register module handlers for the 'spring-*' modules
    for (var x in mapOfModulesToLinks) {
        if (x.indexOf('spring-') != -1)
            mapOfModulesToLinks[x] = new SpringModule();
    }
    mapOfModulesToLinks ['servlets'] = new ServletsModule();
    mapOfModulesToLinks ['java'] = new JavaModule();
    mapOfModulesToLinks['spring-data-mongodb'] = new SpringDataMongoDbModule();
    mapOfModulesToLinks['spring-security'] = new SpringSecurityModule();

    var codeModules = 'services,web'.split(',');
    for (var i = 0; i < codeModules.length; i++) {
        var m = codeModules[i];
        mapOfModulesToLinks[m] = new GitHubProjectModule(m);
    }

    visitElements('.git-code', ['q', 'extension', 'module'], function (node, val, attrs) {
        var q = attrs['q'],
            mod = attrs['module'],
            ext = attrs['extension'],
            fn = val;
        $.getGithubFqnFile(mod, q, fn, ext, function (data) {
            console.log(StringUtils.code(data))
            node.html(StringUtils.code(data));
        });
    });

    visitElements('.git-gist', ['q' , 'module', 'gist'], function (node, val, attrs) {
        var q = attrs['q'], gist = attrs['gist'], mod = attrs['module'];
        var user = 'joshlong'
        $.getGithubGist(user, gist, function (data) {
            var html = StringUtils.code(data)
            var url = 'https://gist.github.com/' + user + '/' + gist + '/edit';

            html += '<div style="font-family:sans-serif;font-size:smaller;"><a target="_blank" href="' + url + '">GIST # ' + gist + '</a></div>';
            node.html(html);

            // maybe we can append something so that i can see where the gist came from ?

        });
    });


    // then we handle .class-reference elements including links to SpringSource and Java (TM) JavaDocs
    visitElements('.class-reference', ['module', 'q'], function (node, val, attrs) {
        try {
            val = val + '';
            var processor = mapOfModulesToLinks[attrs['module']];
            var url = processor.urlForCodeReference(val, attrs),
                label = processor.labelForCodeReference(val, attrs);

            node.html(StringUtils.a(label, url));
        } catch (e) {
            console.log('ERROR! ' + e + ' in processing module ' + attrs['module']);  //  console.log('hit an error ' + e)
        }
    });

    // handle links to wikipedia
    visitElements('.wikipedia', [], function (node, val, attrs) {
        val = val + '';
        var url = 'http://wikipedia.org/wiki/' + val.trim();
        node.html(StringUtils.a(val, url));

    });
})
;
