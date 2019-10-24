package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.Controller;
import dao.SellDAO;
import dao.StaffDAO;
import factory.AttributesFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import fao.FileDirection;
import fao.FileWageExport;
import fao.FileWagesExport;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.GenericSwingWorker;
import transferObject.Bill;
import transferObject.Staff;
import utility.CalendarUtility;
import utility.Utility;

public class WageView extends SearchTableView{

	private static final String TITLE = "BẢNG LƯƠNG NHÂN VIÊN";
	private static final String[] COLUMN_TITLE = { 
				"Tên nhân viên", "Chức vụ", 
				"<html><center>Mức lương<br>cơ bản</center></br></html>", 
				"Ngày nghỉ", "Ngày công",
				"<html><center>Lương<br>cơ bản</center></br></html>", 
				"Phụ cấp", "Chi hộ", 
				"<html><center>Lương<br>hiệu quả</center></br></html>",
				"Thưởng lễ", "Tổng lương", "BHXH", "Tạm ứng", "Còn nhận"}; ;
	private static final Class<?>[] COLUMN_CLASS = {String.class, String.class, Long.class, Double.class, Double.class, Long.class, Long.class, Long.class, Long.class, Long.class, Long.class, Long.class, Long.class, Long.class};
	private static final Integer[] COLUMN_EDITABLE = {3, 9, 12};
	private static final Color HIGH_LIGHT = Color.ORANGE;
	
	private Controller controller;
	private Calendar from, to;
	private long totalRevenue;
	
	public WageView() {
		super(TITLE);
	}
	
	public void setController(Controller controller){
		this.controller = controller;
	}
	
	@Override
	protected JPanel getSouthJP() {
		JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		JComboBox<String> columnChooser = new JComboBox<String>();
		JTextField filter = new JTextField();
		filter.setPreferredSize(new Dimension(500, columnChooser.getPreferredSize().height));
		JLabel columnJL = new JLabel("Tìm kiếm theo cột:");
		search.add(columnJL);
		search.add(columnChooser);
		search.add(filter);
		JPanel info = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		JLabel averageJL = new JLabel();
		JLabel countJL = new JLabel();
		JLabel sumJL = new JLabel();
		info.add(averageJL);
		info.add(countJL);
		info.add(sumJL);
		table.setFilterField(filter);
		table.setColumnChooser(columnChooser);
		table.setDisplay(sumJL, countJL, averageJL);
		
		JPanel eastJP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		JButton exportJB = new JButton("Xuất");
		exportJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				final class ExportWorker extends GenericSwingWorker{
					public ExportWorker(Container container, String title, boolean isDisposed) {
						super(container, title, isDisposed);
					}
					
					@Override
					public void process() {
						try {
							Calendar half = Calendar.getInstance();
							half.setTimeInMillis((from.getTimeInMillis() + to.getTimeInMillis()) / 2);
							FileWageExport.export(half.get(Calendar.MONTH), totalRevenue);
							FileWagesExport.export(half.get(Calendar.MONTH), totalRevenue);
						} catch (SQLException | IOException err) {
							err.printStackTrace();
						}
					}
				}
				ExportWorker task = new ExportWorker(null, "Đang tạo tập tin...", false);
				task.execute();
			}
		});
		eastJP.add(exportJB);
		
		JPanel southJP = new JPanel(new BorderLayout(5, 5));
		southJP.add(info, BorderLayout.NORTH);
		southJP.add(search, BorderLayout.CENTER);
		southJP.add(eastJP, BorderLayout.EAST);
		return southJP;
	}
	
	@SuppressWarnings("serial")
	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel){
			public void setValueAt(Object value, int row, int column){
				super.setValueAt(value, row, column);
				int thisRow = table.convertRowIndexToModel(row);
				String nameStaff = (String) tableModel.getValueAt(thisRow, 0);
				double dayOff = (double) tableModel.getValueAt(thisRow, 3);
				int holiday = Integer.valueOf(tableModel.getValueAt(thisRow, 9).toString());
				int imprest = Integer.valueOf(tableModel.getValueAt(thisRow, 12).toString());
				StaffDAO.getStaffList().get(nameStaff).setWageView(dayOff, imprest, holiday);
				controller.requestView(from, to);
			}
		};
		table.setColColor(3, HIGH_LIGHT);
		table.setColColor(9, HIGH_LIGHT);
		table.setColColor(12, HIGH_LIGHT);
		table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(), 40));
		table.getColumnModel().getColumn(1).setCellRenderer(GUICellRendererFactory.getStringRenderer(GUICellStyleFactory.getCenter()));
		table.getColumnModel().getColumn(2).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(3).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightBold()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(7).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(8).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(9).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(10).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightBold16()));
		table.getColumnModel().getColumn(11).setCellRenderer(GUICellRendererFactory.getSignedPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(12).setCellRenderer(GUICellRendererFactory.getSignedPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(13).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightBold()));
	}
	@Override
	protected void getParameters(Object... objects) throws SQLException {
		from = (Calendar) objects[0];
		to = (Calendar) objects[1];
		ArrayList<Bill> sellList = SellDAO.getBills(from, to);
		totalRevenue = 0;
		for (Bill bill : sellList){
			for (String nameProduct : bill.getProductList()){
				totalRevenue += bill.getProduct(nameProduct).getCost();
			}
		}
		int totalDay = CalendarUtility.getTotalDay(from, to);
		Vector<Object[]> data = new Vector<Object[]>();
		for (Staff staff : StaffDAO.getStaffList().values()){
			double workDay = totalDay - staff.getDayOff();
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
			
			Object[] thisRow = {
					staff.getName(), staff.getPosition(), staff.getBaseWage(), staff.getDayOff(), workDay,
					baseWage, subWage, pob, eff, staff.getHoliday(), sumWage, -staff.getInsurance(), -staff.getImprest(),
					sumWage - staff.getInsurance() - staff.getImprest()
			};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}	
}
