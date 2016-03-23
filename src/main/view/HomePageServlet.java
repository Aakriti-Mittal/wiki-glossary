package main.view;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@SuppressWarnings("serial")
public class HomePageServlet extends HttpServlet 
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException 
	{
		
		
		
		HttpSession session = req.getSession(false);
		if(session == null)
		{
			//Invalid Session; 
			//Generate Login Page with Error Info 
			req.setAttribute("errMsg", "In-Valid Session !!! Please Login ...");
			RequestDispatcher dispatcher = req.getRequestDispatcher("loginErr");
			dispatcher.forward(req, resp);
		}else{
			//Valid Session; 
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		
		RequestDispatcher header = req.getRequestDispatcher("Header.html"); 
		header.include(req, resp);
		            
		String name= req.getParameter("q");
		out.println("<p class='add-result'>"+name+"</p>");
		out.println("<p class='user_privilege' style='display-none'>"+session.getAttribute("flag")+"</p>");
		
		RequestDispatcher main = req.getRequestDispatcher("default.html");
		main.include(req, resp);


		RequestDispatcher footer = req.getRequestDispatcher("Footer.html");
		footer.include(req, resp);
		}
	}//End of doPost
}//End of Class
