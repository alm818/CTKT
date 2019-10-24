package factory;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import controller.AdvancedQueryController;
import controller.NullController;
import controller.QueryController;
import controller.SupplierController;
import controller.WageController;
import fao.FileDirection;
import fao.FileTax;
import frame.ChangePassFrame;
import frame.MainFrame;
import frame.WageSettingFrame;
import gui.LocalizedFileChooser;
import misc.GenericSwingWorker;
import transferObject.DataStorage;
import transferObject.TaxConnector;
import view.DiaryView;
import view.ExportView;
import view.ImportView;
import view.IncomeBillView;
import view.IncomeProductView;
import view.ProductView;
import view.PromoView;
import view.ReturnView;
import view.SetTaxView;
import view.StaffView;
import view.SupplierView;
import view.TaxView;
import view.WageView;

public class ListenerFactory {
	private MainFrame mainFrame;
	public ListenerFactory(MainFrame mainFrame){
		this.mainFrame = mainFrame;
	}
	public ActionListener getSettingListener() {
		return new SettingListener();
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
	public ActionListener getIncomeProductListener(){
		return new IncomeProductListener();
	}
	public ActionListener getSetPassListener(){
		return new SetPassListener();
	}
	public ActionListener getDairyListener(){
		return new DairyListener();
	}
	public ActionListener getStaffListener(){
		return new StaffListener();
	}
	public ActionListener getWageListener(){
		return new WageListener();
	}
	public ActionListener getSetTaxListener(){
		return new SetTaxListener();
	}
	public ActionListener getTaxListener(){
		return new TaxListener();
	}
	
	private class SettingListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			@SuppressWarnings("unused")
			WageSettingFrame frame = new WageSettingFrame();
		}
	}
	private class SupplierListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			NullController controller = new NullController(new SupplierView());
			mainFrame.setContent("Thông tin nhà cung cấp", controller.getContentPane());
		}
	}
	private class ProductListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			SupplierController controller = new SupplierController(new ProductView());
			mainFrame.setContent("Thông tin mặt hàng", controller.getContentPane());
		}
	}
	private class DairyListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			AdvancedQueryController controller = new AdvancedQueryController(new DiaryView());
			mainFrame.setContent("Nhật ký nhập/xuất", controller.getContentPane());
		}
	}
	private class StaffListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			NullController controller = new NullController(new StaffView());
			mainFrame.setContent("Danh sách nhân viên", controller.getContentPane());
		}
	}
	private class WageListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			WageController controller = new WageController(new WageView());
			mainFrame.setContent("Bảng lương nhân viên", controller.getContentPane());
		}
	}
	private class PromoListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new PromoView());
			mainFrame.setContent("Hàng khuyến mãi lẻ", controller.getContentPane());
		}
	}
	private class ReturnListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new ReturnView());
			mainFrame.setContent("Hóa đơn bán trả lại", controller.getContentPane());
		}
	}
	private class BuyListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new ImportView());
			mainFrame.setContent("Chi tiết nhập hàng", controller.getContentPane());
		}
	}
	private class SellListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new ExportView());
			mainFrame.setContent("Chi tiết xuất hàng", controller.getContentPane());
		}
	}
	private class IncomeBillListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new IncomeBillView());
			mainFrame.setContent("Lợi nhuận theo đơn hàng", controller.getContentPane());
		}
	}
	private class IncomeProductListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			QueryController controller = new QueryController(new IncomeProductView());
			mainFrame.setContent("Lợi nhuận theo mặt hàng", controller.getContentPane());
		}
	}
	private class SetTaxListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			NullController controller = new NullController(new SetTaxView());
			mainFrame.setContent("Tùy chỉnh thuế", controller.getContentPane());
		}
	}
	private class TaxListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			LocalizedFileChooser fileChooser = new LocalizedFileChooser("Chọn file tồn thuế");
		    FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel 97-2003 Workbook", "xls");
			fileChooser.setFileFilter(filter);
			DataStorage data = FileDirection.getData();
			if (data.getTaxAddress() == null)
				fileChooser.setCurrentDirectory(null);
			else
				fileChooser.setCurrentDirectory(new File(data.getTaxAddress()));
		    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fileChooser.showOpenDialog(mainFrame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				data.setTaxAddress(file.getParent());
				final class Worker extends GenericSwingWorker{
					public Worker(Container container, String title, boolean isDisposed) {
						super(container, title, isDisposed);
					}

					@Override
					public void process() {
						try {
							FileTax.extract(file);
							TaxConnector connector = FileTax.getConnector();
							NullController controller = new NullController(new TaxView(connector));
							mainFrame.setContent("Bảng cân đối HĐ", controller.getContentPane());
						} catch (NullPointerException | IOException | ParseException | SQLException e) {
							e.printStackTrace();
						}
					}
				}
				
				Worker task = new Worker(mainFrame, "Đang chạy dữ liệu...", false);
				task.execute();
			}
		}
	}
	private class SetPassListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			@SuppressWarnings("unused")
			JFrame frame = new ChangePassFrame(mainFrame);
		}
	}
}
