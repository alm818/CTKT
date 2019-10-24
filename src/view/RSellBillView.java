package view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import dao.CustomerDAO;
import dao.RSellDAO;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import factory.ComparatorFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;
import transferObject.Bill;
import transferObject.Element;
import utility.Utility;

public class RSellBillView extends PostView{
	private static final String[] COLUMN_TITLE = { "Ngày hóa đơn", "Mã hóa đơn", "Mã khách hàng", "Tên khách hàng", "BTL không KM", "BTL KM", "Thành tiền", "Đơn giá"};
	private static final Class<?>[] COLUMN_CLASS = { Calendar.class, String.class, String.class, String.class, Double.class, Double.class, Long.class, Long.class};
	private static final Integer[] COLUMN_EDITABLE = {};
	
	public RSellBillView(String title, String name, Object... objects) {
		super(title, name, objects);
	}

	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel);
		table.getColumnModel().getColumn(0).setCellRenderer(GUICellRendererFactory.getCalendarRenderer(GUICellStyleFactory.getCenter()));
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(7).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightBold()));
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		String nameProduct = (String) objects[0];
		Calendar from = (Calendar) objects[1];
		Calendar to = (Calendar) objects[2];
		ArrayList<Bill> billList = RSellDAO.getDiary(nameProduct, from, to);
		billList.sort(ComparatorFactory.getBillComparator());
		Vector<Object[]> data = new Vector<Object[]>();
		for (Bill bill : billList){
			String codeCustomer = bill.getTarget();
			String nameCustomer = CustomerDAO.getCustomerList().get(codeCustomer).getName();
			Element e = bill.getProduct(nameProduct);
			Object[] thisRow = { bill.getDay(), bill.getCodeBill(), codeCustomer, nameCustomer, e.getQ(), e.getPQ(), e.getCost(), bill.getP(nameProduct)};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}
}
