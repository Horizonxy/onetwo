package org.onetwo.plugins.task.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.onetwo.plugins.task.utils.TaskUtils;

@Entity
@Table(name="TASK_EXEC_LOG")
@TableGenerator(table=TaskUtils.SEQ_TABLE_NAME, name="TaskExecLogEntityGenerator", pkColumnName="GEN_NAME",valueColumnName="GEN_VALUE", pkColumnValue="SEQ_ADMIN_USER", allocationSize=50, initialValue=1000)
public class TaskExecLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4528661067353586519L;
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="TaskExecLogEntityGenerator") 
	@Column(name="ID")
	private Long id;
	private Long taskId;
	private String taskStatus;
	private Date operatorTime;
	private Long operatorId;
	private String operatorName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(String taskStatus) {
		this.taskStatus = taskStatus;
	}

	public Date getOperatorTime() {
		return operatorTime;
	}

	public void setOperatorTime(Date operatorTime) {
		this.operatorTime = operatorTime;
	}

	public Long getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Long operatorId) {
		this.operatorId = operatorId;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

}