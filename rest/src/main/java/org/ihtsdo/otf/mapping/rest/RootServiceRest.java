package org.ihtsdo.otf.mapping.rest;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.ihtsdo.otf.mapping.helpers.FeedbackEmail;
import org.ihtsdo.otf.mapping.helpers.LocalException;
import org.ihtsdo.otf.mapping.jpa.services.MappingServiceJpa;
import org.ihtsdo.otf.mapping.model.MapRecord;
import org.ihtsdo.otf.mapping.model.MapUser;
import org.ihtsdo.otf.mapping.model.UserError;
import org.ihtsdo.otf.mapping.services.MappingService;

/**
 * Top level class for all REST services.
 */
public class RootServiceRest {
	
	//
	// Fields
	//

	/**  The config. */
	public Properties config = null;

	/** The address that messages will appear as though they come from. */
	String m_from = null; 
	
	/** The password for the SMTP host. */
	String host_password = null; 

	/** The SMTP host that will be transmitting the message. */
	String host = null; 

	/** The port on the SMTP host. */
	String port = null; 

	/** The list of addresses to send the message to. */
	String recipients = null; 

	/** Subject text for the email. */
	String m_subject = "IHTSDO Mapping Tool Exception Report";

	/** Text for the email. */
	StringBuffer m_text; 

	/**
	 * Returns the config properties.
	 *
	 * @throws Exception the exception
	 */
	public void getConfigProperties() throws Exception {

		if (config == null) {

			String configFileName = System.getProperty("run.config");
			Logger.getLogger(this.getClass())
					.info("  run.config = " + configFileName);
			config = new Properties();
			FileReader in = new FileReader(new File(configFileName));
			config.load(in);
			in.close();
			
			m_from = config.getProperty("mail.smtp.user");
			host_password = config.getProperty("mail.smtp.password");
			host = config.getProperty("mail.smtp.host");
			port = config.getProperty("mail.smtp.port");
			recipients = config.getProperty("mail.smtp.to");

			Logger.getLogger(this.getClass()).info("  properties = " + config);
		}

	}

	/**
	 * Handle exception. For {@link LocalException} print the stack trace and inform the
	 * user with a message generated by the application.  For all other exceptions, also
	 * send email to administrators with the message and the stack trace.
	 *
	 * @param e the e
	 * @param whatIsHappening the what is happening
	 * @throws WebApplicationException the web application exception
	 */
	public void handleException(Exception e, String whatIsHappening)
		throws WebApplicationException {

		e.printStackTrace(); 
		if (e instanceof LocalException) {				
			throw new WebApplicationException(Response.status(500).entity(e.getMessage()).build());
		}

		try {
  	 	  getConfigProperties();				
		  sendEmail(e, whatIsHappening);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new WebApplicationException(Response.status(500).entity(ex.getMessage()).build());
		}
		
        throw new WebApplicationException(Response.status(500).entity(
		  "Unexpected error trying to " + whatIsHappening + ". Please contact the administrator.").build());			
	}
	
	/**
	 * Send email regarding Exception e.
	 *
	 * @param e the e
	 * @param whatIsHappening the what is happening
	 */
	private void sendEmail(Exception e, String whatIsHappening) {
		
		// if email settings are not specified, do nothing
		if (m_from != null) {

			Properties props = new Properties();
			props.put("mail.smtp.user", m_from);
			props.put("mail.smtp.password", host_password);
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");
			
			m_subject = "IHTSDO Mapping Tool Exception Report";
			m_text = new StringBuffer();
			if (!(e instanceof LocalException))
				m_text.append("Unexpected error trying to " + whatIsHappening + ". Please contact the administrator.").append("\n\n");
	
			m_text.append("MESSAGE: " + e.getMessage()).append("\n\n");
			for (StackTraceElement element : e.getStackTrace()) {
				m_text.append("  ").append(element).append("\n");
			}
	
			try {
				Authenticator auth = new SMTPAuthenticator();
				Session session = Session.getInstance(props, auth);
	
				MimeMessage msg = new MimeMessage(session);
				msg.setText(m_text.toString());
				msg.setSubject(m_subject);
				msg.setFrom(new InternetAddress(m_from));
				String[] recipientsArray = recipients.split(";");
				for (String recipient : recipientsArray) {
				  msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
				}
				Transport.send(msg);
	
			} catch (Exception mex) {
				mex.printStackTrace();
			}
		}
	}

