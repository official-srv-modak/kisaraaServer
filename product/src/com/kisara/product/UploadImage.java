package com.kisara.product;
import java.io.*;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.output.*;
import org.json.JSONException;
import org.json.JSONObject;

import com.kisara.Account.AccountManager;
import com.kisara.shopManager.DatabaseConnection;
import com.kisara.shopManager.FileSystem;

public class UploadImage extends HttpServlet {
   
   private boolean isMultipart;
   private String filePath;
   private long maxFileSize = 50000000 * 1024;
   private int maxMemSize = 4 * 1024;
   private File file ;
	public static String resourceHomeAddress = FileSystem.getProperty(FileSystem.propertyFilePath, "File.System.Home", 1).get("File.System.Home").toString();
	
   static String getImageIdFromFileSystem(String filePath)
   {
	   String out = null;
	   out = DatabaseConnection.readTablePredicate(DatabaseConnection.dbName, DatabaseConnection.fileSystemTableName, "logical_path", filePath).get("id").get(0);
	   return out;
   }

   public void init( ){
      // Get the file location where it would be stored.
      filePath = resourceHomeAddress+File.separator+"images"+File.separator;
   }
   
   public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {
   

	   /*CloseableHttpClient httpClient = HttpClients.createDefault();
	   HttpPost uploadFile = new HttpPost("...");
	   MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	   builder.addTextBody("field1", "yes", ContentType.TEXT_PLAIN);

	   // This attaches the file to the POST:
	   File f = new File(request.getParameter("file_name"));
	   builder.addBinaryBody(
	       "file",
	       new FileInputStream(f),
	       ContentType.APPLICATION_OCTET_STREAM,
	       f.getName()
	   );

	   HttpEntity multipart = builder.build();
	   uploadFile.setEntity(multipart);*/
	   
	   
      // Check that we have a file upload request
     // isMultipart = ServletFileUpload.isMultipartContent(multipart);
     // if(isMultipart)
      //{
    	  JSONObject out = new JSONObject();
    	  
          DiskFileItemFactory factory = new DiskFileItemFactory();
       
          // maximum size that will be stored in memory
          factory.setSizeThreshold(maxMemSize);
       
          // Location to save data that is larger than maxMemSize.
          factory.setRepository(new File(resourceHomeAddress+File.separator+"temp"));

          // Create a new file upload handler
          ServletFileUpload upload = new ServletFileUpload(factory);
       
          // maximum file size to be uploaded.
          
          upload.setSizeMax( maxFileSize );

          try { 
             // Parse the request to get file items.
             List fileItems = upload.parseRequest(request);
    	
             // Process the uploaded file items
             Iterator i = fileItems.iterator();
             String session = "", username = "", fileName = "";
             FileItem finalItem = null;
             while ( i.hasNext () ) {
                FileItem fi = (FileItem)i.next();
                if(fi.isFormField())
                {
                	if(fi.getFieldName().equals("session"))
                	{
                		session = fi.getString();
             
                	}
                	if(fi.getFieldName().equals("username"))
                	{
                		username = fi.getString();
                	}
                }
                
                if ( !fi.isFormField () ) {
                   // Get the uploaded file parameters
                   fileName = fi.getName();
                   finalItem = fi;
                
                   // Write the file
                   if( fileName.lastIndexOf(File.separator) >= 0 ) {
                      file = new File( filePath + fileName.substring( fileName.lastIndexOf(File.separator))) ;
                   } else {
                      file = new File( filePath + fileName.substring(fileName.lastIndexOf(File.separator)+1)) ;
                   }
                   
                }
             }
             JSONObject jsonObject = new JSONObject();
             jsonObject.put("session_id", session);
     		if(AccountManager.sessionLogin(jsonObject) != null)	// session manager
     		{
     			if(AccountManager.isAdmin(username))
     			{
     				if(finalItem != null)
     				{
     					finalItem.write( file ) ;
     					
     					//Insert into file system table
     					String type = FileSystem.determineTypeOfFile(file.getAbsolutePath());
					    ArrayList<String> columnsList = new ArrayList<String>(), valuesList = new ArrayList<String>();
     					columnsList = DatabaseConnection.getColumnNames(DatabaseConnection.dbName, DatabaseConnection.fileSystemTableName);
					    valuesList = DatabaseConnection.makeValuesList(FileSystem.getUniqueId(file.getAbsolutePath()), file.getAbsolutePath(), type, "CURDATE()", "NOW()");
					    DatabaseConnection.insertIntoTable(DatabaseConnection.dbName, DatabaseConnection.fileSystemTableName, columnsList, valuesList);
     					
     					String id = getImageIdFromFileSystem(file.getAbsolutePath());
     					out.put("image_id", id);
                        out.put("Uploaded Filename",fileName);
     				}
     				else
     				{
     					out.put("response","Error writing file");
     				}
     				
     			}
     			else
 				{
 					out.put("response","Not admin");
 				}
     		}
     		else
				{
					out.put("response","Not a kisara user");
				}
             
             }
          catch(org.apache.commons.io.FileExistsException e1)
          {
        	  try {
				out.put("response","file with same name already present in the server");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          }
          catch(Exception ex) {
                System.out.println(ex);
                
             }
          
          	
          try {
    			if(out == null)
    		    	out = new JSONObject();
    			PrintWriter output = response.getWriter();
    			response.setContentType("application/json");
    			response.setCharacterEncoding("UTF-8");
    			output.print(out);
    			output.flush();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
      //}
      
      }
      
 }
