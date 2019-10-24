package view;

import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import dao.BillDAO;
import dao.BuyDAO;
import dao.CustomerDAO;
import dao.ProductDAO;
import dao.RBuyDAO;
import dao.RSellDAO;
import dao.SellDAO;
import dao.SupplierDAO;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import factory.ComparatorFactory;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.BillType;
import misc.Pair;
import transferObject.Bill;
import transferObject.Element;
import utility.Utility;

public class DiaryView extends SearchTableView{
	private static final String TITLE = "NHẬT KÝ NHẬP/XUẤT THEO TỪNG MẶT HÀNG";
	private static final String[] COLUMN_TITLE = { 
			"<html><center>Ngày<br>hóa đơn</center></br></html>", 
			"<html><center>Mã<br>hóa đơn</center></br></html>", 
			"Mã NCC/Khách hàng", 
			"Tên NCC/Khách hàng", 
			"Tồn không KM", "Tồn KM", 
			"<html><center>Mua/Bán<br>không KM</center></br></html>", 
			"<html><center>Mua/Bán<br>KM</center></br></html>", 
			"<html><center>Đơn giá<br>mua/bán</center></br></html>", 
			"Thành tiền"};
	private static final Class<?>[] COLUMN_CLASS = {Calendar.class, String.class, String.class, String.class, Double.class, Double.class, Double.class, Double.class, Long.class, Long.class};

	public DiaryView() {
		super(TITLE);
	}

	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		table = new DynamicTable(tableModel);
		table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(), 40));
		table.setUnsorted(0);
		table.getColumnModel().getColumn(0).setCellRenderer(GUICellRendererFactory.getCalendarRenderer(GUICellStyleFactory.getCenter()));
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getSignedQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(7).setCellRenderer(GUICellRendererFactory.getSignedQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(8).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(9).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightBold()));
	}

	@Override
	public void getParameters(Object... objects) throws SQLException {
		String codeProduct = (String) objects[0];
		String nameProduct = (String) objects[1];
		
		//NEW-CONVERSE
		if (ProductDAO.isNew(codeProduct, nameProduct)){
			Pair<String, String> pair = ProductDAO.converseNew(codeProduct, nameProduct);
			codeProduct = pair.getFirst();
			nameProduct = pair.getSecond();
		}
		
		Calendar from = (Calendar) objects[2];
		Calendar to = (Calendar) objects[3];
		ArrayList<Bill> billList = new ArrayList<Bill>();
		billList.addAll(BuyDAO.getDiary(nameProduct, from, to));
		billList.addAll(SellDAO.getDiary(nameProduct, from, to));
		billList.addAll(RSellDAO.getDiary(nameProduct, from, to));
		billList.addAll(RBuyDAO.getDiary(nameProduct, from, to));
		billList.sort(ComparatorFactory.getBillComparator());
		Vector<Object[]> data = new Vector<Object[]>();
		for (Bill bill : billList){
			String codeBill = bill.getCodeBill();
			Element e = bill.getProduct(nameProduct);
			String code, name;
			BillType type = Utility.getBillType(codeBill);
			switch (type){
				case BUY:
				case RBUY:
					name = bill.getTarget();
					code = SupplierDAO.getSupplierList().get(name).getCode();
					break;
				case SELL:
				case RSELL:
					code = bill.getTarget();
					name = CustomerDAO.getCustomerList().get(code).getName();
					break;
				default:
					throw new SQLException("Invalid Bill " + bill.getCodeBill());
			}
			Pair<Double, Double> qStatus= BillDAO.getStatus(codeBill, nameProduct);
			double npG = qStatus.getFirst(), pG = qStatus.getSecond();
			Double npQ = bill.getQ(nameProduct);
			npQ = npQ == 0 ? null : npQ;
			Double pQ = bill.getPQ(nameProduct);
			pQ = pQ == 0 ? null : pQ;
			if (type == BillType.SELL || type == BillType.RBUY){
				pQ = Utility.reverseSign(pQ);
				npQ = Utility.reverseSign(npQ);
			}
			Object[] thisRow = { bill.getDay(), bill.getCodeBill(), code, name, npG, pG, npQ, pQ, bill.getP(nameProduct), e.getCost()};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}

}
