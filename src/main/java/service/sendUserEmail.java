package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class sendUserEmail {

    public void send() throws IOException {

        final String username = "YOUR EMAIL";
        final String password = "YOUR PASSWORD";

        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
        Session session = Session.getInstance(prop,
                new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("YOUR EMAIL"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse("RECIEVER/SUPERADMIN EMAIL")
            );
            message.setSubject("Sending Daily Report");

            Multipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Hello Superadmin! Here is your daily report!!");
            multipart.addBodyPart(textPart);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File("src/main/webapp/static/DailyReport.pdf"));
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Mail sent");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}