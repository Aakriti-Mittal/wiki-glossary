package main.controller;

import java.io.IOException;  
import java.io.PrintWriter;  
  
import javax.servlet.*;  
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
  
public class SessionFilter implements Filter{  
  
public void init(FilterConfig arg0) throws ServletException {}  
      
public void doFilter(ServletRequest req, ServletResponse resp,  
    FilterChain chain) throws IOException, ServletException {  
	
	String url=((HttpServletRequest) req).getServletPath();
	
	resp.setContentType("text/html");
	PrintWriter out = resp.getWriter();
	HttpSession session = ((HttpServletRequest) req).getSession(false);
	if(session == null)
	{	
		//Generate Login Page with Error Info
		if(!url.endsWith("/Login.html") && !url.endsWith("/login")){
		req.setAttribute("errMsg", "In-Valid Session !!! Please Login ...");
		RequestDispatcher dispatcher = req.getRequestDispatcher("loginErr");
		dispatcher.forward(req, resp);
		return;
		}
	}
	else
	{
		if(url.endsWith("/Login.html"))
		{
		RequestDispatcher dispatcher = req.getRequestDispatcher("home");
		dispatcher.forward(req, resp);
		return;
		}
	}
	try {
		chain.doFilter(req, resp);
		return;
	} catch (Exception e) {
		// TODO Auto-generated catch block
//		e.printStackTrace();
	}
    }  
    public void destroy() {}  
}  