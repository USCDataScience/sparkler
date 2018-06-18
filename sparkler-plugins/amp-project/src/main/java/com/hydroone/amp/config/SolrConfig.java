/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydroone.amp.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

/**
 *
 * @author michelad
 */

@Configuration
@EnableSolrRepositories(basePackages={"com.hydroone.amp.page"})
public class SolrConfig {
    
    	@Bean
	public SolrClient solrClient(@Value("${solr.host}") String solrHost) {
		return new HttpSolrClient.Builder(solrHost).build();
	}
}
