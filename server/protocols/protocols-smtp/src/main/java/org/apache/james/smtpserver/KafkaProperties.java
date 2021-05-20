package org.apache.james.smtpserver;

import java.io.File;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProperties.class);
    public static final String TOPIC = "topic1";


    //    public static final String KAFKA_SERVER_URL = "192.168.3.185:9092,192.168.3.186:9092,192.168.3.187:9092";
    private static String KAFKA_SERVER_URL;
    public static final int KAFKA_PRODUCER_BUFFER_SIZE = 64 * 1024;
    public static final int CONNECTION_TIMEOUT = 100000;
    public static final String TOPIC2 = "topic2";
    public static final String TOPIC3 = "topic3";
    public static final String CLIENT_ID = "SimpleConsumerDemoClient";

    public KafkaProperties() {
        Configurations configs = new Configurations();
        try {
            Configuration config = configs.properties(new File("kafka.properties"));
            String dbHost = config.getString("spring.kafka.bootstrap-servers");
            LOGGER.info("kafka Configurations: " + dbHost);
            this.setKafkaServerUrl(dbHost);
        } catch (ConfigurationException cex) {
            LOGGER.info("kafka Configurations Exception: " + cex);
        }
    }

    public static String getKafkaServerUrl() {
        return KAFKA_SERVER_URL;
    }

    public static void setKafkaServerUrl(String kafkaServerUrl) {
        KAFKA_SERVER_URL = kafkaServerUrl;
    }


}

