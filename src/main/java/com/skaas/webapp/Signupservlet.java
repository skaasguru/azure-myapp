package com.skaas.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.skaas.core.AppConfig;
import com.skaas.core.MySQLConnector;
import com.microsoft.azure.storage.blob.BlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.ListBlobsOptions;
import com.microsoft.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentResponse;

/**
 * Servlet implementation class Signupservlet
 */
public class Signupservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Signupservlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session=request.getSession(false);
		System.out.println((session != null) + "--" + (session.getAttribute("id") != null));
		if(session != null && session.getAttribute("id") != null){  
	    	String user_id = (String)session.getAttribute("id");
	    	
	    	String query1 = "DELETE FROM contacts WHERE `user_id`=" + user_id + ";";
	    	String query2 = "DELETE FROM users WHERE `id`=" + user_id + ";";
			try {
				MySQLConnector mysql = new MySQLConnector();
				mysql.execute(query1);
				mysql.execute(query2);
				mysql.close();
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

			ContainerURL containerURL;
			try {
				containerURL = AppConfig.getContainerURL();
				ContainerListBlobFlatSegmentResponse listBlobResponse = containerURL.listBlobsFlatSegment(null, new ListBlobsOptions()).blockingGet();
				if (listBlobResponse.body().segment() != null) {
					for (BlobItem blob : listBlobResponse.body().segment().blobItems()) {
						BlobURL blobURL = containerURL.createBlobURL(blob.name());
						blobURL.delete().subscribe();
					}
				}
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}
		    session.invalidate();
			response.sendRedirect("index.jsp");
		} else {
			PrintWriter out = response.getWriter();
			out.println("You're not logged in");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String  name = request.getParameter("name"),
				email = request.getParameter("email"),
				password = request.getParameter("password");
		
		if (name != null && email != null && password != null) {
			String query = "INSERT INTO users (`name`, `email`, `password`) VALUES ('" + name + "', '" + email + "', '" + password + "');";
			try {
				MySQLConnector mysql = new MySQLConnector();
				mysql.execute(query);
				mysql.close();
				response.sendRedirect("login.jsp");
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
				out.println("User may already exist. Please try again with different email ID");
			}
		} else {
			out.println("The parameters 'name', 'email' and/or 'password' is not found in the request");
		}
	}

}
