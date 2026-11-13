package com.waltercraft.event.config;

import com.waltercraft.event.entity.EventStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToEventStatusConverter());
    }

    private static class StringToEventStatusConverter implements Converter<String, EventStatus> {
        @Override
        public EventStatus convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }
            return EventStatus.fromString(source);
        }
    }
}
