package edu.usc.irds.sparkler.sparklerrest.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SolrController {

    @GetMapping("/solr")
    public String solrIndex(Model model){

        return "greeting";
    }
}
