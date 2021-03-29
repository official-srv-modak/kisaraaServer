package com.kisara.shopManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/*
 *  AUTHOR - SOURAV MODAK
 *  
 *  This is a class which will logically bind the resources in the server as a file system and 
 *  assign a logical path and id to access the resources (graphical resources).
 *  
 *  Created on 9th March 2021
 *  
 *  Add modification history below.
 *  
 */


public class FileSystem extends HttpServlet {
	
	
	public static String  propertyFilePath= "Server.properties";
	public static String resourceHomeAddress = getProperty(propertyFilePath, "File.System.Home", 1).get("File.System.Home").toString();
	public static File homeAddressDir = new File(resourceHomeAddress);
			//"/Applications/XAMPP/xamppfiles/htdocs/shop_website"; 
	
	public static HashMap<String, String> getProperty(String propertyFilePath, String propertyName, int processFlag)
	{
		HashMap<String, String> output = new HashMap<String, String>();
		
		File propertyfile = new File(propertyFilePath);
		if(propertyfile.exists())
		{
			//System.out.println("Checking properies file in "+propertyfile.getAbsolutePath()+"\nFound");
			Properties prop = new Properties();
			try {
				prop.load(new FileInputStream(propertyfile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Set <String>keySet = prop.stringPropertyNames();
			
			for(Iterator<String> key = keySet.iterator(); key.hasNext();)
			{
				String keyAtInd = key.next();
				String fromFile = (String)prop.getProperty(keyAtInd);
				if(processFlag == 1)
				{
					fromFile = fromFile.replace("?", File.separator+File.separator);
				}
				output.put(keyAtInd, fromFile);
			}
		}
		else
		{
			try {
				propertyfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Server Property file not found \""+propertyFilePath+"\"");
			System.out.println("Created a new file at "+propertyfile.getAbsolutePath()+", define the properties");
			System.exit(0);
		}
		return output;
	}
	public static String isDirectoryPresent(ArrayList<String> paths)
	{
		/*
		 * this is to check if the directories are present in the sub directory structure passed in paths
		 * returns the path of the first directory if found else returns null;
		 */
		for(String path : paths)
		{
			File file = new File(path);
			if(file.exists())
			{
				if(file.isDirectory())
					return file.getAbsolutePath();
			}
			else
			{
				System.out.println("Incorrect path "+file.getAbsolutePath());
			}
					
		}
		return null;
	}
	
	public static String getPathOfImage(String id)
	{
		/*
		 *  This is to get the path from the database when the serial is specified.
		 *  
		 *  returns - the Path of the image
		 *  		  or null if the serial is wrong or doesn't corresponds to any image
		 */
		
		String path = null;
		
		path = DatabaseConnection.getSingleTableDataMapWithValuePredicate(DatabaseConnection.dbName, DatabaseConnection.fileSystemTableName, "logical_path", "id", id).get(0);
		
		if(path.isEmpty())
			return null;
		return path;
	}
	public static String [] getFullPath(String parentPath, String [] fileList)
	{
		/*
		 * this is to append the parent path to the list which returned in the file.list()
		 */
		
		for(int i = 0; i<fileList.length; i++)
			fileList[i] = parentPath +File.separator+ fileList[i];
		
		return fileList;
	}
	
	public static ArrayList<String> getFullPathList(String parentPath, String [] fileList)
	{
		ArrayList<String> output = new ArrayList<String>();
		for(int i = 0; i<fileList.length; i++)
			output.add(parentPath +File.separator+ fileList[i]);
		
		return output;
	}
	
	public static ArrayList<String> scanWholeDirectory(String directoryPath, int processMode)
	{
		/* processMode can be used to select the hidden files or not
		 * 0 - to include hidden files too in the returned tree
		 * 1 - to not include hidden files too in the returned tree
		 */
		
		File homeDirectory = new File(homeAddressDir.getAbsolutePath());
		ArrayList <String> pathTree = new ArrayList<String>();
		if(homeDirectory.exists())
		{
			String [] directories = {homeAddressDir.getAbsolutePath()};
			pathTree.add(homeAddressDir.getAbsolutePath());
			for(int i = 0; i < directories.length; i++)
			{
				String[] subDirectories = {};
				
				switch(processMode)
				{
					case 0 :
						subDirectories = new File(directories[i]).list();
						break;
					case 1 :
						subDirectories = new File(directories[i]).list(new FilenameFilter() {
						    public boolean accept(File dir, String name) {
						        return ! name.startsWith(".");
						    }
						});
						break;
					default :
						System.out.println("Invalid processMode supplied");
						break;
							
				}
				ArrayList<String>subDirectoriesList = getFullPathList(directories[i], subDirectories);
				pathTree.addAll(pathTree.size(), subDirectoriesList);
				String directoryFlag = isDirectoryPresent(subDirectoriesList);
				while(directoryFlag != null)
				{
					directories = Arrays.copyOf(directories, directories.length + 1);
					directories[directories.length - 1] = directoryFlag;
					subDirectoriesList.remove(directoryFlag);
					directoryFlag = isDirectoryPresent(subDirectoriesList);
				}
			}
			
		}
		else
		{
			System.out.println("Incorrect path "+homeDirectory.getAbsolutePath());
		}
		
		return pathTree;
	}
	
	public static String getUniqueId(String path)
	{
		String value = "";
		ArrayList valArr = DatabaseConnection.getSingleTableDataMap(DatabaseConnection.dbName, DatabaseConnection.fileSystemTableName, "id");
		
		if(valArr.isEmpty())
			value = "1001";
		
	    return value;
	}
	
	public static String changeSlash(String str)
	{
		String output = "";
		for(char val : str.toCharArray())
		{
			if(val=='\\')
				val = '&';
			output += val;
		}
		return output;
	}
	public static Boolean isUnixHiddenFile(String path)
	{
		String fileName = path;
		if(path.contains("\\"))
		{
			path = changeSlash(path);
			fileName = path.split("&")[path.split("&").length - 1];
		}
		else if(path.contains("/"))
		{
			fileName = path.split("/")[path.split("/").length - 1];
		}
		
		if(fileName.startsWith("."))
		{
			return true;
		}
		return false;
	}
	
	public static void resetDirectoryOverride(String dbName, String fileSystemTableName, String homeDirectoryPath, int processMode)
	{
		/* processMode can be used to select the hidden files or not
		 * 0 - to include hidden files too in the returned tree
		 * 1 - to not include hidden files too in the returned tree
		 */
		
		ArrayList<String> pathTree = new ArrayList<String>();
		System.out.println("[WARNING] : Whole file system will get reset. Don't exit the program");
		
		DatabaseConnection.truncateTable(dbName, fileSystemTableName);
		pathTree = scanWholeDirectory(homeDirectoryPath, processMode);
		for(String path : pathTree)
		{
			if(isUnixHiddenFile(path))
				continue;
			DatabaseConnection.redefineFileSystemTable();
		    String type = determineTypeOfFile(path);
		    ArrayList<String> columnsList = new ArrayList<String>(), valuesList = new ArrayList<String>();
		    columnsList = DatabaseConnection.getColumnNames(dbName, fileSystemTableName);
		    valuesList = DatabaseConnection.makeValuesList(getUniqueId(path), path, type, "CURDATE()", "NOW()");
		    DatabaseConnection.insertIntoTable(dbName, fileSystemTableName, columnsList, valuesList);
		}
		System.out.println("System Resetted");
	}
	public static void resetDirectory(String dbName, String fileSystemTableName, String homeDirectoryPath, int processMode)
	{
		 /* processMode can be used to select the hidden files or not
		 * 0 - to include hidden files too in the returned tree
		 * 1 - to not include hidden files too in the returned tree
		 */
		ArrayList<String> pathTree = new ArrayList<String>();
		System.out.println("[WARNING] : Whole file system will get reset. It is not adviced without proper authorisation from development team.\nSystem can get corrupted\nWant to proceed? {YES\\NO}");
		while(true)
		{
			String choice = new Scanner(System.in).next();
			if(choice.equalsIgnoreCase("YES"))
			{
				DatabaseConnection.truncateTable(dbName, fileSystemTableName);
				pathTree = scanWholeDirectory(homeDirectoryPath, processMode);
				for(String path : pathTree)
				{
					if(isUnixHiddenFile(path))
						continue;
					DatabaseConnection.redefineFileSystemTable();
				    String type = determineTypeOfFile(path);
				    ArrayList<String> columnsList = new ArrayList<String>(), valuesList = new ArrayList<String>();
				    columnsList = DatabaseConnection.getColumnNames(dbName, fileSystemTableName);
				    valuesList = DatabaseConnection.makeValuesList(getUniqueId(path), path, type, "CURDATE()", "NOW()");
				    DatabaseConnection.insertIntoTable(dbName, fileSystemTableName, columnsList, valuesList);
				}
				System.out.println("System Resetted");
				break;
			}
			else if(choice.equalsIgnoreCase("NO"))
				break;
			else
				System.out.println("INVALID ANDSWER");
		}
		
	}
	
	public static String getFileType(String fileExtension)
	{
		/*
		 * This will return the file type depending upon the file extension registered
		 * this will return as image, video, etc
		 * return null if cannot determine
		 */
		if(fileExtension.equals("png") || fileExtension.equals("gif") || fileExtension.equals("jpg") || fileExtension.equals("jpeg"))
		{
			return "image";
		}
		else if(fileExtension.equals("mp4") || fileExtension.equals("avi") || fileExtension.equals("webm"))
		{
			return "video";
		}
		else
		{
			return null;
		}
	}
	public static String getFileExtension(String fullName) {
		if(fullName != null && !fullName.isEmpty())
		{
			String fileName = new File(fullName).getName();
		    int dotIndex = fileName.lastIndexOf('.');
		    return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
		}
		return null;
	}
	
	public static String determineTypeOfFile(String directoryAddress)
	{
		/*
		 * This will return the type of file passed as address in directoryAddress
		 * If the address is wrong then it will return null
		 * else it will return directory or extension of file after dot
		 */
		String output = null;
		File file = new File(directoryAddress);
		if(directoryAddress != null && file.exists())
		{
			if(file.isDirectory())
				output = "directory";
			else
				output = getFileExtension(directoryAddress);
		}
		return output;
	}
	
	public static void initialiseMediaScanner(String dbName, String fileSystemTableName, String homeDirectoryPath) throws IOException, InterruptedException
	{
		/*
		 * Media scanner method is to scan the entire resource home directory for changes or initializations process
		 * and do process as needed.
		 * This returns a ArrayList<String> of list of paths of all the files and folders in the home directory
		 * 
		 * processMode can be used to select the hidden files or not
		 * 0 - to include hidden files too in the returned tree
		 * 1 - to not include hidden files too in the returned tree
		 * 
		 */
		
		//Getting from the DB
		HashMap <String, ArrayList<String>> fromDataBase = DatabaseConnection.readTable(dbName, fileSystemTableName);
		ArrayList <String> fromDbList = fromDataBase.get("logical_path");
		
		// Checking if the DB is empty
		if(fromDbList.isEmpty())
		{
			resetDirectoryOverride(dbName,fileSystemTableName, homeDirectoryPath, 0);
		}
		
		Path faxFolder = Paths.get(homeDirectoryPath);
		WatchService watchService = FileSystems.getDefault().newWatchService();
		faxFolder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

		boolean valid = true;
		do {
			WatchKey watchKey = watchService.take();

			for (WatchEvent event : watchKey.pollEvents()) {
				WatchEvent.Kind kind = event.kind();
				if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
					String fileName = event.context().toString();
					System.out.println("Changes detected, created " + fileName);
					
					// to database as created
					if(!isUnixHiddenFile(fileName))
					{
						String type = determineTypeOfFile(homeDirectoryPath+File.separator+fileName);
					    ArrayList<String> columnsList = new ArrayList<String>(), valuesList = new ArrayList<String>();
					    columnsList = DatabaseConnection.getColumnNames(dbName, fileSystemTableName);
					    valuesList = DatabaseConnection.makeValuesList(getUniqueId(homeDirectoryPath+File.separator+fileName), homeDirectoryPath+File.separator+fileName, type, "CURDATE()", "NOW()");
					    DatabaseConnection.insertIntoTable(dbName, fileSystemTableName, columnsList, valuesList);
					    columnsList = null;
					    valuesList = null;
						System.out.println("System Updated");
					}
					

				}
				else if(StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind()))
				{
					String fileName = event.context().toString();
					System.out.println("Changes detected, deleted " + fileName);
					
					// to remove from database as deleted
					if(!isUnixHiddenFile(fileName))
					{
						String type = determineTypeOfFile(homeDirectoryPath+File.separator+fileName);
					    ArrayList<String> columnsList = new ArrayList<String>(), valuesList = new ArrayList<String>();
					    columnsList.add("logical_path");
					    valuesList.add(homeDirectoryPath+File.separator+fileName);
					    DatabaseConnection.deleteFromTable(dbName, fileSystemTableName, columnsList, valuesList);
					    columnsList = null;
					    valuesList = null;
						System.out.println("System Updated");
					}
					

				}
				/*
				 * else if(StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) { String
				 * fileName = event.context().toString();
				 * System.out.println("Changes detected, modified " + fileName);
				 * 
				 * 
				 * // to remove from database as deleted if(!isUnixHiddenFile(fileName)) {
				 * String type = determineTypeOfFile(homeDirectoryPath+File.separator+fileName);
				 * ArrayList<String> columnsList = new ArrayList<String>(), valuesList = new
				 * ArrayList<String>(); columnsList.add("logical_path");
				 * valuesList.add(homeDirectoryPath+File.separator+fileName);
				 * DatabaseConnection.deleteFromTable(dbName, fileSystemTableName, columnsList,
				 * valuesList); columnsList = null; valuesList = null;
				 * 
				 * 
				 * // to database as created type =
				 * determineTypeOfFile(homeDirectoryPath+File.separator+fileName); columnsList =
				 * new ArrayList<String>(); valuesList = new ArrayList<String>(); columnsList =
				 * DatabaseConnection.getColumnNames(dbName, fileSystemTableName); valuesList =
				 * DatabaseConnection.makeValuesList(getUniqueId(homeDirectoryPath+File.
				 * separator+fileName), homeDirectoryPath+File.separator+fileName, type,
				 * "CURDATE()", "NOW()"); DatabaseConnection.insertIntoTable(dbName,
				 * fileSystemTableName, columnsList, valuesList); columnsList = null; valuesList
				 * = null; System.out.println("System Updated"); }
				 * 
				 * }
				 */
			}
			valid = watchKey.reset();
			
		} while (valid);
	}
	
	
	
	public static void main(String args[]) 
	{
		//System.out.println(getUniqueId("/Applications/XAMPP/xamppfiles/htdocs/shop_website/home/catalog/images/women/women2.png"));
		//start();
		//System.out.print(determineTypeOfFile("C:\\Users\\Saurav\\Downloads\\kisaraa\\home\\catalog\\images\\women\\women3.png"));
	}
	
	public static void start()
	{
		System.out.println("Initialised file system for directory : "+homeAddressDir.getAbsolutePath());
    	
    	Thread fileSystemThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					initialiseMediaScanner(DatabaseConnection.dbName, DatabaseConnection.fileSystemTableName, homeAddressDir.getAbsolutePath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
    	fileSystemThread.start();
	}
	public void init(){ 
		start();
	}
}
