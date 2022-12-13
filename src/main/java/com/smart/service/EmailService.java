package com.smart.service;

import java.io.File;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String to, String subject, String message) {

		boolean result = false;

		// Variable for email
		String host = "smtp.gmail.com";
		String from = "babaanu2627@gmail,com";
		// get the system properties

		Properties properties = System.getProperties();
		System.out.println("PROPERTIES " + properties);

		// setting important information to properties object

		// host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// step1 : to get the session object

		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication("babaanu2627@gmail.com", "jnaogxowxllotdzi");
			}

		});

		session.setDebug(true);

		// step2: compose the message[multi-media]
		MimeMessage mime = new MimeMessage(session);

		try {
			// from email
			mime.setFrom(from);

			// adding recipient to message
			mime.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// adding subject
			mime.setSubject(subject);

			// adding message
			// mime.setText(message);

			// adding message with html
			mime.setContent(message, "text/html");

			// send message
			// step3 : sending message using transport Class
			Transport.send(mime);

			System.out.println("sent Successfully...");

			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

}
