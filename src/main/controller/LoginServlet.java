package main.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;

import main.model.UserAuthenticate;


public class LoginServlet extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest req, 
						 HttpServletResponse resp)
	throws ServletException, IOException 
	{
		//1. Get all Form Data
		String usrId = req.getParameter("uid");
		String password = req.getParameter("psw");
		String domain=req.getParameter("domain");
		//2. Validate the Form Data
		UserAuthenticate util = new UserAuthenticate();
		boolean isValid = util.validate(usrId, password, domain);
		
		if(isValid)
		{
			//3. Interact with DB to Authenticate
				/*
				 * I. Create the Session for the First Time
				 */
				HttpSession session = req.getSession(true);
				 //setting session to expiry in 1 mins
	            session.setAttribute("userID", usrId);
	            
	            Settings settings = Settings.settingsBuilder().put("cluster.name", "oci").build();
	    		Client client = TransportClient.builder().settings(settings).build().
	    				addTransportAddress(new InetSocketTransportAddress
	    						(InetAddress.getByName("localhost"), 9300));
	    		SearchResponse sr=client.prepareSearch("roles").setTypes("user")
	    				.setQuery(QueryBuilders.wildcardQuery("name", usrId)).execute().actionGet();
	    		
	    		Map<String, Object> m=sr.getHits().getHits()[0].getSource();
	    		String value=m.get("role").toString();
	    		
	    		int flag=0;
	    		if(value.contentEquals("admin"))
	    			flag=2;
	    		else if(value.contentEquals("update"))
	    			flag=1;
	    		session.setAttribute("flag", flag);
				resp.sendRedirect("home");
				
			//End of Authenticate
			
		}else{
			req.setAttribute("errMsg", "In-Valid User Name / Password");
			RequestDispatcher dispatcher = req.getRequestDispatcher("loginErr");
			dispatcher.forward(req, resp);
		}//End of Validation
		
	}//End of doPost()
}//End of Class
