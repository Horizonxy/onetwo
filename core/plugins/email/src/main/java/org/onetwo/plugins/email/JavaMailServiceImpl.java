package org.onetwo.plugins.email;

import java.io.File;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.FileUtils;
import org.onetwo.common.utils.LangUtils;
import org.slf4j.Logger;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class JavaMailServiceImpl implements JavaMailService {

	private final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	
	@Resource
	private JavaMailSender javaMailSender;

//	@Resource
//	private MailTextContextParser mailTextContextParser;
	private String encoding = LangUtils.UTF8;

	@Override
	public void send(MailInfo mailInfo) throws MessagingException {
		try {
			if(mailInfo.isMimeMail()){
				sendMimeMail(mailInfo);
			}else{
				sendTextMail(mailInfo);
			}
		} catch (Exception e) {
//			logger.error("发送邮件失败："+e.getMessage(), e);
			throw new MessagingException("发送邮件失败："+e.getMessage(), e);
		}
	}

	protected void sendMimeMail(MailInfo mailInfo) throws Exception {

		MimeMessage msg = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg, true, encoding);

		helper.setFrom(mailInfo.getFrom());
		helper.setTo(mailInfo.getTo());
		helper.setSubject(mailInfo.getSubject());
		helper.setBcc(mailInfo.getBcc());
		helper.setCc(mailInfo.getCc());

//		String content = this.mailTextContextParser.parseContent(mailInfo);
		helper.setText(mailInfo.getContent(), true);

		if(mailInfo.getAttachments()!=null){
			for(File attachment : mailInfo.getAttachments()){
				String fileName = FileUtils.getFileName(attachment.getName());
				helper.addAttachment(fileName, attachment);
			}
		}
		for(Entry<String, InputStreamSource> in : mailInfo.getAttachmentInputStreamSources().entrySet()){
			helper.addAttachment(in.getKey(), in.getValue());
		}

		javaMailSender.send(msg);
		logger.info("HTML版邮件已发送至{}", StringUtils.join(mailInfo.getTo(), ","));
		
	}
	
	protected void sendTextMail(MailInfo mailInfo) throws Exception {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(mailInfo.getFrom());
		msg.setTo(mailInfo.getTo());
		msg.setSubject(mailInfo.getSubject());
		msg.setBcc(mailInfo.getBcc());
		msg.setCc(mailInfo.getCc());

//		String content = this.mailTextContextParser.parseContent(mailInfo);
		msg.setText(mailInfo.getContent());

		javaMailSender.send(msg);
		if (logger.isInfoEnabled()) {
			logger.info("纯文本邮件已发送至{}", StringUtils.join(msg.getTo(), ","));
		}
	}
	/*
	private String getContent(MailInfo mailInfo) throws Exception{
		String content = "";
		if(mailInfo.isTemplate()){
			content = generateContent(mailInfo.getContent(), mailInfo.getTemplateContext());
		}else{
			content = mailInfo.getContent();
		}
		Assert.notNull(mailInfo.getContentType());
		Template template = null;
		switch (mailInfo.getContentType()) {
			case STATIC_TEXT:
				content = mailInfo.getContent();
				break;
				
			case TEMPLATE_PATH:
				template = this.configuration.getTemplate(mailInfo.getContent(), encoding);
				content = FreeMarkerTemplateUtils.processTemplateIntoString(template, mailInfo.getTemplateContext());
				break;
				
			case TEMPLATE:
//				String name = "st-" + String.valueOf(mailInfo.getContent().hashCode());
				String name = "st-" + MDFactory.MD5.encrypt(mailInfo.getContent());
				this.stringFtlTemplateLoader.putTemplate(name, mailInfo.getContent());
				template = this.configuration.getTemplate(name, encoding);
				content = FreeMarkerTemplateUtils.processTemplateIntoString(template, mailInfo.getTemplateContext());
				break;
	
			default:
				break;
		}
		return content;
	}
*/
	
	public JavaMailSender getJavaMailSender() {
		return javaMailSender;
	}
	public void setJavaMailSender(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/*public void setMailTextContextParser(MailTextContextParser mailTextContextParser) {
		this.mailTextContextParser = mailTextContextParser;
	}*/

}
