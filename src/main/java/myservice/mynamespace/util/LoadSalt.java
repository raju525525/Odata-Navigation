package myservice.mynamespace.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import myservice.mynamespace.data.DBUtillocal;

public class LoadSalt implements Filter {

	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	 RequestDispatcher requestDispatcher = null;

	  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
	   throws IOException, ServletException{
	      HttpServletRequest req = (HttpServletRequest)request;
	      HttpSession session = req.getSession();

	   // New Session so forward to login.jsp
	   if (session.isNew()){
		   response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.print("<html><body>");
			out.print("<b>Invalid authentication token</b>");
			out.print("</body></html>");
	    // requestDispatcher = request.getRequestDispatcher("/index.jsp");
	     //requestDispatcher.forward(request, response);
	  }

	  // Not a new session so continue to the requested resource
	   else{
	      filterChain.doFilter(request, response);
	  }
	}
	public void destroy() {
		// TODO Auto-generated method stub
		
	}/*

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String authHeader = (String) ((HttpServletRequest) request).getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();

				if (basic.equalsIgnoreCase("Basic")) {
					try {
						String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
						System.out.println("Credentials: " + credentials);
						int p = credentials.indexOf(":");
						if (p != -1) {
							String login = credentials.substring(0, p).trim();
							String password = credentials.substring(p + 1).trim();

							boolean existRnt = selectUserTable(login, request);
							if (existRnt == true) {

								String test_passwd = password;
								String test_hash = request.getAttribute("DBPwd") + "";
								boolean checkPassword = checkPassword(test_passwd, test_hash);
								if (checkPassword) {
									System.out.println("Passwords Match");

								} else {
									System.out.println("Passwords do not match");

								}
							} else {
								
								HttpServletResponse response1 = (HttpServletResponse) response;
							    response1.setHeader("X-Frame-Options", "domain.com");
								chain.doFilter(request, response);
								System.out.println("user Not exsist!");
							}

							System.out.printf(login, password);
						} else {
							System.out.println("Invalid authentication token");
						}
					} catch (UnsupportedEncodingException e) {
						e.getStackTrace();
					}
				}
			}
		}

		
		 * HttpServletRequest httpRequest = (HttpServletRequest) request;
		 * Enumeration<String> headerNames = httpRequest.getHeaderNames();
		 * 
		 * if (headerNames != null) { while (headerNames.hasMoreElements()) {
		 * System.out.println("Header: " +
		 * httpRequest.getHeader(headerNames.nextElement())); } }
		 

		chain.doFilter(request, response);
	}

	public void init(FilterConfig arg0) throws ServletException {

	}

	public void destroy() {

	}

	public static boolean checkPassword(String password_plaintext, String stored_hash) {
		boolean password_verified = false;

		if (null == stored_hash || !stored_hash.startsWith("$2a$"))
			throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison");

		password_verified = BCrypt.checkpw(password_plaintext, stored_hash);

		return (password_verified);
	}

	@SuppressWarnings("unused")
	private boolean selectUserTable(String userName, ServletRequest request) {

		int ID = 0;
		String password = null;
		String username = null;

		Connection connection = null;

		try {
			connection = DBUtillocal.getConnection();
			String query = "select * from user where username=" + "'" + userName + "'";
			System.out.println(query);
			Statement pstmt = connection.createStatement();
			java.sql.ResultSet rs = pstmt.executeQuery(query);
			try {
				while (rs.next()) {
					ID = rs.getInt("id");
					password = rs.getString("password");
					username = rs.getString("username");
					System.out.println(
							rs.getInt("id") + "-----" + rs.getString("password") + "-----" + rs.getString("username"));
					request.setAttribute("DBPwd", password);
					return true;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
					DBUtillocal.Close();
				} catch (Exception e) {
					System.out.println("exception in closing connection");
					e.printStackTrace();
				}
			}
		}
		return false;

	}
*/}
