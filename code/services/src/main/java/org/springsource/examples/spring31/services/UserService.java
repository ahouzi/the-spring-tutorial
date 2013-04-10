package org.springsource.examples.spring31.services;


import com.mongodb.gridfs.GridFSDBFile;
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
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * there are several components that need to know and understand this system's notion of users,
 * generally we need an object that knows how to administer users (this class' primary responsibility)
 * and we need an object that can tell Spring Security OAuth how to communicate with the user database
 * (an implementation of {@link UserDetailsService UserDetailsService}, which this class implements,
 * and we need a class that can tell Spring Security OAuth about which rights users have to which
 * resources (an implementation of {@link ClientDetailsService ClientDetailsService}, which this also class
 * implements).
 *
 * @author Josh Long
 */
@SuppressWarnings("unused")
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional
public class UserService implements /*ClientDetailsService, */UserDetailsService {

    public static final String SCOPE_READ = "read";
    public static final String SCOPE_WRITE = "write";

    public static final String ROLE_USER = "ROLE_USER";

    public static final String USER_CACHE_REGION = "users";

    private Logger logger = Logger.getLogger(getClass().getName());

    // variables for the file resizing
    private long imageWidth = 300;
    private String convertCommandPath = "/usr/local/bin/convert";
    private Map<String, Set<String>> multiMapOfExtensionsToVariants = new ConcurrentHashMap<String, Set<String>>();

    private GridFsTemplate gridFsTemplate;
    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManger(EntityManager em) {
        this.entityManager = em;
    }

    @PostConstruct
    public void begin() {

        String jpg = "jpg", gif = "gif", png = "png";

        // build up base variants collection
        for (String e : new String[]{jpg, gif, png})
            multiMapOfExtensionsToVariants.put(e, new ConcurrentSkipListSet<String>());

        // add each key as a variant, even though its the canonical variant
        for (String k : this.multiMapOfExtensionsToVariants.keySet())
            multiMapOfExtensionsToVariants.get(k).add(k);

        // add others
        multiMapOfExtensionsToVariants.get(jpg).add("jpeg");

        if (logger.isDebugEnabled())
            for (String k : this.multiMapOfExtensionsToVariants.keySet())
                logger.debug(k + "=" + this.multiMapOfExtensionsToVariants.get(k));
    }

    public void setConvertCommandPath(String cmd) {
        this.convertCommandPath = cmd;
    }


    public void setImageWidth(long imageWidth) {
        this.imageWidth = imageWidth;
    }

    @Inject
    public void setGridFsTemplate(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public User updateUser(long userId, String un, String pw, String fn, String ln, boolean importedFromServiceProvider) {
        User user = getUserById(userId);
        String oldUserName = user.getUsername();
        user.setUsername(un);
        user.setFirstName(fn);
        user.setLastName(ln);
        user.setPassword(pw);
        user.setImportedFromServiceProvider(importedFromServiceProvider);
        entityManager.merge(user);
        return getUserById(userId);
    }

    /**
     * todo optimize this with a faster query that simply does a COUNT(*) or something
     */
    public boolean isUserNameAlreadyTaken(String username) {
        User u = this.loginByUsername(username);
        return u != null;
    }

    public User createOrGet(String user, String pw, String fn, String ln, boolean importedFromServiceProvider) {
        User usr;
        if ((usr = login(user, pw)) == null) {
            usr = createUser(user, pw, fn, ln, importedFromServiceProvider);
        }
        assert usr != null : "there must be a valid reference for the user to be returned";
        usr.setEnabled(true);
        usr.setImportedFromServiceProvider(importedFromServiceProvider);
        return usr;
    }

    public User login(String username, String password) {
        User user = loginByUsername(username);

        if (user != null && user.getPassword().equalsIgnoreCase(password)) {
            return user;
        }
        return null;
    }

    public User createUser(String username, String pw, String fn, String ln, boolean imported) {
        // first make sure it doesn't already exist
        assert StringUtils.hasText(username) : "the 'username' can't be null";
        assert StringUtils.hasText(pw) : "the 'password' can't be null";
        assert loginByUsername(username) == null : "there is already an existing User with the username '" + username + "'";

        User user = new User();
        user.setUsername(username);
        user.setFirstName(fn);
        user.setLastName(ln);
        user.setImportedFromServiceProvider(imported);
        user.setPassword(pw);
        user.setEnabled(true);
        user.setSignupDate(new Date());
        entityManager.persist(user);
        return user;
    }

    @Cacheable(value = USER_CACHE_REGION)
    public User loginByUsername(String user) {
        Collection<User> customers = entityManager.createQuery("select u from  " + User.class.getName() + " u where u.username = :username", User.class)
                .setParameter("username", user)
                .getResultList();
        return firstOrNull(customers);
    }

    public User loginByUsernameAndPassword(String user, String pw) {
        Collection<User> customers = entityManager.createQuery("select u from  " + User.class.getName() + " u where u.username = :u and u.password = :p", User.class)
                .setParameter("u", user)
                .setParameter("p", pw)
                .getResultList();
        return firstOrNull(customers);

    }

    public void writeUserProfilePhotoAndQueueForConversion(long userId, String ogFileName, byte[] bytes) throws Throwable {
        writeUserProfilePhotoAndQueueForConversion(userId, ogFileName, new ByteArrayInputStream(bytes));
    }

    public void writeUserProfilePhotoAndQueueForConversion(long userId, String ogFileName, InputStream inputStream) throws Throwable {
        writeUserProfilePhoto(userId, ogFileName, inputStream);
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


    public InputStream readUserProfilePhoto(long userId) {
        User user = getUserById(userId);
        assert user != null : "you must specify a valid userId";
        String fileName = fileNameForUserIdProfilePhoto(userId);
        if (logger.isInfoEnabled())
            logger.info("looking for file '" + fileName + "'");
        GridFSDBFile gridFSFile;
        if ((gridFSFile = gridFsTemplate.findOne((new Query().addCriteria(Criteria.where("filename").is(fileName))))) == null) {
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
        User user = loginByUsername(username);
        if (null == user)
            return null;
        return new CrmUserDetails(user);
    }


    /**
     * Implementation of Spring Security's {@link org.springframework.security.core.userdetails.UserDetails UserDetails} contract
     */
    public static class CrmUserDetails implements UserDetails {


        private Collection<GrantedAuthority> grantedAuthorities;
        private User user;

        public CrmUserDetails(User user) {
            assert user != null : "the provided user reference can't be null";
            this.user = user;
            this.grantedAuthorities = new ArrayList<GrantedAuthority>();

            for (String ga : Arrays.asList(ROLE_USER, SCOPE_READ, SCOPE_WRITE))
                this.grantedAuthorities.add(new SimpleGrantedAuthority(ga));

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
            return this.user.getUsername();
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

    private String fileNameForUserIdProfilePhoto(long userId) {
        return String.format("user%sprofilePhoto", Long.toString(userId));
    }


    private boolean ensureRemovalOfFile(File file) {
        return null != file && (!file.exists() || file.delete());
    }

    private <T> T firstOrNull(Collection<T> t) {
        return t.size() > 0 ? t.iterator().next() : null;
    }
}