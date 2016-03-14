package org.onetwo.plugins.permission.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.onetwo.common.db.BaseEntityManager;
import org.onetwo.common.db.ExtQuery.K;
import org.onetwo.common.db.ExtQuery.K.IfNull;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.Assert;
import org.onetwo.plugins.permission.MenuInfoParser;
import org.onetwo.plugins.permission.PermissionUtils;
import org.onetwo.plugins.permission.entity.IMenu;
import org.onetwo.plugins.permission.entity.IPermission;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PermissionManagerImpl implements PluginPermissionManager {
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());

	private Map<String, ? extends IPermission> menuNodeMap;

	@Resource
	private MenuInfoParser menuInfoParser;
	
	@Resource
	private BaseEntityManager baseEntityManager;
	
	public PermissionManagerImpl() {
	}

	@Override
	public void build(){
		PermissionUtils.setMenuInfoParser(menuInfoParser);
		IMenu rootMenu = menuInfoParser.parseTree();
//		logger.info("menu:\n" + rootMenu);
		this.menuNodeMap = menuInfoParser.getPermissionMap();
	}
	
	@Override
	public IPermission getPermission(Class<?> permClass){
		return menuInfoParser.getPermission(permClass);
	}

	@Override
	@Transactional
	public IMenu getDatabaseRootMenu() {
		return (IMenu)baseEntityManager.findUnique(IMenu.class, "code", this.menuInfoParser.getRootMenuCode());
	}
	
	@Override
	@Transactional
	public IMenu getDatabaseMenuNode(Class<?> clazz) {
		String code = menuInfoParser.getCode(clazz);
		return (IMenu)baseEntityManager.findUnique(this.menuInfoParser.getMenuInfoable().getIMenuClass(), "code", code);
	}
	
	/****
	 * 同步菜单
	 */
	@Override
	@Transactional
	public void syncMenuToDatabase(){
		Class<?> rootMenuClass = this.menuInfoParser.getMenuInfoable().getRootMenuClass();
		Class<?> permClass = this.menuInfoParser.getMenuInfoable().getIPermissionClass();
		String rootCode = parseCode(rootMenuClass);
		List<? extends IPermission> permList = (List<? extends IPermission>)this.baseEntityManager.findByProperties(permClass, "code:like", rootCode+"%");
//		Map<String, IPermission> mapByCode = index(permList, on(IPermission.class).getCode());
		
		Session session = baseEntityManager.getRawManagerObject(SessionFactory.class).getCurrentSession();
		for(IPermission dbperm : permList){
			IPermission clsPerm = menuNodeMap.get(dbperm.getCode());
			if(!session.contains(dbperm))
				continue;
			if(clsPerm==null){
				removePermission(dbperm);
				session.evict(dbperm);
			}else if(clsPerm.getClass()!=dbperm.getClass()){
				removePermission(dbperm);
				session.evict(dbperm);
			}
		}
		session.flush();
		session.merge(this.menuInfoParser.getRootMenu());
	}
	
	private void removePermission(IPermission dbperm){
		dbperm.onRemove();
		baseEntityManager.remove(dbperm);
	}
	
	@Override
	public <T> T findById(Long id){
//		return (T)baseEntityManager.findById(this.menuInfoParser.getMenuInfoable().getIPermissionClass(), id);
		return (T)findById(this.menuInfoParser.getMenuInfoable().getIPermissionClass(), id);
	}

	public MenuInfoParser getMenuInfoParser() {
		return menuInfoParser;
	}

	@Override
	public String parseCode(Class<?> permClass) {
		return menuInfoParser.getCode(permClass);
	}

	@Override
	@Transactional(readOnly=true)
	public List<IMenu> findAppMenus(String appCode){
		List<IMenu> menulist = (List<IMenu>)baseEntityManager.findByProperties(this.menuInfoParser.getMenuInfoable().getIMenuClass(), "appCode", appCode);
		return menulist;
	}

	@Override
	@Transactional(readOnly=true)
	public List<? extends IPermission> findAppPermissions(String appCode){
		List<IPermission> menulist = (List<IPermission>)baseEntityManager.findByProperties(this.menuInfoParser.getMenuInfoable().getIPermissionClass(), "appCode", appCode);
		return menulist;
	}

	@Override
	public List<? extends IPermission> findPermissionByCodes(String appCode, String[] permissionCodes) {
		/*Assert.notEmpty(permissionCodes);
		List<IPermission> permlist = (List<IPermission>)baseEntityManager.findByProperties(
																	this.menuInfoParser.getMenuInfoable().getIPermissionClass(), 
																	"code:in", permissionCodes,
																	"appCode", appCode,
																	K.IF_NULL, IfNull.Ignore);
		return permlist;*/
		return findPermissionByCodes(menuInfoParser.getMenuInfoable().getIPermissionClass(), appCode, permissionCodes);
	}
	

	@Override
	public <T> T findById(Class<?> clazz, Long id) {
		return (T)baseEntityManager.findById(clazz, id);
	}

	@Override
	public List<? extends IMenu> findAppMenus(Class<?> clazz, String appCode) {
		List<IMenu> menulist = (List<IMenu>)baseEntityManager.findByProperties(clazz, "appCode", appCode);
		return menulist;
	}

	@Override
	public List<? extends IPermission> findAppPermissions(Class<?> clazz,
			String appCode) {
		List<IPermission> menulist = (List<IPermission>)baseEntityManager.findByProperties(clazz, "appCode", appCode);
		return menulist;
	}

	@Override
	public List<? extends IPermission> findPermissionByCodes(Class<?> clazz,
			String appCode, String[] permissionCodes) {
		Assert.notEmpty(permissionCodes);
		List<IPermission> permlist = (List<IPermission>)baseEntityManager.findByProperties(
																	clazz, 
																	"code:in", permissionCodes,
																	"appCode", appCode,
																	K.IF_NULL, IfNull.Ignore);
		return permlist;
	}
	
	

}
