package com.kisara.search;

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

import com.kisara.Account.AccountManager;
import com.kisara.product.ImageOperations;
import com.kisara.shopManager.DatabaseConnection;

public class SearchManager extends HttpServlet {

	public static JSONObject searchSeller(JSONObject reqObject) {
		try
		{
			JSONObject out = new JSONObject();
		    JSONArray outArray = new JSONArray();
		    
		    String username = reqObject.getString("username");
			outArray = DatabaseConnection.searchDb(DatabaseConnection.dbName, DatabaseConnection.sellerTable, "username", username);
			for(int i = 0; i < outArray.length(); i++)
			{
				JSONObject temp = outArray.getJSONObject(i);
				if(temp.has("images"))
					temp.remove("images");
				
			}
			out.put("cards", outArray);
		    return out;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static JSONObject searchProduct(JSONObject reqObject)
	{
		try
		{
			JSONObject out = new JSONObject();
		    JSONArray outArray = new JSONArray();
		    
		    String query = reqObject.getString("query");
			outArray = DatabaseConnection.searchDb(DatabaseConnection.dbName, DatabaseConnection.productTableName, "name", query);
			outArray = ImageOperations.insertImages(outArray);
			out.put("cards", outArray);
		    return out;
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

		
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
		    BufferedReader reader = req.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } 
		catch (Exception e) 
		{ /*report an error*/ }
		
		try {
			JSONObject jsonObject = new JSONObject(jb.toString());
			
			out = AccountManager.sessionLogin(jsonObject);
			if(out != null)	// session established
			{
				if(outArray.length()==0 && jb.toString() != null)
			    {
					String parameter = jsonObject.getString("parameter");
					if(parameter.equalsIgnoreCase("product"))
						out = searchProduct(jsonObject);
					else if(parameter.equalsIgnoreCase("seller"))
						out = searchSeller(jsonObject);
			    }
				
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
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
