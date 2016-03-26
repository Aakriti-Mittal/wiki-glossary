package main.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import sun.misc.BASE64Decoder;

@SuppressWarnings("serial")
public class DownloadFileServlet extends HttpServlet 
{
    private String INDEX_NAME="wiki";
    private String DOC_TYPE="wiki";
    private static String cluster_name="oci";
    private static String host_name="localhost"; //"u4vmotcdschap04.us.dell.com";
    
    
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
		        
	        ServletContext context = getServletContext();
		    String mimeType = context.getMimeType(fileName);
		        
		    if (mimeType == null)
		    	mimeType = "application/octet-stream";
		        
		    resp.setContentType(mimeType);
		    String headerKey = "Content-Disposition";
		        
		    String headerValue = String.format("attachment; filename=\"%s\"", fileName);
		    resp.setHeader(headerKey, headerValue);
		        
		    OutputStream outStream = resp.getOutputStream();
		    outStream.write(sample, 0, sample.length);
		    outStream.close();
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
