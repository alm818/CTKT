package fao;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import dao.BuyDAO;
import dao.CustomerDAO;
import dao.ProcessDAO;
import dao.ProductDAO;
import dao.RBuyDAO;
import dao.RSellDAO;
import dao.SellDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.DAOFactory;
import factory.FormatFactory;
import misc.NextIndex;
import misc.Pair;
import transferObject.Bill;
import transferObject.Element;
import transferObject.Supplier;
import utility.CalendarUtility;
import utility.StringUtility;
import utility.Utility;

public class FileExtract {
	private final static Logger LOGGER = Logger.getLogger(FileExtract.class.getName());
	private static HashSet<String> addingProduct, addingCustomer, addingSupplier,
		addingBuy, addingBuy_Product,
		addingSell, addingSell_Product,
		addingRSell, addingRSell_Product,
		addingRBuy, addingRBuy_Product;
	private static HashSet<Pair<String, String>> addingSupplier_Product;
	private static BuyDAO buyDAO;
	private static SellDAO sellDAO;
	private static RSellDAO rSellDAO;
	private static RBuyDAO rBuyDAO;
	
	public static void init() throws SQLException{
		buyDAO = DAOFactory.getBuyDAO();
		sellDAO = DAOFactory.getSellDAO();
		rSellDAO = DAOFactory.getRSellDAO();
		rBuyDAO = DAOFactory.getRBuyDAO();
		addingProduct = new HashSet<String>(ProductDAO.getProductList().keySet());
		addingCustomer = new HashSet<String>(CustomerDAO.getCustomerList().keySet());
		addingSupplier = new HashSet<String>(SupplierDAO.getSupplierList().keySet());
		addingBuy = new HashSet<String>(BuyDAO.getCodeBills());
		addingBuy_Product = new HashSet<String>();
		addingSell = new HashSet<String>(SellDAO.getCodeBills());
		addingSell_Product = new HashSet<String>();
		addingRSell = new HashSet<String>(RSellDAO.getCodeBills());
		addingRSell_Product = new HashSet<String>();
		addingRBuy = new HashSet<String>(RBuyDAO.getCodeBills());
		addingRBuy_Product = new HashSet<String>();
		addingSupplier_Product = new HashSet<Pair<String, String>>();
		for (Supplier supplier : SupplierDAO.getSupplierList().values())
			for (String nameProduct : supplier.getRawProducts())
				addingSupplier_Product.add(new Pair<String, String>(supplier.getName(), nameProduct));
	}
	
