package fao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;

import dao.ProductDAO;
import dao.SupplierDAO;
import factory.ComparatorFactory;
import factory.HSSFCellRendererFactory;
import factory.HSSFCellStyleFactory;
import factory.HSSFCellRendererFactory.CellRenderer;
import misc.Pair;
import transferObject.TaxConnector;

public class FileTaxExport {
	private static final String[] COLUMN_TITLE = { 
			"Mã hàng\nthuế", "Tên hàng thuế", 
			"Mã hàng\nthực tế", "Tên hàng thực tế",
			"Tồn đầu\nthuế", "Tồn đầu\nthực tế",
			"SL nhập\nthuế", "SL nhập\nthực tế", 
			"SL xuất\nthuế", "SL xuất\nthực tế", "Ghi chú"};
	
	public static void export(TaxConnector connector, Vector<Object[]> data) throws IOException{
		HashMap<String, ArrayList<Object[]>> supplierMap = new HashMap<String, ArrayList<Object[]>>();
		for (Object[] row : data){
			if ((boolean) row[11]){
				String _nameProduct = (String) row[3];
				String _codeProduct = (String) row[2];
				String codeProduct = _codeProduct, nameProduct = _nameProduct;
				//NEW-CONVERSE
				if (ProductDAO.isNew(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
					codeProduct = pair.getFirst();
					nameProduct = pair.getSecond();
				}
				String nameSupplier = SupplierDAO.getSupplier(nameProduct).getName();
				if (!supplierMap.containsKey(nameSupplier)) supplierMap.put(nameSupplier, new ArrayList<Object[]>());
				supplierMap.get(nameSupplier).add(row);
			}
		}
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFCellStyleFactory factory = new HSSFCellStyleFactory(wb);
		HSSFCellRendererFactory rFactory = new HSSFCellRendererFactory(factory);
		CellRenderer[] renderers = { rFactory.getStringRenderer(), rFactory.getStringRenderer(), rFactory.getStringRenderer(), rFactory.getStringRenderer(),
				rFactory.getQuantityRenderer(), rFactory.getQuantityRenderer(), rFactory.getQuantityRenderer(),
				rFactory.getQuantityRenderer(), rFactory.getQuantityRenderer(),
				rFactory.getQuantityRenderer(), rFactory.getStringRenderer()};
		for (String nameSupplier : supplierMap.keySet()){
			HSSFSheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(SupplierDAO.getSupplierList().get(nameSupplier).getCode()));
			HSSFCell titleCell = sheet.createRow(1).createCell(0);
			titleCell.setCellStyle(factory.getTitleStyle());
			titleCell.setCellValue(nameSupplier);
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, COLUMN_TITLE.length));
			
			HSSFRow headerRow = sheet.createRow(3);
			for (int i = 0; i < COLUMN_TITLE.length; i ++){
				HSSFCell cell = headerRow.createCell(i);
				cell.setCellStyle(factory.getHeaderStyle());
				cell.setCellValue(COLUMN_TITLE[i]);
			}
			
			ArrayList<Object[]> rows = supplierMap.get(nameSupplier);
			rows.sort(ComparatorFactory.getCommentComparator());
			int rowIndex = 4;
			for (Object[] row : rows){
				HSSFRow cellRow = sheet.createRow(rowIndex);
				for (int col = 0; col < COLUMN_TITLE.length; col ++){
					CellRenderer renderer = renderers[col];
					Object value = row[col];
					renderer.render(cellRow.createCell(col), value);
				}
				rowIndex ++;
			}
			for (int i = 0; i < COLUMN_TITLE.length; i ++)
				sheet.autoSizeColumn(i);
		}
		FileOutputStream fileOut = new FileOutputStream(FileDirection.getFileTaxName(connector.getFrom(), connector.getTo()));
		wb.write(fileOut);
		wb.close();
		fileOut.flush();
		fileOut.close();
	}
}
