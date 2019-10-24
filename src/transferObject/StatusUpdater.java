package transferObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dao.ProductDAO;
import dao.RSellDAO;
import misc.BillType;
import utility.Utility;

public class StatusUpdater {
	private String nameProduct;
	private ArrayList<Status> statuses, history;
	private Integer bp;
	
	public StatusUpdater(Product product){
		this.nameProduct = product.getName();
		this.statuses = product.getStatuses();
		bp = null;
		history = null;
	}
	
	public StatusUpdater(String nameProduct, ArrayList<Status> statuses){
		this.nameProduct = nameProduct;
		this.statuses = statuses;
		bp = null;
		history = null;
	}
	
	public Integer getBuyPrice(){
		return bp;
	}
	
	public HashMap<String, Double> getHistory(){
		if (history != null){
			HashMap<String, Double> billMap = new HashMap<String, Double>();
			for (Status status : history){
				String codeBill = status.getBill().getCodeBill();
				if (!billMap.containsKey(codeBill)) billMap.put(codeBill, 0.0);
				billMap.put(codeBill, billMap.get(codeBill) + status.getQ());
			}
			return billMap;
		}
		return null;
	}
	
	private void removeTrivial(){
		int NPIndex = Status.getNPIndex(nameProduct, statuses);
		int PIndex = Status.getPIndex(nameProduct, statuses);
		Status NPStatus = statuses.get(NPIndex);
		Status PStatus = statuses.get(PIndex);
		for (Iterator<Status> i = statuses.iterator(); i.hasNext();) {
			Status status = i.next();
			if (status != NPStatus && status != PStatus && status.isTrivial())
				i.remove();
		}
	}
	
	public void updateBuy(Bill bill){
		Bill tmp_bill;
		if (Utility.getBillType(bill.getCodeBill()) == BillType.BUY)
			tmp_bill = bill;
		else{
			//RSell Bill - Needs to change the price
			Product product = ProductDAO.getProductList().get(nameProduct);
			int last_p = product.getLastP();
			String last_code = product.getLastCode();
			if (last_p == 0){
				last_p = bill.getP(nameProduct);
				last_code = bill.getCodeBill();
			}
			try {
				RSellDAO.setLast(bill.getCodeBill(), nameProduct, last_p, last_code);
			} catch (SQLException err) {
				err.printStackTrace();
			}
			Element e = bill.getProduct(nameProduct);
			int cost = (int) (bill.getQ(nameProduct) * last_p);
			tmp_bill = new Bill(bill.getDay(), bill.getCodeBill(), bill.getTarget());
			tmp_bill.addElement(nameProduct, e.getQ(), e.getPQ(), cost);
		}
		Element e = tmp_bill.getProduct(nameProduct);
		int type = e.getType();
		
		// UNDER TESTING
		int NPIndex = 0, PIndex = 0;
		try {
			NPIndex = Status.getNPIndex(nameProduct, statuses);
			PIndex = Status.getPIndex(nameProduct, statuses);
		} catch (NullPointerException err) {
			System.out.println(nameProduct);
			for (Status status : statuses) {
				System.out.println(status);
			}
		}


		if (ProductDAO.isSplit(nameProduct)){
			if (type == Element.NP){
				Status NPStatus = statuses.get(NPIndex);
				double newQ;
				if (NPStatus.getQ() <= 0){
					newQ = e.getQ() + NPStatus.getQ();
					if (NPIndex == PIndex) NPStatus.setQ(0);
					else statuses.remove(NPIndex);
				} else newQ = e.getQ();
				statuses.add(new Status(tmp_bill, newQ, 0));
			} else if (type == Element.P){
				Status PStatus = statuses.get(PIndex);
				double newPQ;
				if (PStatus.getPQ() <= 0){
					newPQ = e.getPQ() + PStatus.getPQ();
					if (NPIndex == PIndex) PStatus.setPQ(0);
					else statuses.remove(PIndex);
				} else newPQ = e.getPQ();
				statuses.add(new Status(tmp_bill, 0, newPQ));
			} else{
				Status NPStatus = statuses.get(NPIndex);
				Status PStatus = statuses.get(PIndex);
				double newQ;
				if (NPStatus.getQ() <= 0){
					newQ = e.getQ() + NPStatus.getQ();
					statuses.remove(NPIndex);
				} else newQ = e.getQ();
				double newPQ;
				if (PStatus.getPQ() <= 0){
					newPQ = e.getPQ() + PStatus.getPQ();
					statuses.remove(PStatus);
				} else newPQ = e.getPQ();
				statuses.add(new Status(tmp_bill, newQ, newPQ));
			}
		} else{
			if (type == Element.P){
				Status PStatus = statuses.get(PIndex);
				double newPQ;
				if (PStatus.getPQ() <= 0){
					newPQ = e.getPQ() + PStatus.getPQ();
					statuses.remove(PIndex);
				} else newPQ = e.getPQ();
				statuses.add(new Status(tmp_bill, 0, newPQ));
			} else{
				Status NPStatus = statuses.get(NPIndex);
				double newQ, newPQ;
				if (NPStatus.getSumQ() <= 0){
					newPQ = e.getPQ() + NPStatus.getSumQ();
					newQ = e.getQ();
					if (newPQ <= 0){
						newQ += newPQ;
						newPQ = 0;
					}
					statuses.remove(NPIndex);
				} else{
					newQ = e.getQ();
					newPQ = e.getPQ();
				}
				statuses.add(new Status(tmp_bill, newQ, newPQ));
			}
		}
	}
	
