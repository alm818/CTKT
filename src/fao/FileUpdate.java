package fao;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.logging.Logger;

import dao.ProcessDAO;
import dao.ProductDAO;
import dao.StaffDAO;
import factory.AttributesFactory;
import factory.DAOFactory;
import misc.FileType;
import transferObject.DataStorage;
import transferObject.FileClass;
import utility.Utility;

public class FileUpdate {
	private final static Logger LOGGER = Logger.getLogger(FileUpdate.class.getName());

	public static void update() throws IOException, SQLException, ClassNotFoundException{
		DAOFactory.getConn();
		ProductDAO.initMaps();
		
		boolean hasNewFile = false;
		DAOFactory.update();
		FileExtract.init();
		
		DataStorage data = FileDirection.getData();
		File folder = new File(data.getInstallAddress());
		
		// Traverses current files in the installed folder
		for (File file : folder.listFiles()){
			if (file.isFile()){
				FileClass fileClass = new FileClass(file);
				FileType type = fileClass.getType();
				if (type != null){
					// THIS FILE IS AN ACCEPTABLE FILE
					hasNewFile = true;
					LOGGER.info("Extracting " + file.getName());
					File target;
					switch (type){
						case BUY:
							FileExtract.extractBuy(file);
							target = new File(FileDirection.getBuyDir() + file.getName());
							break;
						case SELL:
							FileExtract.extractSell(file);
							target = new File(FileDirection.getSellDir() + file.getName());
							break;
						case RSELL:
							FileExtract.extractRSell(file);
							target = new File(FileDirection.getRSellDir() + file.getName());
							break;
						case RBUY:
							FileExtract.extractRBuy(file);
							target = new File(FileDirection.getRBuyDir() + file.getName());
							break;
						case NSELL:
							FileExtract.extractNSell(file);
							target = new File(FileDirection.getNSellDir() + file.getName());
							break;
						case NBUY:
							FileExtract.extractNBuy(file);
							target = new File(FileDirection.getNBuyDir() + file.getName());
							break;
						default:
							throw new IOException("Invalid file type");
					}
					if (target != null)
						Files.move(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
		
		ProcessDAO processDAO = DAOFactory.getProcessDAO();
		ProductDAO productDAO = DAOFactory.getProductDAO();
		if (hasNewFile){
			FileExtract.commit();
			AttributesFactory.updateToday(data.getToday());
			data.setToday(AttributesFactory.getToday());
			DAOFactory.update();
			processDAO.process();
			productDAO.prepare();
		}
	
		try {
			Utility.runThreads(productDAO, processDAO);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		FileStat.update();
		DAOFactory.update();
		ProductDAO.updateTaxConnection();
		StaffDAO.updateStaffList();
	}
}