	/**
	 * Send user error email.
	 *
	 * @param userError the user error
	 * @throws Exception 
	 */
	public void sendUserErrorEmail(UserError userError) throws Exception {
		
		// only send email if settings are specified
		if (m_from != null) {
		
			Properties props = new Properties();
			props.put("mail.smtp.user", m_from);
			props.put("mail.smtp.password", host_password);
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");
			
			MappingService mappingService = new MappingServiceJpa();
			MapRecord mapRecord = mappingService.getMapRecord(userError.getMapRecordId());
			mappingService.close();
			
			m_subject = "IHTSDO Mapping Tool Editing Error Report: " + mapRecord.getConceptId();
			recipients = userError.getMapUserInError().getEmail();
			
			m_text = new StringBuffer();
	
			m_text.append("USER ERROR on record " + userError.getMapRecordId()).append("\n\n");
			m_text.append("Error type: " + userError.getMapError()).append("\n");
			m_text.append("Reporting lead: " + userError.getMapUserReporting().getName()).append("\n");
			m_text.append("Comment: " + userError.getNote()).append("\n");
			m_text.append("Reporting date: " + userError.getTimestamp()).append("\n\n");
			// TODO: the base url here can not be hardcoded, won't work in dev and uat
			m_text.append("Record URL: https://mapping.snomedtools.org/index.html#/record/recordId/" + userError.getMapRecordId());
			
	
			try {
				Authenticator auth = new SMTPAuthenticator();
				Session session = Session.getInstance(props, auth);
	
				MimeMessage msg = new MimeMessage(session);
				msg.setText(m_text.toString());
				msg.setSubject(m_subject);
				msg.setFrom(new InternetAddress(m_from));
				String[] recipientsArray = recipients.split(";");
				for (String recipient : recipientsArray) {
				  msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
				}
				Transport.send(msg);
	
			} catch (Exception mex) {
				mex.printStackTrace();
			}
		}
	}

	public void sendEmail(FeedbackEmail feedbackEmail) {
		
		// only send email if parameters are specified
		if (m_from != null) {
			try {
				getConfigProperties();
				
				Properties props = new Properties();
				props.put("mail.smtp.user", m_from);
				props.put("mail.smtp.password", host_password);
				props.put("mail.smtp.host", host);
				props.put("mail.smtp.port", port);
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.auth", "true");
				
				Authenticator auth = new SMTPAuthenticator();
				Session session = Session.getInstance(props, auth);
	
				MimeMessage msg = new MimeMessage(session);
				//msg.setText(feedbackEmail.getEmailText());
				msg.setSubject(feedbackEmail.getSubject());
				msg.setFrom(new InternetAddress(m_from));
				
				// get the recipients
				MappingService mappingService = new MappingServiceJpa();
				for (String recipient : feedbackEmail.getRecipients()) {
					MapUser mapUser = mappingService.getMapUser(recipient);
					System.out.println(mapUser.getEmail());
					msg.addRecipient(Message.RecipientType.TO, new InternetAddress(mapUser.getEmail()));	
				}
				
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress("pgranvold@westcoastinformatics.com"));
				
				msg.setContent(feedbackEmail.getEmailText(), "text/html");
				
				
				// msg.addRecipient(Message.RecipientType.TO, new InternetAddress(feedbackEmail.getSender().getEmail()));
				
				
				
				Transport.send(msg);
	
			} catch (Exception mex) {
				mex.printStackTrace();
			}
		}
	}


	/**
	 * SMTPAuthenticator.
	 */
	public class SMTPAuthenticator extends javax.mail.Authenticator {
	  
  	/* (non-Javadoc)
  	 * @see javax.mail.Authenticator#getPasswordAuthentication()
  	 */
  	@Override
    public PasswordAuthentication getPasswordAuthentication() {
	    return new PasswordAuthentication(m_from, host_password);
	  }
	}
}
