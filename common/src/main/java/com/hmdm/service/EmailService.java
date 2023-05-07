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
import com.hmdm.persistence.domain.Customer;
import com.hmdm.util.StringUtil;
import liquibase.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * <p>A service to use for email sending.</p>
 *
 * @author seva
 */
@Singleton
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final String smtpHost;
    private final int smtpPort;
    private final boolean sslEnabled;
    private final boolean startTlsEnabled;
    private final String sslProtocols;
    private final String sslTrust;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String smtpFrom;

    private final String appName;
    private final String baseUrl;

    private final String recoveryEmailSubj;
    private final String recoveryEmailBody;
    private final String signupEmailSubj;
    private final String signupEmailBody;
    private final String signupCompleteEmailSubj;
    private final String signupCompleteEmailBody;

    @Inject
    public EmailService(@Named("smtp.host") String smtpHost,
                        @Named("smtp.port") int smtpPort,
                        @Named("smtp.ssl") boolean sslEnabled,
                        @Named("smtp.starttls") boolean startTlsEnabled,
                        @Named("smtp.ssl.protocols") String sslProtocols,
                        @Named("smtp.ssl.trust") String sslTrust,
                        @Named("smtp.username") String smtpUsername,
                        @Named("smtp.password") String smtpPassword,
                        @Named("smtp.from") String smtpFrom,
                        @Named("rebranding.name") String appName,
                        @Named("base.url") String baseUrl,
                        @Named("email.recovery.subj") String recoveryEmailSubj,
                        @Named("email.recovery.body") String recoveryEmailBody,
                        @Named("email.signup.subj") String signupEmailSubj,
                        @Named("email.signup.body") String signupEmailBody,
                        @Named("email.signup.complete.subj") String signupCompleteEmailSubj,
                        @Named("email.signup.complete.body") String signupCompleteEmailBody) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.sslEnabled = sslEnabled;
        this.startTlsEnabled = startTlsEnabled;
        this.sslProtocols = sslProtocols;
        this.sslTrust = sslTrust;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.smtpFrom = smtpFrom;
        if (appName.equals("")) {
            appName = "Headwind MDM";
        }
        this.baseUrl = baseUrl;
        this.appName = appName;
        this.recoveryEmailSubj = recoveryEmailSubj;
        this.recoveryEmailBody = recoveryEmailBody;
        this.signupEmailSubj = signupEmailSubj;
        this.signupEmailBody = signupEmailBody;
        this.signupCompleteEmailSubj = signupCompleteEmailSubj;
        this.signupCompleteEmailBody = signupCompleteEmailBody;
    }

    public boolean isConfigured() {
        return !smtpHost.equals("");
    }


    public boolean sendEmail(String to, String subj, String body) {
        return sendEmail(to, subj, body, null);
    }

    public boolean sendEmail(String to, String subj, String body, String replyTo) {
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
            if (!StringUtil.isEmpty(sslProtocols)) {
                properties.put("mail.smtp.ssl.protocols", sslProtocols);
            }
            if (!StringUtil.isEmpty(sslTrust)) {
                properties.put("mail.smtp.ssl.trust", sslTrust);
            }

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
            if (replyTo != null && !replyTo.equals("")) {
                message.addHeader("Reply-To", replyTo);
            }
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

    public String getRecoveryEmailSubj(String language) {
        return getLocalizedText(recoveryEmailSubj, language);
    }

    public String getRecoveryEmailBody(String language, String passwordResetToken) {
        String passwordResetUrl = baseUrl + "/#/passwordReset/" + passwordResetToken;

        return getLocalizedText(recoveryEmailBody, language)
                .replace("${passwordResetUrl}", passwordResetUrl);
    }

    public String getVerifyEmailSubj(String language) {
        return getLocalizedText(signupEmailSubj, language);
    }

    public String getVerifyEmailBody(String language, String verifyToken) {
        String signupCompleteUrl = baseUrl + "/#/signupComplete/" + verifyToken;

        return getLocalizedText(signupEmailBody, language)
                .replace("${signupCompleteUrl}", signupCompleteUrl);
    }

    public String getSignupCompleteEmailSubj(String language) {
        return getLocalizedText(signupCompleteEmailSubj, language);
    }


    public String getSignupCompleteEmailBody(Customer customer) {
        String deviceIds = customer.getPrefix() + "001, " +
                customer.getPrefix() + "002," +
                customer.getPrefix() + "003";

        return getLocalizedText(signupCompleteEmailBody, customer.getLanguage())
                .replace("${firstName}", customer.getFirstName())
                .replace("${lastName}", customer.getLastName())
                .replace("${customerName}", customer.getName())
                .replace("${deviceLimit}", "" + customer.getDeviceLimit())
                .replace("${sizeLimit}", "" + customer.getSizeLimit() + " Mb")
                .replace("${prefix}", customer.getPrefix())
                .replace("${deviceIds}", deviceIds);
    }

    public String getSignupNotifyEmailSubj() {
        return "New customer created at " + appName;
    }

    public String getSignupNotifyEmailBody(Customer customer) {
        StringBuilder builder = new StringBuilder();
        builder.append("Username: ");
        builder.append(customer.getName());
        builder.append("\n<br>");
        builder.append("Email: ");
        builder.append(customer.getEmail());
        builder.append("\n<br>");
        builder.append("Name: ");
        builder.append(customer.getFirstName());
        builder.append(" ");
        builder.append(customer.getLastName());
        builder.append("\n<br>");
        builder.append("Description: ");
        builder.append(customer.getDescription());
        builder.append("\n<br>");
        builder.append("Language: ");
        builder.append(customer.getLanguage());
        return builder.toString();
    }

    // Default language is English
    private String getLocalizedText(String path, String language) {
        if (language == null || language.equals("")) {
            language = "en";
        }

        File file = new File(path.replace("_LANGUAGE_", language));
        String ret = readFile(file);
        if (ret == null) {
            file = new File(path.replace("_LANGUAGE_", "en"));
            ret = readFile(file);
        }
        if (ret == null) {
            logger.error("Email template not found: " + file.getAbsolutePath());
            return null;
        }

        return ret
                .replace("${baseUrl}", baseUrl)
                .replace("${appName}", appName);
    }

    private String readFile(File file) {
        if (file.exists()) {
            try {
                return FileUtils.readFileToString(file, "UTF-8");
            } catch (IOException e) {
                logger.error("Failed to read email template: " + file.getAbsolutePath());
                return null;
            }
        }
        return null;
    }

}
