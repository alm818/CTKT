package fao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.WorkbookUtil;

import dao.BuyDAO;
import dao.ProductDAO;
import dao.SupplierDAO;
import factory.ComparatorFactory;
import factory.HSSFCellRendererFactory;
import factory.HSSFCellStyleFactory;
import factory.HSSFCellRendererFactory.CellRenderer;
import misc.Pair;
import transferObject.ResSet;
import transferObject.Supplier;
import transferObject.TaxConnector;
import view.TaxView;

public class FileTaxBillExport {
	private static final double VAT = 0.1;
	private static final Pair<Integer, Integer> COST = new Pair<Integer, Integer>(40000000, 55000001);
	private static final Pair<Integer, Integer> SKU = new Pair<Integer, Integer>(8, 11);
	
	private static final String[] COLUMN_TITLE = { 
			"Mã hàng\nthuế", "Tên hàng thuế", 
			"Số lượng", "Đơn giá",
			"Thành tiền", "Đơn giá mua\ngần nhất"};
	public static void export(TaxConnector connector, Vector<Object[]> data) throws SQLException, IOException{
		HashMap<Integer, ArrayList<String>> groupMap = new HashMap<Integer, ArrayList<String>>();
		HashMap<Integer, HashSet<String>> groupNames = new HashMap<Integer, HashSet<String>>();
		HashMap<String, Object[]> rowMap = new HashMap<String, Object[]>();
		int idx = TaxView.comments[0].indexOf(":");
		String contain = TaxView.comments[0].substring(0, idx);
		for (Object[] row : data){
			String comment = (String) row[10];
			if (comment.contains(contain)){
				String _nameProduct = (String) row[3];
				String _codeProduct = (String) row[2];
				String codeProduct = _codeProduct, nameProduct = _nameProduct;
				//NEW-CONVERSE
				if (ProductDAO.isNew(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
					codeProduct = pair.getFirst();
					nameProduct = pair.getSecond();
				}
				ResSet resProduct = connector.getProductMap().get(nameProduct);
				if (!resProduct.contains("price")){
					Integer price = BuyDAO.getLastestPrice(nameProduct, connector.getFrom());
					if (price == null) continue;
					resProduct.put("price", price);
				}
				double q = Double.parseDouble(comment.substring(idx + 1).trim());
				resProduct.put("q", q);
				Supplier supplier = SupplierDAO.getSupplier(nameProduct);
				resProduct.put("pricePercent", supplier.getPricePercent());
				int id = supplier.getGroupID();
				if (!groupMap.containsKey(id)) groupMap.put(id, new ArrayList<String>());
				if (!groupNames.containsKey(id)) groupNames.put(id, new HashSet<String>());
				groupMap.get(id).add(nameProduct);
				groupNames.get(id).add(supplier.getName());
				rowMap.put(nameProduct, row);
			}
		}
		ArrayList<Integer> ids = new ArrayList<Integer>(groupMap.keySet());
		Collections.sort(ids);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFCellStyleFactory factory = new HSSFCellStyleFactory(wb);
		HSSFCellRendererFactory rFactory = new HSSFCellRendererFactory(factory);
		CellRenderer[] renderers = { rFactory.getStringRenderer(), rFactory.getStringRenderer(), rFactory.getQuantityRenderer(),
				rFactory.getPriceRenderer(), rFactory.getPriceRenderer(), rFactory.getPriceRenderer()};
		int billID = 1;
		for (int id : ids){
			ArrayList<String> customers = connector.getCustomers(id);
			HSSFSheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Nhóm " + id));
			HSSFCell titleCell = sheet.createRow(1).createCell(1);
			titleCell.setCellStyle(factory.getTitleStyle());
			titleCell.setCellValue(String.format("Nhóm %d bao gồm các nhà cung cấp:", id));
			int rowIndex = 2;
			for (String nameSupplier : groupNames.get(id)){
				HSSFCell supplierCell = sheet.createRow(rowIndex).createCell(1);
				supplierCell.setCellStyle(factory.getCompanyStyle());
				supplierCell.setCellValue(nameSupplier);
				rowIndex ++;
			}
			rowIndex += 3;
			ArrayList<String> nameProducts = groupMap.get(id);
			while (nameProducts.size() > 0){
				int cost = ThreadLocalRandom.current().nextInt(COST.getFirst(), COST.getSecond());
				int sku = ThreadLocalRandom.current().nextInt(SKU.getFirst(), SKU.getSecond());
				Collections.shuffle(nameProducts);
				ArrayList<String> names = new ArrayList<String>(nameProducts.subList(0, Math.min(sku, nameProducts.size())));
				int totalWorth = 0;
				HashMap<String, Double> qMap = new HashMap<String, Double>();
				for (String name : names){
					qMap.put(name, 0.0);
					ResSet resProduct = connector.getProductMap().get(name);
					int price = (int) resProduct.get("price");
					double q = (double) resProduct.get("q");
					totalWorth += (price * q);
				}
				int newCost = cost;
				while (names.size() > 0 && cost > 0){
					for (Iterator<String> i = names.iterator(); i.hasNext();){
						String name = i.next();
						ResSet resProduct = connector.getProductMap().get(name);
						int price = (int) resProduct.get("price");
						double q = (double) resProduct.get("q");
						long worth = (int) (price * q);
						long expectedCost = (long) (cost * worth / (double) totalWorth);
						if (worth <= expectedCost){
							qMap.put(name, qMap.get(name) + q);
							newCost -= worth;
							nameProducts.remove(name);
							resProduct.put("q", 0.0);
							i.remove();
						} else{
							double needQ = Math.min(Math.ceil(expectedCost / (double) price), q);
							qMap.put(name, qMap.get(name) + needQ);
							newCost -= (needQ * price);
							q -= needQ;
							if (q == 0){
								nameProducts.remove(name);
								i.remove();
							}
							resProduct.put("q", q);
						}
					}
					totalWorth -= (cost - newCost);
					cost = newCost;
				}
				
				HSSFRow infoRow = sheet.createRow(rowIndex);
				HSSFCell billCell = infoRow.createCell(0);
				billCell.setCellStyle(factory.getRealWageStyle());
				billCell.setCellValue("Hóa đơn " + billID);
				HSSFCell customerCell = infoRow.createCell(1);
				customerCell.setCellStyle(factory.getRealWageStyle());
				customerCell.setCellValue("Khách hàng: " + customers.get(ThreadLocalRandom.current().nextInt(customers.size())));
				rowIndex ++;
				
				HSSFRow headerRow = sheet.createRow(rowIndex);
				for (int i = 0; i < COLUMN_TITLE.length; i ++){
					HSSFCell cell = headerRow.createCell(i);
					cell.setCellStyle(factory.getHeaderStyle());
					cell.setCellValue(COLUMN_TITLE[i]);
				}
				rowIndex ++;
				
				ArrayList<String> keySet = new ArrayList<String>(qMap.keySet());
				keySet.sort(ComparatorFactory.getNameComparator());
				double sumQ = 0;
				long sumCost = 0;
				for (String nameProduct : keySet){
					Object[] dataRow = rowMap.get(nameProduct);
					ResSet resProduct = connector.getProductMap().get(nameProduct);
					double q = qMap.get(nameProduct);
					int price = (int) resProduct.get("price");
					double pricePercent = (double) resProduct.get("pricePercent");
					int realPrice = (int) (price / (1 + VAT) * (1 + pricePercent));
					int thisCost = (int) (realPrice * q);
					sumQ += q;
					sumCost += thisCost;
					Object[] row = { dataRow[0], dataRow[1], q, realPrice, thisCost, price};
					HSSFRow cellRow = sheet.createRow(rowIndex);
					for (int col = 0; col < COLUMN_TITLE.length; col ++){
						CellRenderer renderer = renderers[col];
						Object value = row[col];
						renderer.render(cellRow.createCell(col), value);
					}
					rowIndex ++;
				}
				
				HSSFRow sumRow = sheet.createRow(rowIndex);
				HSSFCell textCell = sumRow.createCell(1);
				textCell.setCellStyle(factory.getWageHeaderStyle());
				textCell.setCellValue("Tổng cộng");
				rFactory.getQuantityRenderer().render(sumRow.createCell(2), sumQ);
				rFactory.getPriceRenderer().render(sumRow.createCell(4), sumCost);
				
				rowIndex += 4;
				billID ++;
			}
			for (int i = 0; i < COLUMN_TITLE.length; i ++)
				sheet.autoSizeColumn(i);
		}
		FileOutputStream fileOut = new FileOutputStream(FileDirection.getFileTaxBillName(connector.getFrom(), connector.getTo()));
		wb.write(fileOut);
		wb.close();
		fileOut.flush();
		fileOut.close();
	}
}
