package com.multitenant.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public JtsModule jtsModule() {
        return new JtsModule();
    }

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        module.configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
        // This will prevent Jackson from attempting to initialize lazy proxies
        module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        // We also want to completely ignore uninitialized proxies without throwing exceptions
        module.enable(Hibernate6Module.Feature.REPLACE_PERSISTENT_COLLECTIONS);
        return module;
    }
}
