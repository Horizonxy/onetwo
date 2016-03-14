package org.onetwo.common.jfishdbm.utils;

import org.onetwo.common.jfishdbm.dialet.DBDialect;
import org.onetwo.common.jfishdbm.mapping.MappedEntryManager;
import org.onetwo.common.jfishdbm.support.DbmDaoImplementor;
import org.onetwo.common.spring.SpringUtils;
import org.springframework.context.ApplicationContext;

final public class DbmSpringUtils {

	private DbmSpringUtils(){
	}
	
	/*public static DBDialect getMatchDBDiaclet(ApplicationContext applicationContext, DBMeta dbmeta){
		DBDialect dialect = applicationContext.getBean(dbmeta.getDialetName(), DBDialect.class);
		if(dialect instanceof InnerDBDialet){
			InnerDBDialet innerDialect = (InnerDBDialet) dialect;
			if(dbmeta!=null && innerDialect.getDbmeta()==null)
				innerDialect.setDbmeta(dbmeta);
		}
		return dialect;
	}*/
	
	public static DBDialect getJFishDBDiaclet(ApplicationContext applicationContext){
		return getJFishDao(applicationContext).getDialect();
	}
	
	public static DbmDaoImplementor getJFishDao(ApplicationContext applicationContext){
		DbmDaoImplementor dao = SpringUtils.getHighestOrder(applicationContext, DbmDaoImplementor.class);
		return dao;
	}
	
	public static MappedEntryManager getJFishMappedEntryManager(ApplicationContext applicationContext){
		MappedEntryManager mappedEntryManager = SpringUtils.getHighestOrder(applicationContext, MappedEntryManager.class);
		return mappedEntryManager;
	}
	
}
