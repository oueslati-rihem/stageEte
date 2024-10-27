package com.authentifcation.projectpitwo.util;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailUtil2 {
    @Autowired
    private JavaMailSender emailSender;



    public void SendSimpleMessage(String to , String subject , String text , List<String> list){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("orihem47@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        if(list != null && list.size() > 0)
            message.setCc(getCcArray(list));
        emailSender.send(message);
    }

    public String[] getCcArray(List<String> cclist){
        String[] cc = new String[cclist.size()];
        for (int i = 0 ; i < cclist.size(); i++){
            cc[i] = cclist.get(i);
        }
        return cc;
    }
    public void sendEmail(String to, String subject, String content, String eventLink) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("orihem47@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);

        // URL of the image for congratulations
        String congratsImageUrl = "https://th.bing.com/th/id/OIP.pKuNnk8zv8bVJ_9qoviMZQHaE8?rs=1&pid=ImgDetMain"; // Replace with the actual URL of the image

        // Create the HTML content with embedded image and event link
        String htmlContent = "<p><b>Your Participation Acceptance</b></p>"
                + "<p><img src=\"" + congratsImageUrl + "\" alt=\"Congratulations\" style=\"width: 200px;\"></p>"
                + "<p><b>Email:</b> " + to + "</p>"
                + "<p><b>Content:</b> " + content + "</p>"
                + "<p><a href=\"" + eventLink + "\">Click here to join us</a></p>";

        // Set the HTML content of the message
        message.setContent(htmlContent, "text/html; charset=utf-8");

        // Embedding an image requires setting the message type to HTML
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }

}