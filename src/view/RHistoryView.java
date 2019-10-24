package view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import dao.BillDAO;
import dao.RSellDAO;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;
import transferObject.Bill;
import transferObject.RStatus;
import utility.Utility;

public class RHistoryView extends PostView{
	private static final String[] COLUMN_TITLE = { "Ngày hóa đơn", "Mã hóa đơn", "Số lượng", "Thành tiền", "Đơn giá bán"};
	private static final Class<?>[] COLUMN_CLASS = {Calendar.class, String.class, Double.class, Long.class, Long.class};
	private static final Integer[] COLUMN_EDITABLE = {};

	public RHistoryView(String title, String name, Object... objects) {
		super(title, name, objects);
	}

	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel);
		table.getColumnModel().getColumn(0).setCellRenderer(GUICellRendererFactory.getCalendarRenderer(GUICellStyleFactory.getCenter()));
		table.getColumnModel().getColumn(2).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(3).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightBold()));
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		String codeRSell = (String) objects[0];
		String nameProduct = (String) objects[1];
		Bill bill = BillDAO.getBill(codeRSell);
		int id = bill.getProduct(nameProduct).getID();
		ArrayList<RStatus> history = RSellDAO.getHistory(id);
		Vector<Object[]> data = new Vector<Object[]>();
		for (RStatus status : history){
			Bill sellBill = status.getBill();
			int price = sellBill.getP(nameProduct);
			long cost = (long) (price * status.getRQ());
			Object[] thisRow = { sellBill.getDay(), sellBill.getCodeBill(), status.getRQ(), cost, price};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}

}
