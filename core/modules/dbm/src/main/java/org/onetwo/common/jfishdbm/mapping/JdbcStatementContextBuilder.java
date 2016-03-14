package org.onetwo.common.jfishdbm.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onetwo.common.jfishdbm.event.JFishEventAction;
import org.onetwo.common.jfishdbm.mapping.SQLBuilderFactory.SqlBuilderType;
import org.onetwo.common.utils.ArrayUtils;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;

public class JdbcStatementContextBuilder implements JdbcStatementContext<List<Object[]>> {
/*
	public static DymanicSQLBuilder create(JFishMappedEntry entry, DSqlType dtype){
		SQLBuilder sb = sqlBuilderFactory.createQMark(entry.getTableInfo().getName(), entry.getTableInfo().getAlias(), dtype);
		return create(entry, sb);
	}*/
	public static JdbcStatementContextBuilder create(JFishEventAction eventAction, AbstractJFishMappedEntryImpl entry, EntrySQLBuilder sqlBuilder){
		JdbcStatementContextBuilder dsql = new JdbcStatementContextBuilder(eventAction, entry, sqlBuilder);
		return dsql;
	}
	
	private AbstractJFishMappedEntryImpl entry;
	private EntrySQLBuilder sqlBuilder;
	private Map<DbmMappedField, Object> columnValues = CUtils.newLinkedHashMap();
	private List<Object> causeValues = new ArrayList<Object>(5);
	private List<Object[]> values;
	private final JFishEventAction eventAction;

	private JdbcStatementContextBuilder(JFishEventAction eventAction, AbstractJFishMappedEntryImpl entry, EntrySQLBuilder sqlBuilder) {
		super();
		this.entry = entry;
		this.sqlBuilder = sqlBuilder;
		this.values = new ArrayList<Object[]>();
		this.eventAction = eventAction;
	}
	
	public JdbcStatementContextBuilder append(DbmMappedField field, Object val){
		this.sqlBuilder.append(field);
		this.columnValues.put(field, val);
		return this;
	}
	
	public JdbcStatementContextBuilder appendWhere(DbmMappedField column, Object val){
		this.sqlBuilder.appendWhere(column);
		this.causeValues.add(val);
		return this;
	}
	
	public SqlBuilderType getSqlType(){
		return this.sqlBuilder.getType();
	}
	
	public JdbcStatementContextBuilder processColumnValues(Object entity){
		Assert.notNull(entity);
		Object val = null;
		EntrySQLBuilder builder = getSqlBuilder();
		for(DbmMappedField field : builder.getFields()){
			val = field.getValueForJdbcAndFireDbmEventAction(entity, getEventAction());
			if(field.isVersionControll()){
				if(JFishEventAction.insert==getEventAction()){
					val = field.getVersionableType().getVersionValule(val);
					field.setValue(entity, val);//write the version value into the entity
				}else if(JFishEventAction.update==getEventAction()){
					Assert.notNull(val, "version field["+field.getName()+"] can't be null: " + entry.getEntityName());
					val = field.getVersionableType().getVersionValule(val);
//					field.setValue(entity, val);
				}
			}
			this.columnValues.put(field, val);
		}
		return this;
	}
	
	public JdbcStatementContextBuilder addBatch(){
		Object[] batchValues = null;
		if(SqlBuilderType.update==getSqlType()){
//			this.getColumnValues().addAll(getCauseValues());
			batchValues = ArrayUtils.addAll(this.columnValues.values().toArray(), causeValues.toArray());
			
		}else if(SqlBuilderType.insert==getSqlType()){
			batchValues = columnValues.values().toArray();
			
		}else if(SqlBuilderType.delete==getSqlType()){
//			this.getColumnValues().addAll(getCauseValues());
			batchValues = ArrayUtils.addAll(columnValues.values().toArray(), causeValues.toArray());
			
		}else if(SqlBuilderType.query==getSqlType()){
//			this.getColumnValues().addAll(getCauseValues());
			batchValues = ArrayUtils.addAll(columnValues.values().toArray(), causeValues.toArray());
			
		}else{
			LangUtils.throwBaseException("not support type : " + getSqlType());
		}
		if(!LangUtils.isEmpty(batchValues)){
			this.values.add(batchValues);
			this.causeValues.clear();
			this.columnValues.clear();
		}
		return this;
	}
	
	public JdbcStatementContextBuilder processWhereCauseValuesFromEntity(Object entity){
		Assert.notNull(entity);
		Object val = null;
		for(DbmMappedField field : this.sqlBuilder.getWhereCauseFields()){
			val = field.getValueForJdbc(entity);
			this.causeValues.add(val);
		}
		return this;
	}
	
	public JdbcStatementContextBuilder addCauseValue(Object value){
		causeValues.add(value);
		return this;
	}
	public JdbcStatementContextBuilder build(){
		sqlBuilder.build();
		addBatch();
		return this;
	}

	public String getSql(){
		return sqlBuilder.getSql();
	}

	public List<Object[]> getValue() {
		return values;
	}

	public Object[] getValueArray() {
		return values.toArray();
	}

	public JFishMappedEntry getEntry() {
		return entry;
	}

	public EntrySQLBuilder getSqlBuilder() {
		return sqlBuilder;
	}

	public JFishEventAction getEventAction() {
		return eventAction;
	}
}
