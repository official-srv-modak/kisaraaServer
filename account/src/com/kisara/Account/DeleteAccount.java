package com.kisara.Account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kisara.shopManager.DatabaseConnection;

public class DeleteAccount extends HttpServlet{
	
	
	static Boolean deleteUserAccount(JSONObject jsonObject)
	{
		try
		{
			String username = jsonObject.getString("username");
			Boolean flag = DatabaseConnection.deleteRowFromAtable(DatabaseConnection.userDbName, DatabaseConnection.userTable, "username", username);
			return flag;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	static Boolean deleteCustAccount(JSONObject jsonObject)
	{
		try
		{
			String username = jsonObject.getString("username"), orderHistoryTableName = "";
			orderHistoryTableName = DatabaseConnection.getSingleTableDataMapWithValuePredicate(DatabaseConnection.userDbName, DatabaseConnection.customerAccount, "order_history_table", "username", username).get(0);
			Boolean flag = DatabaseConnection.deleteRowFromAtable(DatabaseConnection.userDbName, DatabaseConnection.userTable, "username", username);
			if(flag)
			{
				flag = DatabaseConnection.deleteRowFromAtable(DatabaseConnection.userDbName, DatabaseConnection.customerAccount, "username", username);
				if(flag)
				{
					flag = DatabaseConnection.dropTable(DatabaseConnection.orderDb, orderHistoryTableName);
					return flag;
				}
				else
					return false;
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
						Boolean flag = deleteCustAccount(jsonObject);
						if(!flag)
							out.put("response", "technical error, could not delete the user");
						else
							out.put("response", "user deleted successfully");
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