	public void updateSell(Bill bill){
		Element e = bill.getProduct(nameProduct);
		int type = e.getType();
		
		// UNDER TESTING
		int NPIndex = 0, PIndex = 0;
		try {
			NPIndex = Status.getNPIndex(nameProduct, statuses);
			PIndex = Status.getPIndex(nameProduct, statuses);
		} catch (NullPointerException err) {
			System.out.println(nameProduct);
			for (Status status : statuses) {
				System.out.println(status);
			}
		}
		
		Status NPStatus = statuses.get(NPIndex);
		Status PStatus = statuses.get(PIndex);
		int cost = 0;
		history = new ArrayList<Status>();
		if (ProductDAO.isSplit(nameProduct)){
			if (type == Element.NP){
				int i = 0;
				double q = e.getQ();
				while (true){
					Status status = statuses.get(i);
					int statusType = status.getBill().getProduct(nameProduct).getType();
					int price = status.getP(nameProduct);
					if (status == NPStatus){
						NPStatus.setQ(NPStatus.getQ() - q);
						cost += (q * price);
						history.add(new Status(NPStatus.getBill(), q, 0));
						break;
					} else{
						if (statusType == Element.P) i ++;
						else if (statusType == Element.NP){
							if (q >= status.getQ()){
								q -= status.getQ();
								cost += (status.getQ() * price);
								history.add(new Status(status.getBill(), status.getQ(), 0));
								statuses.remove(i);
							} else{
								status.setQ(status.getQ() - q);
								cost += (q * price);
								history.add(new Status(status.getBill(), q, 0));
								break;
							}
						} else{
							if (q >= status.getQ()){
								q -= status.getQ();
								cost += (status.getQ() * price);
								history.add(new Status(status.getBill(), status.getQ(), 0));
								status.setQ(0);
								i ++;
							} else{
								status.setQ(status.getQ() - q);
								cost += (q * price);
								history.add(new Status(status.getBill(), q, 0));
								break;
							}
						}
					}
				}
			} else if (type == Element.P){
				int i = 0;
				double pq = e.getPQ();
				while (true){
					Status status = statuses.get(i);
					int statusType = status.getBill().getProduct(nameProduct).getType();
					if (status == PStatus){
						PStatus.setPQ(PStatus.getPQ() - pq);
						break;
					} else{
						if (statusType == Element.NP) i ++;
						else if (statusType == Element.P){
							if (pq >= status.getPQ()){
								pq -= status.getPQ();
								statuses.remove(i);
							} else{
								status.setPQ(status.getPQ() - pq);
								break;
							}
						} else{
							if (pq >= status.getPQ()){
								pq -= status.getPQ();
								status.setPQ(0);
								i ++;
							} else{
								status.setPQ(status.getPQ() - pq);
								break;
							}
						}
					}
				}
			} else{
				int i = 0;
				double q = e.getQ();
				while (true){
					Status status = statuses.get(i);
					int statusType = status.getBill().getProduct(nameProduct).getType();
					int price = status.getP(nameProduct);
					if (status == NPStatus){
						NPStatus.setQ(NPStatus.getQ() - q);
						cost += (q * price);
						history.add(new Status(NPStatus.getBill(), q, 0));
						break;
					} else{
						if (statusType == Element.P) i ++;
						else if (statusType == Element.NP){
							if (q >= status.getQ()){
								q -= status.getQ();
								cost += (status.getQ() * price);
								history.add(new Status(status.getBill(), status.getQ(), 0));
								statuses.remove(i);
							} else{
								status.setQ(status.getQ() - q);
								cost += (q * price);
								history.add(new Status(status.getBill(), q, 0));
								break;
							}
						} else{
							if (q >= status.getQ()){
								q -= status.getQ();
								cost += (status.getQ() * price);
								history.add(new Status(status.getBill(), status.getQ(), 0));
								status.setQ(0);
								i ++;
							} else{
								status.setQ(status.getQ() - q);
								cost += (q * price);
								history.add(new Status(NPStatus.getBill(), q, 0));
								break;
							}
						}
					}
				}
				i = 0;
				double pq = e.getPQ();
				while (true){
					Status status = statuses.get(i);
					int statusType = status.getBill().getProduct(nameProduct).getType();
					if (status == PStatus){
						PStatus.setPQ(PStatus.getPQ() - pq);
						break;
					} else{
						if (statusType == Element.NP) i ++;
						else if (statusType == Element.P){
							if (pq >= status.getPQ()){
								pq -= status.getPQ();
								statuses.remove(i);
							} else{
								status.setPQ(status.getPQ() - pq);
								break;
							}
						} else{
							if (pq >= status.getPQ()){
								pq -= status.getPQ();
								status.setPQ(0);
								i ++;
							} else{
								status.setPQ(status.getPQ() - pq);
								break;
							}
						}
					}
				}
			}
		} else{
			if (type == Element.P){
				int i = 0;
				double pq = e.getPQ();
				while (true){
					Status status = statuses.get(i);
					int statusType = status.getBill().getProduct(nameProduct).getType();
					if (status == PStatus){
						PStatus.setPQ(PStatus.getPQ() - pq);
						break;
					} else{
						if (statusType != Element.P) i ++;
						else {
							if (pq >= status.getPQ()){
								pq -= status.getPQ();
								statuses.remove(i);
							} else{
								status.setPQ(status.getPQ() - pq);
								break;
							}
						}
					}
				}
			} else{
				int i = 0;
				double q = e.getPQ() + e.getQ();
				while (true){
					Status status = statuses.get(i);
					int statusType = status.getBill().getProduct(nameProduct).getType();
					int price = status.getP(nameProduct);
					if (status == NPStatus){
						NPStatus.setPQ(NPStatus.getPQ() - q);
						if (NPStatus.getPQ() < 0){
							NPStatus.setQ(NPStatus.getQ() + NPStatus.getPQ());
							NPStatus.setPQ(0);
						}
						cost += (q * price);
						history.add(new Status(NPStatus.getBill(), q, 0));
						break;
					} else{
						if (statusType == Element.P) i ++;
						else{
							double sumQ = status.getQ() + status.getPQ();
							if (q >= sumQ){
								q -= sumQ;
								cost += (sumQ * price);
								history.add(new Status(status.getBill(), sumQ, 0));
								statuses.remove(i);
							} else{
								status.setPQ(status.getPQ() - q);
								if (status.getPQ() < 0){
									status.setQ(status.getQ() + status.getPQ());
									status.setPQ(0);
								}
								cost += (q * price);
								history.add(new Status(status.getBill(), q, 0));
								break;
							}
						}
					}
				}
			}
		}
		bp = (int) (cost / bill.getQ(nameProduct));
		removeTrivial();
	}
}
