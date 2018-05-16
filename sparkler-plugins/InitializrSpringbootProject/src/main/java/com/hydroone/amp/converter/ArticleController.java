/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydroone.amp.converter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author michelad
 */

@Controller
public class ArticleController {
    
    @GetMapping("/articles")
    public String articles(){
        return "articles";
    }
    
//    @GetMapping("/articles/{test}")
//    public String articles(){
//        return "articles";
//    }
}
