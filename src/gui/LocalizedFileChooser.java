package gui;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class LocalizedFileChooser extends JFileChooser{
	public LocalizedFileChooser(String title){
		super();
		this.setDialogTitle(title);
		setUILanguage();
	}
	private void setUILanguage(){
        UIManager.put("FileChooser.lookInLabelText", "Tìm:");
        UIManager.put("FileChooser.openButtonText", "Chọn");
        UIManager.put("FileChooser.cancelButtonText", "Hủy");
        UIManager.put("FileChooser.fileNameLabelText", "Tập tin:");
        UIManager.put("FileChooser.folderNameLabelText", "Thư mục:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Định dạng:");
        UIManager.put("FileChooser.upFolderToolTipText", "Trở về thư mục gốc");
        UIManager.put("FileChooser.newFolderToolTipText", "Tạo thư mục mới");
        UIManager.put("FileChooser.listViewButtonToolTipText", "Coi theo danh sách");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Coi chi tiết");
        SwingUtilities.updateComponentTreeUI(this);
	}
}
