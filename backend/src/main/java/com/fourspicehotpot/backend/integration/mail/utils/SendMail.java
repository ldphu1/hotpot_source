package com.fourspicehotpot.backend.integration.mail.utils;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.InputStream;
import java.util.Properties;

public class SendMail {
    private static final String username = "hieulm24@gmail.com";
    private static final String password = "hngm qadr cfnz quis";

    static {

        System.setProperty("mail.mime.splitlongparameters", "false");
        System.setProperty("mail.mime.charset", "UTF-8");
    }

    public static void sendMail(String to, String subject, String htmlBody) throws Exception {
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", "smtp.gmail.com");
        mailProps.put("mail.smtp.port", "587");
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.from", username);
        mailProps.put("mail.debug", "true");
        Session mailSession = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });


        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(username, "Lẩu Thái FourSpiceHotpot", "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");

        MimeMultipart multipart = new MimeMultipart("related");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        MimeBodyPart imagePart = new MimeBodyPart();
        InputStream is = SendMail.class.getClassLoader().getResourceAsStream("static/logo/logo.png");

        if (is != null) {
            DataSource source = new jakarta.mail.util.ByteArrayDataSource(is, "image/png");
            imagePart.setDataHandler(new DataHandler(source));
            imagePart.setHeader("Content-ID", "<logoImage>");
            imagePart.setDisposition(MimeBodyPart.INLINE);
            multipart.addBodyPart(imagePart);
        }

        message.setContent(multipart);
        Transport.send(message);
    }


    public static void main(String[] args) {
        try {
            String testTo = "hieu2k4boy@gmail.com";
            String testSubject = "Test Email - FourSpice Hotpot";
            String testBody = "<h1>Xin chào!</h1><p>Đây là email test định dạng HTML.</p>";

            sendMail(testTo, testSubject, testBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}