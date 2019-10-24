package fao;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import dao.ProductDAO;
import dao.SupplierDAO;
import factory.DAOFactory;
import misc.BiMap;
import misc.Pair;
import transferObject.Element;
import utility.StringUtility;
import utility.Utility;

public class FileInstall{
	@SuppressWarnings("unused")
	private final static Logger LOGGER = Logger.getLogger(FileInstall.class.getName());
	private static BiMap<Pair<String, String>, Pair<String, String>> ppMap = new BiMap<Pair<String, String>, Pair<String, String>>();
	private static HashMap<Pair<String, String>, Element> products = new HashMap<Pair<String, String>, Element>();
	private static HashMap<Pair<String, String>, String> codeBills = new HashMap<Pair<String, String>, String>();
	
	public static void execute() throws IOException, URISyntaxException, SQLException{
		initPtoPP();
		nonsplitProduct();
		initQ();
		initP();
		addSupplier();
		addConnection();
		for (Pair<String, String> pair : products.keySet()){
			Element e = products.get(pair);
			ProductDAO.insert(pair.getFirst(), pair.getSecond());
			String codeBill = codeBills.get(pair);
			if (e.getCost() != 0){
				int p = (int) (e.getCost() / e.getQ());
				ProductDAO.updateP(pair.getSecond(), p);
				ProductDAO.updateLast(pair.getSecond(), p, codeBill);
				ProductDAO.updateInitQ(pair.getSecond(), e.getQ());
			} else ProductDAO.updateInitQ(pair.getSecond(), e.getQ());
		}
	}
	
	private static void initPtoPP() throws IOException, SQLException, URISyntaxException{
		InputStream pp_is = FileInstall.class.getResourceAsStream(FileDirection.PtoPP);
		NPOIFSFileSystem pp_fs = new NPOIFSFileSystem(pp_is);
		HSSFWorkbook pp_wb = new HSSFWorkbook(pp_fs.getRoot(), true);
		Sheet pp_sheet = pp_wb.getSheetAt(0);
		
		String insert = "INSERT INTO APP.PtoPP VALUES (?, ?, ?, ?)";
		PreparedStatement stm = DAOFactory.getConn().prepareStatement(insert);
		
		for (int i = pp_sheet.getFirstRowNum(); i <= pp_sheet.getLastRowNum(); i ++){
			Row row = pp_sheet.getRow(i);
			Cell aCell = row.getCell(0);
			Cell bCell = row.getCell(1);
			Cell cCell = row.getCell(2);
			Cell dCell = row.getCell(3);
			String code = StringUtility.getCellString(aCell);
			String name = StringUtility.getCellString(bCell);
			String promoCode = StringUtility.getCellString(cCell);
			String promoName = StringUtility.getCellString(dCell);
			if (code.length() * name.length() * promoCode.length() * promoName.length() == 0) continue;
			stm.setString(1, code);
			stm.setString(2, name);
			stm.setString(3, promoCode);
			stm.setString(4, promoName);
			ppMap.add(new Pair<String, String>(code, name), new Pair<String, String>(promoCode, promoName));
			stm.addBatch();
		}
		stm.executeBatch();
		stm.close();
		pp_wb.close();
		pp_fs.close();
	}
	
	private static void nonsplitProduct() throws IOException, SQLException, URISyntaxException{
		InputStream sp_is = FileInstall.class.getResourceAsStream(FileDirection.NONSPLIT_PRODUCT);
		NPOIFSFileSystem sp_fs = new NPOIFSFileSystem(sp_is);
		HSSFWorkbook sp_wb = new HSSFWorkbook(sp_fs.getRoot(), true);
		Sheet sp_sheet = sp_wb.getSheetAt(0);
		
		String insert = "INSERT INTO APP.NonSplitProduct VALUES (?, ?)";
		PreparedStatement stm = DAOFactory.getConn().prepareStatement(insert);
		
		for (int i = sp_sheet.getFirstRowNum(); i < sp_sheet.getLastRowNum(); i ++){
			Row row = sp_sheet.getRow(i);
			Cell aCell = row.getCell(0);
			Cell bCell = row.getCell(1);
			String code = StringUtility.getCellString(aCell);
			String name = StringUtility.getCellString(bCell);
			if (code.length() * name.length() == 0) continue;
			stm.setString(1, code);
			stm.setString(2, name);
			stm.addBatch();
		}
		stm.executeBatch();
		stm.close();
		sp_wb.close();
		sp_fs.close();
	}
	
