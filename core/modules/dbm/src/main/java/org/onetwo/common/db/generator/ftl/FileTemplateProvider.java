package org.onetwo.common.db.generator.ftl;

import org.onetwo.common.file.FileUtils;
import org.onetwo.common.spring.ftl.StringTemplateProvider;
import org.onetwo.common.utils.StringUtils;

public class FileTemplateProvider implements StringTemplateProvider {

	@Override
	public String getTemplateContent(String name) {
		name = FileUtils.replaceBackSlashToSlash(name);
		String content = StringUtils.join(FileUtils.readAsList(name, "utf-8"), "\n");
		return content;
	}
	

}
