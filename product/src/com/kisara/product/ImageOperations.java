package com.kisara.product;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kisara.shopManager.DatabaseConnection;



public class ImageOperations {
		
	public static JSONArray insertImages(JSONArray outArray)
	{
		try
		{
			for(int i = 0; i<outArray.length(); i++)
			{
				JSONObject temp = outArray.getJSONObject(i);
				String pid = temp.getString("pid");
				JSONArray imageArr = ImageOperations.readImageTable(pid);
				if(imageArr != null)
				{
					temp.put("images", imageArr);
					outArray.put(i, temp);
				}
				
			}
			return outArray;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean isAlpha(String name) 
	{
	    char[] chars = name.toCharArray();

	    for (char c : chars) {
	        if(!Character.isLetter(c)) {
	            return false;
	        }
	    }

	    return true;
	}
	
	public static JSONArray readImageTable(String pid)
	{
		try
		{
			String tempString = "";
			JSONArray temp = new JSONArray();
			if(!isAlpha(pid))
			{
				String imageTableName = DatabaseConnection.generateTableName("image", pid);
				HashMap<String, ArrayList<String>> tempMap = DatabaseConnection.readTable(DatabaseConnection.imageDb, imageTableName);
				ArrayList<String> tempList= tempMap.get("images");
				
				if(tempList != null)
				{
					for(String val : tempList)
					{
						temp.put(val);
					}
				}
				return temp;
					
			}
			else
			{
				return null;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static String createImageTable(String pid)
	{
		if(!pid.isEmpty())
		{
			String imageTableName = DatabaseConnection.generateTableName("image", pid);
			Boolean flag = DatabaseConnection.tableExist(DatabaseConnection.imageDb, imageTableName);
			if(!flag)
			{
				flag = DatabaseConnection.duplicateTable(DatabaseConnection.imageDb, DatabaseConnection.imageDb, DatabaseConnection.imageTableTemplate, imageTableName);
				if(flag)
					return imageTableName;
				else
					return null;
			}
			else
				return imageTableName;
			
		}
		else
			return null;
	}
	
	static Boolean insertImageIdToTable(String pid, JSONObject imageArrayJSON)
	{
		JSONArray imageArray = null;
		try {
			imageArray = imageArrayJSON.getJSONArray("images");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		if(imageArray.length() > 0 && imageArray.length() <= DatabaseConnection.numberOfImages)
		{
			String imageTableName = DatabaseConnection.generateTableName("image", pid);
			Boolean flag = DatabaseConnection.tableExist(DatabaseConnection.imageDb, imageTableName);
			if(flag)
			{
				//truncate
				flag = DatabaseConnection.truncateTable(DatabaseConnection.imageDb, imageTableName);
				if(flag)
				{
					//insert
					try {
						JSONObject toDbObject = new JSONObject();
						toDbObject.put("images", imageArray);
						HashMap toDb = DatabaseConnection.jsonToHashMapList(toDbObject);
						if(toDb != null)
						{
							flag = DatabaseConnection.insertIntoTableHashMap(DatabaseConnection.imageDb, imageTableName, toDb);
							if(flag)
								return true;
							else
								return false;
						}
						else
							return false;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
				}
				else
					return false;
				
			}
			else
			{
				// create table
				String imagetableName = createImageTable(pid);
				if(imagetableName != null)
				{
					//insert
					try {
						JSONObject toDbObject = new JSONObject();
						toDbObject.put("images", imageArray);
						HashMap toDb = DatabaseConnection.jsonToHashMapList(toDbObject);
						if(toDb != null)
						{
							flag = DatabaseConnection.insertIntoTableHashMap(DatabaseConnection.imageDb, imageTableName, toDb);
							if(flag)
								return true;
							else
								return false;
						}
						else
							return false;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
				}
				else
					return false;
			}
		}
		else
		{
			return false;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
