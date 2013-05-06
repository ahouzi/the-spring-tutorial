package org.springsource.crm.android.view;

import android.app.*;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import org.springframework.http.HttpStatus;
import org.springframework.social.connect.DuplicateConnectionException;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.util.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springsource.crm.android.*;

import java.util.Map;

/**
 * @author Josh Long
 */
public class SignInActivity extends Activity {
    private static final String TAG = SignInActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private String appAuthorizationError = "Your email or password was entered incorrectly.";
    private String okBtn = "OK";
    private String loadingPleaseWaitMessage = "Loading. Please wait...";
    private String signingInMessage = "Signing in...";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        editText(R.id.username).setText("starbuxman");
        editText(R.id.password).setText("cowbell");

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                finish();
            }
        });

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (validateFormData()) {
                    SignInTask signInTask = new SignInTask();
                    signInTask.execute();
                } else {
                    displayAppAuthorizationError(appAuthorizationError);
                }
            }
        });
    }

    private boolean validateFormData() {
        return getUsername().length() > 0 && getPassword().length() > 0;
    }
/*
    private void displayGreenhouseOptions() {
        Intent intent = new Intent();
//      todo   intent.setClass(this, MainActivity.class);

        startActivity(intent);
        setResult(RESULT_OK);
        finish();
    }*/

    protected String valueForEditText(int i) {
        EditText editText = editText(i);
        return editText.getText().toString().trim();
    }

    protected EditText editText(int i) {
        return (EditText) findViewById(i);
    }

    private void displayAppAuthorizationError(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(okBtn, null)
                .create();
        alertDialog.show();
    }

    private AccessGrant extractAccessGrant(Map<String, Object> result) {
        return new AccessGrant((String) result.get("access_token"), (String) result.get("scope"),
                (String) result.get("refresh_token"), getIntegerValue(result, "expires_in"));
    }

    // Retrieves object from map into an Integer, regardless of the object's actual type. Allows for flexibility in object type (eg, "3600" vs 3600).
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        try {
            return Integer.valueOf(String.valueOf(map.get(key))); // normalize to String before creating integer value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected void exception(Throwable th) {
        Log.d(TAG, th.getMessage(), th);
    }

    public void showProgressDialog() {
        showProgressDialog(this.loadingPleaseWaitMessage);
    }

    public void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
        }

        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    protected String getUsername() {
        return valueForEditText(R.id.username);
    }

    protected String getPassword() {
        return valueForEditText(R.id.password);
    }

    protected void displayCrmOptions() {
        System.out.println("Starting CRM flow.");
    }

    private class SignInTask extends AsyncTask<Void, Void, Void> {
        private MultiValueMap<String, String> formData;
        private CrmApplicationContext crmApplicationContext = CrmApplicationContext.currentApplicationContext();
        private Throwable exception;
        private String invalidEmailOrPasswordMessage = "Your email or password was entered incorrectly.";
        private String connectionAlreadyExistsMessage = "the connection already exists";
        private String networkConnectionErrorMessage = "A problem occurred with the network connection.";

        @Override
        protected void onPreExecute() {
            showProgressDialog(signingInMessage);
            formData = new LinkedMultiValueMap<String, String>();
            formData.add("grant_type", "password");
            formData.add("username", getUsername());
            formData.add("password", getPassword());
            formData.add("client_id", crmApplicationContext.getClientId());
            formData.add("client_secret", crmApplicationContext.getClientSecret());
            formData.add("scope", "read,write");
            Log.d(TAG, formData.toString());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dismissProgressDialog();
            if (exception != null) {
                String message = null;
                if (exception instanceof HttpClientErrorException) {
                    HttpClientErrorException httpClientErrorException = (HttpClientErrorException) exception;
                    if (httpClientErrorException.getStatusCode() == HttpStatus.BAD_REQUEST || httpClientErrorException.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        message = invalidEmailOrPasswordMessage;
                    }
                } else if (exception instanceof DuplicateConnectionException) {
                    message = connectionAlreadyExistsMessage;
                } else {
                    Log.e(TAG, exception.getLocalizedMessage(), exception);
                    message = this.networkConnectionErrorMessage;
                }
                displayAppAuthorizationError(message);
            } else {
                displayCrmOptions();
            }
        }
        @Override
        protected Void doInBackground(Void... params) {
            return null ;
        }
    }
