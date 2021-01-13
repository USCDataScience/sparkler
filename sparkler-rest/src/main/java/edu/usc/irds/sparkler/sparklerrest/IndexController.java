package edu.usc.irds.sparkler.sparklerrest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String showIndex(){
        return "index";
    }
}