package edu.usc.irds.sparkler.plugin;

import com.google.common.net.InternetDomainName;
import edu.usc.irds.sparkler.*;
import org.pf4j.Extension;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by tg on 12/19/17.
 * URL filter plugin to restrict to the same host
 */
@Extension
public class UrlFilterSameHost extends AbstractExtensionPoint implements URLFilter {

    private Map<String, Object> pluginConfig;

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        super.init(context, pluginId);

        SparklerConfiguration config = jobContext.getConfiguration();
        // TODO should change everywhere
        pluginConfig = config.getPluginConfiguration(pluginId);
    }


    @Override
    public boolean filter(String child, String parent) {
        try {
            boolean subdomains = true;
            try{
                subdomains = (boolean) pluginConfig.getOrDefault("urlfilter.samehost.allowsubdomains", true);
            } catch (Exception ignored){

            }

            if(subdomains){
                String domain = InternetDomainName.from(new URL(child).getHost()).topPrivateDomain().toString();
                String pdomain = InternetDomainName.from(new URL(parent).getHost()).topPrivateDomain().toString();
                return domain.equals(pdomain);
            } else{
                return new URL(child).getHost().equals(new URL(parent).getHost());
            }


        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
