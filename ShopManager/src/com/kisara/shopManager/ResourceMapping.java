package com.kisara.shopManager;

import java.io.File;
import java.nio.file.Files;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

public class ResourceMapping {

	public static Boolean chechResourceMappingIntegrity(ArrayList<String> fileSystemListDb, ArrayList<String> resourceMappingList)
	{
		if(fileSystemListDb.size() != resourceMappingList.size())
			return false;
		if(!fileSystemListDb.equals(resourceMappingList))
			return false;
		return true;
	}
	
	
	
	/*public static Boolean doResourceMapping()
	{
		String sql = "";
		try
		{
			sql="insert into "+DatabaseConnection.dbName+"."+DatabaseConnection.resourceMappingTable+"(path, current_id) select logical_path, id from "+DatabaseConnection.dbName+"."+DatabaseConnection.resourceMappingTable;
			//System.out.println(sql);
			Statement s=DatabaseConnection.db.createStatement();
			s.executeUpdate(sql);
			return true;
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println(sql);
			return false;
		}
		
		ArrayList<String> imageList = DatabaseConnection.getImagesList(DatabaseConnection.dbName, DatabaseConnection.productTableName);
		for(String image : imageList)
		{
			for(int i = 0; i<DatabaseConnection.numberOfImages; i++)
			{
				try
				{
					//sql = "insert into "+DatabaseConnection.dbName+"."+DatabaseConnection.resourceMappingTable+"(pid) select pid from "+DatabaseConnection.dbName+"."+DatabaseConnection.productTableName+" where image_id"+i+" = "
					//System.out.println(sql);
					Statement s=DatabaseConnection.db.createStatement();
					s.executeUpdate(sql);
					return true;
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
					System.out.println(sql);
					return false;
				}
			}
		}
		
	}
	public static Boolean buildResourceMappingTable()
	{
		ArrayList<String> fileSystemListDb = new ArrayList<String>();
		ArrayList<String> resourceMappingList = new ArrayList<String>();
		fileSystemListDb = DatabaseConnection.getSingleTableDataMap(DatabaseConnection.dbName, DatabaseConnection.fileSystemTableName, "logical_path");
		resourceMappingList = DatabaseConnection.getSingleTableDataMap(DatabaseConnection.dbName, DatabaseConnection.resourceMappingTable, "path");
		if(!chechResourceMappingIntegrity(fileSystemListDb, fileSystemListDb))
		{
			if(!resourceMappingList.isEmpty())	// assuming whatever is there is not corrupted data
			{
				DatabaseConnection.copyTableWithoutCreatingDestinationTable(DatabaseConnection.dbName, DatabaseConnection.resourceMappingTable, DatabaseConnection.resourceMappingTableOld);
				DatabaseConnection.truncateTable(DatabaseConnection.dbName, DatabaseConnection.resourceMappingTable);
				
			}
			else
			{
				
			}
		}
		else
		{
			
		}
		return null;
	}*/
	
	public static void createResourceMappingTable()
	{
		String homeAddress = FileSystem.homeAddressDir.getAbsolutePath();
		ArrayList<String> fileSystemList = new ArrayList<String>();
		ArrayList<String> fileSystemListDb = new ArrayList<String>();
		ArrayList<String> resourceMappingList = new ArrayList<String>();
		fileSystemList = FileSystem.scanWholeDirectory(homeAddress, 1);
		fileSystemListDb = DatabaseConnection.getSingleTableDataMap(DatabaseConnection.dbName, DatabaseConnection.fileSystemTableName, "logical_path");
		resourceMappingList = DatabaseConnection.getSingleTableDataMap(DatabaseConnection.dbName, DatabaseConnection.resourceMappingTable, "path");
		
		
		
		// If resource mapping doesnt match FS, build resource mapping again
		
		
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
