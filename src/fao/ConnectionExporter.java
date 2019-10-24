package fao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.text.similarity.LevenshteinDetailedDistance;
import org.apache.commons.text.similarity.LevenshteinResults;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;

import factory.ComparatorFactory;
import misc.BiMap;
import transferObject.Result;
import utility.StringUtility;
import utility.Utility;

public class ConnectionExporter {
	private static final BiMap<String, String> MANUAL_CONNECTIONS;
	static{
		MANUAL_CONNECTIONS = new BiMap<String, String>();
		MANUAL_CONNECTIONS.add("Devondale - Sua FullCream 1L - 2015", "BB KEM SỬA FULL LÍT");
		MANUAL_CONNECTIONS.add("KEO CAY CON TAU G22- KHUYNH DIEP 2016", "KEO CAY CON TAU G22- CAM THAO");
		MANUAL_CONNECTIONS.add("ST.SRM NGANNGUAMUN CHIETXUAT TRAIMO 170g", "ST IVES SRM TUOI MAT HUONG MO 170G");
		MANUAL_CONNECTIONS.add("Devondale - Sua Smart 1L - 2015", "Smart milk 1 litre");
		MANUAL_CONNECTIONS.add("Devondale - Sua bot FullCream 1KG", "SUA BOT NGUYEN KEM UONG LIEN 1K");
	}
	
	public static void main(String[] args) throws IOException{
		export();
	}
	
	private static void export() throws IOException{
		InputStream np_is = FileInstall.class.getResourceAsStream(FileDirection.NEW_PRODUCT);
		NPOIFSFileSystem np_fs = new NPOIFSFileSystem(np_is);
		HSSFWorkbook np_wb = new HSSFWorkbook(np_fs.getRoot(), true);
		Sheet np_sheet = np_wb.getSheetAt(0);
		int rowIndex = 1;
		HashMap<String, String> taxMap = new HashMap<String, String>();
		while (true){
			rowIndex ++;
			String code = StringUtility.getCellString(np_sheet.getRow(rowIndex).getCell(0));
			String name = StringUtility.getCellString(np_sheet.getRow(rowIndex).getCell(1));
			String att = StringUtility.getCellString(np_sheet.getRow(rowIndex).getCell(2));
			if (name.length() == 0) break;
			if (att.contains("Vật tư hàng hóa"))
				taxMap.put(name, code);
		}
		np_wb.close();
		np_fs.close();
		InputStream op_is = FileInstall.class.getResourceAsStream(FileDirection.OLD_PRODUCT);
		NPOIFSFileSystem op_fs = new NPOIFSFileSystem(op_is);
		HSSFWorkbook op_wb = new HSSFWorkbook(op_fs.getRoot(), true);
		Sheet op_sheet = op_wb.getSheetAt(0);
		rowIndex = 0;
		HashMap<String, String> realMap = new HashMap<String, String>();
		while (true){
			rowIndex ++;
			String code = StringUtility.getCellString(op_sheet.getRow(rowIndex).getCell(1));
			String name = StringUtility.getCellString(op_sheet.getRow(rowIndex).getCell(2));
			if (name.length() == 0) break;
			realMap.put(name, code);
		}
		op_wb.close();
		op_fs.close();
		File folder = new File(FileDirection.DIR);
		if (!folder.exists()) folder.mkdirs();
		HSSFWorkbook out_wb = new HSSFWorkbook();
		HSSFSheet out_s = out_wb.createSheet(WorkbookUtil.createSafeSheetName("Connection"));
		out_s.createRow(0).createCell(0).setCellValue("CONNECTION");
		rowIndex = 0;
		for (String tax : taxMap.keySet()){
			if (realMap.containsKey(tax) && realMap.get(tax).equals(taxMap.get(tax))) continue;
			String realRes;
			if (MANUAL_CONNECTIONS.containsL(tax)){
				realRes = MANUAL_CONNECTIONS.getR(tax);
			} else{
				LevenshteinDetailedDistance dist = new LevenshteinDetailedDistance();
				String name = StringUtility.removeAccent(tax).toLowerCase().replaceAll(" +", " ").trim().replaceAll("\\+\\d", "");
				ArrayList<Result> results = new ArrayList<Result>();
				for (String real : realMap.keySet()){
					String realName = StringUtility.removeAccent(real).toLowerCase().replaceAll(" +", " ").trim().replaceAll("\\+\\d", "");
					LevenshteinResults res = dist.apply(name, realName);
					int leven = res.getDeleteCount() + res.getInsertCount() + res.getSubstituteCount() * 2;
					double jaccard = Utility.generalizedJaccard(name.replaceAll("[^0-9]", ""), realName.replaceAll("[^0-9]", ""));
					results.add(new Result(tax, real, leven, jaccard));
				}
				results.sort(ComparatorFactory.getResultComparator());
				int num = 20;
				ArrayList<Result> filter = new ArrayList<Result>(results.subList(0, num));
				filter.sort(ComparatorFactory.getFilterComparator());
				realRes = filter.get(0).getProductName();
			}
			rowIndex ++;
			HSSFRow row = out_s.createRow(rowIndex);
			row.createCell(0).setCellValue(realMap.get(realRes));
			row.createCell(1).setCellValue(realRes);
			row.createCell(2).setCellValue(taxMap.get(tax));
			row.createCell(3).setCellValue(tax);
		}
		for (int i = 0; i < 4; i ++)
			out_s.autoSizeColumn(i);
		FileOutputStream fileOut = new FileOutputStream(FileDirection.CONNECTION_FILE);
		out_wb.write(fileOut);
		out_wb.close();
		fileOut.flush();
		fileOut.close();
	}
}
