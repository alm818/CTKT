package gui;

import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import factory.ListenerFactory;
import frame.MainFrame;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar{
	private ListenerFactory lao;
	private JMenu infoJM, wageJM, checkJM, taxJM, secJM;
	private JMenuItem supplierJMI, productJMI, dairyJMI;
	private JMenuItem settingJMI, staffJMI, wageJMI;
	private JMenuItem promoJMI, returnJMI, sellJMI, buyJMI, incomeBillJMI, incomeProductJMI;
	private JMenuItem setTaxJMI, taxJMI;
	private JMenuItem setPassJMI;
	
	public MenuBar(MainFrame mainFrame) throws ClassNotFoundException, IOException{
		super();
		lao = new ListenerFactory(mainFrame);
		initMenuBar();
	}
	
	private void initialization(){
		infoJM = new JMenu();
		wageJM = new JMenu();
		checkJM = new JMenu();
		taxJM = new JMenu();
		secJM = new JMenu();
		supplierJMI = new JMenuItem();
		productJMI = new JMenuItem();
		dairyJMI = new JMenuItem();
		settingJMI = new JMenuItem();
		staffJMI = new JMenuItem();
		wageJMI = new JMenuItem();
		promoJMI = new JMenuItem();
		returnJMI = new JMenuItem();
		buyJMI = new JMenuItem();
		sellJMI = new JMenuItem();
		incomeBillJMI = new JMenuItem();
		incomeProductJMI = new JMenuItem();
		setTaxJMI = new JMenuItem();
		taxJMI = new JMenuItem();
		setPassJMI = new JMenuItem();
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
		initJMI(supplierJMI, "Danh sách nhà cung cấp", lao.getSupplierListener());
		initJMI(productJMI, "Danh sách mặt hàng", lao.getProductListener());
		initJMI(dairyJMI, "Nhật ký nhập/xuất", lao.getDairyListener());
		initJMI(settingJMI, "Điều chỉnh hệ số lương", lao.getSettingListener());
		initJMI(staffJMI, "Danh sách nhân viên", lao.getStaffListener());
		initJMI(wageJMI, "Bảng lương nhân viên", lao.getWageListener());
		initJMI(promoJMI, "Mặt hàng khuyến mãi đơn lẻ", lao.getPromoListener());
		initJMI(returnJMI, "Đơn hàng bán trả về", lao.getReturnListener());
		initJMI(buyJMI, "Số lượng nhập hàng", lao.getBuyListener());
		initJMI(sellJMI, "Số lượng xuất hàng", lao.getSellListener());
		initJMI(incomeBillJMI, "Lợi nhuận theo đơn hàng", lao.getIncomeBillListener());
		initJMI(incomeProductJMI, "Lợi nhuận theo mặt hàng", lao.getIncomeProductListener());
		initJMI(setTaxJMI, "Tùy chỉnh thuế", lao.getSetTaxListener());
		initJMI(taxJMI, "Kiểm tra thuế", lao.getTaxListener());
		initJMI(setPassJMI, "Cài đặt mật khẩu", lao.getSetPassListener());
		initJM(infoJM, "Thông tin", supplierJMI, productJMI, dairyJMI);
		initJM(wageJM, "Lương", settingJMI, staffJMI, wageJMI);
		initJM(checkJM, "Kiểm tra", promoJMI, returnJMI, buyJMI, sellJMI, incomeBillJMI,incomeProductJMI);
		initJM(taxJM, "Thuế", setTaxJMI, taxJMI);
		initJM(secJM, "Bảo mật", setPassJMI);
	}
}
