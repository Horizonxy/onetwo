package org.onetwo.common.hibernate;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.pojo.BasicLazyInitializer;
import org.hibernate.tuple.StandardProperty;
import org.onetwo.common.db.BaseEntityManager;
import org.onetwo.common.db.IBaseEntity;
import org.onetwo.common.ds.SwitcherInfo;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.hibernate.msf.JFishMultipleSessionFactory;
import org.onetwo.common.spring.SpringApplication;
import org.onetwo.common.utils.AnnotationUtils;
import org.onetwo.common.utils.ArrayUtils;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.Intro;
import org.onetwo.common.utils.ReflectUtils;
import org.onetwo.common.utils.ReflectUtils.IgnoreAnnosCopyer;
import org.onetwo.common.utils.StringUtils;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

public final class HibernateUtils {
	

	public static final Class<? extends Annotation>[] IGNORE_ANNO_CLASSES = new Class[]{ManyToOne.class, ManyToMany.class, OneToMany.class, OneToOne.class, Transient.class};
	public static final String[] TIMESTAMP_FIELDS = new String[]{"createTime", "lastUpdateTime"};
	public static final class HiberanteCopyer extends IgnoreAnnosCopyer {
		private String[] ignoreFields;
		
		public HiberanteCopyer() {
			super(null);
		}
		public HiberanteCopyer(Class<? extends Annotation>[] classes, String... ignoreFields) {
			super(classes);
			this.ignoreFields = ignoreFields;
		}
		
		protected boolean hasIgnoredAnnotation(PropertyDescriptor prop){
			Method m = prop.getReadMethod();
			boolean ignoreProp = AnnotationUtils.containsAny(m.getAnnotations(), classes);
			return ignoreProp?true:AnnotationUtils.isFieldContains(m.getDeclaringClass(), prop.getName(), classes);
		}

		@Override
		public void copy(Object source, Object target, PropertyDescriptor targetProperty) {
			if(target instanceof IBaseEntity){
				if(ArrayUtils.contains(TIMESTAMP_FIELDS, targetProperty.getName()))
					return ;
			}
			if(ArrayUtils.contains(ignoreFields, targetProperty.getName())){
				return ;
			}
			super.copy(source, target, targetProperty);
		}
		
		protected boolean isIgnoreValue(Object val){
			if(val==null)
				return true;
			return false;
		}
		
	};

	private static final HiberanteCopyer WITHOUT_RELATION = new HiberanteCopyer(IGNORE_ANNO_CLASSES);
	private static final HiberanteCopyer COMMON = new HiberanteCopyer(new Class[]{Transient.class});
	
	private static SessionFactory sessionFactory;
	
	public static void initSessionFactory(SessionFactory sessionFactory) {
		HibernateUtils.sessionFactory = sessionFactory;
	}

	public static void init(Object object){
		Hibernate.initialize(object);
	}
	
	public static Serializable generateIdentifier(BaseEntityManager bm, String rootEntityName){
		SessionFactoryImplementor sfi = bm.getRawManagerObject(SessionFactoryImplementor.class);
		SessionImplementor s = (SessionImplementor)sfi.getCurrentSession();
		IdentifierGenerator idg = sfi.getIdentifierGenerator(rootEntityName);
		Assert.notNull(idg);
		return idg.generate(s, null);
	}
	
	
	public static SessionFactory getSessionFactory() {
		if(JFishMultipleSessionFactory.class.isInstance(sessionFactory)){
			SwitcherInfo switcher = SpringApplication.getInstance().getContextHolder().getContextAttribute(SwitcherInfo.CURRENT_SWITCHER_INFO);
			return ((JFishMultipleSessionFactory) sessionFactory).getSessionFactory(switcher.getCurrentSwitcherName());
		}
		return sessionFactory;
	}

	public static ClassMetadata getClassMeta(Session s, Class<?> entityClass){
		return s.getSessionFactory().getClassMetadata(entityClass);
	}
	
