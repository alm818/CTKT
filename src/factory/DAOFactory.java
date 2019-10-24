package factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import dao.BillDAO;
import dao.BuyDAO;
import dao.CustomerDAO;
import dao.DAO;
import dao.ProcessDAO;
import dao.ProductDAO;
import dao.RBuyDAO;
import dao.RSellDAO;
import dao.SellDAO;
import dao.SupplierDAO;
import fao.FileDirection;

public class DAOFactory {
	private static final Logger LOGGER = Logger.getLogger(DAOFactory.class.getName());
	private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DB_URL = FileDirection.DIR + "DB";
	private static final Connection conn;
	private static ProductDAO productDAO;
	private static BuyDAO buyDAO;
	private static RBuyDAO rBuyDAO;
	private static SellDAO sellDAO;
	private static RSellDAO rSellDAO;
	private static ProcessDAO processDAO;
	static{
		Connection tmp = null;
		try {
			Class.forName(DRIVER);
			LOGGER.info("Finished registering driver");
			tmp = DriverManager.getConnection("jdbc:derby:" + DB_URL + ";create=true");
			LOGGER.info("Finished connecting driver");
		}
		catch (Throwable e){
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		conn = tmp;
		
		try {
			productDAO = new ProductDAO();
			buyDAO = new BuyDAO();
			rBuyDAO = new RBuyDAO();
			sellDAO = new SellDAO();
			rSellDAO = new RSellDAO();
			processDAO = new ProcessDAO();
			LOGGER.info("Finished initializing all DAO");
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void close() throws SQLException{
		DAO.close();
		conn.commit();
		conn.close();
		try{
			DriverManager.getConnection("jdbc:derby:" + DB_URL + ";shutdown=true");
		} 
		catch (SQLException e){
			LOGGER.info("Connection shutdowns successfully");
		}
		LOGGER.info("Finished closing connection");
	}
	
	public static void update() throws SQLException{
		SupplierDAO.updateSupplierList();
		CustomerDAO.updateCustomerList();
		ProductDAO.updateProductList();
		BillDAO.updateBill();
	}
	
	public static Connection getConn(){
		return conn;
	}
	
	public static ProductDAO getProductDAO(){
		return productDAO;
	}
	
	public static BuyDAO getBuyDAO(){
		return buyDAO;
	}
	
	public static SellDAO getSellDAO(){
		return sellDAO;
	}

	public static RSellDAO getRSellDAO(){
		return rSellDAO;
	}
	
	public static RBuyDAO getRBuyDAO(){
		return rBuyDAO;
	}
	
	public static ProcessDAO getProcessDAO(){
		return processDAO;
	}
}
