package com.kytheralabs.databricks;

import com.google.gson.Gson;
import org.apache.spark.sql.SparkSession;

public class Persistence {
    Gson gson = new Gson();

    public void persistResults(String crawlId, String pTableName, String warehousename, SparkSession spark, String s) {

    }
}
