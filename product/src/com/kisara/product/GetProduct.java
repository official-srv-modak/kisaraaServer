package com.kisara.product;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kisara.Account.AccountManager;
import com.kisara.shopManager.DatabaseConnection;

/*
 *  AUTHOR - SOURAV MODAK
 *  
 *  This is the class where a servlet is created to to serve as api to display the product
 *  
 *  Created on 13th March 2021
 *   
 *  Add modification history below.
 */

public class GetProduct extends HttpServlet{
	

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
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
				if(outArray.length()==0 && jb.toString() != null)
			    {
				    
				    if(jsonObject.get("pid") instanceof JSONArray)
				    {
				    	JSONArray pidArray = new JSONArray(jsonObject.get("pid").toString());
					    
					    for(int i = 0; i<pidArray.length(); i++)
						{
							String tempPid = (String) pidArray.get(i);
							if(!tempPid.equalsIgnoreCase("all"))
								outArray = DatabaseConnection.getTableDataPredicateJSON(DatabaseConnection.dbName, DatabaseConnection.productTableName, "pid", tempPid);
							else
								outArray = DatabaseConnection.getTableDataJSON(DatabaseConnection.dbName, DatabaseConnection.productTableName);
							
						}
					    
				    }
				    else
				    {
				    	String tempPid = jsonObject.get("pid").toString();
				    	if(!tempPid.equalsIgnoreCase("all"))
							outArray = DatabaseConnection.getTableDataPredicateJSON(DatabaseConnection.dbName, DatabaseConnection.productTableName, "pid", tempPid);
						else
							outArray = DatabaseConnection.getTableDataJSON(DatabaseConnection.dbName, DatabaseConnection.productTableName);
				    }
			    }
			    
				outArray = ImageOperations.insertImages(outArray);
			    if(outArray.length() != 0)
			    	out.put("cards", outArray);
			    	
			    
			 } 
			}
		catch (JSONException e) {
		    // crash and burn
			  System.out.println(jb.toString());
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
