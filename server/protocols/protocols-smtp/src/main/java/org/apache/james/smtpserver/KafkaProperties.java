package org.apache.james.smtpserver;

public class KafkaProperties {
    public static final String TOPIC = "alert_event_email";
    //    public static final String KAFKA_SERVER_URL = "192.168.3.185:9092,192.168.3.186:9092,192.168.3.187:9092";
    public static final String KAFKA_SERVER_URL = "10.128.5.138:9092";
    public static final int KAFKA_PRODUCER_BUFFER_SIZE = 64 * 1024;
    public static final int CONNECTION_TIMEOUT = 100000;
    public static final String TOPIC2 = "alert_event_email";
    public static final String TOPIC3 = "alert_event_email";
    public static final String CLIENT_ID = "SimpleConsumerDemoClient";

}