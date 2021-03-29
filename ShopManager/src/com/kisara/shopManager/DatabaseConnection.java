package com.kisara.shopManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/*
 *  AUTHOR - SOURAV MODAK
 *  
 *  This is an abstract class which is made to take advantage of the mysql database or any
 *  database used. As of now it is only mysql but can be extended and all the code for the 
 *  database operation shall reside here
 *  
 *  Created on 9th March 2021
 *  
 *  Add modification history below.
 *  
 *  
 */


public abstract class DatabaseConnection {
	
	public static String mysqlUserName="root";
	static String passdb="kisaraa#@574";
	public static String timeTableTrain="time_train";
	public static String timeTableFinal="time_final";
	public static String timeTableAlgo="time_algo";
	public static String dbName = "kisaraa";
	public static String userDbName = "user_info";
	public static String fileSystemTableName = "file_system";
	public static String homePageCatalogTable = "home_page_catalog";
	public static String productTableName = "product";
	public static String sellerTable = "seller";
	public static String sellerHistoryDetailsDb = "seller_history_details";
	public static String customerHistoryDetailsDb = "customer_history_detials";
	public static String resourceMappingTable = "resource_mapping_table";
	public static String resourceMappingTableOld = "resource_mapping_table_old";
	public static String accountDetailsDb = "account_details";
	public static String userTable = "all_user_info";
	public static String mysqlUrl = "jdbc:mysql://localhost:3306?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true";
	public static String orderDb = "order_db", orderTable = "order_info";
	public static String orderHistoryDb = "order_db_history";
	public static String imageDb = "image_db", imageTableTemplate = "image_table_template";
	public static int numberOfImages = 5;
	
