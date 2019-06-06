package com.skaas.webapp;

import java.io.IOException;
import java.io.PrintWriter;

import java.security.InvalidKeyException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.azure.storage.blob.BlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.ListBlobsOptions;
import com.microsoft.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentResponse;

import com.skaas.core.AppConfig;
import com.datastax.driver.core.utils.UUIDs;
import com.skaas.core.CassandraConnector;


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
		String user_id = AppConfig.getUserId(request.getCookies());
		if(user_id != null){  
			String query1 = "DELETE FROM contacts WHERE user_id=" + user_id + ";";
	    	String query2 = "DELETE FROM users WHERE id=" + user_id + ";";

	    	CassandraConnector cassandra = new CassandraConnector();
	        cassandra.execute(query1);
	        cassandra.execute(query2);
	        cassandra.close();

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
			Cookie cookie = new Cookie("token", null);
        	cookie.setMaxAge(0); 
        	response.addCookie(cookie);
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
			String query = "INSERT INTO users (id, name, email, password) VALUES ( " + UUIDs.timeBased() + ", '" + name + "', '" + email + "', '" + password + "');";

			CassandraConnector cassandra = new CassandraConnector();
	        cassandra.execute(query);
	        cassandra.close();
		} else {
			out.println("The parameters 'name', 'email' and/or 'password' is not found in the request");
		}
	}

}
