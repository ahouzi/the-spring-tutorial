package org.springsource.examples.spring31.web.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.connect.*;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles2.TilesConfigurer;
import org.springframework.web.servlet.view.tiles2.TilesView;
import org.springsource.examples.spring31.services.CustomerService;
import org.springsource.examples.spring31.services.User;
import org.springsource.examples.spring31.services.UserService;
import org.springsource.examples.spring31.services.config.ServicesConfiguration;
import org.springsource.examples.spring31.web.CustomerApiController;
import org.springsource.examples.spring31.web.UserApiController;
import org.springsource.examples.spring31.web.interceptors.CrmHttpServletRequestEnrichingInterceptor;
import org.springsource.examples.spring31.web.util.HibernateAwareObjectMapper;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.annotation.Inherited;
import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = {CustomerService.class, CustomerApiController.class, WebMvcConfiguration.class})
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    private int maxUploadSizeInMb = 5 * 1024 * 1024; // 5 MB

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver commonsMultipartResolver() {
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setMaxUploadSize(maxUploadSizeInMb);
        return commonsMultipartResolver;
    }

    @Bean
    public MappingJacksonHttpMessageConverter mappingJacksonHttpMessageConverter() {
        MappingJacksonHttpMessageConverter mappingJacksonHttpMessageConverter = new MappingJacksonHttpMessageConverter();
        mappingJacksonHttpMessageConverter.setObjectMapper(new HibernateAwareObjectMapper());
        mappingJacksonHttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON));
        return mappingJacksonHttpMessageConverter;
    }

    @Inject  private MappingJacksonHttpMessageConverter mappingJacksonHttpMessageConverter  ;

    // todo show how to contribute custom HttpMessageConverters and why, in this case, to handle Hibernate's lazy collections over json
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJacksonHttpMessageConverter );
    }


    @Bean
    public UrlBasedViewResolver viewResolver() {
        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
        viewResolver.setViewClass(TilesView.class);
        return viewResolver;
    }

    @Bean
    public TilesConfigurer tilesConfigurer() {
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions(new String[]{
                "/WEB-INF/layouts/tiles.xml",
                "/WEB-INF/views/**/tiles.xml"
        });
        tilesConfigurer.setCheckRefresh(true);
        return tilesConfigurer;
    }

    // todo show and teach i18n
    @Bean
    public MessageSource messageSource() {
        String[] baseNames = "messages".split(",");
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setBasenames(baseNames);
        return resourceBundleMessageSource;
    }


    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/web/**").addResourceLocations("/web/");
    }

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("signin");
        for (String p : "signin,signup,profile,customers,home,oops".split(","))
            registry.addViewController(String.format("/crm/%s.html", p)).setViewName(p);
    }

    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    // configures an error page
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();
        simpleMappingExceptionResolver.setDefaultErrorView("oops");
        simpleMappingExceptionResolver.setDefaultStatusCode(404);
        exceptionResolvers.add(simpleMappingExceptionResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(new CrmHttpServletRequestEnrichingInterceptor());
    }


    /// HANDLE SOCIAL

    /**
     * when we sign in, the result should be a valid Spring Security context.
     */
    public static class SpringSecuritySignInAdapter implements SignInAdapter {

        private UserService userService;

        public SpringSecuritySignInAdapter(UserService userService) {
            this.userService = userService;
        }

        /**
         * local user id will invariably be the username from Facebook, e.g., 'starbuxman' or the email they signed up with.
         * <p/>
         * <CODE>null</CODE> is OK, it signals to Spring Social's
         * {@link org.springframework.social.connect.web.ProviderSignInController provider sign in controller}
         * to use the default URL.
         */
        public String signIn(String localUserId, Connection<?> connection, NativeWebRequest request) {
            UserService.CrmUserDetails details = userService.loadUserByUsername(localUserId);
            assert details != null : "the " + UserService.CrmUserDetails.class.getSimpleName() + " can't be null";
            UsernamePasswordAuthenticationToken toAuthenticate = new UsernamePasswordAuthenticationToken(
                    details, StringUtils.isEmpty(details.getPassword()) ? null : details.getPassword(), details.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(toAuthenticate);
            return null; // null is ok it signals to Spring Social's {@link ProviderSignInController} to use the default URL
        }
    }

    /**
     * delegates to the {@link org.springsource.examples.spring31.services.UserService user service} object
     * to create a new user based on credentials coming back from Facebook
     */
    public static class CrmUserConnectionSignUp implements ConnectionSignUp {

        private UserService userService;

        public CrmUserConnectionSignUp(UserService userService) {
            this.userService = userService;
        }

        /**
         * if this account is created by connecting to facebook, then we'll assign a default password
         * and force the user to choose a password on the signup page.
         *
         * @param connection the Spring Social connection object with information about the object from the social provider.
         * @return the username
         */
        public String execute(Connection<?> connection) {
            UserProfile userProfile = connection.fetchUserProfile();
            User u = userService.loginByUsername(userProfile.getUsername());
            if (null == u) {
                u = userService.createUser(userProfile.getUsername(), "", userProfile.getFirstName(), userProfile.getLastName(), true);
            }
            return u.getUsername();
        }
    }

    @Bean
    public ConnectionFactoryLocator connectionFactoryLocator(Environment environment) {
        ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
        String clientId = environment.getProperty("facebook.clientId"),
                clientSecret = environment.getProperty("facebook.clientSecret");
        registry.addConnectionFactory(new FacebookConnectionFactory(clientId, clientSecret));
        return registry;
    }


    @Bean
    public UsersConnectionRepository usersConnectionRepository(DataSource dataSource, UserService userService, ConnectionFactoryLocator connectionFactoryLocator) {
        JdbcUsersConnectionRepository repository = new JdbcUsersConnectionRepository(dataSource, connectionFactoryLocator, Encryptors.noOpText());
        repository.setConnectionSignUp(new CrmUserConnectionSignUp(userService));
        return repository;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.INTERFACES)
    public ConnectionRepository connectionRepository(UsersConnectionRepository usersConnectionRepository) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails crmUserDetails = (UserDetails) principal;
        return usersConnectionRepository.createConnectionRepository(crmUserDetails.getUsername());
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.INTERFACES)
    public Facebook facebook(ConnectionRepository connectionRepository) {
        return connectionRepository.getPrimaryConnection(Facebook.class).getApi();

//        Connection<Facebook> facebook = connectionRepository.findPrimaryConnection(Facebook.class);
//        return facebook != null ? facebook.getApi() : new FacebookTemplate();
    }

    @Bean
    public ProviderSignInController providerSignInController(UsersConnectionRepository usersConnectionRepository, UserService userService, ConnectionFactoryLocator connectionFactoryLocator) {
        ProviderSignInController providerSignInController = new ProviderSignInController(connectionFactoryLocator, usersConnectionRepository,
                new SpringSecuritySignInAdapter(userService));
        providerSignInController.setSignInUrl("/crm/signin.html");
        providerSignInController.setPostSignInUrl("/crm/customers.html");
        return providerSignInController;
    }










}