	private static void insertData(){
		NextIndex next = new NextIndex();
		ArrayList<Bill> billList = new ArrayList<Bill>(ProcessDAO.getBillLists().values());
		
		final class CycleThread extends Thread{
			public void run(){
				try {
					while (true){
						int index = next.value();
						if (index >= billList.size()) break;
						Bill bill = billList.get(index);
						String codeBill = bill.getCodeBill();
						switch (Utility.getBillType(codeBill)){
							case BUY:
								for (String nameProduct : bill.getProductList()){
									Element e = bill.getProduct(nameProduct);
									buyDAO.insertsProduct(codeBill, nameProduct, e.getQ(), e.getPQ(), e.getCost());
								}
								break;
							case SELL:
								for (String nameProduct : bill.getProductList()){
									Element e = bill.getProduct(nameProduct);
									sellDAO.insertsProduct(codeBill, nameProduct, e.getQ(), e.getPQ(), e.getCost());
								}
								break;
							case RSELL:
								for (String nameProduct : bill.getProductList()){
									Element e = bill.getProduct(nameProduct);
									rSellDAO.insertsProduct(codeBill, nameProduct, e.getQ(), e.getPQ(), e.getCost());
								}
								break;
							case RBUY:
								for (String nameProduct : bill.getProductList()){
									Element e = bill.getProduct(nameProduct);
									rBuyDAO.insertsProduct(codeBill, nameProduct, e.getQ(), e.getPQ(), e.getCost());
								}
								break;
							default:
								throw new SQLException("Invalid codeBill " + codeBill);
							}
					}
				} catch (SQLException e) {
					LOGGER.severe(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		ArrayList<CycleThread> threadList = new ArrayList<CycleThread>();
		for (int i = 0; i < AttributesFactory.CONCURRENT_THREAD; i ++) threadList.add(new CycleThread());
		for (CycleThread thread : threadList)
			thread.start();
		try{
			for (CycleThread thread : threadList)
				thread.join();
		} catch (InterruptedException e){
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void commit(){
		try {
			insertData();
			Utility.runThreads(buyDAO, sellDAO, rSellDAO, rBuyDAO);
		} catch (InterruptedException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void extractBuy(File file) throws IOException, SQLException{
		NPOIFSFileSystem fs = new NPOIFSFileSystem(file);
		HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
		Sheet sheet = wb.getSheetAt(0);
		
		String codeSupplier = null, nameSupplier = null, codeProduct = null, nameProduct = null;
		for (int i = sheet.getFirstRowNum(); i < sheet.getLastRowNum(); i ++){
			Row row = sheet.getRow(i);
			
			Cell bCell = row.getCell(1);
			String bCellString = StringUtility.getCellString(bCell);
			if (bCellString.contains("Mã nhà cung cấp:")){
				codeSupplier = bCellString.substring(16).trim();
			} else if (bCellString.contains("Tên nhà cung cấp:")){
				nameSupplier = bCellString.substring(17).trim();
				if (codeSupplier.length() * nameSupplier.length() != 0){
					if (Utility.isAdding(addingSupplier, nameSupplier))
						SupplierDAO.insert(codeSupplier, nameSupplier);
					else if (!codeSupplier.equals(SupplierDAO.getSupplierList().get(nameSupplier).getCode()))
						SupplierDAO.update(codeSupplier, nameSupplier);
				}
			}
			
			Cell cCell = row.getCell(2);
			String cCellString = StringUtility.getCellString(cCell);
			try{
				if (cCellString.contains("NK")){
					Calendar cal  = CalendarUtility.BuyCal(FormatFactory.parseDate(bCellString));
					AttributesFactory.updateToday(cal);
					Date day = new Date(cal.getTimeInMillis());
					String codeBuy = cCellString + StringUtility.hashDate(day);
					Cell jCell = row.getCell(9);
					String quantity = StringUtility.getCellString(jCell);
					if (quantity.length() == 0)
						continue;
					double q = Double.parseDouble(quantity);
					Cell kCell = row.getCell(10);
					String costString = StringUtility.getCellString(kCell);
					int cost = 0;
					if (costString.length() != 0)
						cost = (int) Double.parseDouble(costString);
					
					if (Utility.isAdding(addingBuy, codeBuy)){
						buyDAO.inserts(codeBuy, day, nameSupplier);
						addingBuy_Product.add(codeBuy);
					}
					if (addingBuy_Product.contains(codeBuy)){
						//For processDAO
						Bill bill = ProcessDAO.getBill(codeBuy);
						if (bill == null){
							bill = new Bill(cal, codeBuy, codeSupplier);
							ProcessDAO.addBill(bill);
						}
						if (cost != 0){
							ProductDAO.updateP(nameProduct, (int) (cost / q));
							bill.addElement(nameProduct, q, 0, cost);
						} else bill.addElement(nameProduct, 0, q, 0);
					}
				}
			} catch (NullPointerException | NumberFormatException | ParseException | IllegalFormatException e){
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
				continue;
			}
			Cell fCell = row.getCell(5);
			String fCellString = StringUtility.getCellString(fCell);
			if (fCellString.contains("Tên hàng:")){
				codeProduct = bCellString.substring(8).trim();
				nameProduct = fCellString.substring(9).trim();
				
				//PROMO-CONVERSE
				if (ProductDAO.isPromo(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.conversePromo(codeProduct, nameProduct);
					codeProduct = pair.getFirst();
					nameProduct = pair.getSecond();
				}
				
				//NEW-CONVERSE
				if (ProductDAO.isNew(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
					codeProduct = pair.getFirst();
					nameProduct = pair.getSecond();
				}
				
				if (Utility.isAdding(addingProduct, nameProduct))
					ProductDAO.insert(codeProduct, nameProduct);
				
				if (Utility.isAdding(addingSupplier_Product, new Pair<String, String>(nameSupplier, nameProduct)))
					SupplierDAO.insertProduct(nameSupplier, nameProduct);
			}
		}
		wb.close();
		fs.close();
	}
	
	public static void extractSell(File file) throws IOException, SQLException{
		NPOIFSFileSystem fs = new NPOIFSFileSystem(file);
		HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
		Sheet sheet = wb.getSheetAt(0);
		Date day = null;
		Calendar cal = null;
		String codeSell = null;
		boolean isIn = false;

		for (int i = sheet.getFirstRowNum(); i < sheet.getLastRowNum(); i ++){
			Row row = sheet.getRow(i);
			Cell bCell = row.getCell(1);
			String bCellString = StringUtility.getCellString(bCell);
			try{
				if (bCellString.contains("Ngày hóa đơn:")){
					cal  = CalendarUtility.SellCal(FormatFactory.parseDate(bCellString.substring(13).trim()));
					AttributesFactory.updateToday(cal);
					day = new Date(cal.getTimeInMillis());
					Cell dCell = row.getCell(4);
					codeSell = StringUtility.getCellString(dCell).trim() + StringUtility.hashDate(day);;
					isIn = true;
				} else if (bCellString.contains("Cộng theo hóa đơn")){
					isIn = false;
				} else if (isIn){
					Cell gCell = row.getCell(6);
					String codeProduct = StringUtility.getCellString(gCell);
					if (codeProduct.equals("Mã hàng") | codeProduct.equals("C") | codeProduct.length() == 0)
						continue;
					Cell hCell = row.getCell(7);
					String nameProduct = StringUtility.getCellString(hCell);
					
					//PROMO-CONVERSE
					if (ProductDAO.isPromo(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.conversePromo(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					//NEW-CONVERSE
					if (ProductDAO.isNew(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					if (Utility.isAdding(addingProduct, nameProduct))
						ProductDAO.insert(codeProduct, nameProduct);
					String codeCustomer = bCellString.trim();
					Cell cCell = row.getCell(2);
					String nameCustomer = StringUtility.getCellString(cCell);
					if (Utility.isAdding(addingCustomer, codeCustomer))
						CustomerDAO.insert(codeCustomer, nameCustomer);
					Cell kCell = row.getCell(10);
					String quantity = StringUtility.getCellString(kCell);
					if (quantity.length() == 0)
						continue;
					double q = Double.parseDouble(quantity);
					Cell lCell = row.getCell(11);
					String costString = StringUtility.getCellString(lCell);
					int cost = 0;
					if (costString.length() != 0)
						cost = (int) Double.parseDouble(costString);
					
					if (Utility.isAdding(addingSell, codeSell)){
						sellDAO.inserts(codeSell, day, codeCustomer);
						addingSell_Product.add(codeSell);
					}
					if (addingSell_Product.contains(codeSell)){
						//For ProcessDAO
						Bill bill = ProcessDAO.getBill(codeSell);
						if (bill == null){
							bill = new Bill(cal, codeSell, codeCustomer);
							ProcessDAO.addBill(bill);
						}
						if (cost != 0) bill.addElement(nameProduct, q, 0, cost);
						else bill.addElement(nameProduct, 0, q, 0);
					}
				}
			} catch (NullPointerException | NumberFormatException | ParseException | IllegalFormatException e){
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
				continue;
			}
		}
		wb.close();
		fs.close();
	}
	
	public static void extractRSell(File file) throws IOException, SQLException{
		NPOIFSFileSystem fs = new NPOIFSFileSystem(file);
		HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
		Sheet sheet = wb.getSheetAt(0);
		String codeCustomer = null, nameCustomer = null, codeProduct = null, nameProduct = null;
		
		for (int i = sheet.getFirstRowNum(); i < sheet.getLastRowNum(); i ++){
			Row row = sheet.getRow(i);
			Cell bCell = row.getCell(1);
			String bCellString = StringUtility.getCellString(bCell);
			Cell dCell = row.getCell(3);
			String dCellString = StringUtility.getCellString(dCell);
			try{
				if (bCellString.contains("Mã khách hàng:")){
					codeCustomer = bCellString.substring(14).trim();
					Cell gCell = row.getCell(6);
					nameCustomer = StringUtility.getCellString(gCell).substring(15).trim();
					if (Utility.isAdding(addingCustomer, codeCustomer))
						CustomerDAO.insert(codeCustomer, nameCustomer);
				} else if (bCellString.contains("Mã hàng:")){
					codeProduct = bCellString.substring(8).trim();
					Cell eCell = row.getCell(4);
					nameProduct = StringUtility.getCellString(eCell).substring(9).trim();
					
					//PROMO-CONVERSE
					if (ProductDAO.isPromo(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.conversePromo(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					//NEW-CONVERSE
					if (ProductDAO.isNew(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					if (Utility.isAdding(addingProduct, nameProduct))
						ProductDAO.insert(codeProduct, nameProduct);
				} else if (dCellString.contains("BTL")){
					Calendar cal  = CalendarUtility.RSellCal(FormatFactory.parseDate(bCellString)); 
					AttributesFactory.updateToday(cal);
					Date day = new Date(cal.getTimeInMillis());
					String codeRSell = dCellString + StringUtility.hashDate(day);
					Cell hCell = row.getCell(7);
					String quantity = StringUtility.getCellString(hCell);
					if (quantity.length() == 0)
						continue;
					double q = Double.parseDouble(quantity);
					Cell mCell = row.getCell(12);
					String costString = StringUtility.getCellString(mCell);
					int cost = 0;
					if (costString.length() != 0)
						cost = (int) Double.parseDouble(costString);
					
					if (Utility.isAdding(addingRSell, codeRSell)){
						rSellDAO.inserts(codeRSell, day, codeCustomer);
						addingRSell_Product.add(codeRSell);
					}
					if (addingRSell_Product.contains(codeRSell)){
						//For ProcessDAO
						Bill bill = ProcessDAO.getBill(codeRSell);
						if (bill == null){
							bill = new Bill(cal, codeRSell, codeCustomer);
							ProcessDAO.addBill(bill);
						}
						if (cost != 0) bill.addElement(nameProduct, q, 0, cost);
						else bill.addElement(nameProduct, 0, q, 0);
					}
				} 
			} catch (NullPointerException | NumberFormatException | ParseException | IllegalFormatException e){
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
				continue;
			}
		}
		wb.close();
		fs.close();
	}
	
	public static void extractRBuy(File file) throws IOException, SQLException{
		NPOIFSFileSystem fs = new NPOIFSFileSystem(file);
		HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
		Sheet sheet = wb.getSheetAt(0);
		String codeSupplier = null, nameSupplier = null, codeProduct = null, nameProduct = null;
		
		for (int i = sheet.getFirstRowNum(); i < sheet.getLastRowNum(); i ++){
			Row row = sheet.getRow(i);
			Cell bCell = row.getCell(1);
			String bCellString = StringUtility.getCellString(bCell);
			Cell cCell = row.getCell(2);
			String cCellString = StringUtility.getCellString(cCell);
			try{
				if (bCellString.contains("Mã nhà cung cấp:")){
					codeSupplier = bCellString.substring(16).trim();
					Cell gCell = row.getCell(6);
					nameSupplier = StringUtility.getCellString(gCell).substring(17).trim();
					if (codeSupplier.length() * nameSupplier.length() != 0){
						if (Utility.isAdding(addingSupplier, nameSupplier))
							SupplierDAO.insert(codeSupplier, nameSupplier);
						else if (!codeSupplier.equals(SupplierDAO.getSupplierList().get(nameSupplier).getCode()))
							SupplierDAO.update(codeSupplier, nameSupplier);
					}
				} else if (bCellString.contains("Mã hàng:")){
					codeProduct = bCellString.substring(8).trim();
					Cell eCell = row.getCell(4);
					nameProduct = StringUtility.getCellString(eCell).substring(9).trim();
					
					//PROMO-CONVERSE
					if (ProductDAO.isPromo(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.conversePromo(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					//NEW-CONVERSE
					if (ProductDAO.isNew(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					if (Utility.isAdding(addingProduct, nameProduct))
						ProductDAO.insert(codeProduct, nameProduct);
				} else if (cCellString.contains("MTL")){
					Calendar cal  = CalendarUtility.RBuyCal(FormatFactory.parseDate(bCellString)); 
					AttributesFactory.updateToday(cal);
					Date day = new Date(cal.getTimeInMillis());
					String codeRBuy = cCellString + StringUtility.hashDate(day);
					Cell jCell = row.getCell(9);
					String quantity = StringUtility.getCellString(jCell);
					if (quantity.length() == 0)
						continue;
					double q = Double.parseDouble(quantity);
					Cell nCell = row.getCell(13);
					String costString = StringUtility.getCellString(nCell);
					int cost = 0;
					if (costString.length() != 0)
						cost = (int) Double.parseDouble(costString);
					
					if (Utility.isAdding(addingRBuy, codeRBuy)){
						rBuyDAO.inserts(codeRBuy, day, nameSupplier);
						addingRBuy_Product.add(codeRBuy);
					}
					if (addingRBuy_Product.contains(codeRBuy)){
						//For ProcessDAO
						Bill bill = ProcessDAO.getBill(codeRBuy);
						if (bill == null){
							bill = new Bill(cal, codeRBuy, codeSupplier);
							ProcessDAO.addBill(bill);
						}
						if (cost != 0) bill.addElement(nameProduct, q, 0, cost);
						else bill.addElement(nameProduct, 0, q, 0);
					}
				}
			} catch (NullPointerException | NumberFormatException | ParseException | IllegalFormatException e){
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
				continue;
			}
		}
		wb.close();
		fs.close();
	}
	
	private static final Dimension SEARCH_RANGE = new Dimension(30, 10);
	private static final String[] NBUY_STRINGS = {
			"Ngày hạch toán", "Số chứng từ", "Mã nhà cung cấp", "Tên nhà cung cấp",
			"Mã hàng", "Tên hàng", "Số lượng mua", "Giá trị mua", "Chiết khấu",
			"Số lượng trả lại", "Giá trị trả lại"
	};
	private static final String[] NSELL_STRINGS = {
			"Ngày hạch toán", "Số chứng từ", "Mã khách hàng", "Tên khách hàng",
			"Mã hàng", "Tên hàng", "Tổng số lượng bán", "Doanh số bán", "Chiết khấu",
			"Tổng số lượng trả lại", "Giá trị trả lại"
	};
	public static void extractNBuy(File file) throws IOException, SQLException{
		NPOIFSFileSystem fs = new NPOIFSFileSystem(file);
		HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
		Sheet sheet = wb.getSheetAt(0);
		Point[] points = new Point[NBUY_STRINGS.length];
		for (int i = 0; i < SEARCH_RANGE.height; i ++){
			Row row = sheet.getRow(i);
			if (row == null) continue;
			for (int j = 0; j < SEARCH_RANGE.width; j ++){
				Cell cell = row.getCell(j);
				if (cell == null) continue;
				String str = StringUtility.getCellString(cell);
				for (int k = 0; k < NBUY_STRINGS.length; k ++)
					if (str.contains(NBUY_STRINGS[k]))
						points[k] = new Point(i, j);
			}
		}
		ArrayList<String> missing = new ArrayList<String>();
		for (int i = 0; i < NBUY_STRINGS.length; i ++){
			Point point = points[i];
			if (point == null)
				missing.add(NBUY_STRINGS[i]);
		}
		if (missing.size() > 0){
			wb.close();
			fs.close();
			throw new IOException("File thiếu các cột sau: " + String.join(", ", missing));
		} else{
			int inc = 0;
			while (true){
				try{;
					inc ++;
					int rowIndex = points[1].x + inc;
					Row row = sheet.getRow(rowIndex);
					String codeBill = StringUtility.getCellString(row.getCell(points[1].y));
					if (codeBill.length() == 0)
						break;
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(row.getCell(points[0].y).getDateCellValue().getTime());
					codeBill += StringUtility.hashDate(cal);
					String codeSupplier = StringUtility.getCellString(row.getCell(points[2].y));
					String nameSupplier = StringUtility.getCellString(row.getCell(points[3].y));
					if (codeSupplier.length() * nameSupplier.length() != 0){
						if (Utility.isAdding(addingSupplier, codeSupplier))
							SupplierDAO.insert(codeSupplier, nameSupplier);
						else if (!codeSupplier.equals(SupplierDAO.getSupplierList().get(nameSupplier).getCode()))
							SupplierDAO.update(codeSupplier, nameSupplier);
					}
					String codeProduct = StringUtility.getCellString(row.getCell(points[4].y));
					String nameProduct = StringUtility.getCellString(row.getCell(points[5].y));
					//PROMO-CONVERSE
					if (ProductDAO.isPromo(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.conversePromo(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					//NEW-CONVERSE
					if (ProductDAO.isNew(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					if (Utility.isAdding(addingProduct, nameProduct))
						ProductDAO.insert(codeProduct, nameProduct);
					
					if (Utility.isAdding(addingSupplier_Product, new Pair<String, String>(nameSupplier, nameProduct)))
						SupplierDAO.insertProduct(nameSupplier, nameProduct);
					Date day;
					double q;
					int cost, ck;
					switch (Utility.getBillType(codeBill)){
						case BUY:
							cal = CalendarUtility.BuyCal(cal);
							AttributesFactory.updateToday(cal);
							day = new Date(cal.getTimeInMillis());
							q = Double.parseDouble(StringUtility.getCellString(row.getCell(points[6].y)).replace(",", ""));
							cost = (int) Double.parseDouble(StringUtility.getCellString(row.getCell(points[7].y)).replace(",", ""));
							ck = (int) Double.parseDouble(StringUtility.getCellString(row.getCell(points[8].y)).replace(",", ""));
							cost -= ck;
							if (Utility.isAdding(addingBuy, codeBill)){
								buyDAO.inserts(codeBill, day, nameSupplier);
								addingBuy_Product.add(codeBill);
							}
							if (addingBuy_Product.contains(codeBill)){
								//For processDAO
								Bill bill = ProcessDAO.getBill(codeBill);
								if (bill == null){
									bill = new Bill(cal, codeBill, codeSupplier);
									ProcessDAO.addBill(bill);
								}
								if (cost != 0){
									ProductDAO.updateP(nameProduct, (int) (cost / q));
									bill.addElement(nameProduct, q, 0, cost);
								} else bill.addElement(nameProduct, 0, q, 0);
							}
							break;
						case RBUY:
							cal  = CalendarUtility.RBuyCal(cal);
							AttributesFactory.updateToday(cal);
							day = new Date(cal.getTimeInMillis());
							q = Double.parseDouble(StringUtility.getCellString(row.getCell(points[9].y)).replace(",", ""));
							cost = (int) Double.parseDouble(StringUtility.getCellString(row.getCell(points[10].y)).replace(",", ""));
							if (Utility.isAdding(addingRBuy, codeBill)){
								rBuyDAO.inserts(codeBill, day, nameSupplier);
								addingRBuy_Product.add(codeBill);
							}
							if (addingRBuy_Product.contains(codeBill)){
								//For ProcessDAO
								Bill bill = ProcessDAO.getBill(codeBill);
								if (bill == null){
									bill = new Bill(cal, codeBill, codeSupplier);
									ProcessDAO.addBill(bill);
								}
								if (cost != 0) bill.addElement(nameProduct, q, 0, cost);
								else bill.addElement(nameProduct, 0, q, 0);
							}
							break;
						default:
							continue;
					}
				}
				catch (NullPointerException | NumberFormatException | IllegalFormatException e){
					LOGGER.severe(e.getMessage());
					e.printStackTrace();
					continue;
				}
			}
			wb.close();
			fs.close();
		}
	}
	
	public static void extractNSell(File file) throws IOException, SQLException{
		NPOIFSFileSystem fs = new NPOIFSFileSystem(file);
		HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
		Sheet sheet = wb.getSheetAt(0);
		Point[] points = new Point[NSELL_STRINGS.length];
		for (int i = 0; i < SEARCH_RANGE.height; i ++){
			Row row = sheet.getRow(i);
			if (row == null) continue;
			for (int j = 0; j < SEARCH_RANGE.width; j ++){
				Cell cell = row.getCell(j);
				if (cell == null) continue;
				String str = StringUtility.getCellString(cell);
				for (int k = 0; k < NSELL_STRINGS.length; k ++)
					if (str.contains(NSELL_STRINGS[k]))
						points[k] = new Point(i, j);
			}
		}
		ArrayList<String> missing = new ArrayList<String>();
		for (int i = 0; i < NSELL_STRINGS.length; i ++){
			Point point = points[i];
			if (point == null)
				missing.add(NSELL_STRINGS[i]);
		}
		if (missing.size() > 0){
			wb.close();
			fs.close();
			throw new IOException("File thiếu các cột sau: " + String.join(", ", missing));
		} else{
			int inc = 0;
			while (true){
				try{
					inc ++;
					int rowIndex = points[1].x + inc;
					Row row = sheet.getRow(rowIndex);
					String codeBill = StringUtility.getCellString(row.getCell(points[1].y));
					if (codeBill.length() == 0)
						break;
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(row.getCell(points[0].y).getDateCellValue().getTime());
					codeBill += StringUtility.hashDate(cal);
					String codeCustomer = StringUtility.getCellString(row.getCell(points[2].y));
					String nameCustomer = StringUtility.getCellString(row.getCell(points[3].y));
					if (Utility.isAdding(addingCustomer, codeCustomer))
						CustomerDAO.insert(codeCustomer, nameCustomer);
					String codeProduct = StringUtility.getCellString(row.getCell(points[4].y));
					String nameProduct = StringUtility.getCellString(row.getCell(points[5].y));
					//PROMO-CONVERSE
					if (ProductDAO.isPromo(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.conversePromo(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					//NEW-CONVERSE
					if (ProductDAO.isNew(codeProduct, nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
						codeProduct = pair.getFirst();
						nameProduct = pair.getSecond();
					}
					
					if (Utility.isAdding(addingProduct, nameProduct))
						ProductDAO.insert(codeProduct, nameProduct);
	
					Date day;
					double q;
					int cost, ck;
					switch (Utility.getBillType(codeBill)){
						case SELL:
							cal = CalendarUtility.BuyCal(cal);
							AttributesFactory.updateToday(cal);
							day = new Date(cal.getTimeInMillis());
							q = Double.parseDouble(StringUtility.getCellString(row.getCell(points[6].y)).replace(",", ""));
							cost = (int) Double.parseDouble(StringUtility.getCellString(row.getCell(points[7].y)).replace(",", ""));
							ck = (int) Double.parseDouble(StringUtility.getCKString(StringUtility.getCellString(row.getCell(points[8].y)).replace(",", "")));
							cost -= ck;
							if (Utility.isAdding(addingSell, codeBill)){
								sellDAO.inserts(codeBill, day, codeCustomer);
								addingSell_Product.add(codeBill);
							}
							if (addingSell_Product.contains(codeBill)){
								//For ProcessDAO
								Bill bill = ProcessDAO.getBill(codeBill);
								if (bill == null){
									bill = new Bill(cal, codeBill, codeCustomer);
									ProcessDAO.addBill(bill);
								}
								if (cost != 0) bill.addElement(nameProduct, q, 0, cost);
								else bill.addElement(nameProduct, 0, q, 0);
							}
							break;
						case RSELL:
							cal  = CalendarUtility.RBuyCal(cal);
							AttributesFactory.updateToday(cal);
							day = new Date(cal.getTimeInMillis());
							q = Double.parseDouble(StringUtility.getCellString(row.getCell(points[9].y)).replace(",", ""));
							cost = (int) Double.parseDouble(StringUtility.getCellString(row.getCell(points[10].y)).replace(",", ""));
							if (Utility.isAdding(addingRSell, codeBill)){
								rSellDAO.inserts(codeBill, day, codeCustomer);
								addingRSell_Product.add(codeBill);
							}
							if (addingRSell_Product.contains(codeBill)){
								//For ProcessDAO
								Bill bill = ProcessDAO.getBill(codeBill);
								if (bill == null){
									bill = new Bill(cal, codeBill, codeCustomer);
									ProcessDAO.addBill(bill);
								}
								if (cost != 0) bill.addElement(nameProduct, q, 0, cost);
								else bill.addElement(nameProduct, 0, q, 0);
							}
							break;
						default:
							continue;
					}
				}
				catch (NullPointerException | NumberFormatException | IllegalFormatException e){
					LOGGER.severe(e.getMessage());
					e.printStackTrace();
					continue;
				}
			}
			wb.close();
			fs.close();
		}
	}
}