/*    todo
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String url = crmApplicationContext.getPathUtils().buildUri("/oauth/token").toString();
                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(formData, requestHeaders);
                RestTemplate restTemplate = new RestTemplate(true);
                Map<String, Object> responseBody = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class).getBody();
                Log.d(TAG, responseBody.toString());

                // Persist the connection and AccessGrant to the repository
                AccessGrant accessGrant = extractAccessGrant(responseBody);
                 ConnectionFactory connectionFactory = crmApplicationContext.getConnectionFactory();
                ConnectionRepository connectionRepository = crmApplicationContext.getConnectionRepository();
                Connection<Crm> connection = connectionFactory.createConnection(accessGrant);
                connectionRepository.addConnection(connection);
            } catch (Throwable th) {
                this.exception = th;
            }
            return null;
        }*/

/*    /*//***************************************
     // Private classes
     /*//***************************************
     private class SignInTask extends AsyncTask<Void, Void, Void> {
     private MultiValueMap<String, String> formData;

     private Exception exception;

     @Override protected void onPreExecute() {
     showProgressDialog("Signing in...");

     EditText editText = (EditText) findViewById(R.id.username);
     String username = editText.getText().toString().trim();

     editText = (EditText) findViewById(R.id.password);
     String password = editText.getText().toString().trim();

     formData = new LinkedMultiValueMap<String, String>();
     formData.add("grant_type", "password");
     formData.add("username", username);
     formData.add("password", password);
     formData.add("client_id", getApplicationContext().getClientId());
     formData.add("client_secret", getApplicationContext().getClientSecret());
     formData.add("scope", "read,write");
     Log.d(TAG, formData.toString());
     }

     @Override
     @SuppressWarnings("unchecked")
     protected Void doInBackground(Void... params) {
     try {
     final String url = getApplicationContext().getApiUrlBase() + "oauth/token";
     Log.d(TAG, url);
     HttpHeaders requestHeaders = new HttpHeaders();
     requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
     HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(formData, requestHeaders);
     RestTemplate restTemplate = new RestTemplate(true);
     Map<String, Object> responseBody = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class).getBody();
     Log.d(TAG, responseBody.toString());

     // Persist the connection and AccessGrant to the repository
     AccessGrant accessGrant = extractAccessGrant(responseBody);
     GreenhouseConnectionFactory connectionFactory = getApplicationContext().getConnectionFactory();
     ConnectionRepository connectionRepository = getApplicationContext().getConnectionRepository();
     Connection<Greenhouse> connection = connectionFactory.createConnection(accessGrant);
     connectionRepository.addConnection(connection);
     } catch (Exception e) {
     Log.e(TAG, e.getLocalizedMessage(), e);
     this.exception = e;
     }

     return null;
     }

     @Override protected void onPostExecute(Void v) {
     dismissProgressDialog();
     if (exception != null) {
     String message;
     if (exception instanceof HttpClientErrorException
     && (((HttpClientErrorException) exception).getStatusCode() == HttpStatus.BAD_REQUEST)
     || ((HttpClientErrorException) exception).getStatusCode() == HttpStatus.UNAUTHORIZED) {
     message = "Your email or password was entered incorrectly.";
     } else if (exception instanceof DuplicateConnectionException) {
     message = "The connection already exists.";
     } else {
     Log.e(TAG, exception.getLocalizedMessage(), exception);
     message = "A problem occurred with the network connection. Please try again in a few minutes.";
     }
     displayAppAuthorizationError(message);
     } else {
     displayGreenhouseOptions();
     }
     }
     }*/

}
