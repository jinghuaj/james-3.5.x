/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.smtpserver;



import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.Part;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.james.protocols.smtp.SMTPSession;
import org.apache.james.protocols.smtp.dsn.DSNStatus;
import org.apache.james.protocols.smtp.hook.HookResult;
import org.apache.james.protocols.smtp.hook.HookReturnCode;
import org.apache.james.queue.api.MailQueue;
import org.apache.james.queue.api.MailQueueFactory;
import org.apache.mailet.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minidev.json.JSONObject;


/**
 * Queue the message
 */
public class SendMailHandler implements JamesMessageHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendMailHandler.class);

    private final MailQueueFactory<?> queueFactory;
    private MailQueue queue;

    TopicProducer topicProducer = new TopicProducer();

    @Inject
    public SendMailHandler(MailQueueFactory<?> queueFactory) {
        this.queueFactory = queueFactory;
    }


    public void init(Configuration config) throws ConfigurationException {
        this.queue = this.queueFactory.createQueue(MailQueueFactory.SPOOL);
    }

    @Override
    public void destroy() {
        try {
            queue.close();
        } catch (IOException e) {
            LOGGER.debug("error close queue", e);
        }
    }

    /**
     * Adds header to the message
     */
    @Override
    public HookResult onMessage(SMTPSession session, Mail mail) {
        LOGGER.debug("sending mail");

        try {
            queue.enQueue(mail);
            sendMessageKafka(mail);
            LOGGER.info("Successfully spooled mail {} from {} on {} for {}", mail.getName(), mail.getMaybeSender(), session.getRemoteAddress().getAddress(), mail.getRecipients());
        } catch (MessagingException me) {
            LOGGER.error("Unknown error occurred while processing DATA.", me);
            return HookResult.builder()
                .hookReturnCode(HookReturnCode.denySoft())
                .smtpDescription(DSNStatus.getStatus(DSNStatus.TRANSIENT, DSNStatus.UNDEFINED_STATUS) + " Error processing message.")
                .build();
        }
        return HookResult.builder()
            .hookReturnCode(HookReturnCode.ok())
            .smtpDescription(DSNStatus.getStatus(DSNStatus.SUCCESS, DSNStatus.CONTENT_OTHER) + " Message received")
            .build();
    }


    /**
     *
     * @Author: yanjinghua
     * @Description: 将邮件内容推送到kafka
     * @Date:2021/06/04
     */
    public void sendMessageKafka(final Mail mc) {
        LOGGER.info("向kafka发送数据" + mc);
        try {
            StringBuffer recipients = new StringBuffer();
            for (Iterator i = mc.getRecipients().iterator(); i.hasNext(); ) {
                recipients.append(i.next().toString());
                if (i.hasNext()) {
                    recipients.append("\r\n");
                }
            }
            String sender = "" + mc.getSender();
            StringBuffer bodytext = new StringBuffer();

            if (!"".equalsIgnoreCase(sender) && mc.getMessage().getSender() != null) {
                sender = mc.getMessage().getSender().toString();
            }

            Producer producer = new Producer();
            String[] contents = producer.getMailContent((Part) mc.getMessage(), bodytext);
            JSONObject obj = new JSONObject();
            obj.put("receiver", recipients.toString());
            obj.put("sender", sender);
            obj.put("subject", mc.getMessage().getSubject());
            obj.put("text", contents[0]);
            if (contents[1] == null || "".equalsIgnoreCase(contents[1])) {
                obj.put("html", bodytext.toString());
            } else {
                obj.put("html", contents[1]);
            }
            obj.put("charset", contents[2]);

            LOGGER.info("send to kafka message is " + obj.toString());
            producer.sendMessage("alert_event_email", obj.toString());


            HashMap m = new HashMap();
            m.put("receiver", recipients.toString());
            m.put("sender", sender);
            m.put("subject", mc.getMessage().getSubject());
            m.put("text", contents[0]);
            if (contents[1] == null || "".equalsIgnoreCase(contents[1])) {
                m.put("html", bodytext.toString());
            } else {
                m.put("html", contents[1]);
            }

            m.put("charset", contents[2]);
            topicProducer.sendMessage("alert.event.email", m);



        } catch (Exception e) {
            System.err.println("send kafka error" + e);
        }
    }


}
