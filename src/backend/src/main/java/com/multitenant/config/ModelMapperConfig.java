package com.multitenant.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.Converter;
import org.modelmapper.AbstractConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.multitenant.model.registru.TipCerere;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setFieldMatchingEnabled(true)
            .setSkipNullEnabled(true);

        Converter<String, TipCerere> stringToTipCerere = new AbstractConverter<String, TipCerere>() {
            @Override
            protected TipCerere convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                try {
                    return TipCerere.valueOf(source);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        };

        Converter<TipCerere, String> tipCerereToString = new AbstractConverter<TipCerere, String>() {
            @Override
            protected String convert(TipCerere source) {
                return source == null ? null : source.name();
            }
        };

        modelMapper.addConverter(stringToTipCerere);
        modelMapper.addConverter(tipCerereToString);

        return modelMapper;
    }
}