	public static LocalSessionFactoryBean getLocalSessionFactoryBean(){
		Map<String, SessionFactory> sfMap = SpringApplication.getInstance().getBeansMap(SessionFactory.class);
		Iterator<String> it = sfMap.keySet().iterator();
		while(it.hasNext()){
			Object fb = SpringApplication.getInstance().getBean("&"+it.next());
			if(fb!=null && LocalSessionFactoryBean.class.isInstance(fb))
				return (LocalSessionFactoryBean)fb;
		}
		return null;
	}

	public static ClassMetadata getClassMeta(Session s, String entityClass){
		return s.getSessionFactory().getClassMetadata(entityClass);
	}
	
	public static ClassMetadata getClassMeta(String entityClass){
		return getSessionFactory().getClassMetadata(entityClass);
	}
	
	public static ClassMetadata getClassMeta(Class<?> entityClass){
		return getClassMeta(entityClass, false);
	}
	
	public static String getIdName(Class<?> entityClass){
		ClassMetadata meta = getClassMeta(entityClass, true);
		return meta.getIdentifierPropertyName();
	}
	
	public static ClassMetadata getClassMeta(Class<?> entityClass, boolean throwIfNoteFound){
		ClassMetadata meta = getSessionFactory().getClassMetadata(entityClass);
		if(meta==null && throwIfNoteFound)
			throw new BaseException("can not find the entity["+entityClass+"], check it please!");
		return meta;
	}
	
	public static boolean setPropertyState(StandardProperty[] props, Object[] currentState, String property, Object value){
		Assert.hasText(property);
		for (int i = 0; i < props.length; i++) {
			if (props[i].getName().equals(property)) {
				currentState[i] = value;
				return true;
			}
		}
		return false;
	}
	

	public static final String JAVASSIST_KEY = Intro.JAVASSIST_KEY;
	/*****
	 * 复制对象属性，但会忽略那些null值和配置了关系的属性
	 * @param source
	 * @param target
	 */
	public static <T> void copyWithoutRelations(T source, T target){
		Class<?> targetClass = getTargetClass(target);
		ReflectUtils.getIntro(targetClass).copy(source, target, WITHOUT_RELATION);
	}
	
	public static Class<?> getTargetClass(Object target){
		Class<?> targetClass = target.getClass();
		if(isJavassistClass(targetClass)){
			BasicLazyInitializer handler = (BasicLazyInitializer)ReflectUtils.getFieldValue(target, "handler", false);
			if(handler!=null){
				targetClass = handler.getPersistentClass();
			}else{
				String className = StringUtils.substringBefore(targetClass.getName(), JAVASSIST_KEY);
				targetClass = ReflectUtils.loadClass(className);
			}
		}
		return targetClass;
	}
	
	public static boolean isJavassistClass(Class<?> clazz){
		return clazz.getName().contains(JAVASSIST_KEY);
	}
	
	public static void copyIgnoreRelationsAndFields(Object source, Object target, String... ignoreFields){
		ReflectUtils.getIntro(target.getClass()).copy(source, target, new HiberanteCopyer(IGNORE_ANNO_CLASSES, ignoreFields));
	}
	/****
	 * 复制对象属性，但会忽略那些null值
	 * @param source
	 * @param target
	 */
	public static <T> void copy(T source, T target){
		Class<?> targetClass = getTargetClass(target);
		ReflectUtils.getIntro(targetClass).copy(source, target, COMMON);
	}
	public static <T> void copyIgnore(T source, T target, String... ignoreFields){
		ReflectUtils.getIntro(target.getClass()).copy(source, target, new HiberanteCopyer(new Class[]{Transient.class}, ignoreFields));
	}
	
	/****
	 * 复制对象属性，但会忽略那些null值、空白字符和配置了关系的属性
	 * @param source
	 * @param targetClass
	 * @return
	 */
	public static <T> T copyToTargetWithoutRelations(Object source, Class<T> targetClass, String... ignoreFields){
		return ReflectUtils.copy(source, targetClass, new HiberanteCopyer(IGNORE_ANNO_CLASSES, ignoreFields));
	}
	/***
	 * 复制对象属性，但会忽略那些null值、空白字符
	 * @param source
	 * @param targetClass
	 */
	public static <T> T copy(T source, Class<T> targetClass){
		return ReflectUtils.copy(source, targetClass, COMMON);
	}
	
}
