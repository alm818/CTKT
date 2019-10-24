package transferObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import misc.FileType;
import utility.StringUtility;

public class FileClass {
	private static final HashMap<String, FileType> TITLE_LIST;
	static{
		TITLE_LIST = new HashMap<String, FileType>();
		TITLE_LIST.put("SỔ CHI TIẾT MUA HÀNG THEO NHÀ CUNG CẤP VÀ MẶT HÀNG", FileType.BUY);
		TITLE_LIST.put("THỐNG KÊ BÁN HÀNG KHÔNG THEO ĐƠN ĐẶT HÀNG", FileType.SELL);
		TITLE_LIST.put("BÁO CÁO CHI TIẾT TÌNH HÌNH HÀNG BÁN TRẢ LẠI THEO KHÁCH HÀNG VÀ MẶT HÀNG", FileType.RSELL);
		TITLE_LIST.put("BÁO CÁO CHI TIẾT HÀNG MUA TRẢ LẠI THEO NHÀ CUNG CẤP VÀ MẶT HÀNG", FileType.RBUY);
		TITLE_LIST.put("SỔ CHI TIẾT MUA HÀNG", FileType.NBUY);
		TITLE_LIST.put("SỔ CHI TIẾT BÁN HÀNG", FileType.NSELL);
	}
	private static final int SIZE = 20;
	
	private String name;
	private FileType type;
	
	public FileClass(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			name = file.getName();
			
			type = null;
			NPOIFSFileSystem fs = new NPOIFSFileSystem(file);
			HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true);
			Sheet sheet = wb.getSheetAt(0);
			loop: for (int i = 0; i < SIZE; i ++){
				Row row = sheet.getRow(i);
				if (row == null) continue;
				for (int j = 0; j < SIZE; j ++){
					Cell cell = row.getCell(j);
					if (cell == null) continue;
					String str = StringUtility.getCellString(cell);
					if (TITLE_LIST.containsKey(str)){
						type = TITLE_LIST.get(str);
						break loop;
					}
				}
			}
			wb.close();
			fs.close();
			fis.close();
		}
		catch (Throwable e) {
			type = null;
		}
	}
	
	public String getName(){
		return name;
	}
	
	public FileType getType(){
		return type;
	}
}
