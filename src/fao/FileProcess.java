package fao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileProcess {
	
	public static void writeFile(String fileAddress, Object object) throws IOException{
		File file = new File(fileAddress);
		int fromIndex = 0;
		File checkDir;
		String separator = FileDirection.SEPARATOR;
		checkDir = new File(fileAddress.substring(0, fileAddress.lastIndexOf(separator)));
		if (!checkDir.exists())
			while (fileAddress.indexOf(separator, fromIndex) >= 0){
				fromIndex = fileAddress.indexOf(separator, fromIndex + separator.length());
				if (fromIndex < 0) break;
				String dirName = fileAddress.substring(0, fromIndex);
				File dirFile = new File(dirName);
				if (!dirFile.exists()) dirFile.mkdirs(); 
			}
		FileOutputStream stream = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(stream);
		out.writeObject(object);
		out.flush();
		out.close();
		stream.close();
	}
		
	public static Object readFile(String fileAddress) throws IOException, ClassNotFoundException{
		File file = new File(fileAddress);
		if (!file.exists()) return null;
		Object object = null;
		FileInputStream stream = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(stream);
		object = in.readObject();
		in.close();
		stream.close();
		return object;
	}
}
