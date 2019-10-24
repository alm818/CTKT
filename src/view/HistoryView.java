package view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import dao.BillDAO;
import dao.CustomerDAO;
import dao.RSellDAO;
import dao.SellDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.Pair;
import transferObject.Bill;
import transferObject.Status;
import utility.Utility;

public class HistoryView extends PostView{
	private static final String[] COLUMN_TITLE = { "Ngày hóa đơn", "Mã hóa đơn", "Mã NCC", "Tên NCC", "Số lượng", "Thành tiền", "Đơn giá"};
	private static final Class<?>[] COLUMN_CLASS = {Calendar.class, String.class, String.class, String.class, Double.class, Long.class, Long.class};
	private static final Integer[] COLUMN_EDITABLE = {};

	public HistoryView(String title, String name, Object...objects) {
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
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightBold()));
	}

	@Override
	protected void getParameters(Object... objects) throws SQLException {
		String codeSell = (String) objects[0];
		String nameProduct = (String) objects[1];
		Bill bill = BillDAO.getBill(codeSell);
		int sp_id = bill.getProduct(nameProduct).getID();
		ArrayList<Status> history = SellDAO.getHistory(sp_id);
		Vector<Object[]> data = new Vector<Object[]>();
		for (Status status : history){
			Bill buyBill = status.getBill();
			String codeBill = buyBill.getCodeBill();
			String codeTarget, nameTarget;
			double q = status.getQ();
			long cost = status.getCost(nameProduct);
			int price = status.getP(nameProduct);
			Calendar day = buyBill.getDay();
			switch (Utility.getBillType(codeBill)){
				case INITNP:
					codeTarget = AttributesFactory.CODE;
					nameTarget = AttributesFactory.NAME;
					break;
				case INITP:
					codeTarget = AttributesFactory.PROMO_CODE;
					nameTarget = AttributesFactory.PROMO_NAME;
					break;
				case BUY:
					nameTarget = buyBill.getTarget();
					codeTarget = SupplierDAO.getSupplierList().get(nameTarget).getCode();
					break;
				case RSELL:
					int id = buyBill.getProduct(nameProduct).getID();
					Pair<Integer, String> res = RSellDAO.getLast(id);
					codeBill = res.getSecond();
					price = res.getFirst();
					cost = (long) (price * q);
					Bill newBill = BillDAO.getBill(codeBill);
					if (newBill == null){
						day = AttributesFactory.CAL2016;
						codeTarget = AttributesFactory.CODE;
						nameTarget = AttributesFactory.NAME;
					} else{
						day = newBill.getDay();
						switch (Utility.getBillType(codeBill)){
							case BUY:
								nameTarget = newBill.getTarget();
								codeTarget = SupplierDAO.getSupplierList().get(nameTarget).getCode();
								break;
							case RSELL:
								codeTarget = newBill.getTarget();
								nameTarget = CustomerDAO.getCustomerList().get(codeTarget).getName();
								break;
							default:
								throw new SQLException("Invalid codeBill " + codeBill);
						}
					}
					break;
				default:
					throw new SQLException("Invalid codeBill " + codeBill);
			}
			Object[] thisRow = { day, codeBill, codeTarget, nameTarget, q, cost, price};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}

}
