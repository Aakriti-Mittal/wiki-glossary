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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;


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
		
		if(session == null)	//Invalid Session
		{ 
			//Generate Login Page with Error Info
			req.setAttribute("errMsg", "In-Valid Session !!! Please Login ...");
			RequestDispatcher dispatcher = req.getRequestDispatcher("loginErr");
			dispatcher.forward(req, resp);
		}
		else	//Valid Session
		{
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
			Client client = getClient();
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
			String h=hyperlinks(more_desc.toString(), client);
			out.println(h);
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
			if(lastUpdatedTime!=null && lastUpdatedTime.toString().trim().length()>2)
			{
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
	
	
//	public String hyperlinks2(String d, Client client) throws UnknownHostException
//    {
//        int pos1=0,pos2=0;
//        String n="";
//        while(pos1<d.length())
//        {
//            while(pos1<d.length()&&!Character.isLetterOrDigit(d.charAt(pos1)))
//            {
//                n=n+d.substring(pos1, pos1+1);
//                pos1++;
//            }
//            if(pos1>=d.length())
//                break;
//            pos2=pos1;
//            while(pos2<d.length() && Character.isLetterOrDigit(d.charAt(pos2)))
//                pos2++;
//            String sub=d.substring(pos1, pos2);
//            SearchResponse response = client.prepareSearch("wiki").setTypes("wiki")
//	    				.setQuery(QueryBuilders.wildcardQuery("topic_title".toLowerCase(), sub.toLowerCase()))
//	    				.execute().actionGet();
//            SearchHit[] hits=response.getHits().hits();
//            if(hits.length>0)
//            {
//                SearchHit hit=hits[0];
//                String replacing="<a href='open_doc?id_value="+hit.getId()+"' target='_blank'>";
//                n=n+d.substring(pos1, pos2).replaceAll(sub, replacing+sub+"</a>");
//            }
//            else
//                n=n+d.substring(pos1, pos2);
//            if(pos2+1<d.length())
//                n=n+d.substring(pos2, pos2+1);
//            pos1=pos2+1;
//        }
//        return n;
//    }
//
//	//taking all title in array , after that hyperlink
//	// with spliting
//	public String hyperlinks1(String originalString, Client client) throws UnknownHostException
//    {	
//    	long l1,l2;
//    	l1=System.currentTimeMillis();
//    	//getting all the documents
//		SearchResponse response = client.prepareSearch(INDEX_NAME)
//				.setTypes(DOC_TYPE)
//				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//				.setQuery(QueryBuilders.wildcardQuery("topic_title", "*"))
//				.setSize(10000).execute().actionGet();
//		
//		SearchHit[] hits = response.getHits().getHits();
//
//		//displaying the title of all the documents
//		for (int i = 0; i <hits.length; i++)
//		{
//			String topic_title = (String) hits[i].getSource().get("topic_title");
//			if(originalString.contains(topic_title))
//			{
//				originalString=originalString.replaceAll("(?i)"+topic_title,"<a href='open_doc?id_value="+hits[i].getId()+"' target='_blank'>"+topic_title+"</a>" );
//			}
//		}
//        l2=System.currentTimeMillis();
//        System.out.println(l2-l1);
//        return originalString;
//    }	        
	
	
	public String hyperlinks(String originalString, Client client) throws UnknownHostException
    {	
    	//getting all the documents
		SearchResponse response = client.prepareSearch(INDEX_NAME)
				.setTypes(DOC_TYPE)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.wildcardQuery("topic_title", "*"))
				.setSize(10000).execute().actionGet();
		
		SearchHit[] hits = response.getHits().getHits();

		SearchHit tmp;
        for (int i = 0; i < hits.length-1; i++) {
        	for (int j = i+1; j < hits.length; j++) {
            if (((String) hits[i].getSource().get("topic_title")).length() < 
            ((String) hits[j].getSource().get("topic_title")).length()) {
                tmp = hits[i];
                hits[i] = hits[j];
                hits[j] = tmp;
            }
        	}
        }
        
		//displaying the title of all the documents
		for (int i = 0; i <hits.length; i++)
		{
			
			String topic_title = (String) hits[i].getSource().get("topic_title");
			if(originalString.contains(topic_title))
			{
				String topic_title_array[]=originalString.split("(?i)"+topic_title);
				int firstIndex=0,lastIndex=0;
				for(int j=0;j<topic_title_array.length-1;j++)
				{	
					lastIndex=(originalString.toLowerCase()).indexOf(topic_title.toLowerCase(), firstIndex);
					String topic_title2=originalString.substring(lastIndex, lastIndex+topic_title.length());
					firstIndex=lastIndex+topic_title.length();
					try {
							if(!Character.isLetterOrDigit(originalString.substring(lastIndex+topic_title.length(),lastIndex+topic_title.length()+1).charAt(0)))
							{
							if(!(originalString.substring(lastIndex-2,lastIndex)).contains("'>"))
								originalString=originalString.replaceAll(topic_title2+"\\b","<a href='open_doc?id_value="+hits[i].getId()+"' target='_blank'>"+topic_title2+"</a>" );
							} 
						}
					catch (Exception e) {
						originalString=originalString.replaceAll(topic_title2+"\\b","<a href='open_doc?id_value="+hits[i].getId()+"' target='_blank'>"+topic_title2+"</a>" );
					}
				}
			}
			if(originalString.contains("DMT"))
			{
			originalString=originalString.replaceAll("(?i)"+"DMT"+"\\b","<a href='open_doc?id_value="+hits[i].getId()+"' target='_blank'>"+"DMT"+"</a>" );
			}
		}
        return originalString;
    }	
}
