/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package myservice.mynamespace.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.text.AbstractDocument.Content;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.entity.ContentType;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import myservice.mynamespace.data.DBUtillocal;
import myservice.mynamespace.data.Storage;
import myservice.mynamespace.service.DemoEdmProvider;
import myservice.mynamespace.service.DemoEntityCollectionProcessor;
import myservice.mynamespace.service.DemoEntityProcessor;
import myservice.mynamespace.service.DemoPrimitiveProcessor;

public class DemoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(DemoServlet.class);

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			// client.getConfiguration().setHttpClientFactory(new
			// BasicAuthHttpClientFactory("username", "password"));

			/*
			 * ODataClient client = ODataClientFactory.getClient();
			 * client.getConfiguration().setHttpClientFactory(new
			 * BasicAuthHttpClientFactory("username", "password")); String
			 * serviceRoot; EdmMetadataRequest metadataRequest =
			 * client.getRetrieveRequestFactory().getMetadataRequest(serviceRoot
			 * ); ODataRetrieveResponse<Edm> metadataRespose =
			 * metadataRequest.execute(); Edm edm = metadataRespose.getBody();
			 */

			// To access the web service you just need to add the basic HTTP
			// authentification to the configuration as follows:

			String authHeader = (String) ((HttpServletRequest) req).getHeader("Authorization");
			// Basic cmFqdTUyNTpyYWp1c3VzaG1hYQ==

			String path = authHeader;
			// Split path into segments
			try {
			String array[] = path.split("Basic");
				// Grab the last segment
				String delimiter = "";
				/*
				 * String result = String.join(delimiter, array).trim();
				 * System.out.println(result);
				 */

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

									boolean existRnt = selectUserTable(login, req);
									if (existRnt == true) {

										String test_passwd = password;
										String test_hash = req.getAttribute("DBPwd") + "";
										boolean checkPassword = checkPassword(test_passwd, test_hash);
										if (checkPassword) {
											System.out.println("Passwords Match");
											HttpSession session = req.getSession(true);
											String result = String.join(delimiter, array).trim();
											session.setAttribute("token", result);
											System.out.println(session.getAttribute("token"));
											Storage storage = (Storage) session.getAttribute(Storage.class.getName());
											if (storage == null) {
												storage = new Storage();
												session.setAttribute(Storage.class.getName(), storage);
											}

											// create odata handler and
											// configure it
											// with EdmProvider and
											// Processor
											OData odata = OData.newInstance();
											ServiceMetadata edm = odata.createServiceMetadata(new DemoEdmProvider(),
													new ArrayList<EdmxReference>());
											ODataHttpHandler handler = odata.createHandler(edm);
											handler.register(new DemoEntityCollectionProcessor(storage));
											handler.register(new DemoEntityProcessor(storage));
											handler.register(new DemoPrimitiveProcessor(storage));

											// let the handler do the work
											handler.process(req, resp);

										} else {
											System.out.println("Passwords do not match");

											resp.setContentType("text/xml");
											PrintWriter out = resp.getWriter();
											out.print("<html><body>");
											out.print("<b>Passwords do not match</b>");
											out.print("</body></html>");

										}
									} else {
										System.out.println("user Not exsist!");
										resp.setContentType("text/html");
										PrintWriter out = resp.getWriter();
										out.print("<html><body>");
										out.print("<b>user Not exsist!</b>");
										out.print("</body></html>");

									}

									System.out.printf(login, password);
								} else {
									System.out.println("Invalid authentication token");
									resp.setContentType("text/html");
									PrintWriter out = resp.getWriter();
									out.print("<html><body>");
									out.print("<b>Invalid authentication token</b>");
									out.print("</body></html>");
								}
							} catch (UnsupportedEncodingException e) {
								e.getStackTrace();
							}
						}
					}
				}

				/*
				 * HttpServletRequest httpRequest = (HttpServletRequest)
				 * request; Enumeration<String> headerNames =
				 * httpRequest.getHeaderNames();
				 * 
				 * if (headerNames != null) { while
				 * (headerNames.hasMoreElements()) {
				 * System.out.println("Header: " +
				 * httpRequest.getHeader(headerNames.nextElement())); } }
				 */

				/*
				 * HttpSession session = req.getSession(true); Storage storage =
				 * (Storage) session.getAttribute(Storage.class.getName()); if
				 * (storage == null) { storage = new Storage();
				 * session.setAttribute(Storage.class.getName(), storage); }
				 * 
				 * // create odata handler and configure it with EdmProvider and
				 * // Processor OData odata = OData.newInstance();
				 * ServiceMetadata edm = odata.createServiceMetadata(new
				 * DemoEdmProvider(), new ArrayList<EdmxReference>());
				 * ODataHttpHandler handler = odata.createHandler(edm);
				 * handler.register(new DemoEntityCollectionProcessor(storage));
				 * handler.register(new DemoEntityProcessor(storage));
				 * handler.register(new DemoPrimitiveProcessor(storage));
				 * 
				 * // let the handler do the work handler.process(req, resp);
				 */
			} catch (Exception e) {
				resp.setContentType(HttpHeader.CONTENT_TYPE);
				resp.setStatus(HttpStatusCode.UNAUTHORIZED.getStatusCode());
			}
		} catch (RuntimeException e) {
			LOG.error("Server Error occurred in DemoServlet", e);
			throw new ServletException(e);
		}

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

	public static void main(String[] args) {
		String path = "Basic cmFqdTUyNTpyYWp1c3VzaG1hYQ==";
		// Split path into segments
		String array[] = path.split("Basic");
		// Grab the last segment
		String delimiter = "";
		String result = String.join(delimiter, array).trim();
		System.out.println(result);
	}

}
