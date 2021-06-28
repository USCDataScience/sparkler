Sample spark submit

 ~/Projects/spark-3.0.2-bin-hadoop2.7/bin/spark-submit --class edu.usc.irds.sparkler.Main --master spark://localhost:7077 --driver-java-options '-Dpf4j.pluginsDir=/home/bugg/Projects/sparkler-fork/sparkler-core/build/plugins/' build/sparkler-app-0.3.1-SNAPSHOT.jar inject -su https://news.bbc.co.uk


Databricks API

curl -vvv -n -H 'Content-Type:application/json' -H "Authorization: Bearer xxx" https://kli-mmit.cloud.databricks.com/api/2.0/jobs/runs/submit -d '{"new_cluster":{"spark_conf":{"spark.locality.wait.node":"0","spark.executor.extraJavaOptions":"-Dpf4j.pluginsDir=/dbfs/FileStore/sparkler-submit/plugins/", "spark.task.cpus":"8"},"spark_version":"8.3.x-scala2.12","aws_attributes":{"availability":"SPOT_WITH_FALLBACK","first_on_demand":1,"zone_id":"us-west-2c"},"node_type_id":"c5d.4xlarge","init_scripts":[{"dbfs":{"destination":"dbfs:/FileStore/KLI/crawlinit.sh"}}],"num_workers":10, "cluster_log_conf":{ "dbfs" : { "destination" : "dbfs:/FileStore/logs" } }},"spark_submit_task":{"parameters":["--driver-java-options","-Dpf4j.pluginsDir=/dbfs/FileStore/sparkler-submit/plugins/","--driver-memory","10g","--executor-memory","10g","--class","edu.usc.irds.sparkler.Main","dbfs:/FileStore/sparkler-submit/sparkler-app-0.3.1-SNAPSHOT.jar","crawl","-id","testclustercrawl7", "-tn", "4000","-co","{\"plugins.active\":[\"urlfilter-regex\",\"urlfilter-samehost\",\"fetcher-chrome\"],\"plugins\":{\"fetcher.chrome\":{\"chrome.dns\":\"local\"}}}"]},"run_name":"testsubmi4t"}'
