package fao;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDetailedDistance;
import org.apache.commons.text.similarity.LevenshteinResults;
import org.apache.commons.text.similarity.LongestCommonSubsequence;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import dao.BillDAO;
import dao.BuyDAO;
import dao.CustomerDAO;
import dao.ProductDAO;
import dao.RBuyDAO;
import dao.RSellDAO;
import dao.SellDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.ComparatorFactory;
import factory.FormatFactory;
import misc.BiMap;
import misc.Pair;
import transferObject.Bill;
import transferObject.Product;
import transferObject.ResSet;
import transferObject.Result;
import transferObject.TaxConnector;
import utility.CalendarUtility;
import utility.StringUtility;
import utility.Utility;

public class FileTax {
	private static TaxConnector connector;
	private static final int SEED = 20;
	
	public static TaxConnector getConnector(){
		return connector;
	}
	
	public static void extract(File file) throws IOException, ParseException, SQLException{
		NPOIFSFileSystem fs = new NPOIFSFileSystem(file);
		HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
		Sheet sheet = wb.getSheetAt(0);
		String title = StringUtility.getCellString(sheet.getRow(0).getCell(0));
		if (!title.equals("TỔNG HỢP TỒN KHO")){
			wb.close();
			fs.close();
			throw new IOException("Tập tin không đúng dạng: " + title);
		} else{
			// GET FROM AND TO
			String time = StringUtility.getCellString(sheet.getRow(1).getCell(0)).toLowerCase();
			Calendar from = Calendar.getInstance();
			Calendar to = Calendar.getInstance();
			if (time.contains(AttributesFactory.MONTH) && time.contains(AttributesFactory.YEAR)){
				int yearIdx = time.indexOf(AttributesFactory.YEAR);
				int monthIdx = time.indexOf(AttributesFactory.MONTH);
				int year = Integer.valueOf(time.substring(yearIdx + AttributesFactory.YEAR.length()).trim());
				int month = Integer.valueOf(time.substring(monthIdx + AttributesFactory.MONTH.length(), yearIdx).trim()) - 1;
				from.set(year, month, 1);
				to.set(year, month, from.getActualMaximum(Calendar.DATE));
			} else if (time.contains(AttributesFactory.FROM_DAY) && time.contains(AttributesFactory.TO_DAY)){
				int first = time.indexOf(AttributesFactory.FROM_DAY);
				int second = time.indexOf(AttributesFactory.TO_DAY);
				from = FormatFactory.parseDate(time.substring(first + AttributesFactory.FROM_DAY.length(), second).trim());
				to = FormatFactory.parseDate(time.substring(second + AttributesFactory.TO_DAY.length()).trim());
			} else{
				wb.close();
				fs.close();
				throw new IOException("Tập tin không đúng mẫu ngày: " + time);
			}
			CalendarUtility.startCal(from);
			CalendarUtility.endCal(to);
			
			// GET INFO ABOUT EACH NAME TAX FROM THE FILE
			int rowIndex = 4;
			HashMap<String, ResSet> taxMap = new HashMap<String, ResSet>();
			while (true){
				rowIndex ++;
				Row row = sheet.getRow(rowIndex);
				String codeTax = StringUtility.getCellString(row.getCell(1));
				String nameTax = StringUtility.getCellString(row.getCell(2));
				if (nameTax.length() == 0) break;
				double startQTax = row.getCell(4).getNumericCellValue();
				double buyQTax = row.getCell(6).getNumericCellValue();
				double sellQTax = row.getCell(8).getNumericCellValue();
				ResSet resSet = new ResSet();
				resSet.put("code", codeTax);
				resSet.put("name", nameTax);
				resSet.put("buyQ", buyQTax);
				resSet.put("sellQ", sellQTax);
				resSet.put("startQ", startQTax);
				taxMap.put(nameTax, resSet);
			}
			wb.close();
			fs.close();
			
			// GET INFO ABOUT EACH NAME PRODUCT FROM DB
			// USING OLD_NAME AS KEY
			ArrayList<Bill> buyList = BuyDAO.getBills(from, to);
			ArrayList<Bill> sellList = SellDAO.getBills(from, to);
			ArrayList<Bill> rSellList = RSellDAO.getBills(from, to);
			ArrayList<Bill> rBuyList = RBuyDAO.getBills(from, to);
			HashMap<String, ResSet> productMap = new HashMap<String, ResSet>();
			for (Product product : ProductDAO.getProductList().values()){
				String nameProduct = product.getName();
				if (SupplierDAO.getSupplier(nameProduct) == null) continue;
				String codeProduct = product.getCode();
				String _codeProduct = codeProduct, _nameProduct = nameProduct;
				
				//GET-NEW
				if (ProductDAO.isOld(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
					_codeProduct = pair.getFirst();
					_nameProduct = pair.getSecond();
				}
				
				ResSet resSet = new ResSet();
				resSet.put("name", nameProduct);
				resSet.put("code", codeProduct);
				resSet.put("newName", _nameProduct);
				resSet.put("newCode", _codeProduct);
				resSet.put("buyQ", 0.0);
				resSet.put("sellQ", 0.0);
				resSet.put("day", Calendar.getInstance());
				resSet.put("buyDay", AttributesFactory.CAL2016);
				productMap.put(nameProduct, resSet);
			}
			for (Bill bill : buyList){
				for (String nameProduct : bill.getProductList()){
					if (!productMap.containsKey(nameProduct)) continue;
					ResSet resSet = productMap.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
					Calendar buyDay = (Calendar) resSet.get("buyDay");
					int price = bill.getP(nameProduct);
					if (price > 0)
						if (buyDay.before(bill.getDay())){
							resSet.put("buyDay", bill.getDay());
							resSet.put("price", price);
						}
					double buy = (double) resSet.get("buyQ") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("buyQ", buy);
				}
			}
			connector = new TaxConnector(taxMap, productMap, from, to);
			for (Bill bill : sellList){
				for (String nameProduct : bill.getProductList()){
					if (!productMap.containsKey(nameProduct)) continue;
					int groupID = SupplierDAO.getSupplier(nameProduct).getGroupID();
					connector.addCustomer(groupID, CustomerDAO.getCustomerList().get(bill.getTarget()).getName());
					ResSet resSet = productMap.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
					double sell = (double) resSet.get("sellQ") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("sellQ", sell);
				}
			}
			for (Bill bill : rSellList){
				for (String nameProduct : bill.getProductList()){
					if (!productMap.containsKey(nameProduct)) continue;
					int groupID = SupplierDAO.getSupplier(nameProduct).getGroupID();
					connector.addCustomer(groupID, CustomerDAO.getCustomerList().get(bill.getTarget()).getName());
					ResSet resSet = productMap.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
				}
			}
			for (Bill bill : rBuyList){
				for (String nameProduct : bill.getProductList()){
					if (!productMap.containsKey(nameProduct)) continue;
					ResSet resSet = productMap.get(nameProduct);
					Calendar day = (Calendar) resSet.get("day");
					if (day.after(bill.getDay())){
						resSet.put("day", bill.getDay());
						resSet.put("codeBill", bill.getCodeBill());
					}
				}
			}
			for (ResSet resSet : productMap.values()){
				if (resSet.contains("codeBill")){
					String nameProduct = (String) resSet.get("name");
					String codeBill = (String) resSet.get("codeBill");
					Pair<Double, Double> start = BillDAO.getStatus(codeBill, nameProduct);
					resSet.put("startQ", start.getFirst() + start.getSecond());
				} else resSet.put("startQ", 0.0);
			}
			
			//MAKE CONNECTIONS USING NEW_NAME TO CONNECT
			HashSet<String> taxSet = new HashSet<String>(taxMap.keySet());
			HashSet<String> productSet = new HashSet<String>(productMap.keySet());
			BiMap<String, String> taxConnection = ProductDAO.getTaxConnection();
			for (Entry<String, String> entry : taxConnection.getLeftEntry()){
				String productName = entry.getKey();
				String taxName = entry.getValue();
				if (taxSet.contains(taxName)){
					connector.addTrue(productName, taxName);
					taxSet.remove(taxName);
					productSet.remove(productName);
				}
			}
			LevenshteinDetailedDistance levenDist = new LevenshteinDetailedDistance();
			LongestCommonSubsequence lcsDist = new LongestCommonSubsequence();
			JaroWinklerDistance jaroDist = new JaroWinklerDistance();
			HashSet<String> falseSet = new HashSet<String>();
			for (String taxName : taxSet){
				String stripedTaxName = StringUtility.removeAccent(taxName).toLowerCase().replaceAll(" +", " ").trim();
				ArrayList<Result> results = new ArrayList<Result>();
				for (String productName : productSet){
					String newName = (String) productMap.get(productName).get("newName");
					String stripedProductName = StringUtility.removeAccent(newName).toLowerCase().replaceAll(" +", " ").trim();
					LevenshteinResults res = levenDist.apply(stripedTaxName, stripedProductName);
					int leven = res.getDeleteCount() + res.getInsertCount() + res.getSubstituteCount() * 2;
					double jaccard = Utility.generalizedJaccard(stripedTaxName.replaceAll("[^0-9]", ""), stripedProductName.replaceAll("[^0-9]", ""));
					int lcs =lcsDist.apply(stripedProductName.replaceAll(" ", ""), stripedTaxName.replaceAll(" ",""));
					double jaro = jaroDist.apply(stripedProductName, stripedTaxName);
					results.add(new Result(taxName, productName, leven, jaccard, lcs, jaro));
				}
				results.sort(ComparatorFactory.getResultComparator());
				ArrayList<Result> filter = new ArrayList<Result>(results.subList(0, Math.min(SEED, results.size())));
				filter.sort(ComparatorFactory.getExtendedFilterComparator());
				Result best = filter.get(0);
				String productName = best.getProductName();
				double buyQ = (double) productMap.get(productName).get("buyQ");
				double buyTaxQ = (double) taxMap.get(taxName).get("buyQ");
				if (best.getLeven() == 0 || (buyQ == buyTaxQ && buyQ > 0)){
					connector.addTrue(productName, taxName);
					productSet.remove(productName);
				} else
					falseSet.add(taxName);
			}
			for (String taxName : falseSet){
				String stripedTaxName = StringUtility.removeAccent(taxName).toLowerCase().replaceAll(" +", " ").trim();
				ArrayList<Result> results = new ArrayList<Result>();
				for (String productName : productSet){
					String newName = (String) productMap.get(productName).get("newName");
					String stripedProductName = StringUtility.removeAccent(newName).toLowerCase().replaceAll(" +", " ").trim();
					LevenshteinResults res = levenDist.apply(stripedTaxName, stripedProductName);
					int leven = res.getDeleteCount() + res.getInsertCount() + res.getSubstituteCount() * 2;
					double jaccard = Utility.generalizedJaccard(stripedTaxName.replaceAll("[^0-9]", ""), stripedProductName.replaceAll("[^0-9]", ""));
					int lcs =lcsDist.apply(stripedProductName.replaceAll(" ", ""), stripedTaxName.replaceAll(" ",""));
					double jaro = jaroDist.apply(stripedProductName, stripedTaxName);
					results.add(new Result(taxName, productName, leven, jaccard, lcs, jaro));
				}
				results.sort(ComparatorFactory.getResultComparator());
				ArrayList<Result> filter = new ArrayList<Result>(results.subList(0, Math.min(SEED, results.size())));
				filter.sort(ComparatorFactory.getExtendedFilterComparator());
				Result best = filter.get(0);
				String productName = best.getProductName();
				connector.addFalse(productName, taxName);
			}
		}
	}
}
