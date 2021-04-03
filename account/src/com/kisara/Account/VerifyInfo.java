package com.kisara.Account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kisara.shopManager.DatabaseConnection;

public class VerifyInfo extends HttpServlet {
	
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
			
				if(jsonObject.has("chosen_username"))
				{
					String username = jsonObject.getString("chosen_username");
					ArrayList<String> usernameFromDb = DatabaseConnection.getSingleTableDataMapWithValuePredicate(DatabaseConnection.userDbName, DatabaseConnection.userTable, "username", "username", username);
					if(usernameFromDb.size()>0 &&  usernameFromDb.get(0).equals(username))
					{
						out.put("username", username);
						out.put("response", "username already present");
						out.put("allowed", "false");
					}
					else
					{
						out.put("username", username);
						out.put("allowed", "true");
					}
				}
				else if(jsonObject.has("chosen_mobile"))
				{
					String mobile = jsonObject.getString("chosen_mobile");
					ArrayList<String> mobileFromDb = DatabaseConnection.getSingleTableDataMapWithValuePredicate(DatabaseConnection.userDbName, DatabaseConnection.userTable, "phone_number", "phone_number", mobile);
					if(mobileFromDb.size()>0 &&  mobileFromDb.get(0).equals(mobile))
					{
						out.put("mobile", mobile);
						out.put("response", "mobile number already registered");
						out.put("allowed", "false");
					}
					else
					{
						out.put("mobile", mobile);
						out.put("allowed", "true");
					}
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
