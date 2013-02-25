;
(function ($) {
    var githubCacheFilePath = [];
    var githubCacheSha = [];

    var fnSuccess =
        function (content, encoding, startLineNum, endLineNum, callback) {

            var contentArray = [], nl = "\n";

            if (content && encoding === "base64")
                contentArray = window.atob(content.replace(/\n/g, "")).split(nl);
            else {
                contentArray = content.split(nl);
            }
            endLineNum = endLineNum || contentArray.length;
            callback(contentArray.slice(startLineNum - 1, endLineNum).join("\n"));
        };

    $.getGithubFileByFilePath =
        function (user, repo, filePath, callback, startLineNum, endLineNum) {
            if (githubCacheFilePath[filePath]) {
                $.getGithubFile(user, repo, githubCacheFilePath[filePath], callback, startLineNum, endLineNum)
            } else {
                $.ajax({
                    type: "GET",
                    url: "https://api.github.com/repos/" + user + "/" + repo + "/contents/" + filePath,
                    dataType: "jsonp",
                    success: function (data) {
                        githubCacheFilePath[filePath] = data.data.sha;
                        $.getGithubFile(user, repo, githubCacheFilePath[filePath], callback, startLineNum, endLineNum)
                    }
                });
            }
        };


    $.getGithubFqnFile = function (mod, q, fn, ext, cb, startLineNum, endLineNum) {

        ext = (ext && ext != null ) ? (ext + '').toLowerCase() : ext;

        console.log('fn='+fn)  ;

        var isJava = ext === 'java'  ;

        var filePath = (isJava ? '_X1_/src/main/java/_X2_' : '_X1_/_X2_')
                        .replace('_X1_', mod)
                        .replace('_X2_',  isJava ? StringUtils.encodeFullyQualifiedPath(fn) + '.' + ext : fn );

        console.log('filepath='+ filePath);

        var url = 'http://githubproxy.cloudfoundry.com/joshlong/the-spring-tutorial/_B_/_M_?file=_F_'
            .replace('_M_', mod)
            .replace('_B_', q)
            .replace('_F_', filePath);

        console.log('url = ' + url);

        $.ajax({
            type: 'GET',
            url: url,
            dataType: 'jsonp',
            success: function (content) {
               // console.log('content=' + content); // fnSuccess(content, '', +startLineNum || 1, +endLineNum || 0, cb);
                cb(content);
            }
        });
    };

/*

    $.getGithubGist =
        function (id, fileName, callback, startLineNum, endLineNum) {

            $.ajax({
                    type: "GET",
                    url: 'https://api.github.com/gists/' + id,
                    dataType: 'jsonp',
                    success: function (data) {
                        for (var f in data.data.files) {
                            var content = null;
                            if ((!fileName && fileName != '' ) || f == fileName) {
                                content = data.data.files[f].content;
                                break
                            }
                        }
                        fnSuccess(content, '', +startLineNum || 1, +endLineNum || 0, callback);
                    }
                }
            );
        };
*/

    $.getGithubGist =
        function (  user, gist, cb ) {

            var nurl =  'http://githubproxy.cloudfoundry.com/gist/_U_/_GIST_'.replace('_U_',user).replace('_GIST_', gist);
            $.ajax({
                    type: "GET",
                    url: nurl,
                    dataType: 'jsonp',
                    success: function (data) {
                       cb(data);
                    }
                }
            );
        };


    $.getGithubFile =
        function (user, repo, sha, callback, startLineNum, endLineNum) {
            if (githubCacheSha[sha]) {
                fnSuccess(githubCacheSha[sha].data.content, githubCacheSha[sha].data.encoding, +startLineNum || 1, +endLineNum || 0, callback);
            } else {
                $.ajax({
                    type: "GET", url: "https://api.github.com/repos/" + user + "/" + repo + "/git/blobs/" + sha, dataType: "jsonp", success: function (data) {
                        githubCacheSha[sha] = data;
                        fnSuccess(githubCacheSha[sha].data.content, githubCacheSha[sha].data.encoding, +startLineNum || 1, +endLineNum || 0, callback);
                    }
                });
            }
        };
}(jQuery));