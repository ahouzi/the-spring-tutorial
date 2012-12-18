package org.springsource.examples.spring31.services;


import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.BaseClientDetails;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * RunBothClientAndServer to manage {@link User users} that in turn can manage {@link Customer customers}.
 *
 * @author Josh Long
 */
@SuppressWarnings("unchecked")
@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserService implements ClientDetailsService, UserDetailsService {

    public static final String USER_CACHE_REGION = "users";

    private Logger logger = Logger.getLogger(getClass().getName());

    // variables for the file resizing
    private long defaultImageWidth = 300;
    private Map<String, Set<String>> multiMapOfExtensionsToVariants = new ConcurrentHashMap<String, Set<String>>();
    private String convertCommandPath = "/usr/local/bin/convert";

    // oauth
    private String defaultOauth2GrantTypes = "authorization_code,implicit";
    private String defaultOauth2Scopes = "read,write";
    private String defaultOauth2Resource = "crm";

    private TransactionTemplate transactionTemplate;
    private GridFsTemplate gridFsTemplate;
    private EntityManager entityManager;

    @PostConstruct
    public void begin() {

        Set<String> t = new ConcurrentSkipListSet<String>();
        Collections.addAll(t, "jpeg");

        multiMapOfExtensionsToVariants.put("jpg", t);
        multiMapOfExtensionsToVariants.put("gif", new HashSet<String>());
        multiMapOfExtensionsToVariants.put("png", new HashSet<String>());

        for (String k : this.multiMapOfExtensionsToVariants.keySet())
            multiMapOfExtensionsToVariants.get(k).add(k);

        for (String k : this.multiMapOfExtensionsToVariants.keySet())
            logger.info(k + "=" + this.multiMapOfExtensionsToVariants.get(k));
    }

    @PersistenceContext
    public void setEntityManger(EntityManager em) {
        this.entityManager = em;
    }


    @Inject
    public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
        this.transactionTemplate.afterPropertiesSet();
    }

    @Inject
    public void setGridFsTemplate(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public User updateUser(long userId, String email, String pw) {
        User user = getUserById(userId);
        user.setEmail(email);
        user.setPassword(pw);
        entityManager.merge(user);
        return getUserById(userId);
    }

    public User createOrGet(String user, String pw) {
        User usr;
        if ((usr = login(user, pw)) == null) {
            usr = createUser(user, pw);
        }
        assert usr != null : "there must be a valid reference for the user to be returned";
        return usr;
    }

    public User login(String email, String password) {
        User user = loginByUsername(email);

        if (user != null && user.getPassword().equalsIgnoreCase(password)) {
            return user;
        }
        return null;
    }

    public User createUser(String email, String pw) {
        // first make sure it doesn't already exist
        assert StringUtils.hasText(email) : "the 'email' can't be null";
        assert StringUtils.hasText(pw) : "the 'password' can't be null";
        assert loginByUsername(email) == null : "there is already an existing User with the email '" + email + "'";

        User user = new User();
        user.setEmail(email);
        user.setPassword(pw);
        user.setSignupDate(new Date());
        entityManager.persist(user);
        return user;
    }

    @Cacheable(value = USER_CACHE_REGION)
    public User loginByUsername(String user) {
        Collection<User> customers = entityManager.createQuery("select u from  " + User.class.getName() + " u where u.email = :email", User.class)
                .setParameter("email", user)
                .getResultList();
        if (customers.size() > 0)
            return customers.iterator().next();
        return null;
    }

    public void writeUserProfilePhotoAndQueueForConversion(long userId, String ogFileName, byte[] bytes) throws Throwable {
        writeUserProfilePhotoAndQueueForConversion(userId, ogFileName, new ByteArrayInputStream(bytes));
    }

    public void writeUserProfilePhotoAndQueueForConversion(long userId, String ogFileName, InputStream inputStream) throws Throwable {
        writeUserProfilePhoto(userId, ogFileName, inputStream);
        String ext = deriveFileExtension(ogFileName);
        convertAndResizeUserProfilePhoto(userId, ext);
        if (logger.isDebugEnabled())
            logger.debug("sent a request to process the userId[" + userId + "] and extension [" + ext + "]");
    }

    protected void resizeToWidth(File file, File output, long width) throws Throwable {

        List<String> listOfString = Arrays.asList(convertCommandPath, file.getAbsolutePath(), "-resize " + width + "x", output.getAbsolutePath());

        String totalCommand = org.apache.commons.lang.StringUtils.join(listOfString, " ");

        Process process = Runtime.getRuntime().exec(totalCommand);
        int retCode = process.waitFor();
        assert retCode == 0 && output.exists() :
                "Something went wrong with running the 'convert'" +
                        " command. The return / exit code is " + retCode +
                        " and the full output is:" + IOUtils.toString(process.getErrorStream()) +
                        ". There should be a file at " + output.getAbsolutePath();

    }

    public long convertAndResizeUserProfilePhoto(Long userId, String fileExtension) throws Throwable {
        InputStream profilePhotoBytesFromGridFs = null, fileInputStream = null;
        OutputStream outputStream = null;
        File tmpStagingFile = null, convertedFile = null;

        try {
            User user = getUserById(userId);
            assert user != null : "the user reference should still be valid!";
            tmpStagingFile = File.createTempFile("profilePhoto" + userId, "." + fileExtension);
            convertedFile = File.createTempFile("profilePhotoConvertedAndResized" + userId, ".jpg");
            profilePhotoBytesFromGridFs = readUserProfilePhoto(userId);
            outputStream = new FileOutputStream(tmpStagingFile);
            copyStreamsAndClose(profilePhotoBytesFromGridFs, outputStream);
            resizeToWidth(tmpStagingFile, convertedFile, this.defaultImageWidth);
            fileInputStream = new FileInputStream(convertedFile);
            writeUserProfilePhoto(userId, convertedFile.getName(), fileInputStream);
            logger.debug("wrote converted image for " + userId + ". The temporary staging file is " + convertedFile.getAbsolutePath());
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(profilePhotoBytesFromGridFs);
            IOUtils.closeQuietly(outputStream);
            assert ensureRemovalOfFile(convertedFile) : "the file " + pathForFile(convertedFile) + " must be either be deleted or not exist.";
            assert ensureRemovalOfFile(tmpStagingFile) : "the file " + pathForFile(convertedFile) + " must be either deleted or not exist.";
        }


        return userId;
    }


    private String pathForFile(File fi) {
        return fi == null ? "null" : fi.getAbsolutePath();
    }

    /**
     * copy the streams, and make sure that the streams' descriptors are closed.
     *
     * @param inputStream  the input stream
     * @param outputStream the output stream
     */
    private void copyStreamsAndClose(InputStream inputStream, OutputStream outputStream) throws Exception {
        assert null != inputStream : "the input stream can't be null";
        assert null != outputStream : "the output stream can't be null";
        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

    }

    public void writeUserProfilePhoto(long userId, String ogFileName, InputStream inputStream) throws Throwable {
        final String ext = deriveFileExtension(ogFileName);
        final User usr = getUserById(userId);
        String fileName = fileNameForUserIdProfilePhoto(userId);
        entityManager.refresh(usr);
        Query q = new Query(Criteria.where("filename").is(fileName));
        gridFsTemplate.delete(q);
        gridFsTemplate.store(inputStream, fileName);
        usr.setProfilePhotoExt(ext);
        usr.setProfilePhotoImported(true);
        entityManager.merge(usr);
    }

    public void writeUserProfilePhoto(long userId, String ogFileName, byte[] bytes) throws Throwable {
        writeUserProfilePhoto(userId, ogFileName, new ByteArrayInputStream(bytes));
    }

    public InputStream readUserProfilePhoto(long userId) {
        User user = getUserById(userId);
        assert user != null : "you must specify a valid userId";
        String fileName = fileNameForUserIdProfilePhoto(userId);
        if (logger.isInfoEnabled())
            logger.info("looking for file '" + fileName + "'");
        GridFSDBFile gridFSFile;
        if ((gridFSFile = gridFsTemplate.findOne(criteriaQueryFor(fileName))) == null) {
            logger.debug("couldn't find the user profile byte[]s for user #" + userId);
            return null;
        }
        return gridFSFile.getInputStream();
    }

    public void removeUser(long userId) {
        User user = getUserById(userId);
        entityManager.remove(user);
    }

    public User getUserById(long id) {
        return this.entityManager.find(User.class, id);
    }

    @Override
    public CrmUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new CrmUserDetails(loginByUsername(username));
    }

    public ClientDetails loadClientByClientId(String clientId) throws OAuth2Exception {
        // clientId is the ID of the user
        Long userId = Long.parseLong(clientId);
        User usr = getUserById(userId);

        CrmUserDetails crmUserDetails = loadUserByUsername(usr.getEmail());

        return new CrmClientDetails(
                crmUserDetails.getUsername(),
                defaultOauth2Resource,
                defaultOauth2Scopes,
                defaultOauth2GrantTypes,
                authorities(crmUserDetails),
                null,
                crmUserDetails);
    }

    /**
     * Implementation of Spring Security OAuth's
     * {@link org.springframework.security.oauth2.provider.ClientDetails ClientDetails}
     * contract.
     *
     * @author Josh Long
     */
    public static class CrmClientDetails extends BaseClientDetails {

        private CrmUserDetails userDetails;

        public CrmClientDetails(String clientId, String resourceIds, String scopes, String grantTypes, String authorities, String redirectUris, CrmUserDetails userDetails) {
            super(clientId, resourceIds, scopes, grantTypes, authorities, redirectUris);
            this.userDetails = userDetails;
        }

        public CrmUserDetails getUserDetails() {
            return this.userDetails;
        }

    }

    /**
     * Implementation of Spring Security's {@link org.springframework.security.core.userdetails.UserDetails UserDetails} contract
     *
     * @author Josh Long
     */
    public static class CrmUserDetails implements UserDetails {


        // mostly for use in the Oauth stuff
        public static final String SCOPE_READ = "read";


        public static final String SCOPE_WRITE = "write";

        public static final String ROLE_USER = "ROLE_USER";

        /**
         * Roles for the user
         */
        private Set<String> roles = new HashSet<String>();
        private Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();

        /**
         * Regular CRM user
         */
        private User user;

        public CrmUserDetails(User user) {
            assert user != null : "the provided user reference can't be null";
            this.user = user;


            this.roles = from(ROLE_USER, SCOPE_READ, SCOPE_WRITE);

            // setup granted authorities
            for (String r : this.roles) {
                grantedAuthorities.add(new SimpleGrantedAuthority(r));
            }
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return this.grantedAuthorities;
        }

        @Override
        public String getPassword() {
            return this.user.getPassword();
        }

        @Override
        public String getUsername() {
            return this.user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return isEnabled();
        }

        @Override
        public boolean isAccountNonLocked() {
            return isEnabled();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return isEnabled();
        }

        @Override
        public boolean isEnabled() {
            return user.isEnabled();
        }

        public User getUser() {
            return this.user;
        }

    }

    private static <T> Set<T> from(T... ex) {
        Set<T> t = new ConcurrentSkipListSet<T>();
        Collections.addAll(t, ex);
        return t;
    }


    private String deriveFileExtension(final String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        for (String k : multiMapOfExtensionsToVariants.keySet()) {
            Collection<String> variants = multiMapOfExtensionsToVariants.get(k);
            for (String var : variants) {
                if (lowerCaseFileName.endsWith(var))
                    return k;
            }
        }
        return null;
    }

    private Query criteriaQueryFor(String fileName) {
        return (new Query().addCriteria(Criteria.where("filename").is(fileName)));
    }

    private String fileNameForUserIdProfilePhoto(long userId) {
        return String.format("user%sprofilePhoto", Long.toString(userId));
    }

    private String authorities(UserDetails userDetails) {
        return org.apache.commons.lang.StringUtils.join(Collections2.transform(userDetails.getAuthorities(), new Function<GrantedAuthority, String>() {
            @Override
            public String apply(GrantedAuthority input) {
                return input.getAuthority();
            }
        }), ',');
    }


    private boolean ensureRemovalOfFile(File file) {
        return null != file && (!file.exists() || file.delete());
    }

}


