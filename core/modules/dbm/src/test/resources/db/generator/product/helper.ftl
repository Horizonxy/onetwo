
<#macro generatedFormField table isSearchFormField=false fieldClass='formFieldClass'>
    <#list table.columns as column>
        <#assign requiredValue = 'true'/>
        <#if !column.primaryKey>
            <#if isSearchFormField==true>
                <#assign requiredValue = 'false'/>
            <#else>
                <#assign requiredValue = column.nullable?string('false', 'true')/>
            </#if><#t>
                <tr>
                    <td>${(column.comments[0])!''}:</td>
                    <td>
                      <#if column.mapping.isNumberType()==true>
                        <input class="easyui-numberbox ${fieldClass}" type="text" name="${column.javaName}" 
                               data-options="required:${requiredValue}
                                             <#if column.mapping.isSqlFloat()==true>, precision:2</#if> "/>
                               
                      <#elseif column.mapping.isSqlTimestamp()==true>
                        <input class="easyui-datetimebox ${fieldClass}" type="text" name="${column.javaName}" 
                               data-options="required:${requiredValue},
                                             editable:false "/>
        
                      <#elseif column.mapping.isSqlTime()==true>
                        <input class="easyui-timespinner ${fieldClass}" type="text" name="${column.javaName}" 
                               data-options="required:${requiredValue},
                                             editable:false "/>
        
                      <#elseif column.mapping.isSqlDate()==true>
                        <input class="easyui-datebox ${fieldClass}" type="text" name="${column.javaName}" 
                               data-options="required:${requiredValue},
                                             editable:false "/>
                               
                      <#elseif column.mapping.isBooleanType()==true>
                        <input class="easyui-combobox ${fieldClass}" type="text" name="${column.javaName}" 
                               data-options="required:${requiredValue},
                                             editable:false,
                                             data: [{value:'true', text:'是', selected:'true'}, {value:'false', text:'否'}] "/>
                               
                      <#elseif column.commentsInfo['字典类型']??>
                        <input class="easyui-combobox ${fieldClass}" name="${column.javaName}" 
                               data-options="required:${requiredValue},
                                              method: 'get',
                                              editable: false,
                                            <#if isSearchFormField==true><#t>
                                              loadFilter: helper.addEmptyOptionForComboboxFilter,
                                            </#if><#t>
                                              url: '${'$'}{siteConfig.baseURL}/configmgr/dictionary/combobox/${column.commentsInfo['字典类型']}.json' "/>
                               
                      <#else>
                        <input class="easyui-textbox ${fieldClass}" type="text" name="${column.javaName}" 
                            <#if column.columnSize gte 500 ><#t/>
                               style="width:185px;height:200px"
                            </#if><#t/>
                               data-options="required:${requiredValue},
                                         <#if column.columnSize gte 500 ><#t/>
                                             multiline:true, 
                                         </#if><#t/>
                                             validType:'length[0,${column.columnSize}]' "/>
                      </#if>
                    </td>
                </tr>
       </#if>
    </#list>
</#macro>