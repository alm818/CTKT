package misc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ThreadedStatement extends Thread{
	private static final Logger LOGGER = Logger.getLogger(ThreadedStatement.class.getSimpleName());
	private static final int CHUNK_SIZE = 1000;
	private static final int MAX_CHUNKS = 20;
	private FunctionalStatement fs;
	private Connection conn;
	private String stmString;
	private ArrayList<ArrayList<ArrayList<Object[]>>> sqDataChunks;
	private ArrayList<ArrayList<Object[]>> dataChunks;
	private int currentIndex, size;
	
	private class StmThread extends Thread{
		private ArrayList<Object[]> dataChunk;
		private PreparedStatement stm;
		StmThread(ArrayList<Object[]> dataChunk) throws SQLException{
			this.dataChunk = dataChunk;
			stm = conn.prepareStatement(stmString);
		}
		public void run(){
			try {
				for (Object[] data : dataChunk)
					fs.insert(stm, data);
				stm.executeBatch();
				conn.commit();
				LOGGER.info("Finished committing");
				stm.clearBatch();
				stm.close();
			} catch (SQLException e) {
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public ThreadedStatement(Connection conn, String stmString, FunctionalStatement fs){
		this.conn = conn;
		this.stmString = stmString;
		this.fs = fs;
		sqDataChunks = new ArrayList<ArrayList<ArrayList<Object[]>>>();
		dataChunks = new ArrayList<ArrayList<Object[]>>();
		dataChunks.add(new ArrayList<Object[]>());
		sqDataChunks.add(dataChunks);
		currentIndex = 0;
		size = 0;
	}
	
	public synchronized void addData(Object...objects){
		size ++;
		ArrayList<Object[]> dataChunk = dataChunks.get(currentIndex);
		if (dataChunk.size() < CHUNK_SIZE)
			dataChunk.add(objects);
		else if (dataChunks.size() < MAX_CHUNKS){
			dataChunks.add(new ArrayList<Object[]>());
			currentIndex ++;
			ArrayList<Object[]> newChunk = dataChunks.get(currentIndex);
			newChunk.add(objects);
		} else{
			dataChunks = new ArrayList<ArrayList<Object[]>>();
			dataChunks.add(new ArrayList<Object[]>());
			currentIndex = 0;
			ArrayList<Object[]> newChunk = dataChunks.get(currentIndex);
			newChunk.add(objects);
			sqDataChunks.add(dataChunks);
		}
	}
	
	public void run(){
		LOGGER.info("Start executing " + size + " statements");
		LOGGER.info("Number of threads using: " + (int)(Math.ceil(size / CHUNK_SIZE) + 1));
		for (ArrayList<ArrayList<Object[]>> dataChunks : sqDataChunks){
			ArrayList<StmThread> threads = new ArrayList<StmThread>();
			for (ArrayList<Object[]> dataChunk : dataChunks){
				StmThread thread = null;
				try{
					thread = new StmThread(dataChunk);
				} catch (SQLException se){
					LOGGER.severe(se.getMessage());
					se.printStackTrace();
				}
				threads.add(thread);
			}
			for (StmThread thread : threads)
				thread.start();
			try{
				for (StmThread thread : threads)
					thread.join();
			} catch (InterruptedException e){
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
