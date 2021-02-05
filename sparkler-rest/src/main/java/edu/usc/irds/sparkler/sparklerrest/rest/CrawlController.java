package edu.usc.irds.sparkler.sparklerrest.rest;

import edu.usc.irds.sparkler.sparklerrest.crawler.Crawler;
import edu.usc.irds.sparkler.sparklerrest.exceptions.InjectFailedException;
import edu.usc.irds.sparkler.sparklerrest.inject.Injection;
import edu.usc.irds.sparkler.sparklerrest.inject.InjectionMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "/rest/crawl")
public class CrawlController {

    Crawler crawler = new Crawler();

    @PostMapping(path="/{crawlid}", produces = "application/json")
    public InjectionMessage postInject(@PathVariable("crawlid") String name, @RequestBody Injection employee, HttpServletResponse response)
    {

            crawler.crawlJob(employee.getConfigOverride(), employee.getCrawldb(), name);
            return new InjectionMessage(name, null, "Successfully started crawling URLs");

    }

}
