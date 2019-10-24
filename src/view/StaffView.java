package view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dao.StaffDAO;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import frame.StaffFrame;
import gui.DynamicTable;
import gui.DynamicTableModel;
import transferObject.Staff;
import utility.Utility;

public class StaffView extends NullView{
	private static final String TITLE = "DANH SÁCH NHÂN VIÊN";
	private static final String[] COLUMN_TITLE = { "Tên nhân viên", "Chức vụ", 
				"<html><center>Mức lương<br>cơ bản</center></br></html>", 
				"Mức phụ cấp", "Mức chi hộ", "BHXH", 
				"<html><center>Mức lương<br>hiệu quả</center></br></html>"};
	private static final Class<?>[] COLUMN_CLASS = {String.class, String.class, Long.class, Long.class, Long.class, Long.class, Long.class};
	private static final Integer[] COLUMN_EDITABLE = {2, 3, 4, 5, 6};
	
	public StaffView() {
		super(TITLE);
	}

	@Override
	protected JPanel getSouthJP() {
		JButton addJB = new JButton("Thêm");
		addJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				StaffFrame frame = new StaffFrame();
				frame.addWindowListener(new WindowAdapter(){
					public void windowClosed(WindowEvent evt){
						controller.requestView();
					}
				});
			}
		});
		
		JButton deleteJB = new JButton("Xóa");
		deleteJB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRowCount() == 1){
					int row = table.convertRowIndexToModel(table.getSelectedRow());
					String nameStaff = (String) tableModel.getValueAt(row, 0);
					int confirm = JOptionPane.showConfirmDialog(null, "Có chắc chắn xóa nhân viên " + nameStaff, "Xóa nhân viên", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (confirm == JOptionPane.YES_OPTION){
						try {
							StaffDAO.remove(nameStaff);
						} catch (SQLException err) {
							err.printStackTrace();
						}
						controller.requestView();
					}
				}
			}
		});
		
		JPanel southJP = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
		southJP.add(addJB);
		southJP.add(deleteJB);
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
				Staff staff = StaffDAO.getStaffList().get(nameStaff);
				int baseWage = Integer.valueOf(tableModel.getValueAt(thisRow, 2).toString());
				int subWage = Integer.valueOf(tableModel.getValueAt(thisRow, 3).toString());
				int pob = Integer.valueOf(tableModel.getValueAt(thisRow, 4).toString());
				int insurance = Integer.valueOf(tableModel.getValueAt(thisRow, 5).toString());
				int baseEff = Integer.valueOf(tableModel.getValueAt(thisRow, 6).toString());
				staff.setStaffView(baseWage, subWage, baseEff, pob, insurance);
			}
		};
		table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(), 40));
		table.getColumnModel().getColumn(1).setCellRenderer(GUICellRendererFactory.getStringRenderer(GUICellStyleFactory.getCenter()));
		table.getColumnModel().getColumn(2).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightBold()));
		table.getColumnModel().getColumn(3).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRight()));
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		Vector<Object[]> data = new Vector<Object[]>();
		for (Staff staff : StaffDAO.getStaffList().values()){
			Object[] thisRow = {
					staff.getName(), staff.getPosition(),
					staff.getBaseWage(), staff.getSubWage(), staff.getPOB(), 
					staff.getInsurance(), staff.getBaseEff()
			};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}
}
