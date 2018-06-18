/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hydroone.amp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author michelad
 */

@Controller
public class PageController {
    
    
    PageService pageService;

    @Autowired
    public void setPageService(PageService pageService) {
        this.pageService = pageService;
    }
    
    @RequestMapping("/")
    public ModelAndView page(@PageableDefault(
			page = 0, size = PageService.DEFAULT_PAGE_SIZE) Pageable pageable){
        ModelAndView view = new ModelAndView();
        
        view.addObject("pages", pageService.find(pageable));
        view.setViewName("articles");
        return view;
    }
    
    
    @RequestMapping("/{path1}/{path2}/{path3}")
    public ModelAndView page(
            @PathVariable(required = false, value = "path1") String path1,
            @PathVariable(required = false, value = "path2") String path2,
            @PathVariable(required = false, value = "path3") String path3,
            @Value("${h1.default.hostname}") String hostname, @PageableDefault(
			page = 0, size = 1) Pageable pageable
    ) {
        ModelAndView view = new ModelAndView();
        view.addObject("page", pageService.findByUrlEndingWith(hostname +"/"+ path1+"/"+path2+"/"+path3, pageable));
        view.setViewName("article");
        return view;
    }
    @RequestMapping("/{path1}/{path2}/{path3}//{path4}")
    public ModelAndView page(
            @PathVariable(required = false, value = "path1") String path1,
            @PathVariable(required = false, value = "path2") String path2,
            @PathVariable(required = false, value = "path3") String path3,
            @PathVariable(required = false, value = "path4") String path4,
            @Value("${h1.default.hostname}") String hostname, @PageableDefault(
			page = 0, size = 1) Pageable pageable
    ) {
        ModelAndView view = new ModelAndView();
        view.addObject("page", pageService.findByUrlEndingWith(hostname +"/"+ path1+"/"+path2+"/"+path3+"/"+path4, pageable));
        view.setViewName("article");
        return view;
    }
}
