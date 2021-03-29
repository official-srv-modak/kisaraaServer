package com.kisara.product;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kisara.Account.AccountManager;
import com.kisara.shopManager.DatabaseConnection;

public class InsertProduct extends HttpServlet {
	

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		JSONObject out = new JSONObject();
	    JSONArray outArray = new JSONArray();
	    //String temp = req.getParameter("pid");

		
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
		    BufferedReader reader = req.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } catch (Exception e) { /*report an error*/ }
		
		JSONObject jsonObject = null;
		try
		{
			jsonObject = new JSONObject(jb.toString());
			
		} catch (JSONException e) {
		    // crash and burn
			  System.out.println(jb.toString());
		    e.printStackTrace();
		}
		
		
		try 
		{
			if(AccountManager.sessionLogin(jsonObject) != null)	// session manager
			{
				/// now since this is an admin operation we check if the user has admin right
				if(AccountManager.isAdmin(jsonObject.getString("username")))
				{
					HashMap<String, ArrayList<String>> toDb = new HashMap<String, ArrayList<String>>();
					/// editing unnecessary headers
					jsonObject.remove("session_id");
					jsonObject.remove("username");
					jsonObject.remove("available_flag");
					jsonObject.put("available_flag", "available");
					jsonObject.put("update_date", "CURDATE()");
					jsonObject.put("update_time", "NOW()");
					String productName = jsonObject.getString("name");
					JSONArray imageArray = jsonObject.getJSONArray("images");
					jsonObject.remove("images");
				
					
					Iterator <String> keys = jsonObject.keys();
					while(keys.hasNext())
					{
						String key = keys.next();
						if(key.contains("image_id"))
							jsonObject.remove(key);
					}
					/// to db
					toDb = DatabaseConnection.jsonToHashMapList(jsonObject);
					Boolean flag = DatabaseConnection.insertIntoTableHashMap(DatabaseConnection.dbName, DatabaseConnection.productTableName, toDb);
					
					
					String pid = "", sql;
					sql = "select pid from "+DatabaseConnection.dbName+"."+DatabaseConnection.productTableName+" where name = \""+productName+"\" order by update_date, update_time asc";
					HashMap<String, String> temp = DatabaseConnection.executeSelectQuerySingleOutputSpecificColumns(DatabaseConnection.dbName, DatabaseConnection.productTableName, sql, "pid");
					pid = temp.get("pid");
					// insert image
					JSONObject tempImageArrayJson = new JSONObject();
					tempImageArrayJson.put("images", imageArray);
					flag = ImageOperations.insertImageIdToTable(pid, tempImageArrayJson);
				
					if(flag)
					{
						out.put("response", "success");
						out.put("pid", pid);
					}
					else
					{
						out.put("response", "technical error");
						DatabaseConnection.deleteRowFromAtable(DatabaseConnection.dbName, DatabaseConnection.productTableName, "pid", pid);
					}
				}
				else
				{
					out.put("response", "user doesn't have sufficient permission");
				}
			}
			else
			{
				out.put("response", "invalid session");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		try {
			if(out == null)
		    	out = new JSONObject();
			PrintWriter output = res.getWriter();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			output.print(out);
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(jb.toString());
			e.printStackTrace();
		}
	}
}
