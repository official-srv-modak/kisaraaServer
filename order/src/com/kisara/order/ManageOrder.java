package com.kisara.order;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.kisara.Account.AccountManager;
import com.kisara.shopManager.DatabaseConnection;
import com.mysql.fabric.xmlrpc.base.Data;


public class ManageOrder extends HttpServlet {
	
	public static JSONObject readOrderHistory(JSONObject reqObject)
	{
		try
		{
			String cid = reqObject.getString("cid"), historyTableName = getOrderHistoryTableName(cid);
			if(!historyTableName.isEmpty())
			{
				JSONObject out = new JSONObject(DatabaseConnection.readTable(DatabaseConnection.orderHistoryDb, historyTableName));
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
	
	public static JSONObject readOrderHistorySingle(JSONObject reqObject)
	{
		try
		{
			String oid = reqObject.getString("oid");
			String cid = reqObject.getString("cid"), historyTableName = getOrderHistoryTableName(cid);
			JSONObject out = new JSONObject(DatabaseConnection.readTablePredicateDynamic(DatabaseConnection.orderHistoryDb, historyTableName, "oid", oid));
			return out;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static JSONObject readOrder(JSONObject reqObject)
	{
		try
		{
			String oid = reqObject.getString("oid");
			JSONObject out = new JSONObject(DatabaseConnection.readTablePredicateDynamic(DatabaseConnection.orderDb, DatabaseConnection.orderTable, "oid", oid));
			return out;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static JSONObject cancelOrder(JSONObject reqObject)
	{
		try
		{
			// do arithematic operation on product table
			// do accounting
			JSONObject out = new JSONObject();
			String cid = reqObject.getString("cid"), oid = reqObject.getString("oid"), cancelledReason = reqObject.getString("cancelled_reason");
			String historyTableName = getOrderHistoryTableName(cid);
			Boolean flag = DatabaseConnection.updateTable(DatabaseConnection.orderDb, DatabaseConnection.orderTable, "order_satisfied", "cancelled", "oid", oid);
			if(!flag)
				return null;
			flag = DatabaseConnection.updateTable(DatabaseConnection.orderDb, DatabaseConnection.orderTable, "cancelled_reason", cancelledReason, "oid", oid);
			if(!flag)
				return null;
			
			
			//history
			flag = DatabaseConnection.updateTable(DatabaseConnection.orderHistoryDb, historyTableName, "order_satisfied", "cancelled", "oid", oid);
			if(!flag)
				return null;
			
			flag = DatabaseConnection.updateTable(DatabaseConnection.orderHistoryDb, historyTableName, "cancelled_reason", cancelledReason, "oid", oid);
			if(!flag)
				return null;
			
			out.put("order_response", "updated");
			return out;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	public static String getOrderHistoryTableName(String cid)
	{
		String sql = "", tableName = DatabaseConnection.generateHistoryTableName("order_info", cid);
		try
		{
			sql = "use order_db_history";
			Statement s=DatabaseConnection.db.createStatement();
			s.executeUpdate(sql);
			sql = "show tables like \""+tableName+"\"";
			ResultSet r=s.executeQuery(sql);
			
			String tempResult = "";
			while(r.next())
			{
				tempResult = r.getString("Tables_in_order_db_history ("+tableName+")");
			}
			return tempResult;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static Boolean checkQuantityAvailabilty(String pid, int quantityAsked)
	{
		String quantityString = DatabaseConnection.getSingleTableDataMapWithValuePredicate(DatabaseConnection.dbName, DatabaseConnection.productTableName, "quantity", "pid", pid).get(0);
		int quantity = Integer.parseInt(quantityString);
		
		if(quantityAsked > quantity)
			return false;
		else
			return true;
		
	}
	public static JSONObject modifyOrder(JSONObject reqObject)
	{
		try
		{
			JSONObject out = new JSONObject();
			String parameter = reqObject.getString("parameter");
			String oid = reqObject.getString("oid"), cid = reqObject.getString("cid"), pid = reqObject.getString("pid");
			
			
			if(parameter.equalsIgnoreCase("quantity"))
			{
				// check product table for availability
				// do arithmatic operaion on tha product table
				// do accounting
				String newQuantity =  reqObject.getString("quantity"), historyTableName = getOrderHistoryTableName(cid);
				if(checkQuantityAvailabilty(pid, Integer.parseInt(newQuantity)))
				{
					Boolean flag = DatabaseConnection.updateTable(DatabaseConnection.orderDb, DatabaseConnection.orderTable, "quantity", newQuantity, "oid", oid);
					if(!flag)
						return null;
					flag = DatabaseConnection.updateTable(DatabaseConnection.orderHistoryDb, historyTableName, "quantity", newQuantity, "oid", oid);
					if(!flag)
						return null;
					out.put("order_response", "updated");
				}
				else
				{
					out.put("order_response", "failed");
					out.put("reason", "quantity not available");
				}
				
			}
			else if(parameter.equalsIgnoreCase("customer_address"))
			{
				String newAddress =  reqObject.getString("customer_address"), historyTableName = getOrderHistoryTableName(cid);
				Boolean flag = DatabaseConnection.updateTable(DatabaseConnection.orderDb, DatabaseConnection.orderTable, "customer_address", newAddress, "oid", oid);
				if(!flag)
					return null;
				flag = DatabaseConnection.updateTable(DatabaseConnection.orderHistoryDb, historyTableName, "customer_address", newAddress, "oid", oid);
				if(!flag)
					return null;
				out.put("order_response", "updated");
			}
			
			return out;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static JSONObject placeOrder(JSONObject reqObject)
	{
		try
		{
			JSONObject out = new JSONObject();
			HashMap<String, String> orderDet = new HashMap<String, String>();
			HashMap<String, String> orderFromDb = new HashMap<String, String>();
			
			Iterator<String> it = reqObject.keys();
			while(it.hasNext())
			{
				String key = it.next();
				orderDet.put(key, reqObject.getString(key));
			}
			orderDet.remove("session_id");
			orderDet.remove("operation");
			Boolean flag = DatabaseConnection.insertIntoTableHashMapSingle(DatabaseConnection.orderDb, DatabaseConnection.orderTable, orderDet);
			if(flag)
			{
				String cid = reqObject.getString("cid"), sid = reqObject.getString("sid");
				
				String dbName = DatabaseConnection.orderDb, tableName = DatabaseConnection.orderTable, sql = "";
				sql = "select * from "+dbName+"."+tableName+" where cid =\""+cid+"\" and sid = \""+sid+"\" order by date_of_order asc, time_of_order asc";
				
				orderFromDb = DatabaseConnection.executeSelectQuerySingleOutput(dbName, tableName, sql);
				if(!orderFromDb.isEmpty())
				{
					
					flag = DatabaseConnection.insertIntoHistoryTable(dbName, tableName, DatabaseConnection.orderHistoryDb, cid, orderFromDb);
					if(flag)
					{
						///// subtract quantity from product table
						// do accounting
						out.put("order_response", "placed");
						JSONObject orderFromDbJson = new JSONObject(orderFromDb);
						out.put("order_details", orderFromDbJson);
					}
					else
					{
						out.put("order_response", "failed");
						out.put("reason", "Technical error");
					}
				}
				else
				{
					out.put("order_response", "failed");
					out.put("reason", "Technical error");
				}
			}
			else
			{
				out.put("order_response", "failed");
				out.put("reason", "Technical error");
			}
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
		
		JSONObject out = new JSONObject(), reqObject = new JSONObject();
		
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
		
		try {
			reqObject = new JSONObject(jb.toString());
			
			out = AccountManager.sessionLogin(reqObject);
			if(out != null)	// session established
			{
				out = new JSONObject();
				String operation = reqObject.getString("operation");
				if(operation.equalsIgnoreCase("create"))
				{
					/// create order
					out = placeOrder(reqObject);
				}
				else if(operation.equalsIgnoreCase("modify"))
				{
					/// modify order
					out = modifyOrder(reqObject);
				}
				else if (operation.equalsIgnoreCase("delete"))
				{
					/// delete order
					out = cancelOrder(reqObject);
				}
				else if (operation.equalsIgnoreCase("read_order"))
				{
					/// delete order
					out = readOrder(reqObject);
				}
				else if (operation.equalsIgnoreCase("read_order_history"))
				{
					/// delete order
					out = readOrderHistory(reqObject);
				}
				else if (operation.equalsIgnoreCase("read_order_history_single"))
				{
					/// delete order
					out = readOrderHistorySingle(reqObject);
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
