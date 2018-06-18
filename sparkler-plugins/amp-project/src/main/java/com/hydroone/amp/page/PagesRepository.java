/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydroone.amp.page;

import com.hydroone.amp.page.model.Page;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;

/**
 *
 * @author michelad
 */
public interface PagesRepository extends SolrCrudRepository<Page, String>{
    @Query(filters={"status:FETCHED", "crawl_id:test2"})
    org.springframework.data.domain.Page<Page> findByUrlEndingWith(String urlFragment, Pageable pageable);
}
