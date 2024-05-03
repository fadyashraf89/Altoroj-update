package com.ibm.security.appscan.altoromutual.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.ibm.security.appscan.Log4AltoroJ;
import com.ibm.security.appscan.altoromutual.util.DBUtil;
import com.ibm.security.appscan.altoromutual.util.ServletUtil;

public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public LoginServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Log out
		try {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.removeAttribute(ServletUtil.SESSION_ATTR_USER);
			}
		} catch (Exception e) {
			// Do nothing
		} finally {
			response.sendRedirect("index.jsp");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Log in
		HttpSession session = request.getSession(true);

		String username = null;

		try {
			username = request.getParameter("uid");
			if (username != null) {
				username = username.trim().toLowerCase();
			}

			String password = request.getParameter("passw");
			password = password.trim().toLowerCase(); // In real life, passwords are usually case sensitive

			if (!DBUtil.isValidUser(username, password)) {
				Log4AltoroJ.getInstance().logError("Login failed >>> User: " + username + " >>> Password: " + password);
				throw new Exception("Login Failed: We're sorry, but this username or password was not found in our system. Please try again.");
			}
		} catch (Exception ex) {
			request.getSession(true).setAttribute("loginError", ex.getLocalizedMessage());
			response.sendRedirect("login.jsp");
			return;
		}

		// Handle the cookie using ServletUtil.establishSession(String)
		try {
			Cookie accountCookie = ServletUtil.establishSession(username, session);
			// Add the 'HttpOnly' and 'Secure' flags to the cookie
			accountCookie.setHttpOnly(true);
			if ("https".equalsIgnoreCase(request.getScheme())) {
				accountCookie.setSecure(true);
			}
			response.addCookie(accountCookie);
			response.sendRedirect(request.getContextPath() + "/bank/main.jsp");
		} catch (Exception ex) {
			ex.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