	private static Connection connection()
	{
		try
		{
			Connection db1= DriverManager.getConnection(mysqlUrl, mysqlUserName, passdb);
			//Connection db1= DriverManager.getConnection("jdbc:mysql://localhost:3306/", userdb, passdb);
			return db1;
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println("Error in establishing connection, check if mysql server is running or not.\n\nTerminated.");
			System.exit(0);
		}
		return null;
	}
	public static Connection db=connection();
;
	public static boolean checkConnection()
	{
		try
		{
			Statement s=db.createStatement();
			String query;
			int count=0;
			query="SHOW DATABASES";
			ResultSet r=s.executeQuery(query);
			String database;
			while(r.next())
			{
				count++;
			}
			if (count!=0)
			{
				//System.out.println("Connection Established");
				return true;
			}
			else
				return false;
			
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	public static ArrayList<String> getColumnNames(String dbName, String tableName)
	{
		checkConnection();
		ArrayList<String> output = new ArrayList<String>();
		String sql = "";
		
		try {
			sql = "select column_name from information_schema.columns where table_schema = '"+dbName+"' and table_name = '"+tableName+"' order by column_name;";
			//System.out.println(sql);
			Statement s=DatabaseConnection.db.createStatement();
			ResultSet r=s.executeQuery(sql);
			while(r.next())
			{
				output.add(r.getString("column_name"));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
		}
		
		return output;
	}
	

	public static JSONArray searchDb(String dbName, String tableName, String predicateColumn, String query)
	{
		checkConnection();
		JSONArray card = new JSONArray();
	
		String sql = "";
		try {
			sql = "select * from "+dbName+"."+tableName+" where "+predicateColumn+" like \"%"+query+"%\"";
			Statement s=DatabaseConnection.db.createStatement();
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			while(r.next())
			{
				JSONObject rec = new JSONObject();
				JSONArray imageArr = new JSONArray();
				for(int i = 0; i < columnNames.size(); i++)
				{
					String columnName = columnNames.get(i).toString();
					if(columnName.startsWith("image") && r.getString(columnName) != null && !r.getString(columnName).isEmpty() && !r.getString(columnName).equals("0"))
						{
							imageArr.put(r.getString(columnName));
						}
					else
						rec.put(columnName, r.getString(columnName));
				}
				if(imageArr!=null && imageArr.length() > 0)
					rec.put("images", imageArr);
				card.put(rec);
				rec = null;
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
		}
		
		return card;
	}
	
	public static Boolean hasColumn(ResultSet r, String columnName)
	{
		
		try {
			r.getObject(columnName);
			return true;
		}
		catch(SQLException e)
		{
			return false;
		}
		
	}
	
	public static Boolean deleteFromTable(String dbName, String tableName, ArrayList<String> columnNameList, ArrayList<String> valueList)
	{
		checkConnection();
		if(columnNameList.size() != valueList.size())
		{
			System.out.println("Inconsistency in number of columns and values supplied\nOperation dropped\ncolumnNameList(Size) = "+columnNameList.size()+" valueList(Size) = "+valueList.size());
			return false;
		}
		for(int i = 0; i < columnNameList.size(); i++)
		{
			String columnName = columnNameList.get(i);
			String value = valueList.get(i);
			String sql = "";
			try
			{
				sql="delete from "+dbName+"."+tableName+" where "+columnName+" = \""+value+"\"";
				if(sql.contains("\\"))
				{
					sql = sql.replace("\\", "\\\\");
				}
				Statement s=db.createStatement();
				s.executeUpdate(sql);
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
				System.out.println(sql);
				return false;
			}
		}
		
		return true;
		
	}
	
	public static ArrayList<String>getImagesList(String dbName, String tableName)
	{
		String sql = "";
		try
		{
			sql = "select * from "+dbName+"."+tableName;
			Statement s=DatabaseConnection.db.createStatement();
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ArrayList<String> output = new ArrayList<String>();
			ResultSet r=s.executeQuery(sql);
			
			while(r.next())
			{
				for(int i = 0; i < columnNames.size(); i++)
				{
					String columnName = columnNames.get(i).toString();
					if(columnName.startsWith("image") && r.getString(columnName) != null && !r.getString(columnName).isEmpty() && !r.getString(columnName).equals("0"))
						{
							output.add(r.getString(columnName));
						}
				}
			}
			return output;
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println(sql);
			return null;
		}
	}
	
	public static Boolean redefineFileSystemTable()
	{
		String sql = "";
		try
		{
			sql="ALTER TABLE `kisaraa`.`file_system`\n"
					+ "  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1001;";
			//System.out.println(sql);
			Statement s=db.createStatement();
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
	
	public static HashMap<String, ArrayList<String>> executeSelectQueryMultipleOutput(String dbName, String tableName, String sql)
	{
		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
		try {
			Statement s=DatabaseConnection.db.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			
			for(int i = 0; i < columnNames.size(); i++)
			{
				String columnName = columnNames.get(i).toString();
				ArrayList <String> values = new ArrayList<String>();
				while(r.next())
				{
					values.add(r.getString(columnName));
				}
				output.put(columnName, values);
				r.beforeFirst();
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
			return null;

		}
		return output;
	}
	
	public static HashMap<String, String> executeSelectQuerySingleOutputSpecificColumns(String dbName, String tableName, String sql, String columns)
	{
		HashMap<String, String> output = new HashMap<String, String>();
		try {
			Statement s=DatabaseConnection.db.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
			
			String[] columnNames = columns.split(",");
			ResultSet r=s.executeQuery(sql);
			
			
			for(int i = 0; i < columnNames.length; i++)
			{
				String columnName = columnNames[i].toString();
				while(r.next())
				{
					output.put(columnName, r.getString(columnName));
				}
			
				r.beforeFirst();
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
			return null;

		}
		return output;
	}
	
	public static HashMap<String, String> executeSelectQuerySingleOutput(String dbName, String tableName, String sql)
	{
		HashMap<String, String> output = new HashMap<String, String>();
		try {
			Statement s=DatabaseConnection.db.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			
			for(int i = 0; i < columnNames.size(); i++)
			{
				String columnName = columnNames.get(i).toString();
				while(r.next())
				{
					output.put(columnName, r.getString(columnName));
				}
			
				r.beforeFirst();
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
			return null;

		}
		return output;
	}
	
	public static HashMap<String, ArrayList<String>> readTableUserWithoutPassword(String dbName, String tableName, String username)
	{
		checkConnection();
		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
		
		
		String sql = "";
		try {
			sql = "select * from "+dbName+"."+tableName+" where username = \""+username+"\"";
			Statement s=DatabaseConnection.db.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			
			for(int i = 0; i < columnNames.size(); i++)
			{
				String columnName = columnNames.get(i).toString();
				ArrayList <String> values = new ArrayList<String>();
				while(r.next())
				{
					values.add(r.getString(columnName));
				}
				output.put(columnName, values);
				r.beforeFirst();
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);

		}
		output.remove("password");
		return output;
	}
	
	public static HashMap<String, ArrayList<String>> readTableUser(String dbName, String tableName, String username, String password)
	{
		checkConnection();
		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
		
		
		String sql = "";
		try {
			sql = "select * from "+dbName+"."+tableName+" where username = \""+username+"\" and password = md5(\""+password+"\")";
			Statement s=DatabaseConnection.db.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			
			for(int i = 0; i < columnNames.size(); i++)
			{
				String columnName = columnNames.get(i).toString();
				ArrayList <String> values = new ArrayList<String>();
				while(r.next())
				{
					values.add(r.getString(columnName));
				}
				output.put(columnName, values);
				r.beforeFirst();
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);

		}
		output.remove("password");
		return output;
	}
	
	public static HashMap<String, ArrayList<String>> readTablePredicate(String dbName, String tableName, String predicateColumn, String predicateValue)
	{
		checkConnection();
		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
		
		String sql = "";
		try {
			sql = "select * from "+dbName+"."+tableName+" where "+predicateColumn+" = \""+predicateValue+"\"";
			Statement s=DatabaseConnection.db.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			for(int i = 0; i < columnNames.size(); i++)
			{
				String columnName = columnNames.get(i).toString();
				ArrayList <String> values = new ArrayList<String>();
				while(r.next())
				{
					values.add(r.getString(columnName));
				}
				output.put(columnName, values);
				
				r.beforeFirst();
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
			return null;

		}
		
		return output;
		
	}
	
	public static HashMap readTablePredicateDynamic(String dbName, String tableName, String predicateColumn, String predicateValue)
	{
		checkConnection();
		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
		int size = 0;
		HashMap<String, String> output1 = new HashMap<String, String>();
		
		String sql = "";
		try {
			sql = "select * from "+dbName+"."+tableName+" where "+predicateColumn+" = \""+predicateValue+"\"";
			Statement s=DatabaseConnection.db.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			for(int i = 0; i < columnNames.size(); i++)
			{
				String columnName = columnNames.get(i).toString();
				ArrayList <String> values = new ArrayList<String>();
				String temp = "";
				while(r.next())
				{
					temp = r.getString(columnName);
					values.add(temp);
				}
				size = values.size();
				if(size== 1)
				{
					output1.put(columnName, temp);
				}
				else
				{
					output.put(columnName, values);
				}
				r.beforeFirst();
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
			return null;

		}
		
		if(size > 1)
			return output;
		else
			return output1;
	}
	
	public static HashMap readTableDyanamic(String dbName, String tableName)
	{
		checkConnection();
		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
		int size = 0;
		HashMap<String, String> output1 = new HashMap<String, String>();
				
		String sql = "";
		try {
			sql = "select * from "+dbName+"."+tableName;
			Statement s=DatabaseConnection.db.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			for(int i = 0; i < columnNames.size(); i++)
			{
				String columnName = columnNames.get(i).toString();
				ArrayList <String> values = new ArrayList<String>();
				String temp = "";
				while(r.next())
				{
					temp = r.getString(columnName);
					values.add(temp);
				}
				size = values.size();
				if(size== 1)
				{
					output1.put(columnName, temp);
				}
				else
				{
					output.put(columnName, values);
				}
				r.beforeFirst();
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);

		}
		
		if(size > 1)
			return output;
		else
			return output1;
	}
	
	public static HashMap<String, ArrayList<String>> readTable(String dbName, String tableName, ArrayList<String>columnNameList)
	{
		checkConnection();
		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
		
		
		
		String columns = "("+getValuesCommaSeparated(columnNameList)+")";
		
		String sql = "";
		try {
			sql = "select "+columns+" from "+dbName+"."+tableName;
			Statement s=DatabaseConnection.db.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			for(int i = 0; i < columnNames.size(); i++)
			{
				String columnName = columnNames.get(i).toString();
				ArrayList <String> values = new ArrayList<String>();
				while(r.next())
				{
					values.add(r.getString(columnName));
				}
				output.put(columnName, values);
				r.beforeFirst();
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);

		}
		
		return output;
	}
	
	public static HashMap<String, ArrayList<String>> readTable(String dbName, String tableName)
	{
		checkConnection();
		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
		
		
		
		String sql = "";
		try {
			sql = "select * from "+dbName+"."+tableName;
			Statement s=DatabaseConnection.db.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, 
                    ResultSet.CONCUR_UPDATABLE);
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			
			for(int i = 0; i < columnNames.size(); i++)
			{
				String columnName = columnNames.get(i).toString();
				ArrayList <String> values = new ArrayList<String>();
				while(r.next())
				{
					values.add(r.getString(columnName));
				}
				output.put(columnName, values);
				r.beforeFirst();
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
		}
		
		return output;
	}
	
	public static ArrayList<String> getSingleTableDataMap(String dbName, String tableName, String columnName)
	{
		checkConnection();
		ArrayList<String> output = new ArrayList<String>();
		
		
		String sql = "";
		try {
			sql = "select "+columnName+" from "+dbName+"."+tableName;
			Statement s=DatabaseConnection.db.createStatement();
			ResultSet r=s.executeQuery(sql);
			
			while(r.next())
				output.add(r.getString(columnName));
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
		}
			
		return output;
	}
	
	public static ArrayList<String> getSingleTableDataMapWithValuePredicate(String dbName, String tableName, String columnName, String predicateColumn, String value)
	{
		checkConnection();
		ArrayList<String> output = new ArrayList<String>();
		
		
		String sql = "";
		try {
			sql = "select "+columnName+" from "+dbName+"."+tableName+" where "+predicateColumn+"=\""+value+"\"";
			Statement s=DatabaseConnection.db.createStatement();
			ResultSet r=s.executeQuery(sql);
			
			while(r.next())
				output.add(r.getString(columnName));
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
		}
			
		return output;
	}
	
	public static JSONArray getTableDataPredicateJSON(String dbName, String tableName, String predicateColumnName, String predicateColumnValue)
	{
		checkConnection();
		JSONObject jsonOut = new JSONObject();
		JSONObject out = new JSONObject();
		JSONArray card = new JSONArray();
	
		String sql = "";
		try {
			sql = "select * from "+dbName+"."+tableName+" where "+predicateColumnName+" = \""+predicateColumnValue+"\" order by rand()";
			Statement s=DatabaseConnection.db.createStatement();
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			while(r.next())
			{
				JSONObject rec = new JSONObject();
				JSONArray imageArr = new JSONArray();
				for(int i = 0; i < columnNames.size(); i++)
				{
					String columnName = columnNames.get(i).toString();
					/*if(columnName.startsWith("image") && r.getString(columnName) != null && !r.getString(columnName).isEmpty() && !r.getString(columnName).equals("0"))
						{
							imageArr.put(r.getString(columnName));
						}
					else*/
						rec.put(columnName, r.getString(columnName));
				}
				if(imageArr.length() > 0)
					rec.put("images", imageArr);
				card.put(rec);
				rec = null;
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
		}
		
		return card;
	}
	
	public static Boolean copyTableWithoutCreatingDestinationTable(String dbName, String sourceTable, String destinationTable)
	{
		String sql = "";
		try
		{
			sql="insert into "+dbName+"."+destinationTable+" select * from "+dbName+"."+sourceTable;
			//System.out.println(sql);
			Statement s=db.createStatement();
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
	public static JSONArray getTableDataJSON(String dbName, String tableName)
	{
		checkConnection();
		JSONArray card = new JSONArray();
	
		String sql = "";
		try {
			sql = "select * from "+dbName+"."+tableName+" order by rand()";
			Statement s=DatabaseConnection.db.createStatement();
			ArrayList<String> columnNames = getColumnNames(dbName, tableName);
			ResultSet r=s.executeQuery(sql);
			
			while(r.next())
			{
				JSONObject rec = new JSONObject();
				JSONArray imageArr = new JSONArray();
				for(int i = 0; i < columnNames.size(); i++)
				{
					String columnName = columnNames.get(i).toString();
					/*if(columnName.startsWith("image") && r.getString(columnName) != null && !r.getString(columnName).isEmpty() && !r.getString(columnName).equals("0"))
						{
							imageArr.put(r.getString(columnName));
						}
					else*/
						rec.put(columnName, r.getString(columnName));
				}
				if(imageArr.length() > 0)
					rec.put("images", imageArr);
				card.put(rec);
				rec = null;
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sql);
		}
		
		return card;
	}
	
	public static ArrayList<String> makeValuesList(String... values)
	{
		ArrayList<String> output = new ArrayList<String>();
		for(String val : values)
			output.add(val);
		return output;
	}
	
	public static String getValuesCommaSeparated(ArrayList<String> list)
	{
		String output = "";
		for(int i = 0; i < list.size(); i++)
		{
			String val = list.get(i);
			if(i != list.size() - 1)
				output += val+", ";
			else
				output += val;
		}
		return output;
	}
	
	public static String getValuesCommaSeparated(Set<String> list)
	{
		String output = "";
		int i = 0;
		for(Iterator<String> it = list.iterator(); it.hasNext(); )
		{
			String val = it.next();
			if(i != list.size() - 1)
				output += val+", ";
			else
				output += val;
			i++;
		}
		return output;
	}
	
	public static String getValuesCommaSeparatedAndApostrophes(ArrayList<String> list)
	{
		String output = "";
		for(int i = 0; i < list.size(); i++)
		{
			String val = list.get(i);
			if(val == null)
				val = "";
			if(!val.isEmpty())
			{
				if(val.contains("()"))
				{
					if(i != list.size() - 1)
						output += val+", ";
					else
						output += val;
				}
				else
				{
					if(i != list.size() - 1)
						output += "\""+val+"\", ";
					else
						output += "\""+val+"\"";
				}
			}
			else
			{
				if(i != list.size() - 1)
					output += "NULL, ";
				else
					output += "NULL";
			}
		}
		return output;
	}
	
	public static Boolean insertIntoTableHashMapSingle(String dbName, String tableName, HashMap<String, String> valuesMap)
	{
		/*
		 * Use this to insert into the database
		 */
		checkConnection();
		Set<String> columnNamesList = new HashSet<String>();
		
		columnNamesList = valuesMap.keySet();
		String columnNames = getValuesCommaSeparated(columnNamesList);
		Iterator<String> iter = columnNamesList.iterator();
		
		
		ArrayList<String> valuesList = new ArrayList<String>();
		for(Iterator<String> iter1 = columnNamesList.iterator(); iter1.hasNext();)
		{
			valuesList.add(valuesMap.get(iter1.next()));
		}
		String values = getValuesCommaSeparatedAndApostrophes(valuesList);
		
		String sql = "";
		try
		{
			sql="insert into "+dbName+"."+tableName+" ("+columnNames+") values ("+values+")";
			if(sql.contains("\\"))
			{
				sql = sql.replace("\\", "\\\\");
			}
			//System.out.println(sql);
			Statement s=db.createStatement();
			s.executeUpdate(sql);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println(sql);
			return false;
		}
			
		
		
		return true;
	}
	
	public static Boolean insertIntoTableHashMap(String dbName, String tableName, HashMap<String, ArrayList<String>> valuesMap)
	{
		/*
		 * Use this to insert into the database
		 */
		checkConnection();
		Set<String> columnNamesList = new HashSet<String>();
		
		columnNamesList = valuesMap.keySet();
		String columnNames = getValuesCommaSeparated(columnNamesList);
		Iterator<String> iter = columnNamesList.iterator();
		ArrayList <String> val = valuesMap.get(iter.next());
		
		for(int i = 0; i < val.size(); i++)
		{
			ArrayList<String> valuesList = new ArrayList<String>();
			for(Iterator<String> iter1 = columnNamesList.iterator(); iter1.hasNext();)
			{
				valuesList.add(valuesMap.get(iter1.next()).get(i));
			}
			String values = getValuesCommaSeparatedAndApostrophes(valuesList);
			
			String sql = "";
			try
			{
				sql="insert into "+dbName+"."+tableName+" ("+columnNames+") values ("+values+")";
				if(sql.contains("\\"))
				{
					sql = sql.replace("\\", "\\\\");
				}
				//System.out.println(sql);
				Statement s=db.createStatement();
				s.executeUpdate(sql);
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
				System.out.println(sql);
				return false;
			}
			
		}
		
		return true;
	}
	
	public static Boolean insertIntoTable(String dbName, String tableName, ArrayList<String> columnNamesList, ArrayList<String> valuesList)
	{
		checkConnection();
		if(columnNamesList.size() != valuesList.size())
		{
			System.out.println("Inconsistency in number of columns and values supplied\nOperation dropped\ncolumnNameList(Size) = "+columnNamesList.size()+" valueList(Size) = "+valuesList.size());
			return false;
		}
		String columnNames = getValuesCommaSeparated(columnNamesList), values = getValuesCommaSeparatedAndApostrophes(valuesList);
		String sql = "";
		try
		{
			sql="insert into "+dbName+"."+tableName+" ("+columnNames+") values ("+values+")";
			if(sql.contains("\\"))
			{
				sql = sql.replace("\\", "\\\\");
			}
			//System.out.println(sql);
			Statement s=db.createStatement();
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
	public static int countRecord(String dbName, String table)
	{
		checkConnection();
		int count=0;
		String sql = "";
		try
		{
			sql="select count(*) from "+dbName+"."+table;
			Statement s=db.createStatement();
			ResultSet r=s.executeQuery(sql);
			while(r.next())
				count=r.getInt("count(*)");
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println(sql);
			return 0;
		}
		return count;
	}
	
	public static Boolean truncateTable(String dbName, String tableName)
	{
		checkConnection();
		String sql = "";
		try
		{
			sql="delete from "+dbName+"."+tableName;
			if(sql.contains("\\"))
			{
				sql = sql.replace("\\", "\\\\");
			}
			Statement s=db.createStatement();
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
	
	public static String generateHistoryTableName(String tableName, String id)
	{
		String output = "";
		output += tableName+"_"+id;
		return output;
	}
	
	public static Boolean createHistoryTable(String fromDbName, String fromTableName, String toDbname, String id)
	{
		String historyTableName = generateHistoryTableName(fromTableName, id);
		String sql = "";
		try
		{
			sql="CREATE TABLE "+toDbname+"."+historyTableName+" LIKE "+fromDbName+"."+fromTableName;
			if(sql.contains("\\"))
			{
				sql = sql.replace("\\", "\\\\");
			}
			Statement s=db.createStatement();
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
	
	public static Boolean insertIntoHistoryTable(String fromDbName, String fromTableName, String toDbName, String id, HashMap<String, String> values)
	{
		String sql = "";
		String historyTableName = generateHistoryTableName(fromTableName, id);
		try
		{
			sql = "use "+toDbName+"\n";
			Statement s=db.createStatement();
			s.executeUpdate(sql);
			sql = "show tables like \""+historyTableName+"\"";
			ResultSet r=s.executeQuery(sql);
			
			String tempResult = "";
			while(r.next())
			{
				tempResult = r.getString("Tables_in_order_db_history ("+historyTableName+")");
			}
			if(tempResult.equalsIgnoreCase(historyTableName))	// history table already exist
			{
				Boolean flag = insertIntoTableHashMapSingle(toDbName, historyTableName, values);
				return flag;
			}
			else		// history table doesn't exist
			{
				Boolean flag = createHistoryTable(fromDbName, fromTableName, toDbName, id);
				// insert Process
				if(flag)
				{
					flag = insertIntoTableHashMapSingle(toDbName, historyTableName, values);
					return flag;
				}
				else
					return flag;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(sql);
			return false;
		}
	}
	
	public static Boolean updateTable(String dbName, String tableName, String updateColumn, String updateValue, String predicateColumn, String predicateValue)
	{
		String sql = "";
		try
		{
			sql = "update "+dbName+"."+tableName+" set "+updateColumn+" = \""+updateValue+"\" where "+predicateColumn+" = \""+predicateValue+"\"";
			Statement s=db.createStatement();
			s.executeUpdate(sql);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(sql);
			return false;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static HashMap jsonToHashMapList(JSONObject input)
	{
		HashMap output = new HashMap<>();
		Iterator<String> keys = input.keys();
		
		while(keys.hasNext())
		{
			String key = keys.next();
			Object inputValue = null;
			try {
				inputValue = input.get(key);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(inputValue instanceof JSONArray)
			{
				ArrayList<String> temp = new ArrayList<>();
				JSONArray arr = null;;
				try {
					arr = input.getJSONArray(key);
					for(int j = 0; j<arr.length(); j++)
					{
						String val = arr.getString(j);
						temp.add(val);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				output.put(key, temp);
			}
			else
			{
				ArrayList<String> temp = new ArrayList<>();
				temp.add((String) inputValue);
				try {
					output.put(key, temp);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
		return output;
	}
	
	public static Boolean duplicateTable(String sourceDbName, String targetDbName, String sourceTableName, String targetTableName)
	{
		String sql = "";
		try
		{
			sql = "CREATE TABLE "+targetDbName+"."+targetTableName+" LIKE "+sourceDbName+"."+sourceTableName;
			Statement s=db.createStatement();
			s.executeUpdate(sql);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(sql);
			return false;
		}
	}
	
	public static Boolean tableExist(String dbName, String tableName)
	{
		try
		{
			String sql = "";
			sql = "use "+dbName+"\n";
			Statement s=db.createStatement();
			s.executeUpdate(sql);
			sql = "show tables like \""+tableName+"\"";
			ResultSet r=s.executeQuery(sql);
			
			String tempResult = "";
			while(r.next())
			{
				tempResult = r.getString("Tables_in_"+dbName+" ("+tableName+")");
			}
			if(tempResult.equalsIgnoreCase(tableName))	// history table already exist
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public static Boolean deleteRowFromAtable(String dbName, String tableName, String predicateColumn, String predicateValue)
	{
		String sql = "";
		try
		{
			sql = "delete from "+dbName+"."+tableName+" where "+predicateColumn+" = \""+predicateValue+"\"";
			Statement s=db.createStatement();
			s.executeUpdate(sql);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(sql);
			return false;
		}
	}
	
	public static String generateTableName(String property, String propertyId)
	{
		return property+"_"+propertyId;
	}
	
	public static void main(String args[])
	{
		ArrayList<String> columnNamesList = new ArrayList<String>(), valuesList = new ArrayList<String>();
		columnNamesList.add("card_heading");
		columnNamesList.add("image1_url");
		columnNamesList.add("image2_url");
		columnNamesList.add("image3_url");
		columnNamesList.add("image4_url");
		
		valuesList.add("a");
		valuesList.add("a");
		valuesList.add("a");
		valuesList.add("a");
		valuesList.add("a");

		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();

		for(int i = 0; i< columnNamesList.size(); i++)
		{
			ArrayList <String> val = new ArrayList<String>();
			val.add(valuesList.get(i));
			output.put(columnNamesList.get(i), val);
		}
		System.out.println(insertIntoTableHashMap(dbName, homePageCatalogTable, output));
		//System.out.println(insertIntoTable("shop", "home_page_catalog", columnNamesList, valuesList));
	}
	
}
