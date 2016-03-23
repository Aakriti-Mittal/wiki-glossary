package main.view;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginErrorServlet extends HttpServlet 
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		
		//getting the message of invalid login.
		String errMsg = (String)req.getAttribute("errMsg");
		
		//loading the login page again, since invalid user encountered.
		RequestDispatcher dispatcher = req.getRequestDispatcher("Login.html");
		dispatcher.include(req, resp);
		
		//displaying the error message on the web page
		out.println("<div style='position: absolute;top: 13px;z-index: 20;left: 10;'>" +
				"<font color='aqua' style='font-size: 20px;'>" + errMsg +
				"</font></div>");
		
	}//end of login error.
}
