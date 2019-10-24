package frame;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fao.FileDirection;
import fao.FileInstall;
import gui.LocalizedFileChooser;
import misc.GenericSwingWorker;
import misc.SqlRunner;
import utility.GUIUtility;


@SuppressWarnings({ "serial" })
public class InstallFrame extends JFrame {
	private JPanel contentPane;
	private final static Logger LOGGER = Logger.getLogger(InstallFrame.class.getName());

	public InstallFrame() {
		super("Cài đặt chương trình");
		initContentPane();
		this.setContentPane(contentPane);
		this.setVisible(true);
		this.setBounds(GUIUtility.getBound(500, 120));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void initContentPane() {
		InstallFrame self = this;
		JLabel addressJL = new JLabel("Thư mục cài đặt:");
		JTextField addressJTF = new JTextField();
		addressJTF.setEditable(false);
		Dimension d = addressJTF.getPreferredSize();
		d.width = 300;
		addressJTF.setPreferredSize(d);
		JButton addressJB = new JButton("Chọn");
		addressJB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LocalizedFileChooser fileChooser = new LocalizedFileChooser("Chọn thư mục cài đặt");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fileChooser.showOpenDialog(self);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					String address = file.getAbsolutePath();
					addressJTF.setText(address);
				}
			}
		});
		JPanel addressJP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		addressJP.add(addressJL);
		addressJP.add(addressJTF);
		addressJP.add(addressJB);

		JButton okJB = new JButton("Cài đặt");
		okJB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String address = addressJTF.getText();
				if (address.length() == 0)
					JOptionPane.showMessageDialog(self, "Thư mục chưa được chọn", "Lỗi cài đặt",
							JOptionPane.ERROR_MESSAGE);
				else {
					final class InstallWorker extends GenericSwingWorker{
						public InstallWorker(Container container, String title, boolean isDisposed) {
							super(container, title, isDisposed);
						}

						@Override
						public void process() {
							FileDirection.getData().setInstallAddress(address);
							FileDirection.makeFolders();
							LOGGER.info("Finished setting installed folder");
							String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
							String DB_URL = FileDirection.DIR + "DB";
							Connection conn = null;
							try {
								Class.forName(JDBC_DRIVER);
								LOGGER.info("Finished registering driver");
								
								conn = DriverManager.getConnection("jdbc:derby:" + DB_URL + ";create=true");
								LOGGER.info("Finished connecting driver");
								
								InputStream is = InstallFrame.class.getResourceAsStream(FileDirection.SQL_FILE);
								SqlRunner.runScript(conn, is);
								conn.close();
								LOGGER.info("Finished creating tables");
							
								FileInstall.execute();
								LOGGER.info("Finished installing files");
								
								@SuppressWarnings("unused")
								LoginFrame loginFrame = new LoginFrame();
								
							} catch (Throwable e) {
								LOGGER.severe(e.getMessage());
								e.printStackTrace();
							} finally {
								try {
									if (conn != null)
										conn.close();
								} catch (SQLException se) {
									LOGGER.severe(se.getMessage());
									se.printStackTrace();
								}
							}
						}
					}
					
					InstallWorker task = new InstallWorker(self, "Đang cài đặt...", true);
					task.execute();
				}
			}
		});
		JButton cancelJB = new JButton("Hủy cài đặt");
		cancelJB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		JPanel confirmJB = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		confirmJB.add(cancelJB);
		confirmJB.add(okJB);

		contentPane = new JPanel(new GridLayout(0, 1, 0, 0));
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPane.add(addressJP);
		contentPane.add(confirmJB);
	}
}
