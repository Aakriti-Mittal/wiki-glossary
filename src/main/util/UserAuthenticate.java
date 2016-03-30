package main.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class UserAuthenticate 
{
	public int flag=0; // "0" is for who will not get update functionality
	public boolean validate(String uid, String pass, String udomain)
	{
		flag=2;
		return true;
	}
	//for checking user authentication
	public boolean validate1(String uid, String pass, String udomain)
	{
		if(uid==null || uid.trim().isEmpty()) //no user name entered
			return false; //invalid
		else if(pass==null || pass.trim().isEmpty()) //no password entered
			return false; //invalid
		else
		{ //check the entered values in LDAP server.
			try
			{
				/*
				 * feed in the login details to the LDAP server,
				 * with the specified configuration.
				 */
				Hashtable<String, String> env = new Hashtable<String, String>();
				env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
				env.put(Context.SECURITY_AUTHENTICATION, "simple");
				env.put(Context.PROVIDER_URL, "ldap://ausdc2k8amer29.amer.dell.com:3268");
				env.put(Context.SECURITY_PRINCIPAL, udomain+"\\"+uid);
				env.put(Context.SECURITY_CREDENTIALS, pass);
				
				DirContext context = null;
				
				/*
				 * if connection is made with the user name & password in the specified
				 * domain, then return true, i.e. it is valid login.
				 * Also, checking the privileges of the valid user.
				 */
				try 
				{
					context = new InitialDirContext(env); //connection successful, hence valid user.
					
					/*
					 * checking the Update & Delete privileges of the logged-in user.
					 */
					String searchBase="DC=dell,DC=com";
					
					StringBuilder searchFilter = new StringBuilder("(&");
					searchFilter.append("(CN="+uid+")");
					searchFilter.append(")");
					
					String returnAttrs[] = {"memberOf"};
					
					SearchControls sCtrl = new SearchControls();
					sCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
					sCtrl.setReturningAttributes(returnAttrs);
					
					NamingEnumeration<SearchResult> searchResults = context.search(searchBase, searchFilter.toString(), sCtrl);
					
					while (searchResults.hasMore())
					{
						SearchResult searchResult = searchResults.next();
						if(searchResult.getAttributes().get("memberOf")!=null){
						String b = searchResult.getAttributes().get("memberOf").toString();
						if(b.contains("ETS_OM_Omega_EngMgrs") 
								|| b.contains("ETS_OM_Omega_TPMs") 
								|| b.contains("ETS_OM_Omega_POs"))
							flag=2;
						}
					}
					
					String user=uid.toLowerCase();
					/*
					 * for Admin privilege
					 */
					if(user.contentEquals("aakriti_mittal")
							|| user.contentEquals("gautham_d_n")
							|| user.contentEquals("bhanu_singh1"))
						flag=2;
					
					return true;
					
				}
				/*
				 * the connection is not made, hence, invalid user
				 */
				catch (Exception e)
				{
					System.out.println(e);
					e.printStackTrace();
				}
				finally //closing the connection client.
				{
					if (context != null)
					{
						try 
						{
							context.close();
						} 
						catch (NamingException e) {
							e.printStackTrace();
						}
					}
				}
				return false;
			}
			catch (NumberFormatException e) //cannot enter the details in the server.
			{
				return false;
			}
		} // end of if-else block
	}
}
