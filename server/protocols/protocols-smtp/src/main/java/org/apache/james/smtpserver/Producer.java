package org.apache.james.smtpserver;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.Multipart;
import javax.mail.Part;


public class Producer  {
    private final KafkaProducer<Integer, String> producer;

    public Producer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaProperties.KAFKA_SERVER_URL);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 4096);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 40960);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String topic,String message) {
        try {
            producer.send(new ProducerRecord<>(topic,message)).get();
            System.out.println("Sent message: (" + topic + ", " + message + ")");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


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
            if (html != null)
                bodytext.append(html);
            else
                bodytext.append(text);
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