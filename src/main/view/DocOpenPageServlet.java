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
    private String host_name="u4vmotcdschap04.us.dell.com";
//    private String host_name="localhost";
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
			out.println("<p id='dw-user-id' style='display:none'>"+session.getAttribute("userID")+"</p>");
			out.println("<p class='user_privilege' style='display:none'>"+session.getAttribute("flag")+"</p>");
			String id_value= req.getParameter("id_value");
			out.println("<p id='doc_id_no2' style='display:none'>");
			out.println(id_value);
			out.println("</p>");
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
			if(short_desc.toString().trim().length()>1)
			{
				out.println("(<span class='less-description gotham-rounded-light'>");
				out.println(short_desc);
				out.println("");
				out.println("</span>)");
			}
			out.println("</h3>");
			out.println("</header>");
			out.println("<pre><code class='more-description gotham-rounded-light'>");
			out.println(more_desc);
			out.println("</pre></code>");
			//displaying all the attached files.
			if(file_name!=null && file_name.toString().trim().length()>2)
				{
				String[] file_name_array=((String) file_name).split(";");
				out.println("<br/><div id='doc-file-list'>");
				int count1=1;
				for(String file_name_string : file_name_array)
					{
						out.println("<span><a class='download-file' href='FileOperationServlet?file="+file_name_string+"&&id_no="+response.getId()+"&&count1="+count1+"&&operation=download'><span class='glyphicon glyphicon-download-alt' title='Download this file'></span></a>");
						out.println(count1+". "+file_name_string+"<br></span>");
						
						count1++;
					}
				out.println("</div>");
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
			if(client!=null)client.close();
		    if(out!=null)out.close();
		}
		
	}
	
}
