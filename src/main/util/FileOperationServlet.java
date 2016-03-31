package main.util;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import sun.misc.BASE64Decoder;

@SuppressWarnings("serial")
public class FileOperationServlet extends HttpServlet 
{
    private String INDEX_NAME="wiki";
    private String DOC_TYPE="wiki";
    private static String cluster_name="oci";
    private static String host_name="u4vmotcdschap04.us.dell.com";
//    private static String host_name="localhost";
    
    
	//returns ES client. connects to ES cluster.
	private static Client getClient() throws UnknownHostException 
	{
		Settings settings = Settings.settingsBuilder().put("cluster.name", cluster_name).build();
		
		Client client = TransportClient.builder().settings(settings).build().
				addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host_name), 9300));
        
		return client;
    }
	
	
	@Override
	//downloading the file
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{		
		String fileName=null,topic_id_no=null;
		
		int count1=0;
		
		try
		{
			fileName = req.getParameter("file").trim(); //getting the file title to be downloaded

			//getting the ID of the document to which the file is attached.
			topic_id_no = req.getParameter("id_no").trim();
			
			final Client client = getClient(); //connect to ES cluster
			
			String operation_name = req.getParameter("operation").trim();
			
			if(operation_name.contentEquals("download"))
				{
					
				//get the specified document from ES index.
				GetRequestBuilder getRequestBuilder = client.prepareGet(INDEX_NAME, DOC_TYPE, topic_id_no);
				
				//we want the file contents.
				getRequestBuilder.setFields(new String[]{"file_content"});
				
				//execute the get request.
				GetResponse response = getRequestBuilder.execute().actionGet();
				
				//get the file contents of all files attached with the particular document.
				String name = response.getField("file_content").getValue().toString();
				
				/*
				 * getting the position(index) of the file we want in the particular document
				 */
				count1 =Integer.parseInt(req.getParameter("count1").trim());
	    		
				//storing each file content in a separate variable
		        String file_content_array[]=name.split(";");
		            
		        //decoding the file contents
		        byte[] sample=new BASE64Decoder().decodeBuffer(file_content_array[count1-1]);
			        
			    String mimeType = getServletContext().getMimeType(fileName);
			        
			    if (mimeType == null)
			    	mimeType = "application/octet-stream";
			        
			    resp.setContentType(mimeType);
			    String headerKey = "Content-Disposition";
			        
			    String headerValue = String.format("attachment; filename=\"%s\"", fileName);
			    resp.setHeader(headerKey, headerValue);
			        
			    OutputStream outStream = resp.getOutputStream();
			    outStream.write(sample, 0, sample.length);
			    outStream.close();
			    if(client!=null)client.close();
				} 
			else if(operation_name.contentEquals("delete"))
			{
				//getting the user name
				String uid="";
				HttpSession session = req.getSession(false);
				if(session!=null)
					uid=session.getAttribute("userID").toString();
				
				UpdateRequest updateRequest=null;
				GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, topic_id_no).get();
				Map<String, Object> result=response.getSource();
				//for storing values of each field to be displayed
				
				//for last_update_time
				String dt=new Date().toString();
				
				Object file_name_db = null, file_contents_db = null;
				file_name_db=result.get("file_title");
				file_contents_db=result.get("file_content");
				
            	String file_name_array[]=((String) file_name_db).split(";");
            	
            	//separating all the file contents attached with the document
            	String file_contents_array[]=((String) file_contents_db).split(";");
            	
            	String file_name="";
            	String file_contents="";
				
				/*
				 * checking if any files, newly added, are previously attached or not.
				 * Accordingly, append the file names & contents
				 */
				for(int index=0; index < file_name_array.length; index++)
				{
					if(!file_name_array[index].contentEquals(fileName))
					{
						file_name=file_name+file_name_array[index]+";";
						file_contents=file_contents+file_contents_array[index]+";";
					}
				}
				updateRequest = new UpdateRequest(INDEX_NAME, DOC_TYPE, topic_id_no).doc(jsonBuilder()
				        .startObject()
				        .field("file_title", file_name)
				        .field("file_content", file_contents)
				        .field("lastUpdatedBy", uid)
				        .field("lastUpdatedTime", dt)
				        .endObject());
				try
				{
					client.update(updateRequest).get(); //executing the update query
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally{
					if(client!=null)client.close();
					String s="open_doc?id_value="+topic_id_no;
					resp.sendRedirect(s);
				}
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}//end of function
	
    public static void writeByteArraysToFile(String fileName, byte[] content) throws IOException 
    {
        File file = new File(fileName);
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
        writer.write(content);
        writer.flush();
        writer.close();
        
    }
    
}//End of Class
