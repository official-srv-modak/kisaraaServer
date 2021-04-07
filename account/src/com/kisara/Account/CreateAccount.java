package com.kisara.Account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kisara.shopManager.DatabaseConnection;

public class CreateAccount extends HttpServlet{

	static String createUser(JSONObject jsonObject, String privilages, String usertype)
	{
		try
		{
			String username = jsonObject.getString("username"), password = jsonObject.getString("password");
			jsonObject.put("usertype", usertype);
			jsonObject.put("privilages", privilages);
			HashMap<String, ArrayList<String>> toDb = new HashMap<String, ArrayList<String>>(), temp = new HashMap<String, ArrayList<String>>();
			password = AccountManager.getMd5(password);
			jsonObject.remove("password");
			jsonObject.put("password", password);
			toDb = DatabaseConnection.jsonToHashMapList(jsonObject);
			///
			temp = DatabaseConnection.readTableUserWithoutPassword(DatabaseConnection.userDbName, DatabaseConnection.userTable, username);
			if(temp == null || temp.isEmpty())
			{
				Boolean flag = DatabaseConnection.insertIntoTableHashMap(DatabaseConnection.userDbName, DatabaseConnection.userTable, toDb);
				if(flag)
				{
					toDb.clear();
					toDb = DatabaseConnection.readTableUserWithoutPassword(DatabaseConnection.userDbName, DatabaseConnection.userTable, username);
					return toDb.get("uid").get(0);
				}
				else
				{
					return null;
				}
			}
			else
			{
				return temp.get("uid").get(0);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	static String createCustomerAccount(JSONObject jsonObject)
	{
		try
		{
			String uid = createUser(jsonObject, "user", "customer");
			HashMap<String, ArrayList<String>> toDb = new HashMap<String, ArrayList<String>>(), temp = new HashMap<String, ArrayList<String>>();
			String username = jsonObject.getString("username");
			jsonObject.remove("password");
			jsonObject.remove("privilages");
			jsonObject.remove("usertype");
			jsonObject.put("uid", uid);
			jsonObject.put("order_history_table", DatabaseConnection.generateHistoryTableName(DatabaseConnection.orderTable, uid));
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
			LocalDateTime now = LocalDateTime.now();  
			String joined = dtf.format(now);
			jsonObject.put("joined", joined);
			jsonObject.put("activity_status", "active");
			toDb = DatabaseConnection.jsonToHashMapList(jsonObject);
			temp = DatabaseConnection.readTableUserWithoutPassword(DatabaseConnection.userDbName, DatabaseConnection.customerAccount, username);
			if(temp == null || temp.isEmpty())
			{
				Boolean flag = DatabaseConnection.insertIntoTableHashMap(DatabaseConnection.userDbName, DatabaseConnection.customerAccount, toDb);
				if(flag)
				{
					return "user created";
				}
				else
					return "technical error, could not create the user";
			}
			else
				return "user already present";
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
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
			String flag = createCustomerAccount(jsonObject);
			if(flag != null && !flag.equals("technical error, could not create the user"))
			{
				out.put("allowed", "true");
				out.put("response", flag);
			}
			else
			{
				out.put("allowed", "false");
				out.put("response", "technical error, could not create the user");
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
