package main.model;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class UserAuthenticate 
{
	public boolean validate(String uid, String pass, String udomain)
	{
		
		if(uid==null || uid.trim().isEmpty()) {
			return false;
		}else if(pass==null || pass.trim().isEmpty()){
			return false;
		}else{
			
			try 
			{
				Hashtable<String, String> env = new Hashtable<String, String>();
				env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
				env.put(Context.SECURITY_AUTHENTICATION, "simple");
				env.put(Context.PROVIDER_URL, "ldap://ausdc2k8amer29.amer.dell.com:3268");
				env.put(Context.SECURITY_PRINCIPAL, udomain+"\\"+uid);
				env.put(Context.SECURITY_CREDENTIALS, pass);
				DirContext context = null;
				String searchBase="";
				if(udomain.equals("asia-pacific"))
					searchBase="DC=apac,DC=dell,DC=com";
		        else
		        	searchBase = "DC=amer,DC=dell,DC=com";

				try 
				{
					context = new InitialDirContext(env);
					StringBuilder searchFilter = new StringBuilder("(&");
					searchFilter.append("(CN="+uid+")");
					searchFilter.append(")");
					String returnAttrs[] = {"CN"};
					SearchControls sCtrl = new SearchControls();
					sCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
					sCtrl.setReturningAttributes(returnAttrs);
					NamingEnumeration<SearchResult> searchResults = context.search(searchBase, searchFilter.toString(), sCtrl);
					while (searchResults.hasMore())
					{
						SearchResult searchResult = searchResults.next();
						String b = searchResult.getAttributes().get("CN").toString();
						System.out.println("Welcome "+b);
						return true;
					}
				}
				catch (Exception e){
					System.out.println(e);
				}
				finally {
					if (context != null)
					{
						try {
							context.close();
						} 
						catch (NamingException e) {
							e.printStackTrace();
						}
					}
				}
				return false;
			}
			catch (NumberFormatException e) {
				return false;
			}
		}//End of if-else
	}//End of validate
}//End of Class
