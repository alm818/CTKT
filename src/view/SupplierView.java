package view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import fao.FileIncomeExport;
import factory.DAOFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.GenericSwingWorker;
import transferObject.Supplier;
import utility.CalendarUtility;
import utility.Utility;

public class SupplierView extends TableView{
	private static final String TITLE = "THÔNG TIN CÁC NHÀ CUNG CẤP";
	private static final String[] COLUMN_TITLE = { "Mã nhà cung cấp", "Tên nhà cung cấp", "Cận trên % lợi nhuận", "NCC chính"};
	private static final Class<?>[] COLUMN_CLASS = {String.class, String.class, Double.class, Boolean.class};
	private static final Integer[] COLUMN_EDITABLE = {2, 3};
	
	public SupplierView() {
		super(TITLE);
	}

	@Override
	protected JPanel getSouthJP() {
		JButton saveJB = new JButton("Lưu");
		saveJB.setEnabled(false);
		saveJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Vector<Object[]> data = tableModel.getData();
				for (Object[] thisRow : data){
					String nameSupplier = (String) thisRow[1];
					Double lim = (Double) thisRow[2];
					Boolean is_main = (Boolean) thisRow[3];
					try {
						SupplierDAO.setSupplier(nameSupplier, lim, is_main);
					} catch (SQLException e) {
						LOGGER.severe(e.getMessage());
						e.printStackTrace();
					}
				}
				saveJB.setEnabled(false);
				try {
					DAOFactory.getConn().commit();
					SupplierDAO.updateSupplierList();
				} catch (SQLException e) {
					LOGGER.severe(e.getMessage());
					e.printStackTrace();
				}
			}
		});
		tableModel.addTableModelListener(new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent arg0) {
				saveJB.setEnabled(true);
			}
		});
		saveJB.setSize(80, 60);
		JPanel saveJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		saveJP.add(saveJB);
		
		JComboBox<String> yearChooser = new JComboBox<String>();
		Calendar now = Calendar.getInstance();
		for (int i = AttributesFactory.BASED_YEAR; i <= now.get(Calendar.YEAR); i ++) yearChooser.addItem("Năm " + i);
		JComboBox<String>monthChooser = new JComboBox<String>();
		yearChooser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				monthChooser.removeAllItems();
				int year = yearChooser.getSelectedIndex() + AttributesFactory.BASED_YEAR;
				if (year < now.get(Calendar.YEAR))
					for (int i = 1; i <= 12; i ++) monthChooser.addItem("Tháng " + i);
				else
					for (int i = 1; i <= now.get(Calendar.MONTH) + 1; i ++)
						monthChooser.addItem("Tháng " + i);
			}
		});
		Calendar before = Calendar.getInstance();
		int day = before.getActualMaximum(Calendar.DAY_OF_MONTH);
		before.add(Calendar.DAY_OF_MONTH, -day / 2);
		yearChooser.setSelectedItem("Năm " + before.get(Calendar.YEAR));
		monthChooser.setSelectedItem("Tháng " + (before.get(Calendar.MONTH) + 1));
		JButton exportJB = new JButton("Xuất Excel");
		exportJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String yearString = (String) yearChooser.getSelectedItem();
				String monthString = (String) monthChooser.getSelectedItem();
				int year = Integer.valueOf(yearString.substring(4));
				int month = Integer.valueOf(monthString.substring(6)) - 1;
				Calendar from = Calendar.getInstance();
				from.set(year, month, 1);
				CalendarUtility.startCal(from);
				Calendar to = Calendar.getInstance();
				to.set(year, month, from.getActualMaximum(Calendar.DATE));
				CalendarUtility.endCal(to);
				final class ExportWorker extends GenericSwingWorker{
					public ExportWorker(Container container, String title, boolean isDisposed) {
						super(container, title, isDisposed);
					}
					
					@Override
					public void process() {
						try {
							FileIncomeExport.export(from, to);
						} catch (SQLException | IOException err) {
							err.printStackTrace();
						}
					}
				}
				ExportWorker task = new ExportWorker(null, "Đang tạo tập tin...", false);
				task.execute();
			}
		});
		JPanel exportJP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		exportJP.add(yearChooser);
		exportJP.add(monthChooser);
		exportJP.add(exportJB);
		
		JPanel southJP = new JPanel(new BorderLayout(5, 5));
		southJP.add(saveJP, BorderLayout.CENTER);
		southJP.add(exportJP, BorderLayout.EAST);
		return southJP;
	}

	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel);
		table.getColumnModel().getColumn(2).setCellRenderer(GUICellRendererFactory.getPerRenderer(GUICellStyleFactory.getRight()));
		table.getTableHeader().setResizingAllowed(false);
	}
	
	@Override
	public void getParameters(Object... objects) {
		Vector<Object[]> data = new Vector<Object[]>();
		for (Supplier supplier : SupplierDAO.getSupplierList().values()){
			Object[] thisRow = { supplier.getCode(), supplier.getName(), supplier.getLim(), supplier.isMain() };
			data.add(thisRow);
		}
		tableModel.setData(data);
	}

}
