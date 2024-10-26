package com.example.jardin_urbain_ws;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.springframework.context.annotation.Bean;

public class AppConfig {
    @Bean
    public Dataset dataset() {
        return DatasetFactory.create();
    }
}
