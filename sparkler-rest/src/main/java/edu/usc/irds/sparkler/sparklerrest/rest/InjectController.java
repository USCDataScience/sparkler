package edu.usc.irds.sparkler.sparklerrest.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usc.irds.sparkler.sparklerrest.ConfigOverride;
import edu.usc.irds.sparkler.sparklerrest.SparklerConfig;
import edu.usc.irds.sparkler.sparklerrest.exceptions.InjectFailedException;
import edu.usc.irds.sparkler.sparklerrest.inject.InjectStats;
import edu.usc.irds.sparkler.sparklerrest.inject.InjectionMessage;
import edu.usc.irds.sparkler.sparklerrest.inject.Injector;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping(path = "/rest/inject")
public class InjectController {
    Injector injector = new Injector();
    private ObjectMapper objectMapper = null;
    @GetMapping(path="/{crawlid}", produces = "application/json")
    public InjectStats[] getGreeting(@PathVariable("crawlid") String name)
    {
        InjectStats[] injectStats = new InjectStats[]{};

        return injectStats;
    }

    @PostMapping(path="/{crawlid}", produces = "application/json")
    public InjectionMessage postInject(@PathVariable("crawlid") String name, @RequestBody SparklerConfig employee, HttpServletResponse response)
    {
        String[] json = {config2JSON(employee.getConfigOverride())};
        try {
            return injector.injectNewURLs(json, employee.getConfigOverride().getCrawldbUri(), name, employee.getUrls().toArray(String[]::new));
        } catch (InjectFailedException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMsg(), e);
        }
    }

    @GetMapping(path="/{crawlid}/{url}", produces = "application/json")
    public InjectionMessage postInjectUrl(@PathVariable("crawlid") String name, @RequestBody SparklerConfig employee, @PathVariable("url") String url)
    {
        String[] json = {config2JSON(employee.getConfigOverride())};

        Injector injector = new Injector();
        try {
            return injector.injectNewURLs(json, employee.getConfigOverride().getCrawldbUri(), name, new String[]{url});
        } catch (InjectFailedException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMsg(), e);
        }
    }

    private String config2JSON(ConfigOverride config){
        objectMapper = new ObjectMapper();
        try{
            return objectMapper.writeValueAsString(config);
        }catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }
}
