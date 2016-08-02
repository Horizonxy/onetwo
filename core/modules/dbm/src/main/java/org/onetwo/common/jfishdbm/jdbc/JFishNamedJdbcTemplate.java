package org.onetwo.common.jfishdbm.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/***
 * 对jdbc查询的扩展，查询的sql里可包含命名的参数
 * @author weishao
 *
 */
public class JFishNamedJdbcTemplate extends NamedParameterJdbcTemplate implements NamedJdbcTemplate{

	public JFishNamedJdbcTemplate(DataSource dataSource) {
		super(dataSource);
	}

	public JFishNamedJdbcTemplate(JdbcOperations classicJdbcTemplate) {
		super(classicJdbcTemplate);
	}

	public Object execute(String sql, Map<String, ?> paramMap) throws DataAccessException {
		PreparedStatementCreator pstCreator = getPreparedStatementCreator(sql, new MapSqlParameterSource(paramMap));
		final PreparedStatementSetter setter = (PreparedStatementSetter) pstCreator;
//		System.out.println("sql: " + ((SqlProvider)setter).getSql());
		return execute(sql, new MapSqlParameterSource(paramMap), new PreparedStatementCallback<Object>(){

			@Override
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				setter.setValues(ps);
				return ps.execute();
			}
			
		});
	}
	
	@Override
	protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
		ParsedSql parsedSql = getParsedSql(sql);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
		List<SqlParameter> declaredParameters = NamedParameterUtils.buildSqlParameterList(parsedSql, paramSource);
		/*DbmPreparedStatementCreatorFactory pscf = new DbmPreparedStatementCreatorFactory(sqlToUse, declaredParameters);
		return pscf.newPreparedStatementCreator(params);*/
		return this.createPreparedStatementCreator(sqlToUse, params, declaredParameters);
	}
	
	protected PreparedStatementCreator createPreparedStatementCreator(String sqlToUse, Object[] params, List<SqlParameter> declaredParameters) {
		DbmPreparedStatementCreatorFactory pscf = new DbmPreparedStatementCreatorFactory(sqlToUse, declaredParameters);
		return pscf.newPreparedStatementCreator(params);
	}
	
	/*
	@Override
	public <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper)
			throws DataAccessException {

		return query(sql, new MapSqlParameterSource(paramMap), rowMapper);
	}
	
	static class DbmMapSqlParameterSource extends MapSqlParameterSource {
		
		public MapSqlParameterSource addValue(String paramName, Object value) {
			Object newValue = unwrapSqlParameterValue(value);
			return super.addValue(paramName, newValue);
		}
		
		@Override
		public MapSqlParameterSource addValue(String paramName, Object value, int sqlType) {
			Object newValue = unwrapSqlParameterValue(value);
			return super.addValue(paramName, newValue, sqlType);
		}

		@Override
		public MapSqlParameterSource addValue(String paramName, Object value, int sqlType, String typeName) {
			Object newValue = unwrapSqlParameterValue(value);
			return super.addValue(paramName, newValue, sqlType, typeName);
		}

		@Override
		public DbmMapSqlParameterSource addValues(Map<String, ?> values) {
			if (values != null) {
				values.entrySet().forEach(e->addValue(e.getKey(), e.getValue()));
			}
			return this;
		}

		public static Object unwrapSqlParameterValue(Object value){
			return DbmUtils.unwrapSqlParameterValue(value);
		}
	}*/

}
