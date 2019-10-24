package transferObject;

import java.sql.SQLException;
import java.util.ArrayList;

import dao.BillDAO;
import misc.ThreadedStatement;

public class RStatusUpdater {
	private String nameProduct;
	private ArrayList<RStatus> sortedCBs;
	
	public RStatusUpdater(String nameProduct, ArrayList<RStatus> sortedCBs){
		this.nameProduct = nameProduct;
		this.sortedCBs = sortedCBs;
	}
	
	public void update(Bill bill, ThreadedStatement insertRSPF, ThreadedStatement insertRSS, ThreadedStatement setRQ) throws SQLException{
		double rq = bill.getQ(nameProduct);
		double q = 0;
		int cost = 0;
		int rsell_id = BillDAO.getBill(bill.getCodeBill()).getProduct(nameProduct).getID();
		for (int i = sortedCBs.size() - 1; i >= 0; i --){
			RStatus rStatus = sortedCBs.get(i);
			Bill sellBill = rStatus.getBill();
			double sq = sellBill.getQ(nameProduct);
			int sell_id = sellBill.getProduct(nameProduct).getID();
			int price = sellBill.getP(nameProduct);
			if (sellBill.getDay().before(bill.getDay())){
				double left = sq - rStatus.getRQ();
				if (left <= 0) continue;
				if (rq >= left){
					rq -= left;
					rStatus.setRQ(sq);
					q += left;
					cost += (left * price);
					insertRSS.addData(rsell_id, sell_id, left);
					setRQ.addData(sell_id, sq);
				} else{
					rStatus.setRQ(rStatus.getRQ() + rq);
					q += rq;
					cost += (rq * price);
					insertRSS.addData(rsell_id, sell_id, rq);
					setRQ.addData(sell_id, rStatus.getRQ() + rq);
					break;
				}
			}
		}
		if (q > 0){
			int sp = (int) (cost / q);
			int bp = bill.getP(nameProduct);
			insertRSPF.addData(rsell_id, q, sp, bp);
		}
	}
}
