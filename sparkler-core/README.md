Sample spark submit

 ~/Projects/spark-3.0.2-bin-hadoop2.7/bin/spark-submit --class edu.usc.irds.sparkler.Main --master spark://localhost:7077 --driver-java-options '-Dpf4j.pluginsDir=/home/bugg/Projects/sparkler-fork/sparkler-core/build/plugins/' build/sparkler-app-0.3.1-SNAPSHOT.jar inject -su https://news.bbc.co.uk
