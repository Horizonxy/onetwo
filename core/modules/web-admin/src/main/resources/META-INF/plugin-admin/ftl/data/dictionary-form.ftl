
	<@widget.formField name="typeId" type="hidden"/>
	<@widget.formField name="type.name" value="${type.name}" label="类型" disabled="true"/>
	<@widget.formField name="code" label="字典编码" readOnly=dictionary.id!=null/>
	<@widget.formField name="name" label="名称"/>
	<@widget.formField name="value" label="字典值" disabled="${dictionary.enumValue}"/>
	<@widget.formField name="valid" label="是否有效" type="radioGroup"  provider="dictionary" items="BOOL" itemValue="value"  disabled="${dictionary.enumValue}"/>
	<@widget.formField name="enumValue" label="是否枚举常量" type="select" provider="dictionary" items="BOOL" itemValue="value" disabled="true"/>
	<@widget.formField name="sort" label="sort" label="排序"/>
	<@widget.formField name="remark" type="textarea" label="备注" />
	<@widget.formField name="" type="submit" label="提交"/>
	<@widget.formField name="" type="button" value="${pluginConfig.baseURL}/data/dictionary?typeId=${type.id}" label="返回"/>
