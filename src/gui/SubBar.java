package gui;

import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import factory.SubListenerFactory;
import frame.SubFrame;

@SuppressWarnings("serial")
public class SubBar extends JMenuBar{
	private SubListenerFactory lao;
	private JMenu infoJM, checkJM;
	private JMenuItem supplierJMI, productJMI, dairyJMI, addJohnsonJMI;
	private JMenuItem promoJMI, returnJMI, sellJMI, buyJMI, incomeBillJMI;
	
	public SubBar(SubFrame subFrame) throws ClassNotFoundException, IOException{
		super();
		lao = new SubListenerFactory(subFrame);
		initMenuBar();
	}
	
	private void initialization(){
		infoJM = new JMenu();
		checkJM = new JMenu();
		supplierJMI = new JMenuItem();
		productJMI = new JMenuItem();
		dairyJMI = new JMenuItem();
		addJohnsonJMI = new JMenuItem();
		promoJMI = new JMenuItem();
		returnJMI = new JMenuItem();
		buyJMI = new JMenuItem();
		sellJMI = new JMenuItem();
		incomeBillJMI = new JMenuItem();
	}
	private void initJMI(JMenuItem menuItem, String title, ActionListener listener){
		menuItem.setText(title);
		menuItem.addActionListener(listener);
	}
	private void initJM(JMenu menu, String title, JMenuItem... menuItemList){
		menu.setText(title);
		for (JMenuItem menuItem : menuItemList) menu.add(menuItem);
		this.add(menu);
	}
	private void initMenuBar(){
		initialization();
		initJMI(supplierJMI, "Các nhà cung cấp", lao.getSupplierListener());
		initJMI(productJMI, "Các mặt hàng", lao.getProductListener());
		initJMI(dairyJMI, "Nhật ký nhập/xuất", lao.getDairyListener());
		initJMI(promoJMI, "Mặt hàng khuyến mãi đơn lẻ", lao.getPromoListener());
		initJMI(returnJMI, "Đơn hàng bán trả về", lao.getReturnListener());
		initJMI(buyJMI, "Số lượng nhập hàng", lao.getBuyListener());
		initJMI(sellJMI, "Số lượng xuất hàng", lao.getSellListener());
		initJMI(incomeBillJMI, "Lợi nhuận theo đơn hàng", lao.getIncomeBillListener());
		initJM(infoJM, "Thông tin", supplierJMI, productJMI, dairyJMI);
		infoJM.addSeparator();
		initJMI(addJohnsonJMI, "Danh sách mặt hàng Johnson", lao.getAddJohnsonListener());
		infoJM.add(addJohnsonJMI);
		initJM(checkJM, "Kiểm tra", promoJMI, returnJMI, buyJMI, sellJMI, incomeBillJMI);
	}
}
