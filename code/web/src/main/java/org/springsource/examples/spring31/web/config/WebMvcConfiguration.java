package org.springsource.examples.spring31.web.config;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles2.TilesConfigurer;
import org.springframework.web.servlet.view.tiles2.TilesView;
import org.springsource.examples.spring31.services.config.ServicesConfiguration;
import org.springsource.examples.spring31.web.UserApiController;
import org.springsource.examples.spring31.web.interceptors.CrmHttpServletRequestEnrichingInterceptor;
import org.springsource.examples.spring31.web.util.HibernateAwareObjectMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Configuration
@EnableWebMvc
@Import({ServicesConfiguration.class, SecurityConfiguration.class})
@ComponentScan(basePackageClasses = UserApiController.class)
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
        mappingJacksonHttpMessageConverter.setObjectMapper(objectMapper());
        mappingJacksonHttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON));
        return mappingJacksonHttpMessageConverter;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new HibernateAwareObjectMapper();
    }

    // todo show how to contribute custom HttpMessageConverters and why, in this case, to handle Hibernate's l
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJacksonHttpMessageConverter());
    }

    // todo this is for the oauth client stuff so get rid of it later
    @Bean
    public ConversionServiceFactoryBean conversionService() {
        ConversionServiceFactoryBean conversionServiceFactoryBean = new ConversionServiceFactoryBean();
        Set<Object> converters = new HashSet<Object>();
        converters.add(new GenericConverter() {

            private Set<ConvertiblePair> convertibleTypes = new HashSet<ConvertiblePair>(
                    Arrays.asList(new ConvertiblePair(AccessTokenRequest.class, AccessTokenRequest.class)));

            public Set<ConvertiblePair> getConvertibleTypes() {
                return convertibleTypes;
            }

            public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
                return source;
            }
        });
        conversionServiceFactoryBean.setConverters(converters);
        return conversionServiceFactoryBean;
    }

    @Bean
    public UrlBasedViewResolver viewResolver() {
        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
        viewResolver.setViewClass(TilesView.class);
        return viewResolver;
    }

    /*
    // todo what did this offer us before ?
    @Bean
    public BeanNameViewResolver beanNameViewResolver() {
        return new BeanNameViewResolver();
    }*/

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
        registry.addViewController("/").setViewName("home");
        for (String p : "signin,profile,customers,home,oops".split(","))
            registry.addViewController(String.format("/crm/%s.html", p)).setViewName(p);
    }

    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /**
     * This method configures an error page for
     */
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
