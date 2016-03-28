package main.controller;

import java.io.IOException;
import java.net.InetAddress;
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

import main.util.UserAuthenticate;


public class LoginServlet extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		//1. Get the user name & password with domain name
		String usrId = req.getParameter("uid");
		String password = req.getParameter("psw");
		String domain=req.getParameter("domain");
		
		//2. Validate the credentials
		UserAuthenticate util = new UserAuthenticate();
		boolean isValid = util.validate(usrId, password, domain);
		
		if(isValid) //if the user is valid, start a session.
		{
				HttpSession session = req.getSession(true); //starting a session.
	            session.setAttribute("userID", usrId);
	            
	            //setting flag according to the user's privileges.
	            int flag=util.flag;
	    		session.setAttribute("flag", flag);
				
	    		resp.sendRedirect("home");
	    		//End of Authentication
		}
		else //invalid user
		{
			req.setAttribute("errMsg", "In-Valid User Name / Password");
			RequestDispatcher dispatcher = req.getRequestDispatcher("loginErr");
			dispatcher.forward(req, resp);
		}
	} //end of getting & verifying login credentials
}
