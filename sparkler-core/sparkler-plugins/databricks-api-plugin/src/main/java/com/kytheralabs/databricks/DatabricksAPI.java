package com.kytheralabs.databricks;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.GenericProcess;
import edu.usc.irds.sparkler.SparklerConfiguration;
import com.kytheralabs.management.jobutils.JobAPI;
import org.json.simple.JSONObject;

import java.text.ParseException;
import java.util.Map;
import java.util.Scanner;

public class DatabricksAPI extends AbstractExtensionPoint implements GenericProcess {
    SparklerConfiguration pluginConfig = this.jobContext.getConfiguration();

    @Override
    public void executeProcess(GenericProcess.Event event) throws Exception {




        if(event == Event.SHUTDOWN){
            if(pluginConfig.containsKey("databricks.api.events.shutdown")){
                Map<String, Object> m = (Map<String, Object>) pluginConfig.get("databricks.api.events.shutdown");
                for (Map.Entry<String,Object> entry : m.entrySet()) {
                    if(entry.getKey().equals("triggerjob")){
                        triggerJob((Map<String, Object>) entry.getValue());
                    } else if(entry.getKey().equals("updateeventlog")){
                        updateEventLog((Map<String, Object>) entry.getValue());
                    }
                }
            }
        }
    }

    private void triggerJob(Map<String, Object> map){
        String crawlid = this.pluginId;
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

        String environment = "";
        JobAPI.runSingle(notebook, sparkversion, clusterType, clusterSize.intValue(), crawlid, params, environment);

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
