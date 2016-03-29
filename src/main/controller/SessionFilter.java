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
	HttpSession session = ((HttpServletRequest) req).getSession(false);
	if(session == null)
	{	
		if(url.contains("assets") || url.contains(".ico"))
		{
			chain.doFilter(req, resp);
		}
		//Generate Login Page with Error Info
		else if(!url.endsWith("/Login.html") && !url.endsWith("/login") && !url.endsWith("/loginErr")){
		req.setAttribute("errMsg", "In-Valid Session !!! Please Login ...");
		RequestDispatcher dispatcher = req.getRequestDispatcher("loginErr");		
		dispatcher.forward(req, resp);
		}
		else
		{
			chain.doFilter(req, resp);
		}
	}
	else
	{
		if(url.endsWith("/loginErr"))
		{
		RequestDispatcher dispatcher = req.getRequestDispatcher("home");
		dispatcher.forward(req, resp);
		}
		else
		{
			chain.doFilter(req, resp);
		}
	}
    }  
    public void destroy() {}  
}  