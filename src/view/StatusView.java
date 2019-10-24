package view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import dao.CustomerDAO;
import dao.ProductDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;
import transferObject.Bill;
import transferObject.Status;
import utility.Utility;

public class StatusView extends PostView{
	private static final String[] COLUMN_TITLE = { "Ngày hóa đơn", "Mã hóa đơn", "Mã NCC", "Tên NCC", "Tồn không KM", "Tồn KM", "Thành tiền", "Đơn giá"};
	private static final Class<?>[] COLUMN_CLASS = {Calendar.class, String.class, String.class, String.class, Double.class, Double.class, Long.class, Long.class};
	private static final Integer[] COLUMN_EDITABLE = {};

	public StatusView(String title, String name, Object... objects){
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
		ArrayList<Status> statuses = ProductDAO.getProductList().get(nameProduct).getStatuses();
		Vector<Object[]> data = new Vector<Object[]>();
		for (Status status : statuses){
			Bill bill = status.getBill();
			String codeBill = bill.getCodeBill();
			String codeTarget, nameTarget;
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
					nameTarget = bill.getTarget();
					codeTarget = SupplierDAO.getSupplierList().get(nameTarget).getCode();
					break;
				case RSELL:
					codeTarget = bill.getTarget();
					nameTarget = CustomerDAO.getCustomerList().get(codeTarget).getName();
					break;
				default:
					throw new SQLException("Invalid codeBill " + codeBill);
			}
			Object[] thisRow = { bill.getDay(), codeBill, codeTarget, nameTarget, status.getQ(), status.getPQ(), status.getCost(nameProduct), status.getP(nameProduct)};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}

}
