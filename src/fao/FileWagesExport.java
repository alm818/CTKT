package fao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;

import dao.StaffDAO;
import factory.AttributesFactory;
import factory.ComparatorFactory;
import factory.HSSFCellRendererFactory;
import factory.HSSFCellStyleFactory;
import transferObject.Staff;
import factory.HSSFCellRendererFactory.CellRenderer;
import utility.CalendarUtility;

public class FileWagesExport {
	private static final String[] COLUMN_TITLE = { 
			"Họ và tên", "Chức vụ", 
			"Mức lương\ncơ bản", "Ngày công", "Lương\ncơ bản", 
			"Phụ cấp", "Chi hộ", "Lương\nhiệu quả", "Thưởng lễ",
			"Tổng lương", "BHXH", "Tạm ứng", "Còn nhận"};
	
	public static void export(int month, long totalRevenue) throws IOException{
		int realMonth = month + 1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		Calendar localFrom = Calendar.getInstance();
		localFrom.set(year, month, 1);
		Calendar localTo = Calendar.getInstance();
		localTo.set(year, month, localFrom.getActualMaximum(Calendar.DATE));
		int totalDay = CalendarUtility.getTotalDay(localFrom, localTo);
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFCellStyleFactory factory = new HSSFCellStyleFactory(wb);
		HSSFCellRendererFactory rFactory = new HSSFCellRendererFactory(factory);
		CellRenderer[] renderers = { rFactory.getStringRenderer(factory.getLeftStyle()), rFactory.getStringRenderer(factory.getBorderedCenterStyle()), 
				rFactory.getPriceRenderer(factory.getZeroStyle()), rFactory.getQuantityRenderer(factory.getZeroStyle()), rFactory.getPriceRenderer(factory.getZeroStyle()), 
				rFactory.getPriceRenderer(factory.getZeroStyle()), rFactory.getPriceRenderer(factory.getZeroStyle()), rFactory.getPriceRenderer(factory.getZeroStyle()), 
				rFactory.getPriceRenderer(factory.getZeroStyle()), rFactory.getPriceRenderer(factory.getZeroStyle()), rFactory.getPriceRenderer(factory.getZeroStyle()), rFactory.getPriceRenderer(factory.getZeroStyle()), rFactory.getPriceRenderer(factory.getZeroStyle())};
		HSSFSheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Bảng lương tháng " + realMonth));
		HSSFCell titleCell = sheet.createRow(1).createCell(0);
		titleCell.setCellStyle(factory.getTitleStyle());
		titleCell.setCellValue("BẢNG LƯƠNG CÁN BỘ CÔNG NHÂN VIÊN");
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, COLUMN_TITLE.length - 1));
		HSSFCell subTitleCell = sheet.createRow(2).createCell(0);
		subTitleCell.setCellStyle(factory.getClosingStyle());
		subTitleCell.setCellValue(String.format("Tháng %d năm %d", realMonth, year));
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, COLUMN_TITLE.length - 1));
		
		HSSFRow headerRow = sheet.createRow(4);
		for (int i = 0; i < COLUMN_TITLE.length; i ++){
			HSSFCell cell = headerRow.createCell(i);
			cell.setCellStyle(factory.getHeaderStyle());
			cell.setCellValue(COLUMN_TITLE[i]);
		}
		
		int rowIndex = 5;
		ArrayList<Staff> staffList = new ArrayList<Staff>(StaffDAO.getStaffList().values());
		staffList.sort(ComparatorFactory.getStaffComparator());
		int sumStaffBaseWage = 0, sumBaseWage = 0, sumSubWage = 0, sumPOB = 0, 
				sumEff = 0, sumHoliday = 0,sumSumWage = 0, sumInsurance = 0, sumImprest = 0, sumRealWage = 0;
		for (Staff staff : staffList){
			sumStaffBaseWage += staff.getBaseWage();
			sumInsurance += staff.getInsurance();
			sumImprest += staff.getImprest();
			double workDay = totalDay - staff.getDayOff();
			int baseWage;
			if (staff.getPosition().equals(AttributesFactory.ADMIN))
				baseWage = (int) (staff.getBaseWage() * (Math.min(workDay, 26) / 26));
			else
				baseWage = (int) (staff.getBaseWage() * (workDay / 26));
			sumBaseWage += baseWage;
			int subWage;
			if (workDay < totalDay / 2)
				subWage = staff.getSubWage() / 2;
			else
				subWage = staff.getSubWage();
			sumSubWage += subWage;
			int pob = (int) (staff.getPOB() * (Math.min(workDay, 26) / 26));
			sumPOB += pob;
			long dif = totalRevenue - FileDirection.getData().getTargetRevenue();
			double add = dif * AttributesFactory.INTEREST_RATE * FileDirection.getData().getPercentRevenue();
			int eff = staff.getBaseEff() + (int) (staff.getBaseEff() * add / StaffDAO.getTotalEff());
			sumEff += eff;
			int sumWage = baseWage + subWage + pob + eff + staff.getHoliday();
			sumHoliday += staff.getHoliday();
			sumSumWage += sumWage;
			int realWage = sumWage - staff.getImprest() - staff.getInsurance();
			sumRealWage += realWage;
			
			Object[] row = new Object[]{
					staff.getName(), staff.getPosition(), staff.getBaseWage(), workDay,
					baseWage, subWage, pob, eff, staff.getHoliday(), sumWage, staff.getInsurance(), staff.getImprest(),
					realWage
			};
			
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
		HSSFRow sumRow = sheet.createRow(rowIndex);
		HSSFCell plusCell = sumRow.createCell(0);
		plusCell.setCellStyle(factory.getSumWageCenterStyle());
		plusCell.setCellValue("Cộng");
		CellRangeAddress cellRangeAddress = new CellRangeAddress(rowIndex, rowIndex, 0, 1);
		sheet.addMergedRegion(cellRangeAddress);
		HSSFRegionUtil.setBorderTop(BorderStyle.THIN.getCode(), cellRangeAddress, sheet, wb);
		HSSFRegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), cellRangeAddress, sheet, wb);
		HSSFRegionUtil.setBorderRight(BorderStyle.THIN.getCode(), cellRangeAddress, sheet, wb);
		HSSFRegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), cellRangeAddress, sheet, wb);
		CellRenderer priceRenderer = rFactory.getPriceRenderer(factory.getZeroStyle());
		CellRenderer stringRenderer = rFactory.getStringRenderer();
		priceRenderer.render(sumRow.createCell(2), sumStaffBaseWage);
		stringRenderer.render(sumRow.createCell(3), "");
		priceRenderer.render(sumRow.createCell(4), sumBaseWage);
		priceRenderer.render(sumRow.createCell(5), sumSubWage);
		priceRenderer.render(sumRow.createCell(6), sumPOB);
		priceRenderer.render(sumRow.createCell(7), sumEff);
		priceRenderer.render(sumRow.createCell(8), sumHoliday);
		priceRenderer.render(sumRow.createCell(9), sumSumWage);
		priceRenderer.render(sumRow.createCell(10), sumInsurance);
		priceRenderer.render(sumRow.createCell(11), sumImprest);
		priceRenderer.render(sumRow.createCell(12), sumRealWage);
		rowIndex +=2;
		
		HSSFRow signRow = sheet.createRow(rowIndex);
		HSSFCell accountCell = signRow.createCell(4);
		accountCell.setCellStyle(factory.getWageHeaderStyle());
		accountCell.setCellValue("Kế toán trưởng");
		HSSFCell dayCell = signRow.createCell(9);
		dayCell.setCellStyle(factory.getClosingStyle());
		dayCell.setCellValue(String.format("Ngày %d tháng %02d năm %d", localTo.get(Calendar.DAY_OF_MONTH), realMonth, year));
		rowIndex ++;
		
		HSSFRow dirRow = sheet.createRow(rowIndex);
		HSSFCell dirCell = dirRow.createCell(9);
		dirCell.setCellStyle(factory.getWageHeaderStyle());
		dirCell.setCellValue("Giám đốc");
		rowIndex += 4;
		
		HSSFRow nameRow = sheet.createRow(rowIndex);
		HSSFCell dirNameCell = nameRow.createCell(9);
		dirNameCell.setCellStyle(factory.getStaffStyle());
		dirNameCell.setCellValue("Lê Thị Thu Sương");
		
		FileOutputStream fileOut = new FileOutputStream(FileDirection.getFileWagesName(realMonth, year));
		wb.write(fileOut);
		wb.close();
		fileOut.flush();
		fileOut.close();
	}
}
