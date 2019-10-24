package view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import dao.BuyDAO;
import dao.ProductDAO;
import dao.RSellDAO;
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

public class ImportView extends SearchTableView{
	private static final String TITLE = "CHI TIẾT NHẬP HÀNG THEO NHÀ CUNG CẤP";
	private static final String[] COLUMN_TITLE = { "Mã mặt hàng", "Tên mặt hàng", "Mua không KM", "Mua KM", "Tổng mua", "Giá trị mua", "Tổng bán trả lại", "Tổng nhập kho"};
	private static final Class<?>[] COLUMN_CLASS = {String.class, String.class, Double.class, Double.class, Double.class, Long.class, Double.class, Double.class};
	private static final Integer[] COLUMN_EDITABLE = {};
	private Calendar from = null, to = null;

	public ImportView() {
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
		    		String rSellTitle = String.format("CHI TIẾT HÓA ĐƠN BÁN TRẢ LẠI - %s", _nameProduct);
		    		String rSellName = "Hóa đơn bán trả lại";
		    		RSellBillView rSellView = new RSellBillView(rSellTitle, rSellName, nameProduct, from ,to);
		    		String buyTitle = String.format("CHI TIẾT HÓA ĐƠN MUA - %s", _nameProduct);
		    		String buyName = "Hóa đơn mua";
		    		BuyBillView buyView = new BuyBillView(buyTitle, buyName, nameProduct, from, to);
		    		while (true){
		    			if (rSellView.isDone() && buyView.isDone()){
		    				String title = String.format("%s - Từ %s đến %s", _nameProduct, FormatFactory.formatCalendar(from), FormatFactory.formatCalendar(to));
							@SuppressWarnings("unused")
							MultipleFrame frame = new MultipleFrame(title, rSellView, buyView);
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
		ArrayList<Bill> buyList = BuyDAO.getBills(from, to);
		ArrayList<Bill> rSellList = RSellDAO.getBills(from, to);
		Vector<Object[]> data = new Vector<Object[]>();
		HashMap<String, ResSet> rowSet = new HashMap<String, ResSet>();
		if (nameSupplier.equals(AttributesFactory.ALL)){
			for (Bill bill : buyList){
				for (String nameProduct : bill.getProductList()){
					if (!rowSet.containsKey(nameProduct)) rowSet.put(nameProduct, new ResSet());
					ResSet resSet = rowSet.get(nameProduct);
					double buyQ = (double) resSet.get("buyQ") + bill.getQ(nameProduct);
					double buyPQ = (double) resSet.get("buyPQ") + bill.getPQ(nameProduct);
					long cost = (long) Utility.getNumericValue(resSet.get("cost")) + bill.getProduct(nameProduct).getCost();
					resSet.put("buyQ", buyQ);
					resSet.put("buyPQ", buyPQ);
					resSet.put("cost", cost);
				}
			}
			for (Bill bill : rSellList){
				for (String nameProduct : bill.getProductList()){
					if (!rowSet.containsKey(nameProduct)) rowSet.put(nameProduct, new ResSet());
					ResSet resSet = rowSet.get(nameProduct);
					double rsQ = (double) resSet.get("rsQ") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("rsQ", rsQ);
				}
			}
		} else{
			HashSet<String> products = SupplierDAO.getSupplierList().get(nameSupplier).getProducts();
			for (Bill bill : buyList){
				for (String nameProduct : bill.getProductList()){
					if (!products.contains(nameProduct)) continue;
					if (!rowSet.containsKey(nameProduct)) rowSet.put(nameProduct, new ResSet());
					ResSet resSet = rowSet.get(nameProduct);
					double buyQ = (double) resSet.get("buyQ") + bill.getQ(nameProduct);
					double buyPQ = (double) resSet.get("buyPQ") + bill.getPQ(nameProduct);
					long cost = (long) Utility.getNumericValue(resSet.get("cost")) + bill.getProduct(nameProduct).getCost();
					resSet.put("buyQ", buyQ);
					resSet.put("buyPQ", buyPQ);
					resSet.put("cost", cost);
				}
			}
			for (Bill bill : rSellList){
				for (String nameProduct : bill.getProductList()){
					if (!products.contains(nameProduct)) continue;
					if (!rowSet.containsKey(nameProduct)) rowSet.put(nameProduct, new ResSet());
					ResSet resSet = rowSet.get(nameProduct);
					double rsQ = (double) resSet.get("rsQ") + bill.getProduct(nameProduct).getSumQ();
					resSet.put("rsQ", rsQ);
				}
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
			
			double buyQ = (double) resSet.get("buyQ");
			double buyPQ = (double) resSet.get("buyPQ");
			double buy = buyQ + buyPQ;
			long cost = (long) Utility.getNumericValue(resSet.get("cost"));
			double rsQ = (double) resSet.get("rsQ");
			Object[] thisRow = {codeProduct, nameProduct, buyQ, buyPQ, buy, cost, rsQ, buy + rsQ};
			data.add(thisRow);
		}
		tableModel.setData(data);
	}
}
