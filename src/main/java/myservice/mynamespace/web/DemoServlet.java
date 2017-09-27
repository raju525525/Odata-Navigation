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
import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

import myservice.mynamespace.data.DBUtillocal;
import myservice.mynamespace.data.OAuthToken;

public class DemoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(DemoServlet.class);

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String authHeader = (String) ((HttpServletRequest) req).getHeader("Authorization");
			if (authHeader != null) {
				OAuthToken.gettingOauthToken(authHeader, req, resp);
			} else {
				OAuthToken.notGettingOauthToken(req, resp);
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
