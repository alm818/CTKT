package fao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;

import dao.StaffDAO;
import factory.AttributesFactory;
import factory.FormatFactory;
import factory.HSSFCellStyleFactory;
import transferObject.Staff;
import utility.CalendarUtility;

public class FileWageExport {
	public static void export(int month, long totalRevenue) throws SQLException, IOException{
		int realMonth = month + 1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		Calendar localFrom = Calendar.getInstance();
		localFrom.set(year, month, 1);
		Calendar localTo = Calendar.getInstance();
		localTo.set(year, month, localFrom.getActualMaximum(Calendar.DATE));
		int totalDay = CalendarUtility.getTotalDay(localFrom, localTo);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFCellStyleFactory factory = new HSSFCellStyleFactory(wb);
		HSSFSheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName("Bảng in lương tháng " + realMonth));
		sheet.setMargin(Sheet.TopMargin, 0.5);
		sheet.setMargin(Sheet.BottomMargin, 0.75);
		sheet.setMargin(Sheet.LeftMargin, 0.7);
		sheet.setMargin(Sheet.RightMargin, 0.7);
		sheet.setMargin(Sheet.HeaderMargin, 0.3);
		sheet.setMargin(Sheet.FooterMargin, 0.3);
		sheet.setHorizontallyCenter(true);
		sheet.setVerticallyCenter(true);
		int rowIndex = 0;
		int cnt = 0;
		for (Staff staff : StaffDAO.getStaffList().values()){
			cnt ++;
			HSSFCell companyCell = sheet.createRow(rowIndex).createCell(0);
			companyCell.setCellStyle(factory.getCompanyStyle());
			companyCell.setCellValue("CÔNG TY TNHH XUÂN ÁNH KHÁNH HÒA");
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 8));
			rowIndex ++;
			
			HSSFCell monthCell = sheet.createRow(rowIndex).createCell(0);
			monthCell.setCellStyle(factory.getSumWageCenterStyle());
			monthCell.setCellValue(String.format("Lương tháng %02d/%d", realMonth, year));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 8));
			rowIndex += 2;
			
			HSSFCell staffCell = sheet.createRow(rowIndex).createCell(0);
			staffCell.setCellStyle(factory.getStaffStyle());
			staffCell.setCellValue("Nhân viên: " + staff.getName());
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 8));
			rowIndex ++;
			
			double workDay = totalDay - staff.getDayOff();
			HSSFCell workCell = sheet.createRow(rowIndex).createCell(0);
			workCell.setCellStyle(factory.getDayStyle());
			workCell.setCellValue("Ngày công: " + FormatFactory.formatQuantity(workDay));
			rowIndex ++;
				
			int baseWage;
			if (staff.getPosition().equals(AttributesFactory.ADMIN))
				baseWage = (int) (staff.getBaseWage() * (Math.min(workDay, 26) / 26));
			else
				baseWage = (int) (staff.getBaseWage() * (workDay / 26));
			int subWage;
			if (workDay < totalDay / 2)
				subWage = staff.getSubWage() / 2;
			else
				subWage = staff.getSubWage();
			int pob = (int) (staff.getPOB() * (Math.min(workDay, 26) / 26));
			long dif = totalRevenue - FileDirection.getData().getTargetRevenue();
			double add = dif * AttributesFactory.INTEREST_RATE * FileDirection.getData().getPercentRevenue();
			int eff = staff.getBaseEff() + (int) (staff.getBaseEff() * add / StaffDAO.getTotalEff());
			int sumWage = baseWage + subWage + pob + eff + staff.getHoliday();
			int realWage = sumWage - staff.getImprest() - staff.getInsurance();
			
			Object[][] table = new Object[][]{
				new Object[]{ "Nội dung", "Số tiền", "Ghi chú"},
				new Object[]{ "Lương cơ bản:", baseWage, ""},
				new Object[]{ "Phụ cấp:", subWage, ""},
				new Object[]{ "Lương hiệu quả:", eff, ""},
				new Object[]{ "Chi hộ:", pob, ""},
				new Object[]{ "Thưởng lễ:", staff.getHoliday(), ""},
				new Object[]{ "Tổng lương:", sumWage, ""},
				new Object[]{ "Khấu trừ tạm ứng:", staff.getImprest(), ""},
				new Object[]{ "Khấu trừ bảo hiểm:", staff.getInsurance(), ""},
				new Object[]{ "Lương thực nhận:", realWage, ""}
			};
			
			HSSFCellStyle[][] styles = new HSSFCellStyle[][]{
				new HSSFCellStyle[]{ factory.getWageHeaderStyle(), factory.getWageHeaderStyle(), factory.getWageHeaderStyle()},
				new HSSFCellStyle[]{ factory.getDayStyle(), factory.getCenterStyle(), factory.getCenterStyle()},
				new HSSFCellStyle[]{ factory.getDayStyle(), factory.getCenterStyle(), factory.getCenterStyle()},
				new HSSFCellStyle[]{ factory.getDayStyle(), factory.getCenterStyle(), factory.getCenterStyle()},
				new HSSFCellStyle[]{ factory.getDayStyle(), factory.getCenterStyle(), factory.getCenterStyle()},
				new HSSFCellStyle[]{ factory.getDayStyle(), factory.getCenterStyle(), factory.getCenterStyle()},
				new HSSFCellStyle[]{ factory.getSumWageStyle(), factory.getSumWageCenterStyle(), factory.getCenterStyle()},
				new HSSFCellStyle[]{ factory.getDayStyle(), factory.getCenterStyle(), factory.getCenterStyle()},
				new HSSFCellStyle[]{ factory.getDayStyle(), factory.getCenterStyle(), factory.getCenterStyle()},
				new HSSFCellStyle[]{ factory.getRealWageStyle(), factory.getWageHeaderStyle(), factory.getCenterStyle()}
			};
			
			int[] cols = new int[]{ 0, 4, 7};
			int[] start_cols = new int[]{ 0, 4, 7};
			int[] end_cols = new int[]{ 3, 6, 8};
			
			for (int i = 0; i < table.length; i ++){
				Object[] row = table[i];
				HSSFRow xRow = sheet.createRow(rowIndex);
				HSSFCellStyle[] style = styles[i];
				for (int j = 0; j < table[i].length; j ++){
					Object value = row[j];
					HSSFCellStyle cellStyle = style[j];
					String str = value.toString();
					if (i > 0 && j == 1){
						int val = (int) value;
						str = FormatFactory.formatPrice((long) val);
					}
					HSSFCell cell = xRow.createCell(cols[j]);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(str);
					CellRangeAddress cellRangeAddress = new CellRangeAddress(rowIndex, rowIndex, start_cols[j], end_cols[j]);
					sheet.addMergedRegion(cellRangeAddress);
					HSSFRegionUtil.setBorderTop(BorderStyle.THIN.getCode(), cellRangeAddress, sheet, wb);
					HSSFRegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), cellRangeAddress, sheet, wb);
					HSSFRegionUtil.setBorderRight(BorderStyle.THIN.getCode(), cellRangeAddress, sheet, wb);
					HSSFRegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), cellRangeAddress, sheet, wb);
				}
				rowIndex ++;
			}
			
			HSSFRow signRow = sheet.createRow(rowIndex);
			HSSFCell directorCell = signRow.createCell(2);
			directorCell.setCellStyle(factory.getCenterStyle());
			directorCell.setCellValue("Giám đốc");
			HSSFCell receivedCell = signRow.createCell(7);
			receivedCell.setCellStyle(factory.getCenterStyle());
			receivedCell.setCellValue("Kí nhận");
			rowIndex += 4;
			
			HSSFRow nameRow = sheet.createRow(rowIndex);
			HSSFCell directorNameCell = nameRow.createCell(2);
			directorNameCell.setCellStyle(factory.getCenterStyle());
			directorNameCell.setCellValue("Lê Thị Thu Sương");
			HSSFCell staffNameCell = nameRow.createCell(7);
			staffNameCell.setCellStyle(factory.getCenterStyle());
			staffNameCell.setCellValue(staff.getName());
			rowIndex += 2;
			
			HSSFRow closingRow1 = sheet.createRow(rowIndex);
			HSSFCell closingCell1 = closingRow1.createCell(0);
			closingCell1.setCellStyle(factory.getClosingStyle());
			closingCell1.setCellValue("Nếu có thắc mắc xin vui lòng liên hệ giám đốc");
			CellRangeAddress cellRangeAddress1 = new CellRangeAddress(rowIndex, rowIndex, 0, 8);
			sheet.addMergedRegion(cellRangeAddress1);
			rowIndex ++;
			
			HSSFRow closingRow2 = sheet.createRow(rowIndex);
			HSSFCell closingCell2 = closingRow2.createCell(0);
			closingCell2.setCellStyle(factory.getClosingStyle());
			closingCell2.setCellValue("Xin chân thành cám ơn sự cộng tác của các anh/chị-em");
			CellRangeAddress cellRangeAddress2 = new CellRangeAddress(rowIndex, rowIndex, 0, 8);
			sheet.addMergedRegion(cellRangeAddress2);
			if (cnt % 2 == 1) rowIndex += 4;
			else rowIndex ++;
		}
		FileOutputStream fileOut = new FileOutputStream(FileDirection.getFileWageName(realMonth, year));
		wb.write(fileOut);
		wb.close();
		fileOut.flush();
		fileOut.close();
	}
}
