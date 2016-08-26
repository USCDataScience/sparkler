/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usc.irds.sparkler.pipeline

import java.util.Properties

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

/**
 * The primary purpose of the Sparkler Producer is to write dump files to
 * the kafka messaging system. Users must have configured kafka with atleast one
 * listener. Note that the SparklerProducer should be instantiated within a map
 * task. It is purposely made not serializable as the KafkaProducer is not serializable.
 * This object should not be instantiated outside of a RDD transformation function and then
 * used inside it.
 * @param listeners a comma separated list of listeners. Example : host1:9092, host2:9093
 * @param topic the kafka topic to use for sparkler
 */
class SparklerProducer(listeners: String, topic: String) {

  val props : Properties = new Properties()
  props.put("bootstrap.servers", listeners)
  props.put("acks", "all")
  props.put("retries", "0")
  props.put("batch.size", "16384")
  props.put("linger.ms", "1")
  props.put("buffer.memory", "33554432")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
  val producer = new KafkaProducer[String, Array[Byte]](props)

  /**
   * Add additional properties to the KafkaProducer as
   * Key Value pairs of Strings.
   * @param key   the property name
   * @param value the property value
   */
  def putProperty(key: String, value: String): Unit ={
    props.put(key, value)
  }

  /**
   * Sends a dump string to the producer.
   * If the topic doesn't exist, then kafka auto-creates the topic.
   * @param dump the dump message to send to the producer.
   */
  def send(dump : Array[Byte]): Unit = {
    producer.send(new ProducerRecord[String, Array[Byte]](topic, dump))
  }

}
