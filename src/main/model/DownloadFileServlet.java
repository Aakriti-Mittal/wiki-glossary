package main.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.RequestDispatcher;
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
    private static Client getClient() throws UnknownHostException {
		Settings settings = Settings.settingsBuilder()
		        .put("cluster.name", "oci").build();
		Client client = TransportClient.builder().settings(settings).build().
				addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
        return client;
    }  
    private static String INDEX_NAME="wiki";
    private static String DOC_TYPE="wiki";
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException 
	{
			final Client client = getClient();
			String fileName=null,topic_id_no=null;
			int count1=0;
			try {
				fileName = req.getParameter("file").trim();
				topic_id_no = req.getParameter("id_no").trim();
				count1 =Integer.parseInt(req.getParameter("count1").trim());
			GetRequestBuilder getRequestBuilder = client.prepareGet(INDEX_NAME, DOC_TYPE, topic_id_no);
			getRequestBuilder.setFields(new String[]{"file_content"});
			GetResponse response = getRequestBuilder.execute().actionGet();
			String name = response.getField("file_content").getValue().toString();
			
			int occurrences = 0;
            for(char c : name.toCharArray()){
               if(c == ';'){
                  occurrences++;
               }
            }
            if(occurrences==0)
            {
            	byte[] sample=new BASE64Decoder().decodeBuffer(name);
		        ServletContext context = getServletContext();
		        String mimeType = context.getMimeType(fileName);
		        if (mimeType == null) {        
		            mimeType = "application/octet-stream";
		        }              
		        resp.setContentType(mimeType);
		        String headerKey = "Content-Disposition";
		        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
		        resp.setHeader(headerKey, headerValue);
		        OutputStream outStream = resp.getOutputStream();
		        outStream.write(sample, 0, sample.length);
		        outStream.close();
            }
            else{

	            String file_content_array[]=name.split(";");

	            name=file_content_array[count1-1];
	            byte[] sample=new BASE64Decoder().decodeBuffer(name);
		        ServletContext context = getServletContext();
		        String mimeType = context.getMimeType(fileName);
		        if (mimeType == null) {        
		            mimeType = "application/octet-stream";
		        }              
		        resp.setContentType(mimeType);
		        String headerKey = "Content-Disposition";
		        String headerValue = String.format("attachment; filename=\"%s\"", fileName);
		        resp.setHeader(headerKey, headerValue);
		        OutputStream outStream = resp.getOutputStream();
		        outStream.write(sample, 0, sample.length);
		        outStream.close(); 
            }
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}//End of doPost
	
    public static void writeByteArraysToFile(String fileName, byte[] content) throws IOException {
        File file = new File(fileName);
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
        writer.write(content);
        writer.flush();
        writer.close();
 
    }
}//End of Class
