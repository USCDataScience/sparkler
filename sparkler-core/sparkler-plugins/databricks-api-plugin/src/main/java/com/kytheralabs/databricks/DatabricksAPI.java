package com.kytheralabs.databricks;


import com.kytheralabs.management.jobutils.JobAPI;
import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.GenericProcess;
import edu.usc.irds.sparkler.SparklerConfiguration;
import org.json.simple.JSONObject;
import org.pf4j.Extension;
import scala.Option;

import java.text.ParseException;
import java.util.Map;
import java.util.Scanner;

@Extension
public class DatabricksAPI extends AbstractExtensionPoint implements GenericProcess {

    @Override
    public void executeProcess(GenericProcess.Event event, Object spark) throws Exception {
        SparklerConfiguration config = this.jobContext.getConfiguration();
        Map<String, Object> pluginConfig = config.getPluginConfiguration(pluginId);

        if(pluginConfig.containsKey("events")) {
            Map<String,Object> o = (Map<String, Object>) pluginConfig.get("events");
            if(o.containsKey(event.toString().toLowerCase())){
                Map<String, Object> m = (Map<String, Object>) o.get(event.toString().toLowerCase());
                for (Map.Entry<String,Object> entry : m.entrySet()) {
                    if(entry.getKey().equals("triggerjob")){
                        triggerJob((Map<String, Object>) entry.getValue());
                    } else if(entry.getKey().equals("updateeventlog")){
                        updateEventLog((Map<String, Object>) entry.getValue());
                    } else if(entry.getKey().equals("persistdata")){
                        persistData((Map<String, Object>) entry.getValue());
                    }
                }
            }
        }
    }

    private void triggerJob(Map<String, Object> map){
        String crawlid = this.jobContext.getId();
        String notebook = map.get("notebook").toString();
        String sparkversion = map.getOrDefault("sparkversion", "7.3.x-scala2.12").toString();
        String clusterType = map.getOrDefault("instancetype", "i3.xlarge").toString();
        Number clusterSize = 0;
        try {
            clusterSize = parseNumber(map.getOrDefault("clustersize", 1).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String params = "{}";
        if(map.containsKey("params")){
            JSONObject j = new JSONObject((Map) map.get("params"));
            params = j.toJSONString();
        }

        Option<String> urlValue = scala.Option.apply(null);;
        if(map.containsKey("url")){
            urlValue = Option.apply(map.get("url").toString());
        }

        Option<String> keyValue = scala.Option.apply(null);;
        if(map.containsKey("key")){
            keyValue = Option.apply(map.get("key").toString());
        }

        JobAPI.runSingle(notebook, sparkversion, clusterType, clusterSize.intValue(), crawlid, params, urlValue, keyValue);

    }

    private void persistData(Map<String, Object> map){

    }


    private void updateEventLog(Map<String, Object> map){

    }

    private Number parseNumber(String number) throws ParseException {
        Scanner scan = new Scanner(number);
        if(scan.hasNextInt()){
            return Integer.parseInt(number);
        }
        else if(scan.hasNextDouble()) {
            return Double.parseDouble(number);
        }
        else {
            throw new ParseException("Invalid numeric type: \"" + number + "\"", 0);
        }
    }

}
