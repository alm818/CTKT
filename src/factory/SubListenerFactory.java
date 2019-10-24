package factory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import controller.AdvancedQueryController;
import controller.NullController;
import controller.QueryController;
import controller.SupplierController;
import frame.SubFrame;
import view.DiaryView;
import view.ExportView;
import view.ImportView;
import view.JohnsonView;
import view.ProductView;
import view.PromoView;
import view.ReturnView;
import view.SubIncomeBillView;
import view.SubSupplierView;

public class SubListenerFactory {
	private SubFrame subFrame;
	public SubListenerFactory(SubFrame subFrame){
		this.subFrame = subFrame;
	}
	public ActionListener getSupplierListener(){
		return new SupplierListener();
	}
	public ActionListener getProductListener(){
		return new ProductListener();
	}
	public ActionListener getPromoListener(){
		return new PromoListener();
	}
	public ActionListener getReturnListener(){
		return new ReturnListener();
	}
	public ActionListener getSellListener(){
		return new SellListener();
	}
	public ActionListener getBuyListener(){
		return new BuyListener();
	}
	public ActionListener getIncomeBillListener(){
		return new IncomeBillListener();
	}
	public ActionListener getDairyListener(){
		return new DairyListener();
	}
	public ActionListener getAddJohnsonListener(){
		return new AddJohnsonListener();
	}
	private class SupplierListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			NullController controller = new NullController(new SubSupplierView());
			subFrame.setContent("Thông tin nhà cung cấp", controller.getContentPane());
		}
	}
	private class ProductListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			SupplierController controller = new SupplierController(new ProductView());
			subFrame.setContent("Thông tin mặt hàng", controller.getContentPane());
		}
	}
	private class DairyListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			AdvancedQueryController controller = new AdvancedQueryController(new DiaryView());
			subFrame.setContent("Nhật ký nhập/xuất", controller.getContentPane());
		}
	}
	private class AddJohnsonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			NullController controller = new NullController(new JohnsonView());
			subFrame.setContent("Danh sách mặt hàng Johnson", controller.getContentPane());
		}
	}
	private class PromoListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new PromoView());
			subFrame.setContent("Hàng khuyến mãi lẻ", controller.getContentPane());
		}
	}
	private class ReturnListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new ReturnView());
			subFrame.setContent("Hóa đơn bán trả lại", controller.getContentPane());
		}
	}
	private class BuyListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new ImportView());
			subFrame.setContent("Chi tiết nhập hàng", controller.getContentPane());
		}
	}
	private class SellListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new ExportView());
			subFrame.setContent("Chi tiết xuất hàng", controller.getContentPane());
		}
	}
	private class IncomeBillListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new SubIncomeBillView());
			subFrame.setContent("Lợi nhuận theo đơn hàng", controller.getContentPane());
		}
	}
}
