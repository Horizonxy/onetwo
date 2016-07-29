package org.onetwo.common.jfishdbm.jdbc.mapper;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.onetwo.common.jfishdbm.jdbc.type.TypeHandler;
import org.onetwo.common.jfishdbm.mapping.SqlTypeMapping;
import org.onetwo.common.jfishdbm.utils.DbmUtils;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.Intro;
import org.onetwo.common.utils.JFishProperty;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class DbmBeanPropertyRowMapper<T> implements RowMapper<T> {
	final private Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	private ConversionService conversionService = new DefaultConversionService();
	private boolean primitivesDefaultedForNullValue = true;

	private Class<T> mappedClass;
	private Set<String> mappedProperties;
	private Map<String, PropertyDescriptor> mappedFields;
	private SqlTypeMapping sqlTypeMapping;

	public DbmBeanPropertyRowMapper() {
		super();
	}

	public DbmBeanPropertyRowMapper(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
	}

	protected String lowerCaseName(String name) {
		return name.toLowerCase(Locale.US);
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	protected void initialize(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
		this.mappedFields = new HashMap<String, PropertyDescriptor>();
		this.mappedProperties = new HashSet<String>();
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null) {
				this.mappedFields.put(lowerCaseName(pd.getName()), pd);
				String underscoredName = underscoreName(pd.getName());
				if (!lowerCaseName(pd.getName()).equals(underscoredName)) {
					this.mappedFields.put(underscoredName, pd);
				}
				this.mappedProperties.add(pd.getName());
			}
		}
	}

	/**
	 * Convert a name in camelCase to an underscored name in lower case.
	 * Any upper case letters are converted to lower case with a preceding underscore.
	 * @param name the original name
	 * @return the converted name
	 * @since 4.2
	 * @see #lowerCaseName
	 */
	protected String underscoreName(String name) {
		if (!StringUtils.hasLength(name)) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		result.append(lowerCaseName(name.substring(0, 1)));
		for (int i = 1; i < name.length(); i++) {
			String s = name.substring(i, i + 1);
			String slc = lowerCaseName(s);
			if (!s.equals(slc)) {
				result.append("_").append(slc);
			}
			else {
				result.append(s);
			}
		}
		return result.toString();
	}

	protected void initBeanWrapper(BeanWrapper bw) {
		ConversionService cs = getConversionService();
		if (cs != null) {
			bw.setConversionService(cs);
		}
	}
	
	@Override
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		Assert.state(this.mappedClass != null, "Mapped class was not specified");
		T mappedObject = BeanUtils.instantiate(this.mappedClass);
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
		initBeanWrapper(bw);

		ResultSetWrappingSqlRowSet resutSetWrapper = new ResultSetWrappingSqlRowSet(rs);
		SqlRowSetMetaData rsmd = resutSetWrapper.getMetaData();
		int columnCount = resutSetWrapper.getMetaData().getColumnCount();

		for (int index = 1; index <= columnCount; index++) {
			String column = DbmUtils.lookupColumnName(rsmd, index);
			String field = lowerCaseName(column.replaceAll(" ", ""));
			PropertyDescriptor pd = this.mappedFields.get(field);
			if (pd != null) {
				try {
					Object value = getColumnValue(resutSetWrapper, index, pd, rsmd.getColumnType(index));
					if (rowNumber == 0 && logger.isDebugEnabled()) {
						logger.debug("Mapping column '" + column + "' to property '" + pd.getName() +
								"' of type [" + ClassUtils.getQualifiedName(pd.getPropertyType()) + "]");
					}
					try {
						bw.setPropertyValue(pd.getName(), value);
					}
					catch (TypeMismatchException ex) {
						if (value == null && this.primitivesDefaultedForNullValue) {
							if (logger.isDebugEnabled()) {
								logger.debug("Intercepted TypeMismatchException for row " + rowNumber +
										" and column '" + column + "' with null value when setting property '" +
										pd.getName() + "' of type [" +
										ClassUtils.getQualifiedName(pd.getPropertyType()) +
										"] on object: " + mappedObject, ex);
							}
						}
						else {
							throw ex;
						}
					}
				}
				catch (NotWritablePropertyException ex) {
					throw new DataRetrievalFailureException(
							"Unable to map column '" + column + "' to property '" + pd.getName() + "'", ex);
				}
			}
			else {
				// No PropertyDescriptor found
				if (rowNumber == 0 && logger.isDebugEnabled()) {
					logger.debug("No property found for column '" + column + "' mapped to field '" + field + "'");
				}
			}
		}

		return mappedObject;
	}
	
	/*protected Object getColumnValue(ResultSetWrappingSqlRowSet rs, int index, PropertyDescriptor pd) throws SQLException {
		final Object value = JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
		JFishProperty jproperty = Intro.wrap(pd.getWriteMethod().getDeclaringClass()).getJFishProperty(pd.getName(), false);
		Object actualValue = DbmUtils.convertPropertyValue(jproperty, value);
		return actualValue;
	}*/
	protected Object getColumnValue(ResultSetWrappingSqlRowSet rs, int index, PropertyDescriptor pd, int sqlType) throws SQLException {
		JFishProperty jproperty = Intro.wrap(pd.getWriteMethod().getDeclaringClass()).getJFishProperty(pd.getName(), false);
		TypeHandler<?> typeHandler = sqlTypeMapping.getTypeHander(jproperty.getType(), sqlType);
		Object value = typeHandler.getResult(rs, index);
		return value;
	}

}
