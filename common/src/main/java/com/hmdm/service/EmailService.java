/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

/**
 * <p>A service to use for file uploading process.</p>
 *
 * @author isv
 */
@Singleton
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final String smtpHost;
    private final int smtpPort;
    private final boolean sslEnabled;
    private final boolean startTlsEnabled;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String smtpFrom;

    @Inject
    public EmailService(@Named("smtp.host") String smtpHost,
                        @Named("smtp.port") int smtpPort,
                        @Named("smtp.ssl") boolean sslEnabled,
                        @Named("smtp.starttls") boolean startTlsEnabled,
                        @Named("smtp.username") String smtpUsername,
                        @Named("smtp.password") String smtpPassword,
                        @Named("smtp.from") String smtpFrom) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.sslEnabled = sslEnabled;
        this.startTlsEnabled = startTlsEnabled;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.smtpFrom = smtpFrom;
    }

    public boolean isConfigured() {
        return !smtpHost.equals("");
    }

    public boolean sendEmail(String to, String subj, String body) {
        if (smtpHost.equals("")) {
            return false;
        }
        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", smtpHost);
            properties.put("mail.smtp.port", smtpPort);
            properties.put("mail.smtp.auth", !smtpUsername.equals(""));
            properties.put("mail.smtp.ssl.enable", sslEnabled);
            properties.put("mail.smtp.starttls.enable", startTlsEnabled);

            logger.info("SMTP connection: " + smtpHost + ":" + smtpPort + ", ssl:" + sslEnabled + ", startTls:" + startTlsEnabled);

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subj);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(body, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            Transport.send(message);

            return true;

        } catch (Exception e) {
            logger.warn(e.getMessage());
            return false;
        }
    }

    public String getRecoveryEmailSubj() {
        // We have no localization engine on the server side, so just hardcode it
        // Perhaps this text should be moved to the XML config or even to the GUI settings
        return "Password recovery instructions";
    }

    public String getRecoveryEmailBody(String baseUrl, String passwordResetToken) {
        // We have no localization engine on the server side, so just hardcode it
        // Perhaps this text should be moved to the XML config or even to the GUI settings
        String passwordResetUrl = baseUrl + "/#/passwordReset/" + passwordResetToken;

        return "The password reset on " + baseUrl +" has been requested.<br><br>" +
               "To change your password, please follow this link: <a href='" + passwordResetUrl + "'>" + passwordResetUrl + "</a>.<br><br>" +
               "If you didn't request the password reset, just ignore this email.";
    }
}
