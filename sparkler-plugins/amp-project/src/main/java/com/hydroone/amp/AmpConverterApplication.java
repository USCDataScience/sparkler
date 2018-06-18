package com.hydroone.amp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
//import com.hydroone.amp.pages.PagesRepository;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@SpringBootApplication
//@EnableSolrRepositories(basePackageClasses = PagesRepository.class)
public class AmpConverterApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmpConverterApplication.class, args);
	}
}
