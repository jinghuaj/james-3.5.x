package org.apache.james.smtpserver;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Producer  {
    private final KafkaProducer<Integer, String> producer;
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    public Producer() {
        Properties props = new Properties();
        KafkaProperties kafkaprops = new KafkaProperties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaprops.getKafkaServerUrl());
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 4096);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 409600);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);

        //        logger.info("KAFKA INIT: " + props.toString());
    }



    public void sendMessage(String topic,String message) {
        try {
            producer.send(new ProducerRecord<>(topic,message)).get();
        //            System.out.println("Sent message: (" + topic + ", " + message + ")");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @Author: yanjinghua
     * @Description: 将邮件的bodytext解析为text和html
     * @Date:2021/06/04
     */
    public String[] getMailContent(Part part, StringBuffer bodytext) throws Exception {
        String[] contents = new String[3];

        String contenttype = part.getContentType();
        int nameindex = contenttype.indexOf("name");
        boolean conname = false;
        if (nameindex != -1) {
            conname = true;
        }
        String text = null;
        String html = null;
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
                if (multipart.getBodyPart(i).isMimeType("text/plain")) {
                    text = (String) multipart.getBodyPart(i).getContent();
                } else if (multipart.getBodyPart(i).isMimeType("text/html")) {
                    html = (String) multipart.getBodyPart(i).getContent();
                }
            }
            if (html != null) {
                bodytext.append(html);
            } else {
                bodytext.append(text);
            }
        } else if (part.isMimeType("text/plain") && !conname) {
            bodytext.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && !conname) {
            bodytext.append((String) part.getContent());
        } else if (part.isMimeType("message/rfc822")) {
            getMailContent((Part) part.getContent(), bodytext);
        }

        contents[0] = text;
        contents[1] = html;
        contents[2] = null;
        return contents;
    }
}