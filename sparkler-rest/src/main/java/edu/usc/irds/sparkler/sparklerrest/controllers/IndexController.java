package edu.usc.irds.sparkler.sparklerrest.controllers;

import com.ctc.wstx.dtd.DTDElement;
import edu.usc.irds.sparkler.sparklerrest.solr.SolrImpl;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class IndexController {

    @GetMapping("/")
    public String showIndex(Model model){
        SolrImpl s = new SolrImpl();

        try {
            JSONObject info = s.getSolrInfo();
            System.out.println(info);
            model.addAttribute("messages", info.toString());
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return "index";
    }
}