
package main.util;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;


public class Conn_add_new extends HttpServlet 
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
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		//getting the user name
		String uid="";
		HttpSession session = req.getSession(false);
		if(session!=null)
			uid=session.getAttribute("userID").toString();
		
		//for task=search, update, add, or delete
		String taskTitle = req.getParameter("postVariableName").trim();

		//if task=search
		if(taskTitle.equals("gettingTheDoc"))
		{
			String search_str=req.getParameter("search_text"); //the string to be search
			resp.setContentType("text/html");
			
			try(PrintWriter out = resp.getWriter())
			{
	
				//search query
				try(Client client = getClient()) //getting the ES connection
				{
					SearchResponse response=null;
					if(search_str.contains(" ")){			//search_string if contains two words, then use this query
						response = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE)
								//search for the text in these fields only
								.setQuery(QueryBuilders.boolQuery()
										.should(QueryBuilders.matchPhrasePrefixQuery("topic_title", search_str).boost(2))
										.should(QueryBuilders.matchPhrasePrefixQuery("topic_description", search_str).boost((float) 1.5))
										.should(QueryBuilders.matchPhrasePrefixQuery("topic_more_description", search_str))
										.should(QueryBuilders.matchPhrasePrefixQuery("file_title", search_str)))
								.setSize(1000)					//return 1000 documents in the results
								.execute().actionGet();
					}
					else{									//if search_string contains only one word, then use this query
						response = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE)
								//search for the text in these fields only
								.setQuery(QueryBuilders.boolQuery()
										.should(QueryBuilders.wildcardQuery("topic_title","*"+ search_str+ "*").boost(2))
										.should(QueryBuilders.wildcardQuery("topic_description", "*"+ search_str+ "*").boost((float) 1.5))
										.should(QueryBuilders.wildcardQuery("topic_more_description", "*"+ search_str+ "*"))
										.should(QueryBuilders.wildcardQuery("file_title", "*"+ search_str+ "*")))
								.setSize(1000)					//return 1000 documents in the results
								.execute().actionGet();
					}
					
					SearchHit[] hits = response.getHits().getHits(); //getting the results in an array
					
					for (int i = 0; i <hits.length; i++) //for each search result
					{
						SearchHit hit = hits[i];
						//getting the source, without the meta-data of the document
						Map<String, Object> result = hit.getSource();
						//getting the id of the document
						String id_value=hit.getId().toString();
						//ajax call: writing to html
						out.println("<article class='topic'>" +
								"<header class='clearfix'>" +
								"<h3 class='post-title gotham-rounded-bold'>" +
								"<i class='fa fa-book'></i>" +
								"<a href='open_doc?id_value="+id_value+
								"' target='_blank'><span id='main-post-tile'>");
						
						//for storing values of each field to be displayed
						Object topic_title = null, more_desc = null, short_desc = null, file_name = null;
						
						topic_title=result.get("topic_title");
						more_desc=result.get("topic_more_description");
						short_desc=result.get("topic_description");
						file_name=result.get("file_title");
						
						//displaying the required field values
						out.println(topic_title);	//title display
						out.println("</span>");
						
						if(short_desc.toString().trim().length()>1)		//if the short_description field is not blank
						{
							out.println("(<span class='less-description gotham-rounded-light'>");
							out.println(short_desc);		//short description display
							out.println("");
							out.println("</span>)");
						}
						
						out.println("</a></h3>");
						out.println("</header>");
						out.println("<p class='more-description gotham-rounded-light'>");
						
						//cutting down the more description
						String short_more_dec=loadSubstringFromDesc(more_desc.toString(),search_str);
						
						out.println(short_more_dec);		//long description display, in parts
						out.println("</p>");
					
						//displaying all the attached files.
						if(file_name!=null && file_name.toString().trim().length()>2)
						{
							String[] file_name_array=((String) file_name).split(";");
							
							out.println("<br/>");
							
							int count1=1;
							//display each attached file
							for(String file_name_string : file_name_array)
							{
								out.println("<a class='download-file' href='FileOperationServlet?file="+file_name_string+"&&id_no="+id_value+"&&count1="+count1+"&&operation=download'>"+file_name_string+"</a>");
								count1++;
							}
						}
						
						out.println("</article>");
					}
					client.close();	//closing the ES client
				}
				out.close();	//closing the print writer stream to html
			}
		} //end of searching a document
		
		//if task=add
		else if(taskTitle.equals("add"))
		{
			int reply = 44;
			
			//getting the values from the form
			String title = req.getParameter("title");
			String short_desc = req.getParameter("short_desc");
			String long_desc = req.getParameter("long_desc");
			String file_name = req.getParameter("file_name");
			String file_contents = req.getParameter("attach_file");
			
			//for last_update_time
			String dt=new Date().toString();
			
			try(Client client = getClient())	//getting the ES connection
			{
				//searching if document already exists by the same title
				SearchResponse response = client.prepareSearch(INDEX_NAME)
						.setTypes(DOC_TYPE)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(QueryBuilders.matchPhrasePrefixQuery("topic_title", title.trim().toLowerCase()))
						.execute().actionGet();
		    
				SearchHit[] searchresponse=response.getHits().hits();
				
				try //if document already exists, do not insert
				{
					searchresponse[0].getId().toString(); //even if 1 document is present
					reply = 66; // 66 for already available
				}
				catch(ArrayIndexOutOfBoundsException ae) //if not even 1 document is present, then insert
				{
					//insertion query
					client.prepareIndex().setIndex(INDEX_NAME).setType(DOC_TYPE)
						.setSource("topic_title",title, "topic_description",short_desc, 
								"topic_more_description", long_desc,"file_title",file_name,
								"file_content",file_contents, "lastUpdatedBy", uid, 
								"lastUpdatedTime", dt)
						.execute().actionGet();
				}
				finally //sending the response of successful or unsuccessful insertion
				{ 
					client.close();	//closing the ES client
					String s="home?q="+reply;
	    			resp.sendRedirect(s);
				}
			}
		} //end of addition of a document
		
		
		//if task=update
		else if(taskTitle.equals("update"))
		{
			int reply = 44;
			
			//getting the details of the document to update
			String title = req.getParameter("title");
			String short_desc = req.getParameter("short_desc");
			String long_desc = req.getParameter("long_desc");
			String file_name = req.getParameter("file_name");
			String file_contents = req.getParameter("attach_file"); 
			String doc_id_no = req.getParameter("doc_id_no");
			String file_count_delete = req.getParameter("file_count_delete").trim();
			
			//for last_update_time
			String dt=new Date().toString();
			
			try(Client client = getClient())	//getting the ES connection
			{
				//getting the document with the ID
				GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, doc_id_no).get();
				Map<String, Object> result=response.getSource();
				
				try
				{
					UpdateRequest updateRequest=null;
					
					//if any new files are to be attached and remove
					if(file_name.length()>2 || file_count_delete.length()>0)
					{
						//getting the file names of all the associated files
						String file_name_db=result.get("file_title").toString();
					
						//getting the file contents of all the associated files
						String file_contents_db = result.get("file_content").toString();
						
						//for deleting the attached file
						if(file_count_delete.length()>0)
						{
							//splitting the counter of to be removed file
							String file_count_array[]=file_count_delete.split(";");
							
			            	String file_name_d_array[]=((String) file_name_db).split(";");
			            	
			            	//separating all the file contents attached with the document
			            	String file_contents_d_array[]=((String) file_contents_db).split(";");
							
							/*
							 * checking if any files, newly added, are previously attached or not.
							 * Accordingly, append the file names & contents
							 */
							for(int index=0; index < file_name_d_array.length; index++)
							{
								int flag=0;
								for(int i=0; i < file_count_array.length; i++)
									if(index==(Integer.parseInt(file_count_array[i])-1))
									{
										flag=1;
										break;
									}
								if(flag==0){
									file_name=file_name+file_name_d_array[index]+";";
									file_contents=file_contents+file_contents_d_array[index]+";";
								}
							}
							file_name_db = file_name;
							file_contents_db = file_contents;
						}
						//for adding the file
						else if(file_name.length()>2 )
						{
							//separating all the file names attached with the document
							String file_name_array[]=file_name.split(";");
							
							//separating all the file contents attached with the document
							String file_contents_array[]=file_contents.split(";");
						file_name="";
						file_contents="";
						/*
						 * checking if any files, newly added, are previously attached or not.
						 * Accordingly, append the file names & contents
						 */
						for(int index=0; index < file_name_array.length; index++)
						{
							if(!(file_name_db.toLowerCase().contains(file_name_array[index].toLowerCase())))
							{
								file_name=file_name+file_name_array[index]+";";
								file_contents=file_contents+file_contents_array[index]+";";
							}
						}
						
						file_name_db = file_name+file_name_db;
						file_contents_db = file_contents;
						}
						//update query
						updateRequest = new UpdateRequest(INDEX_NAME, DOC_TYPE, doc_id_no).doc(jsonBuilder()
						        .startObject()
						        .field("topic_title", title)
						        .field("topic_description", short_desc)
						        .field("topic_more_description", long_desc)
						        .field("file_title", file_name_db)
						        .field("file_content", file_contents_db)
						        .field("lastUpdatedBy", uid)
						        .field("lastUpdatedTime", dt).endObject());
					}
					else
					{
						//update query, without changes to the attached files.
						updateRequest = new UpdateRequest(INDEX_NAME, DOC_TYPE, doc_id_no).doc(jsonBuilder()
								.startObject()
								.field("topic_title", title)
								.field("topic_description", short_desc)
								.field("topic_more_description", long_desc)
								.field("lastUpdatedBy", uid).field("lastUpdatedTime", dt).endObject());
					}
					try
					{
						client.update(updateRequest).get(); //executing the update query
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				}
				catch(ArrayIndexOutOfBoundsException ae)
				{
					reply = 55; // 55 for doc is not available
				}
				finally //sending the response of successful or unsuccessful updation
				{
					client.close();	//closing the ES client
					String s="home?q="+reply;
					resp.sendRedirect(s);
				}
			}
		} //end of updating a document
		
		//if task=delete
	    else if (taskTitle.equals("delete"))
	    {
	    	int reply = 44;
	    		    	
	    	String title = req.getParameter("title"); //getting the title of the document to delete
	    	
	    	try(Client client = getClient())	//getting the ES connection
	    	{
	    		//getting the particular document
	    		SearchResponse response = client.prepareSearch(INDEX_NAME).setTypes(DOC_TYPE)
	    				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	    				.setQuery(QueryBuilders.matchQuery("topic_title", title.trim().toLowerCase()))
	    				.execute().actionGet();
	    		
	    		SearchHit[] searchresponse = response.getHits().hits();
	    		
	    		try
	    		{
	    			String id_value = searchresponse[0].getId().toString(); //getting the ID of the document
	    			client.prepareDelete(INDEX_NAME, DOC_TYPE, id_value).get(); //delete query
	    		}
	    		catch (ArrayIndexOutOfBoundsException ae)
	    		{
	    			reply = 55;
	    		}
	    		finally  //sending the response of successful or unsuccessful deletion
	    		{
	    			client.close();	//closing the ES client
	    			String s="home?q="+reply;
	    			resp.sendRedirect(s);
	    		}
	    	}
	    } //end of deleting the document
		
		//providing a list of possible titles according to the text in the box
		else if(taskTitle.equals("gettingTheTitleAutocomplete"))
		{
			resp.setContentType("text/html");
			
			try(PrintWriter out = resp.getWriter())
			{
				
				try(Client client = getClient())	//getting ES connection
				{
					//getting all the documents
					SearchResponse response = client.prepareSearch(INDEX_NAME)
							.setTypes(DOC_TYPE)
							.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
							.setQuery(QueryBuilders.wildcardQuery("topic_title", "*"))
							.setSize(4000).execute().actionGet();
					
					SearchHit[] hits = response.getHits().getHits();
					
					//displaying the title of all the documents
					for (int i = 0; i <hits.length; i++)
					{
						Object topic_title = hits[i].getSource().get("topic_title");
						out.println("<span style='display:none' class='doc-title-value'>"+topic_title+"</span>");
					}
					
					SearchHit tmp;
			        SimpleDateFormat f = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
			        try{
			        for (int i = 0; i < hits.length-1; i++) {
			        	for (int j = i+1; j < hits.length; j++) {
			            if (f.parse(((String) hits[i].getSource().get("lastUpdatedTime"))).getTime() < 
			            		f.parse(((String) hits[j].getSource().get("lastUpdatedTime"))).getTime()) {
			                tmp = hits[i];
			                hits[i] = hits[j];
			                hits[j] = tmp;
			            }
			        	}
			        }
			        }
			        catch(Exception e){
			        	e.printStackTrace();
			        }
			        out.println("<div class='recentEvents'>");
			        out.println("<h4>Recent Event</h4>");
			        out.println("<ul>");
			        for(int i = 0; i < 10; i++)
			        {
			        	try {
							out.println("<li>"+(i+1)+". "+"<a href='open_doc?id_value="+hits[i].getId()+"' target='_blank'>"+"<strong>"+
									hits[i].getSource().get("topic_title")+"</strong> </a>modified by "+
									hits[i].getSource().get("lastUpdatedBy")+" on "+
											new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy").parse(hits[i].getSource().get("lastUpdatedTime").toString()))+"</li>");
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
			        out.println("</ul>");
			        out.println("</div>");

			        
			        out.println("<div class='recentFiles'>");
			        out.println("<h4>Recent Files</h4>");
			        out.println("<ul>");
			        int count_file=1;
			        for(int i = 0; i <  hits.length-1; i++)
			        {
			        	String file_title=hits[i].getSource().get("file_title").toString();
			        	if(file_title.length()>2)
			        		if(count_file<10)
			        		{
			        			String file_title_array[]=file_title.split(";");
			        			for(int j=0;j<file_title_array.length;j++)
			        			{
				        			try {
										out.println("<li>"+(count_file)+". "+"<a href='FileOperationServlet?file="+file_title_array[j]+
												"&&id_no="+hits[i].getId()+"&&count1="+(j+1)+"&&operation=download'>"+"<strong>"+file_title_array[j]+
												"</strong></a> Added by "+hits[i].getSource().get("lastUpdatedBy")+" on "+
												new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy").parse(hits[i].getSource().get("lastUpdatedTime").toString()))+"</li>");
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
				        			count_file++;
			        			}
			        		}
			        }
			        out.println("</ul>");
			        out.println("</div>");

					client.close();
				}
				
				out.close();
			}
		} //end of the Auto-Complete
	}//End of doPost
    
	//reading the file contents
	public static byte[] loadFileAsBytesArray(String fileName) throws Exception
	{
		File file = new File(fileName);
        int length = (int) file.length();
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));
        byte[] bytes = new byte[length];
        reader.read(bytes, 0, length);
        reader.close();
        return bytes;
    }
	
	public static String loadSubstringFromDesc(String originText, String search_string)
	{
		String finalString="";
		String str1[]=originText.split("\\.");
		int j=0,flag=0,flag2=0;
		try 
		{
		for(int i=0;i<str1.length;i++){
			if(j<4){
				if(str1[i].toLowerCase().contains(search_string.toLowerCase()))
				{
					flag=1;
					flag2=0;
					finalString=finalString+str1[i]+".";
					j++;
				}
				else if(flag2==0){
					flag2=1;
					finalString=finalString+" ...";
				}
			}
		}
		if(flag==0)
		{
			j=0;
			for(int i=0;i<str1.length;i=i+2){
				if(j<4){
						flag=1;
						finalString=finalString+str1[i]+". ...";
						j++;
				}
			}
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return finalString;
	}

}//End of Class
