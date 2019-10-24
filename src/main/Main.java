package main;

import java.awt.Container;
import java.awt.Font;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import factory.AttributesFactory;
import fao.FileDirection;
import fao.FileUpdate;
import frame.InstallFrame;
import frame.LoginFrame;
import frame.MainFrame;
import frame.SubFrame;
import misc.GenericSwingWorker;

public class Main {
	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	try {
					createAndShowGUI();
				} catch (Throwable e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Có lỗi khi khởi động", JOptionPane.ERROR_MESSAGE);
				}
            }
        });	
	}
	private static void createAndShowGUI() throws ClassNotFoundException, IOException, SQLException{
		setUIFont("Calibri");
		if (FileDirection.getData().isInstalled()){
			@SuppressWarnings("unused")
			LoginFrame loginFrame = new LoginFrame();
		} else{
			@SuppressWarnings("unused")
			InstallFrame installFrame = new InstallFrame();
		}
	}
	
	public static void boot() throws IOException, SQLException, ClassNotFoundException{
		FileUpdate.update(); //THE BOOTING NEEDS TO IDENTIFY NEW FILES IN THE INSTALLED FOLDER
							//THIS IS THE AUTOMATIC UPDATE. USERS ONLY NEEDS TO PUT THE FILES IN THE INSTALLED FOLDER
		LOGGER.info("Finished updating files");
		
		LOGGER.info("Starting main.thread");
		if (FileDirection.getData().isAdmin()){
			MainFrame main = new MainFrame();
			AttributesFactory.setFrame(main);
		} else{
			SubFrame main = new SubFrame();
			AttributesFactory.setFrame(main);
		}
	}
	
	public static void booting(){
		final class BootingWorker extends GenericSwingWorker{
			public BootingWorker(Container container, String title, boolean isDisposed) {
				super(container, title, isDisposed);
			}

			@Override
			public void process() {
				try {
					boot();
				} catch (ExceptionInInitializerError e){
					LOGGER.severe(e.getMessage());
					JOptionPane.showMessageDialog(null, "Người dùng khác đang sử dụng chương trình", "Lỗi khởi động", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				} catch (SQLException | ClassNotFoundException | IOException e) {
					LOGGER.severe(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		BootingWorker task = new BootingWorker(null, "Đang khởi động...", false);
		task.execute();
	}
	
	private static void setUIFont(String fontName){
	    Enumeration<Object> keys = UIManager.getDefaults().keys();
	    while (keys.hasMoreElements()){
	        Object key = keys.nextElement();
	        Object value = UIManager.get(key);
	        if (value instanceof FontUIResource){
	        	FontUIResource thisF = (FontUIResource) UIManager.get(key);
	        	int size = thisF.getSize();
	        	int style = thisF.getStyle();
	            UIManager.put(key, new Font(fontName, style, size));
	        }
	    }
	}
}
