package frame;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;

import dao.StaffDAO;
import factory.DAOFactory;
import factory.FormatFactory;
import fao.FileDirection;
import gui.DynamicTabbedPane;
import gui.MenuBar;
import misc.GenericSwingWorker;
import utility.GUIUtility;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	private final static Logger LOGGER = Logger.getLogger(MainFrame.class.getName());
	private DynamicTabbedPane tabbedPane;
	private MenuBar menuBar;

	public MainFrame() throws ClassNotFoundException, IOException {
		super();
		Calendar today = FileDirection.getData().getToday();
		if (today == null) this.setTitle("Chương trình kiểm toán");
		else this.setTitle(String.format("Chương trình kiểm toán - Đã cập nhật đến ngày %s", FormatFactory.formatCalendar(today)));
		menuBar = new MenuBar(this);
		this.setJMenuBar(menuBar);
		tabbedPane = new DynamicTabbedPane(30);
		this.setContentPane(tabbedPane);
		this.setVisible(true);
		this.setBounds(GUIUtility.getScreenBound());
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				final class ClosingWorker extends GenericSwingWorker{
					public ClosingWorker(Container container, String title, boolean isDisposed) {
						super(container, title, isDisposed);
					}

					@Override
					public void process() {
						try {
							StaffDAO.write();
							FileDirection.write();
							DAOFactory.close();
							MainFrame.this.dispose();
						} catch (SQLException | IOException e) {
							LOGGER.severe(e.getMessage());
							e.printStackTrace();
						}
					}
				}
				ClosingWorker task = new ClosingWorker(null, "Đang đóng...", false);
				task.execute();
			}
		});
	}

	public void setContent(String title, JPanel panel) {
		tabbedPane.addComponent(title, panel);
	}
}
