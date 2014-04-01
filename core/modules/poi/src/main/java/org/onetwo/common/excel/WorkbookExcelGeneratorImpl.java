package org.onetwo.common.excel;

import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.onetwo.common.excel.data.WorkbookData;
import org.onetwo.common.utils.StringUtils;

/***
 * excel（多sheet）生成器
 * @author weishao
 *
 */
public class WorkbookExcelGeneratorImpl extends AbstractWorkbookExcelGenerator {
	
	private WorkbookData workbookData;
//	private Map<String, Object> context;

	public WorkbookExcelGeneratorImpl(WorkbookModel workbookModel, Map<String, Object> context){
		DefaultExcelValueParser excelValueParser = new DefaultExcelValueParser(context);
//		this.context = context;
//		Object data = context.get("reportData0");
		WorkbookListener workbookListener = null;
		if(StringUtils.isNotBlank(workbookModel.getListener()))
			workbookListener = (WorkbookListener)excelValueParser.parseValue(workbookModel.getListener(), workbookModel, null);
		if(workbookListener==null)
			workbookListener = WorkbookData.EMPTY_WORKBOOK_LISTENER;
		this.workbookData = new WorkbookData(workbookModel, new HSSFWorkbook(), excelValueParser, workbookListener);
		this.workbookData.initData();
	}
	@Override
	public void generateIt() {
		this.workbookData.getWorkbookListener().afterCreateWorkbook(getWorkbook());
		for(TemplateModel template : workbookData.getWorkbookModel().getSheets()){
			PoiExcelGenerator pg = DefaultExcelGeneratorFactory.createExcelGenerator(workbookData, template);
			pg.generateIt();
		}
	}

	@Override
	public Workbook getWorkbook() {
		return workbookData.getWorkbook();
	}

}
