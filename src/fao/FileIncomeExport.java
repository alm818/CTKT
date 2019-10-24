package fao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;

import dao.BillDAO;
import dao.CustomerDAO;
import dao.ProductDAO;
import dao.RSellDAO;
import dao.SellDAO;
import dao.SupplierDAO;
import factory.ComparatorFactory;
import factory.HSSFCellRendererFactory;
import factory.HSSFCellRendererFactory.CellRenderer;
import misc.BillType;
import misc.Pair;
import factory.HSSFCellStyleFactory;
import transferObject.Bill;
import transferObject.ResSet;
import transferObject.Supplier;
import utility.Utility;

public class FileIncomeExport {
	private static final String[] COLUMN_TITLE = { 
			"Ngày\nhóa đơn", "Mã\nhóa đơn", 
			"Tên khách hàng", "Mã mặt hàng", "Tên mặt hàng", 
			"Đơn giá\nmua", "Đơn giá\nbán", "Số lượng", 
			"Thành tiền\nchênh lệch", "Phần trăm\nlợi nhuận", "Hóa đơn mua"};
	
	public static void export(Calendar from, Calendar to) throws SQLException, IOException{
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFCellStyleFactory factory = new HSSFCellStyleFactory(wb);
		HSSFCellRendererFactory rFactory = new HSSFCellRendererFactory(factory);
		CellRenderer[] renderers = { rFactory.getCalendarRenderer(), rFactory.getStringRenderer(), rFactory.getStringRenderer(), rFactory.getStringRenderer(), rFactory.getStringRenderer(),
				rFactory.getPriceRenderer(), rFactory.getPriceRenderer(), rFactory.getQuantityRenderer(),
				rFactory.getSignedPriceRenderer(), rFactory.getPercentRenderer()};
		CellRenderer stringRenderer = rFactory.getStringRenderer();
		ArrayList<Bill> billList = SellDAO.getBills(from, to);
		HashMap<String, ArrayList<Object[]>> data = new HashMap<String, ArrayList<Object[]>>();
		for (Bill bill : billList){
			for (String nameProduct : bill.getProductList()){
				double q = bill.getQ(nameProduct);
				if (q == 0) continue;
				ResSet resSet = SellDAO.getProfit(bill.getProduct(nameProduct).getID());
				if (resSet == null) continue;
				String nameCustomer = CustomerDAO.getCustomerList().get(bill.getTarget()).getName();
				String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
				String _codeProduct = codeProduct, _nameProduct = nameProduct;
				//GET-NEW
				if (ProductDAO.isOld(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
					_codeProduct = pair.getFirst();
					_nameProduct = pair.getSecond();
				}
				
				Double rq = (Double) resSet.get("rq");
				if (rq > 0) continue;
				Integer sp = (Integer) resSet.get("sp");
				Integer bp = (Integer) resSet.get("bp");
				if (bp == 0) continue;
				Integer dif = (int) ((sp - bp) * q);
				double percent = (sp - bp) / (double) bp;
				if (!data.containsKey(nameProduct)) data.put(nameProduct, new ArrayList<Object[]>());
				Object[] thisRow = {
						bill.getDay(), bill.getCodeBill(), nameCustomer, _codeProduct, _nameProduct,
						bp, sp, q, dif, percent
				};
				data.get(nameProduct).add(thisRow);
			}
		}
		for (String nameSupplier : SupplierDAO.getSupplierList().keySet()){
			Supplier supplier = SupplierDAO.getSupplierList().get(nameSupplier);
			Double lim = supplier.getLim();
			if (supplier.isMain() && lim != null){
				HSSFSheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(supplier.getCode()));
				HSSFCell titleCell = sheet.createRow(1).createCell(0);
				titleCell.setCellStyle(factory.getTitleStyle());
				titleCell.setCellValue(nameSupplier);
				sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, COLUMN_TITLE.length - 2));
				HSSFRow headerRow = sheet.createRow(3);
				for (int i = 0; i < COLUMN_TITLE.length; i ++){
					HSSFCell cell = headerRow.createCell(i);
					cell.setCellStyle(factory.getHeaderStyle());
					cell.setCellValue(COLUMN_TITLE[i]);
				}
				ArrayList<Object[]> allRows = new ArrayList<Object[]>();
				for (String nameProduct : supplier.getProducts())
					if (data.containsKey(nameProduct))
						allRows.addAll(data.get(nameProduct));
				allRows.sort(ComparatorFactory.getRowComparator());
				
				int rowIndex = 4;
				int maxSize = 1;
				for (Object[] row : allRows){
					double percent = (double) row[9];
					if (percent >= lim) break;
					HSSFRow cellRow = sheet.createRow(rowIndex);
					for (int col = 0; col < COLUMN_TITLE.length - 1; col ++){
						CellRenderer renderer = renderers[col];
						Object value = row[col];
						renderer.render(cellRow.createCell(col), value);
					}
					String codeSell = (String) row[1];
					String _codeProduct = (String) row[3];
					String _nameProduct = (String) row[4];
		    		String nameProduct = _nameProduct;
					//NEW-CONVERSE
					if (ProductDAO.isNew(_codeProduct, _nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(_codeProduct, _nameProduct);
						nameProduct = pair.getSecond();
					}
					int sp_id = BillDAO.getBill(codeSell).getProduct(nameProduct).getID();
					ArrayList<String> codeList = SellDAO.getCodeHistory(sp_id);
					int realSize = 0;
					HashSet<String> codeSet = new HashSet<String>();
					for (String code : codeList){
						switch (Utility.getBillType(code)){
							case INITNP:
							case INITP:
								continue;
							case BUY:
								break;
							case RSELL:
								int id = BillDAO.getBill(code).getProduct(nameProduct).getID();
								Pair<Integer, String> res = RSellDAO.getLast(id);
								code = res.getSecond();
								break;
							default:
								throw new SQLException("Invalid codeBill " + code);
						}
						if (Utility.getBillType(code) == BillType.RSELL || codeSet.contains(code))
							continue;
						codeSet.add(code);
						stringRenderer.render(cellRow.createCell(COLUMN_TITLE.length - 1 + realSize), code);
						realSize ++;
					}
					if (maxSize < realSize) maxSize = realSize;
					rowIndex ++;
				}
				if (maxSize > 1)
					sheet.addMergedRegion(new CellRangeAddress(3, 3, COLUMN_TITLE.length - 1, COLUMN_TITLE.length + maxSize - 2));
				for (int i = 0; i < COLUMN_TITLE.length; i ++)
					sheet.autoSizeColumn(i);
			}
		}
		int month = from.get(Calendar.MONTH) + 1;
		FileOutputStream fileOut = new FileOutputStream(FileDirection.getFileOutName(month));
		wb.write(fileOut);
		wb.close();
		fileOut.flush();
		fileOut.close();
	}
}
