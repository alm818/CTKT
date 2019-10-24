package view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import dao.ProductDAO;
import dao.RBuyDAO;
import dao.SellDAO;
import dao.SupplierDAO;
import factory.AttributesFactory;
import factory.GUICellRendererFactory;
import factory.GUICellStyleFactory;
import factory.FormatFactory;
import frame.MultipleFrame;
import gui.DynamicTable;
import gui.DynamicTableModel;
import misc.Pair;
import transferObject.Bill;
import transferObject.ResSet;
import utility.Utility;

public class ExportView extends SearchTableView{
	private static final String TITLE = "CHI TIẾT XUẤT HÀNG THEO NHÀ CUNG CẤP";
	private static final String[] COLUMN_TITLE = { "Mã mặt hàng", "Tên mặt hàng", "Bán không KM", "Bán KM", "Tổng bán", "Giá trị bán", "Tổng mua trả lại", "Tổng xuất kho"};
	private static final Class<?>[] COLUMN_CLASS = {String.class, String.class, Double.class, Double.class, Double.class, Long.class, Double.class, Double.class};
	private static final Integer[] COLUMN_EDITABLE = {};
	private Calendar from = null, to = null;

	public ExportView() {
		super(TITLE);
	}

	@Override
	protected void setTable() {
		tableModel = new DynamicTableModel(Utility.getVector(COLUMN_TITLE));
		tableModel.setClassColumn(COLUMN_CLASS);
		tableModel.setEditableColumn(COLUMN_EDITABLE);
		table = new DynamicTable(tableModel);
		table.addMouseListener(new MouseAdapter(){
		    @Override
		    public void mouseClicked(MouseEvent e) {
		    	if (e.getClickCount() == 2){
		    		int row = table.rowAtPoint(e.getPoint());
		    		int rowModel = table.convertRowIndexToModel(row);
		    		String _codeProduct = (String) tableModel.getValueAt(rowModel, 0);
		    		String _nameProduct = (String) tableModel.getValueAt(rowModel, 1);
		    		String nameProduct = _nameProduct;
					//NEW-CONVERSE
					if (ProductDAO.isNew(_codeProduct, _nameProduct)){
						Pair<String, String> pair = ProductDAO.converseNew(_codeProduct, _nameProduct);
						nameProduct = pair.getSecond();
					}
		    		String rBuyTitle = String.format("CHI TIẾT HÓA ĐƠN MUA TRẢ LẠI - %s", _nameProduct);
		    		String rBuyName = "Hóa đơn mua trả lại";
		    		RBuyBillView rBuyView = new RBuyBillView(rBuyTitle, rBuyName, nameProduct, from ,to);
		    		String sellTitle = String.format("CHI TIẾT HÓA ĐƠN BÁN - %s", _nameProduct);
		    		String sellName = "Hóa đơn bán";
		    		SellBillView sellView = new SellBillView(sellTitle, sellName, nameProduct, from, to);
		    		while (true){
		    			if (rBuyView.isDone() && sellView.isDone()){
		    				String title = String.format("%s - Từ %s đến %s", _nameProduct, FormatFactory.formatCalendar(from), FormatFactory.formatCalendar(to));
							@SuppressWarnings("unused")
							MultipleFrame frame = new MultipleFrame(title, rBuyView, sellView);
		    				break;
		    			}
		    		}
		    	}
		    }
		});
		table.getColumnModel().getColumn(2).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(3).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(4).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRightBold()));
		table.getColumnModel().getColumn(5).setCellRenderer(GUICellRendererFactory.getPriceRenderer(GUICellStyleFactory.getRightItalic()));
		table.getColumnModel().getColumn(6).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRight()));
		table.getColumnModel().getColumn(7).setCellRenderer(GUICellRendererFactory.getQuantityRenderer(GUICellStyleFactory.getRightBold16()));
	}
	
	@Override
	protected void getParameters(Object... objects) throws SQLException {
		String nameSupplier = (String) objects[0];
		from = (Calendar) objects[1];
		to = (Calendar) objects[2];
		ArrayList<Bill> rBuyList = RBuyDAO.getBills(from, to);
		ArrayList<Bill> sellList = SellDAO.getBills(from, to);
		Vector<Object[]> data = new Vector<Object[]>();
		if (nameSupplier.equals(AttributesFactory.ALL)){
			HashMap<String, ResSet> rowSet = new HashMap<String, ResSet>();
			for (Bill bill : sellList){
				for (String nameProduct : bill.getProductList()){
					if (!rowSet.containsKey(nameProduct)) rowSet.put(nameProduct, new ResSet());
					ResSet resSet = rowSet.get(nameProduct);
					double sellQ = (double) resSet.get("sellQ") + bill.getQ(nameProduct);
					double sellPQ = (double) resSet.get("sellPQ") + bill.getPQ(nameProduct);
					long cost = (long) Utility.getNumericValue(resSet.get("cost")) + bill.getProduct(nameProduct).getCost();
					resSet.put("sellQ", sellQ);
					resSet.put("sellPQ", sellPQ);
					resSet.put("cost", cost);
				}
			}
			for (Bill bill : rBuyList){
				for (String nameProduct : bill.getProductList()){
					if (!rowSet.containsKey(nameProduct)) rowSet.put(nameProduct, new ResSet());
					ResSet resSet = rowSet.get(nameProduct);
					double rbQ = (double) resSet.get("rbQ") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("rbQ", rbQ);
				}
			}
			for (String nameProduct : rowSet.keySet()){
				ResSet resSet = rowSet.get(nameProduct);
				String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
				
				//GET-NEW
				if (ProductDAO.isOld(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
					codeProduct = pair.getFirst();
					nameProduct = pair.getSecond();
				}
				
				double sellQ = (double) resSet.get("sellQ");
				double sellPQ = (double) resSet.get("sellPQ");
				double sell = sellQ + sellPQ;
				long cost = (long) Utility.getNumericValue(resSet.get("cost"));
				double rbQ = (double) resSet.get("rbQ");
				Object[] thisRow = {codeProduct, nameProduct, sellQ, sellPQ, sell, cost, rbQ, sell + rbQ};
				data.add(thisRow);
			}
		} else{
			HashSet<String> products = SupplierDAO.getSupplierList().get(nameSupplier).getProducts();
			HashMap<String, ResSet> rowSet = new HashMap<String, ResSet>();
			for (Bill bill : sellList){
				for (String nameProduct : bill.getProductList()){
					if (!products.contains(nameProduct)) continue;
					if (!rowSet.containsKey(nameProduct)) rowSet.put(nameProduct, new ResSet());
					ResSet resSet = rowSet.get(nameProduct);
					double sellQ = (double) resSet.get("sellQ") + bill.getQ(nameProduct);
					double sellPQ = (double) resSet.get("sellPQ") + bill.getPQ(nameProduct);
					long cost = (long) Utility.getNumericValue(resSet.get("cost")) + bill.getProduct(nameProduct).getCost();
					resSet.put("sellQ", sellQ);
					resSet.put("sellPQ", sellPQ);
					resSet.put("cost", cost);
				}
			}
			for (Bill bill : rBuyList){
				for (String nameProduct : bill.getProductList()){
					if (!products.contains(nameProduct)) continue;
					if (!rowSet.containsKey(nameProduct)) rowSet.put(nameProduct, new ResSet());
					ResSet resSet = rowSet.get(nameProduct);
					double rbQ = (double) resSet.get("rbQ") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("rbQ", rbQ);
				}
			}
			for (String nameProduct : rowSet.keySet()){
				ResSet resSet = rowSet.get(nameProduct);
				String codeProduct = ProductDAO.getProductList().get(nameProduct).getCode();
				
				//GET-NEW
				if (ProductDAO.isOld(codeProduct, nameProduct)){
					Pair<String, String> pair = ProductDAO.getNew(codeProduct, nameProduct);
					codeProduct = pair.getFirst();
					nameProduct = pair.getSecond();
				}
				
				double sellQ = (double) resSet.get("sellQ");
				double sellPQ = (double) resSet.get("sellPQ");
				double sell = sellQ + sellPQ;
				long cost = (long) Utility.getNumericValue(resSet.get("cost"));
				double rbQ = (double) resSet.get("rbQ");
				Object[] thisRow = {codeProduct, nameProduct, sellQ, sellPQ, sell, cost, rbQ, sell + rbQ};
				data.add(thisRow);
			}
		}
		tableModel.setData(data);
	}

}
