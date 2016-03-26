package main.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;


@SuppressWarnings("serial")
public class DocOpenPageServlet extends HttpServlet 
{
    private String INDEX_NAME="wiki";
    private String DOC_TYPE="wiki";
    private String cluster_name="oci";
    private String host_name="localhost";//"u4vmotcdschap04.us.dell.com";
    /*
     * connecting to ES cluster through a client node.
     */
	private Client getClient() throws UnknownHostException {
		Settings settings = Settings.settingsBuilder().put("cluster.name", cluster_name).build();
		
		Client client = TransportClient.builder().settings(settings).build().
				addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host_name), 9300));
		return client;
    }
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		HttpSession session = req.getSession(false); //getting the browser session.
		if(session == null)
		{
			//Invalid Session; 
			//Generate Login Page with Error Info
			req.setAttribute("errMsg", "In-Valid Session !!! Please Login ...");
			RequestDispatcher dispatcher = req.getRequestDispatcher("loginErr");
			dispatcher.forward(req, resp);
		}
		else
		{
			//Valid Session; 
			resp.setContentType("text/html");
			PrintWriter out = resp.getWriter();
			//displaying top(header) of the page
			RequestDispatcher header = req.getRequestDispatcher("Header.html"); 
			header.include(req, resp);
			out.println("<p class='user_privilege' style='display:none'>"+session.getAttribute("flag")+"</p>");
			String id_value= req.getParameter("id_value");
			//update form
			out.println("<div  class='update-div'> ");
			out.println("<i class='fa fa-times' id='update-form-close'></i>");
			out.println("<form action='Conn_add_new' class='form-signin'  method='post'>");
			out.println("<input type='text' class='doc_id_no' name='doc_id_no' value="+id_value+" style='display:none'>");
			out.println("<input type='text' class='input_title' name='title' id='title' placeholder='Title' required>");
			out.println("<input type='text' class='input_short_desc' name='short_desc' id='short_desc' placeholder='Short Desc' required>    ");
			out.println("<textarea class=' input_long_desc' name='long_desc' id='long_desc' placeholder='Long Desc' required></textarea>");
			out.println("<div class='loading-anime-image'>  ");
			out.println("<div class='sk-fading-circle'>");
			out.println("<div class='sk-circle1 sk-circle'></div>");
			out.println("<div class='sk-circle2 sk-circle'></div>");
			out.println("<div class='sk-circle3 sk-circle'></div>");
			out.println("<div class='sk-circle4 sk-circle'></div>");
			out.println("<div class='sk-circle5 sk-circle'></div>");
			out.println("<div class='sk-circle6 sk-circle'></div>");
			out.println("<div class='sk-circle7 sk-circle'></div>");
			out.println("<div class='sk-circle8 sk-circle'></div>");
			out.println("<div class='sk-circle9 sk-circle'></div>");
			out.println("<div class='sk-circle10 sk-circle'></div>");
			out.println("<div class='sk-circle11 sk-circle'></div>");
			out.println("<div class='sk-circle12 sk-circle'></div>");
			out.println("</div>");
			out.println("</div> ");
			out.println("<input type='file' class='input_file-upload' multiple>");
			out.println("<textarea name='attach_file' class='file-contents-stream' placeholder='Long Desc' ></textarea> ");
			out.println("<input type='text'  class='input_file-detail form-control placeholder-pro mainLoginInput' name='file_name' readonly='readonly'/>");     
			out.println("<button type='submit' class='update-button' name='postVariableName' value='update' class='btn'>UPDATE</button>    ");
			out.println("</form>");
			out.println("</div>");
			//search query
			final Client client = getClient();
			GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, id_value).get();
			Map<String, Object> result=response.getSource();
			out.println("<article id='dw-doc-open' class='topic'>");
			out.println("<header class='clearfix'>");
			out.println("<h3 class='post-title gotham-rounded-bold'>");
			out.println("<i class='doc_update_button fa fa-pencil-square-o'></i>");
			out.println("<span id='main-post-tile'>");
			//for storing values of each field to be displayed
			Object topic_title = null, more_desc = null, short_desc = null, file_name = null,lastUpdatedTime=null,lastUpdatedBy=null;
			topic_title=result.get("topic_title");
			more_desc=result.get("topic_more_description");
			short_desc=result.get("topic_description");
			file_name=result.get("file_title");
			lastUpdatedBy=result.get("lastUpdatedBy");
			lastUpdatedTime=result.get("lastUpdatedTime");
			//displaying the required field values
			out.println(topic_title);
			out.println("</span>");
			out.println("(<span class='less-description gotham-rounded-light'>");
			out.println(short_desc);
			out.println("");
			out.println("</span>)");
			out.println("</h3>");
			out.println("</header>");
			out.println("<p class='more-description gotham-rounded-light'>");
			out.println(more_desc);
			out.println("</p>");
			//displaying all the attached files.
			if(file_name!=null && file_name.toString().trim().length()>2)
				{
				String[] file_name_array=((String) file_name).split(";");
				out.println("<br/>");
				int count1=1;
				for(String file_name_string : file_name_array)
					{
						out.println("<a id='download-file' href='FileOperationServlet?file="+file_name_string+"&&id_no="+response.getId()+"&&count1="+count1+"&&operation=download'><span class='glyphicon glyphicon-download-alt' title='Download this file'></span></a>");
						if(session.getAttribute("flag").toString().trim().equals("2"))
						{
						out.println("<a href='FileOperationServlet?file="+file_name_string+"&&id_no="+response.getId()+"&&count1="+count1+"&&operation=delete'><span class='glyphicon glyphicon-trash' title='Delete this file'></span></a>");
						}
						out.println(count1+". "+file_name_string+"<br>");
						
						count1++;
					}
				}
			if(lastUpdatedTime!=null && lastUpdatedTime.toString().trim().length()>2){
				out.println("<p style='margin-top: 22px;float: right;font-size: 12px;'>This was last modified on "+lastUpdatedTime);
				if(lastUpdatedBy!=null && lastUpdatedBy.toString().trim().length()>2)
				out.println(" by <strong>"+lastUpdatedBy);
				out.println("</strong></p>");
			}
			out.println("</article>");
			//displaying the footer section of the page
			RequestDispatcher footer = req.getRequestDispatcher("Footer.html");
			footer.include(req, resp);
		}
		
	}
	
}
