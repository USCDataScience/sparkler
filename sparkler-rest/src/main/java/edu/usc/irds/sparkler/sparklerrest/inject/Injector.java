package edu.usc.irds.sparkler.sparklerrest.inject;

import edu.usc.irds.sparkler.Constants;
import edu.usc.irds.sparkler.SparklerConfiguration;
import edu.usc.irds.sparkler.service.InjectorRunner;

public class Injector {


    public void injectNewURLs(Object[] confoverride, String solrurl , String jobId , String[] seedUrls ){
        SparklerConfiguration conf = Constants.defaults.newDefaultConfig();
        InjectorRunner run = new InjectorRunner();
        if (seedUrls != null && seedUrls.length > 0 ){
            run.runInjector(confoverride, solrurl, jobId, null, seedUrls);
        }

    }
}
