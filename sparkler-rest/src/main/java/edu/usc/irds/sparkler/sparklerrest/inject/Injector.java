package edu.usc.irds.sparkler.sparklerrest.inject;

import edu.usc.irds.sparkler.Constants;
import edu.usc.irds.sparkler.SparklerConfiguration;
import edu.usc.irds.sparkler.service.InjectorRunner;
import edu.usc.irds.sparkler.sparklerrest.exceptions.InjectFailedException;

public class Injector {
    SparklerConfiguration conf = Constants.defaults.newDefaultConfig();
    InjectorRunner run = new InjectorRunner();

    public InjectionMessage injectNewURLs(Object[] confoverride, String solrurl , String jobId , String[] seedUrls ) throws InjectFailedException {

        if (seedUrls != null && seedUrls.length > 0 ){
            try {
                jobId = run.runInjector(confoverride, solrurl, jobId, null, seedUrls);
                return new InjectionMessage(jobId, null, "Successfully injected URLs");
            } catch (Exception e){
                throw new InjectFailedException(e.getMessage(), "Failed to inject urls for job: " + jobId, jobId);
            }
        }

        return new InjectionMessage(jobId, null, "No urls supplied");
    }
}