	private static void initQ() throws IOException, URISyntaxException, SQLException{
		InputStream q_is = FileInstall.class.getResourceAsStream(FileDirection.Q_FILE);
		NPOIFSFileSystem q_fs = new NPOIFSFileSystem(q_is);
		HSSFWorkbook q_wb = new HSSFWorkbook(q_fs.getRoot(), true);
		Sheet q_sheet = q_wb.getSheetAt(0);
		
		for (int i = q_sheet.getFirstRowNum(); i < q_sheet.getLastRowNum(); i ++){
			Row row = q_sheet.getRow(i);
			Cell bCell = row.getCell(1);
			Cell cCell = row.getCell(2);
			Cell dCell = row.getCell(3);
			Cell gCell = row.getCell(6);
			try{
				@SuppressWarnings("unused") // USE AS A FILTER
				Double stt = Double.parseDouble(bCell.toString());
				String codeProduct = StringUtility.getCellString(cCell);
				String nameProduct = StringUtility.getCellString(dCell);
				String quantity = StringUtility.getCellString(gCell);
				Double q = null;
				if (quantity.length() == 0)
					continue;
				else if (quantity.contains("+"))
					q = StringUtility.getSum(quantity);
				else q = Double.parseDouble(gCell.toString());
				
				//PROMO-CONVERSE
				if (ppMap.containsR(new Pair<String, String>(codeProduct, nameProduct))){
					Pair<String, String> pair = ppMap.getL(new Pair<String, String>(codeProduct, nameProduct));
					codeProduct = pair.getFirst();
					nameProduct = pair.getSecond();
				}
				Pair<String, String> pair = new Pair<String, String>(codeProduct, nameProduct);
				if (!products.containsKey(pair)) products.put(pair, new Element());
				products.put(pair, Element.add(products.get(pair), new Element(q, 0, 0)));
			}
			catch (NullPointerException e){
				continue;
			}
			catch (NumberFormatException e){
				continue;
			}
		}
		q_wb.close();
		q_fs.close();
	}
	
	private static void initP() throws IOException, URISyntaxException, SQLException{
		InputStream p_is = FileInstall.class.getResourceAsStream(FileDirection.P_FILE);
		NPOIFSFileSystem p_fs = new NPOIFSFileSystem(p_is);
		HSSFWorkbook p_wb = new HSSFWorkbook(p_fs.getRoot(), true);
		Sheet p_sheet = p_wb.getSheetAt(0);
		String nameProduct = null, codeProduct = null, codeSupplier = null, nameSupplier = null;
		HashSet<String> addingProduct = new HashSet<String>();
		HashSet<String> addingSupplier = new HashSet<String>();
		HashSet<Pair<String, String>> addingSupplier_Product = new HashSet<Pair<String, String>>();
		
		for (int i = p_sheet.getFirstRowNum(); i < p_sheet.getLastRowNum(); i ++){
			Row row = p_sheet.getRow(i);
			Cell bCell = row.getCell(1);
			String bCellString = StringUtility.getCellString(bCell);
			Cell cCell = row.getCell(2);
			String cCellString = StringUtility.getCellString(cCell);
			Cell fCell = row.getCell(5);
			String fCellString = StringUtility.getCellString(fCell);
			Cell iCell = row.getCell(8);
			
			if (bCellString.contains("Mã nhà cung cấp:"))
				codeSupplier = bCellString.substring(16).trim();
			else if (bCellString.contains("Tên nhà cung cấp:"))
				nameSupplier = bCellString.substring(17).trim();
			
			if (cCellString.contains("NK")){
				String price = StringUtility.getCellString(iCell);
				String codeBill = cCellString;
				if (price.length() == 0)
					continue;
				int p = (int) Double.parseDouble(price);
				//PROMO-CONVERSE
				if (ppMap.containsR(new Pair<String, String>(codeProduct, nameProduct))){
					Pair<String, String> pair = ppMap.getL(new Pair<String, String>(codeProduct, nameProduct));
					codeProduct = pair.getFirst();
					nameProduct = pair.getSecond();
				}
				Pair<String, String> pair = new Pair<String, String>(codeProduct, nameProduct);
				if (products.containsKey(pair)){
					if (Utility.isAdding(addingProduct, nameProduct))
						ProductDAO.insert(codeProduct, nameProduct);
					if (Utility.isAdding(addingSupplier, nameSupplier))
						SupplierDAO.insert(codeSupplier, nameSupplier);
					if (Utility.isAdding(addingSupplier_Product, new Pair<String, String>(nameSupplier, nameProduct)))
						SupplierDAO.insertProduct(nameSupplier, nameProduct);
					Element e = products.get(pair);
					int cost = (int) (e.getQ() * p);
					products.put(pair, new Element(e.getQ(), 0, cost));
					codeBills.put(pair, codeBill);
				}
			}
			if (fCellString.contains("Tên hàng:")){
				codeProduct = bCellString.substring(9).trim();
				nameProduct = fCellString.substring(10).trim();
			}
		}
		p_wb.close();
		p_fs.close();
	}
	
