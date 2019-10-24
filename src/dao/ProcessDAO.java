package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import factory.AttributesFactory;
import factory.ComparatorFactory;
import misc.FunctionalStatement;
import misc.NextIndex;
import misc.ThreadedStatement;
import transferObject.Bill;
import transferObject.Element;
import transferObject.Product;
import transferObject.RStatus;
import transferObject.RStatusUpdater;
import transferObject.Status;
import transferObject.StatusUpdater;
import utility.CalendarUtility;
import utility.Utility;

public class ProcessDAO extends DAO{
	private ThreadedStatement insertBST, insertSST, insertRSST, insertRBST, insertSPF, insertSH, insertRSPF, insertRSS, setRQ;
	private static HashMap<String, Bill> processingBill = new HashMap<String, Bill>();
	// CUSTOMER PRODUCT STATUS MAP
	private static HashMap<String, HashMap<String, ArrayList<RStatus>>> CBMap = new HashMap<String, HashMap<String, ArrayList<RStatus>>>();
	private static PreparedStatement getCB, getRQ;
	static{
		String getCustomerBillsString = "SELECT code FROM APP.Sell"
				+ " WHERE customer_id=? AND day>=?";
		String getRQString = "SELECT rq FROM APPDATE.Sell_Profit"
				+ " WHERE sell_id=?";
		try {
			getCB = conn.prepareStatement(getCustomerBillsString);
			getRQ = conn.prepareStatement(getRQString);
			psAll.add(getCB);
			psAll.add(getRQ);
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private HashMap<String, ArrayList<Bill>> productSplitMap;
	private NextIndex next;
	private ArrayList<String> nameList;
	
	private synchronized static ArrayList<RStatus> getCustomerBills(String codeCustomer, String nameProduct) throws SQLException{
		if (CBMap.containsKey(codeCustomer))
			return CBMap.get(codeCustomer).get(nameProduct);
		getCB.setInt(1, CustomerDAO.getID(codeCustomer));
		getCB.setDate(2, CalendarUtility.toDate(AttributesFactory.getLimReturnDay()));
		ResultSet results = getCB.executeQuery();
		HashMap<String, ArrayList<RStatus>> billsMap = new HashMap<String, ArrayList<RStatus>>();
		ArrayList<Bill> billList = new ArrayList<Bill>();
		while (results.next()){
			String codeSell = results.getString("code");
			billList.add(BillDAO.getBill(codeSell));
		}
		results.close();
		billList.sort(ComparatorFactory.getBillComparator());
		for (Bill bill : billList){
			for (String name : bill.getProductList()){
				Element e = bill.getProduct(name);
				if (e.getType() == Element.P) continue;
				getRQ.setInt(1, e.getID());
				ResultSet result = getRQ.executeQuery();
				double rq = 0;
				while (result.next()){
					rq = result.getDouble("rq");
					break;
				}
				result.close();
				if (rq == bill.getQ(name)) continue;
				if (!billsMap.containsKey(name)) billsMap.put(name, new ArrayList<RStatus>());
				billsMap.get(name).add(new RStatus(bill, rq));
			}
		}
		CBMap.put(codeCustomer, billsMap);
		return billsMap.get(nameProduct);
	}
	
	public ProcessDAO() throws SQLException {
		super();
		productSplitMap = new HashMap<String, ArrayList<Bill>>();
	}
	
	public static HashMap<String, Bill> getBillLists(){
		return processingBill;
	}
	
	public static Bill getBill(String codeBill){
		if (!processingBill.containsKey(codeBill)) return null;
		return processingBill.get(codeBill);
	}
	
	public static void addBill(Bill bill){
		processingBill.put(bill.getCodeBill(), bill);
	}
	
	// INSERT TO APPDATE THE STATUS RIGHT BEFORE THE TRANSACTION OF THE BILL
	private void updateBill_Status(Product product, Bill bill) throws SQLException{
		String codeBill = bill.getCodeBill();
		String nameProduct = product.getName();
		ThreadedStatement thisTS;
		switch (Utility.getBillType(codeBill)){
			case BUY:
				thisTS = insertBST;
				break;
			case SELL:
				thisTS = insertSST;
				break;
			case RSELL:
				thisTS = insertRSST;
				
				//RSELL_SELL AND RSELL_PROFIT AND SELL_PROFIT UPDATE
				if (bill.getProduct(nameProduct).getType() != Element.P){
					String codeCustomer = bill.getTarget();
					ArrayList<RStatus> sortedCBs = ProcessDAO.getCustomerBills(codeCustomer, nameProduct);
					if (sortedCBs != null){
						RStatusUpdater updater = new RStatusUpdater(nameProduct, sortedCBs);
						updater.update(bill, insertRSPF, insertRSS, setRQ);
					}
				}
				break;
			case RBUY:
				thisTS = insertRBST;
				break;
			default:
				throw new SQLException("INVALID CODE " + codeBill);
		}
		int id = BillDAO.getBill(codeBill).getProduct(nameProduct).getID();
		double q = 0, pq = 0;
		for (Status status : product.getStatuses()){
			q += status.getQ(nameProduct);
			pq += status.getPQ(nameProduct);
		}
		thisTS.addData(id, q, pq);
	}
	
	public void process() throws SQLException{
		for (Bill bill : processingBill.values()){
			for (String nameProduct : bill.getProductList()){
				if (!productSplitMap.containsKey(nameProduct)) productSplitMap.put(nameProduct, new ArrayList<Bill>());
				productSplitMap.get(nameProduct).add(bill);
			}
		}
		
		next = new NextIndex();
		nameList = new ArrayList<String>(productSplitMap.keySet());
		
		final class CycleThread extends Thread{
			public void run(){
				try {
					while (true){
						int index = next.value();
						if (index >= nameList.size()) break;
						String nameProduct = nameList.get(index);
						ArrayList<Bill> billList = productSplitMap.get(nameProduct);
						billList.sort(ComparatorFactory.getBillComparator());
						for (int i = 0; i < billList.size(); i ++){
							Bill bill = billList.get(i);
							String codeBill = bill.getCodeBill();
							Product product = ProductDAO.getProductList().get(nameProduct);
							updateBill_Status(product, bill);
							//STATUS UPDATE
							StatusUpdater statusUpdater = new StatusUpdater(product);
							switch (Utility.getBillType(codeBill)){
								case SELL:
									statusUpdater.updateSell(bill);
									
									//SELL_PROFIT UPDATE
									int id = BillDAO.getBill(codeBill).getProduct(nameProduct).getID();
									int sp = bill.getP(nameProduct);
									int bp = statusUpdater.getBuyPrice();
									insertSPF.addData(id, sp, bp);
									
									//SELL_HISTORY UPDATE
									if (bill.getProduct(nameProduct).getType() != Element.P){
										HashMap<String, Double> history = statusUpdater.getHistory();
										for (String code : history.keySet()){
											double q = history.get(code);
											insertSH.addData(id, code, q);
										}
									}
									break;
								case BUY:
									statusUpdater.updateBuy(bill);
									if (bill.getProduct(nameProduct).getType() != Element.P)
										product.setLast(bill.getP(nameProduct), codeBill);
									break;
								case RSELL:
									statusUpdater.updateBuy(bill);
									break;
								case RBUY:
									statusUpdater.updateSell(bill);
									break;
								default:
									throw new SQLException("INVALID CODE " + codeBill);
							}
						}
					}
				} catch (SQLException e) {
					LOGGER.severe(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		ArrayList<CycleThread> threadList = new ArrayList<CycleThread>();
		for (int i = 0; i < AttributesFactory.CONCURRENT_THREAD; i ++) threadList.add(new CycleThread());
		for (CycleThread thread : threadList)
			thread.start();
		try{
			for (CycleThread thread : threadList)
				thread.join();
		} catch (InterruptedException e){
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	protected void initThreadedStatement() throws SQLException {
		FunctionalStatement Fs = (PreparedStatement stm, Object[] objects) -> {
			int id = (int) objects[0];
			double q = (double) objects[1];
			double pq = (double) objects[2];
			stm.setInt(1, id);
			stm.setDouble(2, q);
			stm.setDouble(3, pq);
			stm.addBatch();
		};
		
		String stringBST = "INSERT INTO APPDATE.Buy_Status (buy_id, q, pq)" 
				+ " VALUES (?, ?, ?)";
		insertBST = new ThreadedStatement(conn, stringBST, Fs);
		tsAll.add(insertBST);
		
		String stringSST = "INSERT INTO APPDATE.Sell_Status (sell_id, q, pq)" 
				+ " VALUES (?, ?, ?)";
		insertSST = new ThreadedStatement(conn, stringSST, Fs);
		tsAll.add(insertSST);
		
		String stringRSST = "INSERT INTO APPDATE.RSell_Status (rsell_id, q, pq)" 
				+ " VALUES (?, ?, ?)";
		insertRSST = new ThreadedStatement(conn, stringRSST, Fs);
		tsAll.add(insertRSST);
		
		String stringRBST = "INSERT INTO APPDATE.RBuy_Status (rbuy_id, q, pq)" 
				+ " VALUES (?, ?, ?)";
		insertRBST = new ThreadedStatement(conn, stringRBST, Fs);
		tsAll.add(insertRBST);
		
		String stringSPF = "INSERT INTO APPDATE.Sell_Profit (sell_id, sp, bp)"
				+ " VALUES (?, ?, ?)";
		FunctionalStatement SPFFs = (PreparedStatement stm, Object[] objects) -> {
			int id = (int) objects[0];
			int sp = (int) objects[1];
			int bp = (int) objects[2];
			stm.setInt(1, id);
			stm.setInt(2, sp);
			stm.setInt(3, bp);
			stm.addBatch();
		};
		insertSPF = new ThreadedStatement(conn, stringSPF, SPFFs);
		tsAll.add(insertSPF);
		
		String stringSH = "INSERT INTO APPDATE.Sell_History (sell_id, code, q)"
				+ " VALUES (?, ?, ?)";
		FunctionalStatement SHFs = (PreparedStatement stm, Object[] objects) -> {
			int id = (int) objects[0];
			String code = (String) objects[1];
			double q = (double) objects[2];
			stm.setInt(1, id);
			stm.setString(2, code);
			stm.setDouble(3, q);
			stm.addBatch();
		};
		insertSH = new ThreadedStatement(conn, stringSH, SHFs);
		tsAll.add(insertSH);
		
		String stringRSPF = "INSERT INTO APPDATE.RSell_Profit (rsell_id, q, sp, bp)"
				+ " VALUES (?, ?, ?, ?)";
		FunctionalStatement RSPFFs = (PreparedStatement stm, Object[] objects) -> {
			int id = (int) objects[0];
			double q = (double) objects[1];
			int sp = (int) objects[2];
			int bp = (int) objects[3];
			stm.setInt(1, id);
			stm.setDouble(2, q);
			stm.setInt(3, sp);
			stm.setInt(4, bp);
			stm.addBatch();
		};
		insertRSPF = new ThreadedStatement(conn, stringRSPF, RSPFFs);
		tsAll.add(insertRSPF);
		
		String stringRSS = "INSERT INTO APPDATE.RSell_Sell (rsell_id, sell_id, q)" 
				+ " VALUES (?, ?, ?)";
		FunctionalStatement RSSFs = (PreparedStatement stm, Object[] objects) -> {
			int rsell_id = (int) objects[0];
			int sell_id = (int) objects[1];
			double q = (double) objects[2];
			stm.setInt(1, rsell_id);
			stm.setInt(2, sell_id);
			stm.setDouble(3, q);
			stm.addBatch();
		};
		insertRSS = new ThreadedStatement(conn, stringRSS, RSSFs);
		tsAll.add(insertRSS);
		
		String stringRQ = "UPDATE APPDATE.Sell_Profit"
				+ " SET rq=? WHERE sell_id=?";
		FunctionalStatement RQFs = (PreparedStatement stm, Object[] objects) -> {
			int id = (int) objects[0];
			double rq = (double) objects[1];
			stm.setDouble(1, rq);
			stm.setInt(2, id);
			stm.addBatch();
		};
		setRQ = new ThreadedStatement(conn, stringRQ, RQFs);
		tsAll.add(setRQ);
	}
}
