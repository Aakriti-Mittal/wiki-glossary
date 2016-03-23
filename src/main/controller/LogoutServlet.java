package main.controller;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutServlet extends HttpServlet 
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException 
	{
		HttpSession session = req.getSession(false); //validate the session.
		if(session!=null)
			session.invalidate();
		req.setAttribute("errMsg", "Successfully Logged out of the Application ...");
		RequestDispatcher dispatcher = req.getRequestDispatcher("loginErr");
		dispatcher.forward(req, resp);
	}
}
