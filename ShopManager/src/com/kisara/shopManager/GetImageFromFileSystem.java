package com.kisara.shopManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/*
 *  AUTHOR - SOURAV MODAK
 *  
 *  This is the servlet which will present the image in the outstream 
 *  
 *  Created on 9th March 2021
 *  
 *  Add modification history below.
 *  
 */

@SuppressWarnings("serial")
public class GetImageFromFileSystem extends HttpServlet {

	public void doGet(HttpServletRequest request,HttpServletResponse response)  
            throws IOException  
   {  
		 
		   ServletOutputStream out;  
		   out = response.getOutputStream();  
		   String id = request.getParameter("id");
		   if(id != null && !id.isEmpty())
		   {
			   String path = FileSystem.getPathOfImage(id);
			   String extension = DatabaseConnection.getSingleTableDataMapWithValuePredicate(DatabaseConnection.dbName, DatabaseConnection.fileSystemTableName, "type", "id", id).get(0);
			   String fileType = FileSystem.getFileType(extension);
			   if(fileType != null)
			   {
				   String contentType = fileType+"/"+extension;
				   response.setContentType(contentType); 
				   FileInputStream fin = new FileInputStream(path);  
				     
				   BufferedInputStream bin = new BufferedInputStream(fin);  
				   BufferedOutputStream bout = new BufferedOutputStream(out);  
				   int ch =0; ;  
				   while((ch=bin.read())!=-1)  
				   {  
				   bout.write(ch);  
				   }  
				     
				   bin.close();  
				   fin.close();  
				   bout.close();  
				   out.close();
			   }
		   }
		   
   }  
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
