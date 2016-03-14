package org.onetwo.common.db.filequery;

import java.util.Map;

import org.onetwo.common.db.DataBase;
import org.onetwo.common.db.filequery.func.OracleSqlFunctionDialet;
import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.common.db.filequery.func.SqlServerSqlFunctionDialet;
import org.onetwo.common.utils.LangUtils;

public class SqlFunctionFactory {
	public static final String CONTEXT_KEY = "_db";

	private static Map<DataBase, SqlFunctionDialet> DIALETS = LangUtils.newHashMap();

	static {
		DIALETS.put(DataBase.Sqlserver, new SqlServerSqlFunctionDialet());
		DIALETS.put(DataBase.Oracle, new OracleSqlFunctionDialet());
	}

	public static SqlFunctionDialet getSqlFunctionDialet(DataBase dataBase) {
		return DIALETS.get(dataBase);
	}

	private SqlFunctionFactory(DataBase dataBase) {
	}

}
