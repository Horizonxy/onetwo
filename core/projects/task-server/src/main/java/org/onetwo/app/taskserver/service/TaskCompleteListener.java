package org.onetwo.app.taskserver.service;

import org.onetwo.plugins.task.utils.TaskResult;

public interface TaskCompleteListener{

	public void onComplete(TaskResult result);

}