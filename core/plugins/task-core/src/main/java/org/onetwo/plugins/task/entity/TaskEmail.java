package org.onetwo.plugins.task.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.onetwo.common.utils.GuavaUtils;
import org.onetwo.plugins.task.utils.TaskType;
import org.onetwo.plugins.task.utils.TaskUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="TASK_EMAIL")
public class TaskEmail extends TaskBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6892549859686221264L;
	@Size(max=100)
	private String subject;
	@Size(max=4000)
	private String content;
	@Size(max=2000)
	private String attachmentPath;
	@Column(name="IS_HTML")
	private boolean html;

	@Size(max=500)
	private String ccAddress;
	@Size(max=4000)
	private String toAddress;
	
//	@Enumerated(EnumType.STRING)
//	private EmailTextType contentType;
	
	public TaskEmail(){
		setType(TaskType.EMAIL.toString());
	}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@JsonIgnore
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getAttachmentPath() {
		return attachmentPath;
	}

	public void setAttachmentPath(String attachmentPath) {
		this.attachmentPath = attachmentPath;
	}

	public boolean isHtml() {
		return html;
	}
	public void setHtml(boolean html) {
		this.html = html;
	}

	@JsonIgnore
	public String[] getCcAsArray() {
		return GuavaUtils.split(ccAddress, TaskUtils.ATTACHMENT_PATH_SPLITTER_CHAR);
	}

	@JsonIgnore
	public String[] getToAsArray() {
		return GuavaUtils.split(toAddress, TaskUtils.ATTACHMENT_PATH_SPLITTER_CHAR);
	}

	@JsonIgnore
	public String[] getAttachmentPathAsArray() {
		return GuavaUtils.split(getAttachmentPath(), TaskUtils.ATTACHMENT_PATH_SPLITTER);
	}


	/*public EmailTextType getContentType() {
		return contentType;
	}

	public void setContentType(EmailTextType contentType) {
		this.contentType = contentType;
	}*/

	public String getCcAddress() {
		return ccAddress;
	}

	public void setCcAddress(String ccAddress) {
		this.ccAddress = ccAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	
}
