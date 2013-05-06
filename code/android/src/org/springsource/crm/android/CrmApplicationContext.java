package org.springsource.crm.android;

import android.content.Context;
import android.os.StrictMode;
import org.springframework.security.crypto.encrypt.*;
import org.springframework.social.ApiException;
import org.springframework.social.connect.*;
import org.springframework.social.connect.sqlite.SQLiteConnectionRepository;
import org.springframework.social.connect.sqlite.support.SQLiteConnectionRepositoryHelper;
import org.springframework.social.connect.support.*;
import org.springframework.social.oauth2.*;
import org.springsource.crm.*;
import org.springsource.crm.utils.PathUtils;

/**
 * A place to obtain important infrastructure services.
 *
 * @author Josh Long
 */
public class CrmApplicationContext {

    private static CrmApplicationContext APPLICATION_CONTEXT;
    private ConnectionRepository connectionRepository;
    private ApiAdapter<Crm> crmAdapter = new ApiAdapter<Crm>() {
        public boolean test(Crm crm) {
            try {
                crm.userOperations().getUserProfile();
                return true;
            } catch (ApiException e) {
                return false;
            }
        }

        public void setConnectionValues(Crm api, ConnectionValues values) {
            User profile = api.userOperations().getUserProfile();
            values.setProviderUserId(Long.toString(profile.getId()));
            values.setDisplayName(profile.getUsername());
            values.setProfileUrl("profile url");
            values.setImageUrl("picture url");
        }

        public UserProfile fetchUserProfile(Crm api) {
            User profile = api.userOperations().self();
            return new UserProfileBuilder()
                    .setUsername(profile.getUsername())
                    .setFirstName(profile.getFirstName())
                    .setLastName(profile.getLastName())
                    .setName(profile.getUsername())
                    .build();
        }

        public void updateStatus(Crm api, String message) {
            // TODO: add update status functionality
        }
    };
    private ConnectionFactoryRegistry registry;
    private String baseUrl;
    private String clientSecret;
    private String clientId;
    private PathUtils pathUtils;
    private Context applicationScopedContext;
    private OAuth2Template oAuth2Operations;
    private AbstractOAuth2ServiceProvider<Crm> crmServiceProvider;
    private OAuth2ConnectionFactory<Crm> crmOAuth2ConnectionFactory;

    public CrmApplicationContext(Context context) {
        this.applicationScopedContext = context;

        this.baseUrl = context.getString(R.string.base_url);
        this.clientSecret = context.getString(R.string.client_secret);
        this.clientId = context.getString(R.string.client_id);
        this.pathUtils = new PathUtils(this.getApiUrlBase());

        StrictMode.enableDefaults();

        // * Object {client_id: "html5-crm", isDefault: true, redirect_uri: "http://127.0.0.1:8080/crm/profile.html", authorization: "/oauth/authorize", scopes: Array[2]}

        String oauthAuthorizeUrl = this.pathUtils.buildUri("/oauth/authorize").toString();
        String oauthTokenUrl = this.pathUtils.buildUri("/oauth/token").toString();

        this.oAuth2Operations = new OAuth2Template(this.clientId, this.clientSecret, oauthAuthorizeUrl, oauthTokenUrl);
        this.crmServiceProvider = new AbstractOAuth2ServiceProvider<Crm>(oAuth2Operations) {
            public Crm getApi(String accessToken) {
                return new CrmTemplate(accessToken, baseUrl);
            }
        };
        this.crmOAuth2ConnectionFactory = new OAuth2ConnectionFactory<Crm>("crm", crmServiceProvider, this.crmAdapter);
        SQLiteConnectionRepositoryHelper sqLiteConnectionRepositoryHelper = new SQLiteConnectionRepositoryHelper(this.applicationScopedContext);
        TextEncryptor textEncryptor = AndroidEncryptors.noOpText();// text(this.encryptionPassword, this.encryptionSalt);

        this.registry = new ConnectionFactoryRegistry();
        this.registry.addConnectionFactory(crmOAuth2ConnectionFactory);
        this.connectionRepository = new SQLiteConnectionRepository(sqLiteConnectionRepositoryHelper, this.registry, textEncryptor);

        register(this);
    }

    public static CrmApplicationContext currentApplicationContext() {
        return APPLICATION_CONTEXT;
    }

    private static void register(CrmApplicationContext crmApplicationContext) {
        APPLICATION_CONTEXT = crmApplicationContext;
    }

    public OAuth2Operations getOAuthOperations() {
        return this.oAuth2Operations;
    }

    public OAuth2ConnectionFactory<Crm> getConnectionFactory() {
        return this.crmOAuth2ConnectionFactory;
    }

    public PathUtils getPathUtils() {
        return this.pathUtils;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public String getApiUrlBase() {
        return this.baseUrl;
    }

    public ConnectionRepository getConnectionRepository() {
        return connectionRepository;
    }

    public Connection<Crm> getPrimaryConnection() {
        return getConnectionRepository().findPrimaryConnection(Crm.class);
    }

    public Crm getCrm() {
        Connection<Crm> connection = getPrimaryConnection();
        if (connection != null) {
            return connection.getApi();
        }

        return null;
    }
}
