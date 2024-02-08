package dev.ueslei.cloakform.config;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

@Configuration
public class ConverterConfiguration {

    /**
     * Without spring-boot-starter-web, no custom mappers will be added automatically; Therefore, we register all our
     * {@link org.springframework.core.convert.converter.Converter Converters} manually.
     */
    @Bean
    public ConversionService conversionService(ListableBeanFactory beanFactory) {
        final FormattingConversionService service = new DefaultFormattingConversionService();
        ApplicationConversionService.addBeans(service, beanFactory);
        return service;
    }
}
