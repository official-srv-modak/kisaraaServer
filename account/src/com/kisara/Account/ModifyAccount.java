package com.kisara.Account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kisara.shopManager.DatabaseConnection;

public class ModifyAccount extends HttpServlet{

	static JSONObject filterKeysUser(JSONObject modJsonObj)
	{
		Iterator<String> keys = modJsonObj.keys();
		String[] allowedKeysArray = {"username", "first_name", "last_name", "phone_number", "email_id"};
		List<String> allowedKeys = Arrays.asList(allowedKeysArray);
		while(keys.hasNext())
		{
			String key = keys.next();
			if(!allowedKeys.contains(key))
			{
				modJsonObj.remove(key);
			}
		}
		return modJsonObj;
	}
	
	static JSONObject filterKeysCust(JSONObject modJsonObj)
	{
		Iterator<String> keys = modJsonObj.keys();
		String[] allowedKeysArray = {"username", "first_name", "last_name", "phone_number", "email_id", "default_address"};
		List<String> allowedKeys = Arrays.asList(allowedKeysArray);
		while(keys.hasNext())
		{
			String key = keys.next();
			if(!allowedKeys.contains(key))
			{
				modJsonObj.remove(key);
			}
		}
		return modJsonObj;
	}
	
	static Boolean modUserAccount(JSONObject jsonObject)
	{
		try
		{
			String username = jsonObject.getString("username");
			JSONObject modJsonObj = jsonObject.getJSONObject("modification");
			modJsonObj = filterKeysUser(modJsonObj);
			if(modJsonObj != null)
			{
				Iterator<String> keys = modJsonObj.keys();
				while(keys.hasNext())
				{
					String key = keys.next();
					Boolean flag = DatabaseConnection.updateTable(DatabaseConnection.userDbName, DatabaseConnection.userTable, key, modJsonObj.getString(key), "username", username);
					if(!flag)
					{
						return false;
					}
				}
				return true;
			}
			else
				return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	static Boolean modCustAccount(JSONObject jsonObject)
	{
		try
		{
			String username = jsonObject.getString("username");
			JSONObject modJsonObj = jsonObject.getJSONObject("modification");
			modJsonObj = filterKeysCust(modJsonObj);
			if(modJsonObj != null && modJsonObj.length() != 0)
			{
				Iterator<String> keys = modJsonObj.keys();
				while(keys.hasNext())
				{
					String key = keys.next();
					Boolean flag = DatabaseConnection.updateTable(DatabaseConnection.userDbName, DatabaseConnection.userTable, key, modJsonObj.getString(key), "username", username);
					if(!flag)
					{
						return false;
					}
					
					flag = DatabaseConnection.updateTable(DatabaseConnection.userDbName, DatabaseConnection.customerAccount, key, modJsonObj.getString(key), "username", username);
					if(!flag)
					{
						return false;
					}
				}
				return true;
			}
			else
				return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
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
			JSONObject temp = AccountManager.sessionLogin(jsonObject);
			String username = jsonObject.getString("username");
			if(temp != null)	// session manager
			{
				if(outArray.length()==0 && jb.toString() != null)
			    {
					temp = temp.getJSONObject("user_info");
					if(username.equals(temp.get("username")))
					{
						Boolean flag = modCustAccount(jsonObject);
						if(!flag)
							out.put("response", "technical error, could not modify the user");
						else
							out.put("response", "user modified successfully");
					}
					else
					{
						out.put("response", "invalid session/user");
					}
			    }
			}
			else
			{
				out.put("response", "invalid session/user");
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
