
package main.model;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class Conn_add_new extends HttpServlet 
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
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException 
	{
		String uid="";
		HttpSession session = req.getSession(false);
		if(session!=null)
			uid=session.getAttribute("userID").toString();
		String taskTitle = req.getParameter("postVariableName").trim();
//		System.out.println(taskTitle);
		//for getting match value
		if(taskTitle.equals("gettingTheDoc"))
		{
			String search_str=req.getParameter("search_text");
			resp.setContentType("text/html");
			PrintWriter out = resp.getWriter();
			
			//fetching all the doc and putting in html format
			final Client client = getClient();
			SearchResponse response = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE)
	                .setQuery(QueryBuilders.boolQuery()
	                        .should(QueryBuilders.wildcardQuery("topic_title", "*"+search_str+"*"))
	                        .should(QueryBuilders.wildcardQuery("topic_description", "*"+search_str+"*"))
	                        .should(QueryBuilders.wildcardQuery("topic_more_description", "*"+search_str+"*"))
	                        .should(QueryBuilders.wildcardQuery("file_title","*"+search_str+"*")))
	                .execute().actionGet();

		    SearchHits searchHits = response.getHits();
		    SearchHit[] hits = searchHits.getHits();
//		    System.out.println(hits.length);
			out.println("<span id='no-of-doc' style='display:none'>"+hits.length+"</span");
		    for (int i = 0; i <hits.length; i++) {
			        SearchHit hit = hits[i];
			        Map<String, Object> result = hit.getSource();
			        int noOfColumn=result.size();
			    	Set s=result.keySet();
			    	Iterator ref=s.iterator();
				   out.println("<article id='post-1' class=' post-1 topic type-topic status-publish hentry topic-tag-basic topic-tag-suggestion'>");
				   out.println("<header class='clearfix'>");
				   out.println("<h3 class='post-title gotham-rounded-bold'>");
				   out.println("<i class='doc_update_button fa fa-pencil-square-o'></i>");
				   out.println("<span id='main-post-tile'>");
				   //getting data from doc of elasticsearch
				   Object topic_title = null,more_desc = null,short_desc = null,file_name = null;
//				   System.out.println(noOfColumn);
				   if(noOfColumn==3){
				   ref.hasNext();
				   Object key= ref.next();	
				   topic_title=result.get("topic_title");
				   
				   ref.hasNext();
				   key= ref.next();
				   more_desc=result.get("topic_more_description");
				   
				   ref.hasNext();
				   key= ref.next();
				   short_desc=result.get("topic_description");
				   }
				   else
				   {
				   
				   try {
					   
					   Object key= ref.next();
					   
					    key= ref.next();
					    topic_title=result.get("topic_title");

					   key= ref.next();	
					   more_desc=result.get("topic_more_description");
					   
					   key= ref.next();
					   short_desc=result.get("topic_description");
					   
					   key= ref.next();
					   file_name=result.get("file_title");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				   }
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
				   if(file_name!=null){
			            int occurrences = 0;
			            for(char c : ((String) file_name).toCharArray()){
			               if(c == ';'){
			                  occurrences++;
			               }
			            }
			            if(occurrences==0)
			            {
			            	out.println("<br/><a id='download-file' href='DownloadFileServlet?file="+file_name+"&&id_no="+hit.getId()+"&&count1="+1+"'>"+file_name+"</a>");
			            }
			            else{
			            	out.println("<br/>");
			            	String file_name_array[]=((String) file_name).split(";");
				            int count1=1;
				            for(String file_name_string : file_name_array)
				            {
				            out.println("<a id='download-file' href='DownloadFileServlet?file="+file_name_string+"&&id_no="+hit.getId()+"&&count1="+count1+"'>"+file_name_string+"      </a>");
				            count1++;
				            }
			            }
				   }
				   out.println("</article>");
		    }
		}
		
		//for adding new doc
		else if(taskTitle.equals("add"))
		{
			int reply = 44;
			final Client client = getClient();
			String title = req.getParameter("title");
			String short_desc = req.getParameter("short_desc");
			String long_desc = req.getParameter("long_desc");
			String file_name = req.getParameter("file_name");
			String file_contents = req.getParameter("attach_file");
			String dt=new Date().toString();
			//getting the total no of doc in the index and setting id according to that.
			SearchResponse response = client.prepareSearch(INDEX_NAME)
			        .setTypes(DOC_TYPE)
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(QueryBuilders.matchQuery("topic_title".trim().toLowerCase(), title.trim().toLowerCase()))                 // Query
			        .execute()
			        .actionGet();
		    SearchHit[] searchresponse=response.getHits().hits();
		    try{
			    searchresponse[0].getId().toString();
			    reply = 66;//"66" for already available
			    }
		    catch(ArrayIndexOutOfBoundsException ae)
		    {
		    	client.prepareIndex().setIndex(INDEX_NAME).setType(DOC_TYPE)
		    	.setSource("topic_title",title, "topic_description",short_desc, "topic_more_description", long_desc,"file_title",file_name,"file_content",file_contents, "lastUpdatedBy", uid, "lastUpdatedTime", dt).execute().actionGet();
		    }
		    finally{ 
				String s="home?q="+reply;
				resp.sendRedirect(s);
		    }
		}
		//for updating one doc
		else if(taskTitle.equals("update"))
		{
			int reply = 44;
			String title = req.getParameter("title");
			String short_desc = req.getParameter("short_desc");
			String long_desc = req.getParameter("long_desc");
			String file_name = req.getParameter("file_name");
			String file_contents = req.getParameter("attach_file");
			String dt=new Date().toString();
			final Client client = getClient();
			//gettting id of the title and updating in the respective id
			SearchResponse response = client.prepareSearch(INDEX_NAME)
			        .setTypes(DOC_TYPE)
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(QueryBuilders.matchQuery("topic_title".trim().toLowerCase(), title.trim().toLowerCase()))                 // Query
			        .execute()
			        .actionGet();
		    SearchHit[] searchresponse=response.getHits().hits();
		    try{
			    String id_value=searchresponse[0].getId().toString();
			    UpdateRequest updateRequest1=null;
			    if(file_name.length()>2){
			    	GetRequestBuilder getRequestBuilder = client.prepareGet(INDEX_NAME, DOC_TYPE, id_value);
					getRequestBuilder.setFields(new String[]{"file_title"});
					GetResponse response2 = getRequestBuilder.execute().actionGet();
					String file_name_db = response2.getField("file_title").getValue().toString();
					
					getRequestBuilder.setFields(new String[]{"file_content"});
					GetResponse response3 = getRequestBuilder.execute().actionGet();
					String file_contents_db = response3.getField("file_content").getValue().toString();
					
	            	String file_name_array[]=file_name.split(";");
	            	String file_contents_array[]=file_contents.split(";");
	            	file_name="";
					file_contents="";
					for(int index=0;index<file_name_array.length;index++)
					{
						if(!(file_name_db.toLowerCase().contains(file_name_array[index].toLowerCase())))
							{
							file_name=file_name+file_name_array[index]+";";
							file_contents=file_contents+file_contents_array[index]+";";
							}
					}
						updateRequest1 = new UpdateRequest(INDEX_NAME, DOC_TYPE, id_value).doc(jsonBuilder()
						        .startObject().field("topic_description", short_desc).field("topic_more_description", long_desc)
						        .field("file_title", file_name+file_name_db).field("file_content", file_contents+file_contents_db)
						        .field("lastUpdatedBy", uid).field("lastUpdatedTime", dt)
						        .endObject());
				}
			    else{
			    	updateRequest1 = new UpdateRequest(INDEX_NAME, DOC_TYPE, id_value).doc(jsonBuilder()
					        .startObject().field("topic_description", short_desc).field("topic_more_description", long_desc)
					        .field("lastUpdatedBy", uid).field("lastUpdatedTime", dt)
					        .endObject());
			    }
				try {
					client.update(updateRequest1).get();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    }
		    catch(ArrayIndexOutOfBoundsException ae)
		    {
		    	reply = 55;//"55" for doc is not available
		    }
		    finally{
				String s="home?q="+reply;
				resp.sendRedirect(s);
		    }

		} //end of update
		
		//for deleting a doc
	    else if (taskTitle.equals("delete"))
	    {
	    	int reply = 44;
	    	final Client client = getClient();
	    	String title = req.getParameter("title");
	      SearchResponse response = 
	        (SearchResponse)client.prepareSearch(new String[] { INDEX_NAME })
	        .setTypes(new String[] { 
	        		DOC_TYPE })
	        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	        .setQuery(QueryBuilders.matchQuery("topic_title".trim().toLowerCase(), title.trim().toLowerCase()))
	        .execute()
	        .actionGet();
	      SearchHit[] searchresponse = response.getHits().hits();
	      try {
	        String id_value = searchresponse[0].getId().toString();
	        client.prepareDelete(INDEX_NAME, DOC_TYPE, id_value).get();
	      }
	      catch (ArrayIndexOutOfBoundsException ae)
	      {
	        reply = 55;
	      }
	      finally{
				String s="home?q="+reply;
				resp.sendRedirect(s);
		    }

	    }
		//end of delete
		
		//for getting the title for autocomplete
		else if(taskTitle.equals("gettingTheTitleAutocomplete"))
		{
			resp.setContentType("text/html");
			PrintWriter out = resp.getWriter();
			//fetching all the doc and putting in html format
			final Client client = getClient();
			SearchResponse response = client.prepareSearch(INDEX_NAME)
			        .setTypes(DOC_TYPE)
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(QueryBuilders.wildcardQuery("topic_description", "*"))
			        .setSize(400)
			        .execute()
			        .actionGet(); 
		    SearchHits searchHits = response.getHits();
		    SearchHit[] hits = searchHits.getHits();
		    for (int i = 0; i <hits.length; i++) {
			        SearchHit hit = hits[i];
			        Map<String, Object> result = hit.getSource();
			        int noOfColumn=result.size();

			        Object topic_title=null;
			    	Set s=result.keySet();   
			    	Iterator ref=s.iterator();
					   if(noOfColumn==3){
						   Object key= ref.next();	
						   topic_title=result.get("topic_title");
						   
						   key= ref.next();
						   
						   key= ref.next();
						   }
						   else
						   {
						   
						   try {
							   
							   Object key= ref.next();
							   
							    key= ref.next();
							    topic_title=result.get("topic_title");

							   key= ref.next();	
							   
							   key= ref.next();
							   
							   key= ref.next();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						   }		           
				   out.println("<span class='doc-title-value'>"+topic_title+"</span>");
		    }
		}

	}//End of doPost
    public static byte[] loadFileAsBytesArray(String fileName) throws Exception {    	 
        File file = new File(fileName);
        int length = (int) file.length();
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));
        byte[] bytes = new byte[length];
        reader.read(bytes, 0, length);
        reader.close();
        return bytes;
    }  
}//End of Class