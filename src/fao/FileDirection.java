package fao;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Logger;

import factory.FormatFactory;
import transferObject.DataStorage;
import transferObject.StatStorage;

public class FileDirection {
	private static final Logger LOGGER = Logger.getLogger(FileDirection.class.getName());
	public static final String SEPARATOR = System.getProperty("file.separator");
	private static final String PROGRAM = "CTKTFinal";

	private static final String STAT_NAME = "stat.bin";
	public static final String DIR = String.join(SEPARATOR, System.getProperty("user.home"), "Documents", PROGRAM, "");
	private static final String LOG_FILE = DIR + "log.bin";
	private static final String STAT_FILE = DIR + STAT_NAME;
	public static final String CONNECTION_FILE = DIR + "connection.xls";
	
	private static final String RSC = "/rsc/";
	public static final String PROMO_FILE = RSC + "pairPromoList.inf";
	public static final String SQL_FILE = RSC + "install.sql";
	public static final String P_FILE = RSC + "p_file.xls";
	public static final String Q_FILE = RSC + "q_file.xls";
	public static final String PtoPP = RSC + "PtoPP.xls";
	public static final String CONNECTION = RSC + "connection.xls";
	public static final String NONSPLIT_PRODUCT = RSC + "nonsplit_product.xls";
	public static final String ADD_SUPPLIER = RSC + "add_supplier.xls";
	public static final String OLD_PRODUCT = RSC + "Vat_tu_hang_hoa_Mau_day_du.xls";
	public static final String NEW_PRODUCT = RSC + "Vat_tu__hang_hoa__dich_vu_khong_moi.xls";
	
	private static final String OUT = "Xuất";
	private static final String MISA = "Dữ liệu Misa";
	private static final String BUY = "Mua";
	private static final String SELL = "Bán";
	private static final String RSELL = "Bán trả lại";
	private static final String RBUY = "Mua trả lại";
	private static final String NBUY = "Mua mới";
	private static final String NSELL = "Bán mới";
	
	private static DataStorage data;
	static{
		try {
			data = (DataStorage) FileProcess.readFile(LOG_FILE);
			if (data == null) data = new DataStorage();
			LOGGER.info("Finished data storage");
		} catch (ClassNotFoundException | IOException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static DataStorage getData(){
		return data;
	}
	
	public static String getStatFileName(){
		return String.join(SEPARATOR, data.getInstallAddress(), STAT_NAME);
	}
	
	public static String getFileTaxName(Calendar from, Calendar to){
		return String.join(SEPARATOR, data.getInstallAddress(), OUT, String.format("Bảng cân đối hóa đơn từ %s đến %s.xls", FormatFactory.formatCalendarFile(from), FormatFactory.formatCalendarFile(to)));
	}
	
	public static String getFileTaxBillName(Calendar from, Calendar to){
		return String.join(SEPARATOR, data.getInstallAddress(), OUT, String.format("Hóa đơn phải xuất từ %s đến %s.xls", FormatFactory.formatCalendarFile(from), FormatFactory.formatCalendarFile(to)));
	}
	
	public static String getFileOutName(int month){
		return String.join(SEPARATOR, data.getInstallAddress(), OUT, String.format("Báo cáo lợi nhuận đơn hàng tháng %d.xls", month));
	}
	
	public static String getFileWageName(int month, int year){
		return String.join(SEPARATOR, data.getInstallAddress(), OUT, String.format("Bảng in lương tháng %02d_%d.xls", month, year));
	}
	
	public static String getFileWagesName(int month, int year){
		return String.join(SEPARATOR, data.getInstallAddress(), OUT, String.format("Bảng lương tháng %02d_%d.xls", month, year));
	}
	
	public static String getOutDir(){
		return String.join(SEPARATOR, data.getInstallAddress(), OUT);
	}
	
	public static String getBuyDir(){
		return String.join(SEPARATOR, data.getInstallAddress(), MISA, BUY, "");
	}
	
	public static String getSellDir(){
		return String.join(SEPARATOR, data.getInstallAddress(), MISA, SELL, "");
	}
	
	public static String getRSellDir(){
		return String.join(SEPARATOR, data.getInstallAddress(), MISA, RSELL, "");
	}
	
	public static String getRBuyDir(){
		return String.join(SEPARATOR, data.getInstallAddress(), MISA, RBUY, "");
	}
	
	public static String getNSellDir(){
		return String.join(SEPARATOR, data.getInstallAddress(), MISA, NSELL, "");
	}
	
	public static String getNBuyDir(){
		return String.join(SEPARATOR, data.getInstallAddress(), MISA, NBUY, "");
	}
	
	public static void makeFolders(){
		File outFolder = new File(FileDirection.getOutDir());
		outFolder.mkdirs();
		File buyFolder = new File(FileDirection.getBuyDir());
		buyFolder.mkdirs();
		File sellFolder = new File(FileDirection.getSellDir());
		sellFolder.mkdirs();
		File rSellFolder = new File(FileDirection.getRSellDir());
		rSellFolder.mkdirs();
		File rBuyFolder = new File(FileDirection.getRBuyDir());
		rBuyFolder.mkdirs();
		File nSellFolder = new File(FileDirection.getNSellDir());
		nSellFolder.mkdirs();
		File nBuyFolder = new File(FileDirection.getNBuyDir());
		nBuyFolder.mkdirs();
	}
	
	public static void write() throws IOException{
		FileProcess.writeFile(LOG_FILE, data);
		FileProcess.writeFile(STAT_FILE, new StatStorage(data.getTargetRevenue(), data.getPercentRevenue()));
	}
}
