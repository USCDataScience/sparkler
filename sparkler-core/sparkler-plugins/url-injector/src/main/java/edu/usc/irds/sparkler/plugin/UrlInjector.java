package edu.usc.irds.sparkler.plugin;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.Config;
import edu.usc.irds.sparkler.SparklerConfiguration;
import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.UrlInjectorObj;
import io.netty.handler.codec.json.JsonObjectDecoder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;

@Extension
public class UrlInjector extends AbstractExtensionPoint implements Config {

    private static final Logger LOG = LoggerFactory.getLogger(UrlInjector.class);
    private Map<String, Object> pluginConfig;

    @Override
    public Collection<UrlInjectorObj> processConfig(Collection<String> urls) {
        SparklerConfiguration config = jobContext.getConfiguration();
        List<UrlInjectorObj> r = new ArrayList<>();
        try {
            pluginConfig = config.getPluginConfiguration(pluginId);
        } catch (SparklerException e) {
            e.printStackTrace();
        }

        if (pluginConfig != null) {
            // Get replacement values from config
            List<String> vals = (List<String>) pluginConfig.get("values");
            LOG.debug("Found vals: {}", vals);

            // Get mode type from config
            String mode = (String) pluginConfig.get("mode");

            if (mode.equals("replace")) {
                // Function 1: Replace placeholder in url and add new url to list
                r = replaceURLToken(urls, vals);
            } else if (mode.equals("selenium")) {
                // Function 2: Keep url but loop on selenium script
                r = appendSelenium(urls, vals);
            } else if (mode.equals("json")) {
                // Function 3: Keep url but create json to POST
                r = appendJSON(urls, vals);
            } else if (mode.equals("form")){
                r = appendForm(urls, vals);
            }
        }

        return r;
    }

    private List<UrlInjectorObj> appendForm(Collection<String> urls, List<String> tokens) {
        List<UrlInjectorObj> fixedUrls = new ArrayList<>();
        Map script = (Map) pluginConfig.get("form");

        JSONObject root = new JSONObject();
        JSONObject obj = new JSONObject(script);
        root.put("form", obj);
        for (Iterator<String> iterator = urls.iterator(); iterator.hasNext();) {
            String u = iterator.next();
            String method = getHTTPMethod(u);
            u = trimHTTPMethod(u);
            if (tokens.size() > 0) {
                for (String temp : tokens) {
                    root.put("TAG", temp);
                    String json = root.toString();
                    json = json.replace("${token}", temp);
                    UrlInjectorObj o = new UrlInjectorObj(u, json, method);
                    fixedUrls.add(o);
                }
            } else {
                UrlInjectorObj o = new UrlInjectorObj(u, root.toString(), method);
                fixedUrls.add(o);
            }

        }
        return fixedUrls;
    }

    // Simple URL token replacement, takes a list of urls and a list of tokens and
    // applies
    // each token to each url.
    private List<UrlInjectorObj> replaceURLToken(Collection<String> urls, List<String> tokens) {
        List<UrlInjectorObj> fixedUrls = new ArrayList<>();
        JSONObject root = new JSONObject();
        for (Iterator<String> iterator = urls.iterator(); iterator.hasNext();) {
            String u = iterator.next();
            for (String temp : tokens) {
                String rep = u.replace("${token}", temp);
                String method = getHTTPMethod(rep);
                rep = trimHTTPMethod(rep);
                root.put("TAG", temp);
                UrlInjectorObj o = new UrlInjectorObj(rep, root.toString(), method);
                fixedUrls.add(o);
            }
        }

        return fixedUrls;
    }

    private List<UrlInjectorObj> appendJSON(Collection<String> urls, List<String> tokens) {
        List<UrlInjectorObj> fixedUrls = new ArrayList<>();
        String jsonStr = (String) pluginConfig.get("json");

        for (Iterator<String> iterator = urls.iterator(); iterator.hasNext();) {
            String u = iterator.next();
            String method = getHTTPMethod(u);
            u = trimHTTPMethod(u);
            if (tokens.size() > 0) {
                for (String temp : tokens) {
                    JSONObject root = new JSONObject();
                    JSONParser parser = new JSONParser();
                    JSONObject json;
                    String parsedJsonStr = jsonStr.replace("${token}", temp);
                    try {
                        json = (JSONObject) parser.parse(parsedJsonStr);
                        root.put("JSON", json);
                        root.put("TAG", temp);
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    UrlInjectorObj o = new UrlInjectorObj(u, root.toString(), method);
                    fixedUrls.add(o);
                }
            } else {
                UrlInjectorObj o = new UrlInjectorObj(u, jsonStr, method);
                fixedUrls.add(o);
            }
        }

        return fixedUrls;

    }

    private List<UrlInjectorObj> appendSelenium(Collection<String> urls, List<String> tokens) {
        List<UrlInjectorObj> fixedUrls = new ArrayList<>();
        Map script = (Map) pluginConfig.get("selenium");

        JSONObject root = new JSONObject();
        JSONObject obj = new JSONObject(script);
        root.put("selenium", obj);
        for (Iterator<String> iterator = urls.iterator(); iterator.hasNext();) {
            String u = iterator.next();
            String method = getHTTPMethod(u);
            u = trimHTTPMethod(u);
            if (tokens.size() > 0) {
                for (String temp : tokens) {
                    root.put("TAG", temp);
                    String json = root.toString();
                    json = json.replace("${token}", temp);
                    UrlInjectorObj o = new UrlInjectorObj(u, json, method);
                    fixedUrls.add(o);
                }
            } else {
                root.put("TAG", this.pluginConfig.getOrDefault("tag", "no tag defined"));
                UrlInjectorObj o = new UrlInjectorObj(u, root.toString(), method);
                fixedUrls.add(o);
            }

        }
        return fixedUrls;
    }

    private String getHTTPMethod(String url) {
        if (url.startsWith("GET|")) {
            return "GET";
        } else if (url.startsWith("PUT|")) {
            return "PUT";
        } else if (url.startsWith("POST|")) {
            return "POST";
        }
        return "GET";
    }

    private String trimHTTPMethod(String url) {
        if (url.startsWith("GET|")) {
            return url.substring(4);
        } else if (url.startsWith("PUT|")) {
            return url.substring(4);
        } else if (url.startsWith("POST|")) {
            return url.substring(5);
        }
        return url;
    }
}