	private static void addSupplier() throws IOException, URISyntaxException, SQLException{
		InputStream s_is = FileInstall.class.getResourceAsStream(FileDirection.ADD_SUPPLIER);
		NPOIFSFileSystem s_fs = new NPOIFSFileSystem(s_is);
		HSSFWorkbook s_wb = new HSSFWorkbook(s_fs.getRoot(), true);
		Sheet s_sheet = s_wb.getSheetAt(0);
		int first = s_sheet.getFirstRowNum();
		Row headerRow = s_sheet.getRow(first);
		String codeSupplier = StringUtility.getCellString(headerRow.getCell(0));
		String nameSupplier = StringUtility.getCellString(headerRow.getCell(1));
		SupplierDAO.insert(codeSupplier, nameSupplier);
		for (int i = first + 1; i < s_sheet.getLastRowNum(); i ++){
			Row row = s_sheet.getRow(i);
			try{
				String codeProduct = StringUtility.getCellString(row.getCell(0));
				String nameProduct = StringUtility.getCellString(row.getCell(1));
				ProductDAO.insert(codeProduct, nameProduct);
				SupplierDAO.insertProduct(nameSupplier, nameProduct);
			}
			catch (NullPointerException e){
				break;
			}
		}
		s_wb.close();
		s_fs.close();
	}
	
	private static void addConnection() throws IOException, SQLException{
		InputStream c_is = FileInstall.class.getResourceAsStream(FileDirection.CONNECTION);
		NPOIFSFileSystem c_fs = new NPOIFSFileSystem(c_is);
		HSSFWorkbook c_wb = new HSSFWorkbook(c_fs.getRoot(), true);
		Sheet c_sheet = c_wb.getSheetAt(0);
		String insert = "INSERT INTO APP.Conn VALUES (?, ?, ?, ?)";
		PreparedStatement stm = DAOFactory.getConn().prepareStatement(insert);
		
		for (int i = 1; i <= c_sheet.getLastRowNum(); i ++){
			Row row = c_sheet.getRow(i);
			Cell aCell = row.getCell(0);
			Cell bCell = row.getCell(1);
			Cell cCell = row.getCell(2);
			Cell dCell = row.getCell(3);
			String code = StringUtility.getCellString(aCell);
			String name = StringUtility.getCellString(bCell);
			String newCode = StringUtility.getCellString(cCell);
			String newName = StringUtility.getCellString(dCell);
			if (code.length() * name.length() * newCode.length() * newName.length() == 0) continue;
			stm.setString(1, code);
			stm.setString(2, name);
			stm.setString(3, newCode);
			stm.setString(4, newName);
			stm.addBatch();
		}
		stm.executeBatch();
		stm.close();
		c_wb.close();
		c_fs.close();
	}
}
