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
import org.springsource.examples.spring31.web.security.UserSignInUtilities;
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
    public UserSignInUtilities userSignInUtilities(UserService userService) {
        return new UserSignInUtilities(userService);
    }

     protected MappingJacksonHttpMessageConverter mappingJacksonHttpMessageConverter() {
        MappingJacksonHttpMessageConverter mappingJacksonHttpMessageConverter = new MappingJacksonHttpMessageConverter();
        mappingJacksonHttpMessageConverter.setObjectMapper(new HibernateAwareObjectMapper());
        mappingJacksonHttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON));
        return mappingJacksonHttpMessageConverter;
    }


    // todo show how to contribute custom HttpMessageConverters and why, in this case, to handle Hibernate's lazy collections over json
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJacksonHttpMessageConverter() );
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
        for (String p : "signin,profile,customers,home,oops".split(","))
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



}
