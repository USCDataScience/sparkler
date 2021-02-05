package edu.usc.irds.sparkler.sparklerrest.rest;

import edu.usc.irds.sparkler.sparklerrest.exceptions.InjectFailedException;
import edu.usc.irds.sparkler.sparklerrest.inject.InjectStats;
import edu.usc.irds.sparkler.sparklerrest.inject.Injection;
import edu.usc.irds.sparkler.sparklerrest.inject.InjectionMessage;
import edu.usc.irds.sparkler.sparklerrest.inject.Injector;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "/rest/inject")
public class InjectController {
    Injector injector = new Injector();

    @GetMapping(path="/{crawlid}", produces = "application/json")
    public InjectStats[] getGreeting(@PathVariable("crawlid") String name)
    {
        InjectStats[] injectStats = new InjectStats[]{};

        return injectStats;
    }

    @PostMapping(path="/{crawlid}", produces = "application/json")
    public InjectionMessage postInject(@PathVariable("crawlid") String name, @RequestBody Injection employee, HttpServletResponse response)
    {
        try {
            return injector.injectNewURLs(employee.getConfigOverride(), employee.getCrawldb(), name, employee.getUrls());
        } catch (InjectFailedException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMsg(), e);
        }
    }

    @GetMapping(path="/{crawlid}/{url}", produces = "application/json")
    public InjectionMessage postInjectUrl(@PathVariable("crawlid") String name, @RequestBody Injection employee, @PathVariable("url") String url)
    {
        Injector injector = new Injector();
        try {
            return injector.injectNewURLs(employee.getConfigOverride(), employee.getCrawldb(), name, new String[]{url});
        } catch (InjectFailedException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMsg(), e);
        }
    }
}
