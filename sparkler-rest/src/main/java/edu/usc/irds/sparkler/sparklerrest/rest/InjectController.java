package edu.usc.irds.sparkler.sparklerrest.rest;

import edu.usc.irds.sparkler.sparklerrest.inject.Injection;
import edu.usc.irds.sparkler.sparklerrest.inject.Injector;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/rest/inject")
public class InjectController {

    @GetMapping(path="/{crawlid}", produces = "application/json")
    public String getGreeting(@PathVariable("crawlid") String name)
    {
        return "{\"greeting\" : \"Hello, " + name + "!\"}";
    }

    @PostMapping(path="/{crawlid}", produces = "application/json")
    public String postInject(@PathVariable("crawlid") String name, @RequestBody Injection employee)
    {
        Injector injector = new Injector();
        injector.injectNewURLs(employee.getConfigOverride(), employee.getCrawldb(), name, employee.getUrls());
        return "{\"greeting\" : \"Hello, " + name + "!\"}";
    }

    @GetMapping(path="/{crawlid}/{url}", produces = "application/json")
    public String postInjectUrl(@PathVariable("crawlid") String name, @PathVariable("url") String url)
    {
        return "{\"greeting\" : \"Hello, " + name + "!\"}";
    }
}
