package org.springsource.crm.android.view;

import android.app.*;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.*;
import android.widget.Toast;
import org.springframework.social.connect.*;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.*;
import org.springsource.crm.Crm;
import org.springsource.crm.android.*;

public class CrmOAuthActivity extends Activity {
    private static final String TAG = CrmOAuthActivity.class.getSimpleName();
    private OAuth2ConnectionFactory<Crm> crmConnectionFactory;
    private OAuth2Operations oAuth2Operations;
    private WebView webView;
    private ProgressDialog progressDialog;
    private CrmApplicationContext crmApplicationContext;
    private ConnectionRepository connectionRepository;
    private String loadingMessage = "Loading...";
    private String authorizationUrl = "/oauth/authorize";

    protected String getScope() {
        return "read,write";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        this.webView = new WebView(this);
        setContentView(webView);
        this.crmApplicationContext = CrmApplicationContext.currentApplicationContext();
        this.crmConnectionFactory = crmApplicationContext.getConnectionFactory();
        this.oAuth2Operations = crmApplicationContext.getOAuthOperations();
        this.connectionRepository = crmApplicationContext.getConnectionRepository();

        final Activity activity = this;
        this.webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activity.setTitle(loadingMessage);
                activity.setProgress(progress * 100);
                if (progress == 100) {
                    activity.setTitle(R.string.app_name);
                }
            }
        });
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.setWebViewClient(new CrmOAuthWebViewClient());
    }

    public void showLoadingProgressDialog() {
        showProgressDialog(loadingMessage);
    }

    public void showProgressDialog(CharSequence message) {
        if (this.progressDialog == null) {
            this.progressDialog = new ProgressDialog(this);
            this.progressDialog.setIndeterminate(true);
        }

        this.progressDialog.setMessage(message);
        this.progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // http://127.0.0.1:8080/oauth/authorize?response_type=token&state=449b9a0e-55a6-4b1f-a241-3ac03088c5ad&redirect_uri=http%3A%2F%2F127.0.0.1%3A8080%2Fcrm%2Fprofile.html&client_id=html5-crm&scope=read%20write
        this.webView.loadUrl(buildAuthorizeUrl());
    }

    protected String buildAuthorizeUrl() {
        String redirectUri = crmApplicationContext.getPathUtils().buildUri("/crm/profile.html").toString();
        OAuth2Parameters params = new OAuth2Parameters();
        params.setScope("read,write");
        params.setRedirectUri(redirectUri);
        params.add("display", "touch");
        String authorizeUrl = oAuth2Operations.buildAuthorizeUrl(GrantType.IMPLICIT_GRANT, params);
        System.out.println("the authorization url is " + authorizeUrl);
        return authorizeUrl;
    }

    /**

     */
    /*
        private void displayFacebookOptions() {
            Intent intent = new Intent();
            intent.setClass(this, FacebookActivity.class);
            startActivity(intent);
            finish();
        }*/
    protected void displayCrmOptions() {

        System.out.println("displayCrmOptions() does nothing at the moment! this should show the sign in screen!");

    }

    private class CrmOAuthWebViewClient extends WebViewClient {

        /*
         * The WebViewClient has another method called shouldOverridUrlLoading which does not capture the javascript
         * redirect to the success page. So we're using onPageStarted to capture the url.
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // parse the captured url
            Uri uri = Uri.parse(url);

            Log.d(TAG, url);

			/*
             * The access token is returned in the URI fragment of the URL. See the Desktop Apps section all the way
			 * at the bottom of this link:
			 *
			 * http://developers.facebook.com/docs/authentication/
			 *
			 * The fragment will be formatted like this:
			 *
			 * #access_token=A&expires_in=0
			 */
            String uriFragment = uri.getFragment();

            // confirm we have the fragment, and it has an access_token parameter
            if (uriFragment != null && uriFragment.startsWith("access_token=")) {

				/*
                 * The fragment also contains an "expires_in" parameter. In this
				 * example we requested the offline_access permission, which
				 * basically means the access will not expire, so we're ignoring
				 * it here
				 */
                try {
                    // split to get the two different parameters
                    String[] params = uriFragment.split("&");

                    // split to get the access token parameter and value
                    String[] accessTokenParam = params[0].split("=");

                    // get the access token value
                    String accessToken = accessTokenParam[1];

                    // create the connection and persist it to the repository
                    AccessGrant accessGrant = new AccessGrant(accessToken);
                    Connection<Crm> connection = crmConnectionFactory.createConnection(accessGrant);
                    try {
                        connectionRepository.addConnection(connection);
                    } catch (DuplicateConnectionException e) {
                        // connection already exists in repository!
                    }
                } catch (Exception e) {
                    // don't do anything if the parameters are not what is expected
                }
                displayCrmOptions();
            }


			/*
             * if there was an error with the oauth process, return the error description
			 *
			 * The error query string will look like this:
			 *
			 * ?error_reason=user_denied&error=access_denied&error_description=The +user+denied+your+request
			 */
            if (uri.getQueryParameter("error") != null) {
                CharSequence errorReason = uri.getQueryParameter("error_description").replace("+", " ");
                Toast.makeText(getApplicationContext(), errorReason, Toast.LENGTH_LONG).show();
                displayCrmOptions();
            }
        }
    }
}
