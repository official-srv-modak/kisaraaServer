package com.kisara.Account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kisara.shopManager.DatabaseConnection;

/*
 *  AUTHOR - SOURAV MODAK
 *  
 *  AccountManager as of now initialises the session
 *  
 *  Created on 17th March 2021
 *  
 *  Add modification history below.
 *  
 */

public class AccountManager extends HttpServlet{
	
	
	static String accountDb = "user_info", userTableName = "all_user_info", sessionTable = "session_table";
	
	public static Boolean isAdmin(String username)
	{
		HashMap<String, ArrayList<String>> userData = DatabaseConnection.readTableUserWithoutPassword(DatabaseConnection.userDbName, DatabaseConnection.userTable, username);
		if(userData.get("usertype").get(0).equalsIgnoreCase("admin"))
		{
			return true;
		}
		else
			return false;
		
	}
	
	static String fromMd5(String input)
	{
		 try { 
			 MessageDigest md = MessageDigest.getInstance("MD5");
				
			 byte[] arr = md.digest(input.getBytes());
			 return Base64.getEncoder().encodeToString(arr);
		 }  
		  
	     catch (NoSuchAlgorithmException e) 
		 { 
	            e.printStackTrace();
	            return null;
	     } 
	}
	
	static String getMd5(String input) 
    { 
        try { 
  
            MessageDigest md = MessageDigest.getInstance("MD5"); 
  
            byte[] messageDigest = md.digest(input.getBytes()); 
            BigInteger no = new BigInteger(1, messageDigest); 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            } 
            return hashtext; 
        }  
  
        catch (NoSuchAlgorithmException e) { 
            throw new RuntimeException(e); 
        } 
    } 
	
	static HashMap<String, ArrayList<String>> getSession(String sessionId)
	{
		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
		output = DatabaseConnection.readTablePredicate(accountDb, sessionTable, "session_id", sessionId);
		return output;
	}
	
	static Boolean deleteSession(HashMap<String, ArrayList<String>> userData)
	{
		HashMap<String, ArrayList<String>> userDataFromDb = new HashMap<String, ArrayList<String>> ();
	    try {
	    	userDataFromDb = DatabaseConnection.readTablePredicate(accountDb, sessionTable, "username", userData.get("username").get(0));

	    }catch(Exception e)
	    {
	    	e.getMessage();
	    	return false;
	    }
	    
	    if(userDataFromDb.get("username").contains(userData.get("username").get(0)))
	    {
	    	ArrayList<String> tempCol = new ArrayList<String>(), tempVal = new ArrayList<String>();
	    	tempCol.add("username");
	    	tempVal = userData.get("username");
	    	DatabaseConnection.deleteFromTable(accountDb, sessionTable, tempCol, tempVal);
	    }
	    return true;
	}
	static String intialiseSession(HashMap<String, ArrayList<String>> userData)
	{
		String sessionId = null;
		if(!isEmptyHashMap(userData))
	    {
		    // we need to check if the session still exists if so delete it
		    ///// CAUTION THIS PART OF CODE NEEDS TO BE UPGRADED FOR SECURITY
		    ///// CURRENT PROCESS MAKES IT VULNARABLE
		    
			/// CHECK and delete session
		    Boolean deleteFlag = deleteSession(userData);
		    
		    if(!deleteFlag)
		    	return null;
		    
		    // initialise session
		    Date date = new Date();
 		    Long timeMilli = date.getTime();
 			ArrayList<String> idValue = new ArrayList<String>();
 			sessionId = getMd5(timeMilli.toString());
 			idValue.add(sessionId);		
		    // insert into table
		    userData.put("session_id", idValue);
		    DatabaseConnection.insertIntoTableHashMap(accountDb, sessionTable, userData);
		    
	    }
			
		return sessionId;
		
	}
	
	static HashMap<String, ArrayList<String>> createAccount(HashMap<String, ArrayList<String>> userData)
	{
		HashMap<String, ArrayList<String>> output = new HashMap<String, ArrayList<String>>();
		output = null;
		if(!isEmptyHashMap(userData))
		{
			DatabaseConnection.insertIntoTableHashMap(accountDb, userTableName, userData);
			
			String sessionId = intialiseSession(userData);
	    	userData = null;
	    	if(sessionId != null)
	    		output = getSession(sessionId);
		}
		
		return output;

	}
	
	static Boolean isEmptyHashMap(HashMap<String, ArrayList<String>> userData)
	{
		if(userData == null)
			return true;
		if(!userData.isEmpty())
		{
			Set <String>keyset = userData.keySet();
			if(!keyset.isEmpty())
			{
				for(String key : keyset)
				{
					ArrayList<String> temp = new ArrayList<String>();
					temp = userData.get(key);
					if(temp.isEmpty())
						 return true;
				}
			}
			
		}
		return false;
	}
	
	public static JSONObject login(JSONObject jsonObject)
	{
		JSONObject out = new JSONObject();
		
		try
		{
			String username = jsonObject.getString("username");
		    String password = jsonObject.getString("password");
		    
		    HashMap<String, ArrayList<String>> userData = DatabaseConnection.readTableUser(accountDb, userTableName, username, password);
		    
		    JSONObject userJson = new JSONObject();
		    
		    if(!isEmptyHashMap(userData))
		    {
		    	// initialise session
		    	
		    	String sessionId = intialiseSession(userData);
		    	userData = null;
		    	if(sessionId != null)
		    	{
		    		userData = getSession(sessionId);
		    		out.put("session_id", sessionId);
			    	userJson.put("username", userData.get("username").get(0));
			    	userJson.put("first_name", userData.get("first_name").get(0));
			    	userJson.put("last_name", userData.get("last_name").get(0));
			    	userJson.put("phone_number", userData.get("phone_number").get(0));
			    	userJson.put("email_id", userData.get("email_id").get(0));
			    	userJson.put("usertype", userData.get("usertype").get(0));
			    	userJson.put("privilages", userData.get("privilages").get(0));
			    	out.put("user_info", userJson);
		    	}
		    	else
		    		return null;
		    	
		    	return out;
		    }
		    else
		    	return null;
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static JSONObject sessionLogin(JSONObject jsonObject)
	{
		JSONObject out = new JSONObject();

	    JSONObject userJson = new JSONObject();

		try
		{
			String sessionId = jsonObject.getString("session_id");
			HashMap<String, ArrayList<String>> userData = new HashMap<String, ArrayList<String>>();
			if(sessionId != null)
				userData = getSession(sessionId);
			if(!isEmptyHashMap(userData))
			{
				out.put("session_id", sessionId);
		    	userJson.put("username", userData.get("username").get(0));
		    	userJson.put("first_name", userData.get("first_name").get(0));
		    	userJson.put("last_name", userData.get("last_name").get(0));
		    	userJson.put("phone_number", userData.get("phone_number").get(0));
		    	userJson.put("email_id", userData.get("email_id").get(0));
		    	userJson.put("usertype", userData.get("usertype").get(0));
		    	userJson.put("privilages", userData.get("privilages").get(0));
		    	out.put("user_info", userJson);
		    	return out;
			}
			else
				return null;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		JSONObject out = new JSONObject();
		
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
		    BufferedReader reader = req.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } 
		catch (Exception e) { 
			  	e.printStackTrace();
			  }
		try 
		{
			JSONObject jsonObject = new JSONObject(jb.toString());
			
			if(!jsonObject.has("session_id"))	// login process
			{
				out = login(jsonObject);
			}
			else		// not login process
			{
				out = sessionLogin(jsonObject);
			}
	    
	 	} catch (JSONException e) {
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
	
}
