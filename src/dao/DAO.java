package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import factory.DAOFactory;
import misc.ThreadedStatement;
import utility.Utility;

public abstract class DAO extends Thread{
	protected final static Logger LOGGER = Logger.getLogger(DAO.class.getName());
	protected static Connection conn;
	protected static ArrayList<PreparedStatement> psAll;
	static {
		conn = DAOFactory.getConn();
		psAll = new ArrayList<PreparedStatement>();
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	protected ArrayList<ThreadedStatement> tsAll;
	
	public DAO() throws SQLException{
		tsAll = new ArrayList<ThreadedStatement>();
		initThreadedStatement();
	}
	
	protected abstract void initThreadedStatement() throws SQLException;
	
	public void run(){
		try{
			Utility.runThreads(tsAll);
		} catch (InterruptedException e){
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void close() throws SQLException{
		for (PreparedStatement stm : psAll)
			stm.close();
	}
}
