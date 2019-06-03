package com.skaas.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.skaas.core.AppConfig;
import com.skaas.core.MySQLConnector;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Servlet implementation class Loginservlet
 */
public class Loginservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Loginservlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String user_id = AppConfig.getUserId(request.getCookies()); 
        if(user_id != null){  
        	if (request.getParameter("logout") != null) {        		
        		Cookie cookie = new Cookie("token", null);
	        	cookie.setMaxAge(0); 
	        	response.addCookie(cookie);
        	}
        	response.sendRedirect("index.jsp");
        } else {
        	out.println("You're not logged in");
        }  
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
        String email = request.getParameter("email");  
        String password = request.getParameter("password");
        String actualPassword = null;
        String user_id = null;

        String query = "SELECT * FROM users WHERE `email`='" + email + "'";
        try {
			MySQLConnector mysql = new MySQLConnector();
			ResultSet resultset = mysql.executeQuery(query);
			if (resultset.next()){
				user_id = resultset.getString(1);
				actualPassword = resultset.getString(4);
			} else {
				out.print("<h1>User Not Found!</h1>");
				out.close();
			}			
		} catch (ClassNotFoundException | SQLException e) {
			out.print("<h1>DB Error!</h1>");
			out.print(e.getMessage());
			out.close();
			e.printStackTrace();
		}
		System.out.println(email + " (" + password + " == " + actualPassword  + ") =" + password.equals(actualPassword));
        
        if (actualPassword != null) {
	        if(password.equals(actualPassword)){  
	        	String jwt = Jwts.builder()
	        			  .setSubject(user_id.toString())
	        			  .setExpiration(new Date(System.currentTimeMillis() + 3600 * 10 * 1000))
	        			  .claim("email", email)
	        			  .signWith(SignatureAlgorithm.HS256, "secret".getBytes("UTF-8"))
	        			  .compact();
	        	
	        	Cookie cookie = new Cookie("token", jwt);
	        	cookie.setMaxAge(60 * 60 * 10); 
	        	response.addCookie(cookie);
	        	
		        response.sendRedirect("index.jsp");
	        }  
	        else{  
	            out.print("<h1>Wrong Password!</h1>"); 
	        }   
        }
	}

}
