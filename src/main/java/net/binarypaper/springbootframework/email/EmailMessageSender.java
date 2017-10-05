/*
 * Copyright 2016 <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.binarypaper.springbootframework.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Spring annotations
@Component
// Lombok annotations
@Log
public class EmailMessageSender {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender emailSender;

    // Spring annotations
    @JmsListener(destination = "email_queue")
    @Transactional
    public void sendEmail(String message) throws Exception {
        try {
            EmailMessage emailMessage = objectMapper.readValue(message, EmailMessage.class);
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom("admin@emample.com");
            mimeMessageHelper.setTo(emailMessage.getToAddress());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            String body = new String(Base64.getDecoder().decode(emailMessage.getBody()), "utf-8");
            mimeMessageHelper.setText(body, true);
            emailSender.send(mimeMessage);
        } catch (IOException | MessagingException ex) {
            log.log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

}
