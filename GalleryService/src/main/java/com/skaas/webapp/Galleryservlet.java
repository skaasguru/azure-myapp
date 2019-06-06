package com.skaas.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.skaas.core.AppConfig;
import com.microsoft.azure.storage.blob.BlobURL;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.ListBlobsOptions;
import com.microsoft.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentResponse;


public class Galleryservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static HttpClient HTTP = HttpClientBuilder.create().build();

    public Galleryservlet() {
        super();
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Gson gson = new Gson();
        Map<String,Object> body = new HashMap<String,Object>();
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		HttpGet authRequest = new HttpGet(AppConfig.authServiceEndpoint + "/token");
		authRequest.setHeader("Authorization", request.getHeader("Authorization"));
		HttpResponse authResponse = HTTP.execute(authRequest);
		String authJson = EntityUtils.toString(authResponse.getEntity());
		if (authResponse.getStatusLine().getStatusCode() != 200) {
			response.setStatus(authResponse.getStatusLine().getStatusCode());
    		out.println(authJson);
    		out.close();
    		return;
		}
		List<Map<String,Object>> images = new ArrayList<>();
		Map<String,Object> image;

		ContainerURL containerURL;
		try {
			containerURL = AppConfig.getContainerURL();
			ContainerListBlobFlatSegmentResponse listBlobResponse = containerURL.listBlobsFlatSegment(null, new ListBlobsOptions()).blockingGet();
			if (listBlobResponse.body().segment() != null) {
				for (BlobItem blob : listBlobResponse.body().segment().blobItems()) {
					image = new HashMap<String,Object>();
					image.put("key", blob.name());
					image.put("size", blob.properties().contentLength());
					image.put("url", AppConfig.getSAS(blob.name()));
					
					images.add(image);
				}
			}
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		body.put("images", images);

		out.println(gson.toJson(body));		
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder stringBuilder = new StringBuilder();
        String requestChunk;
        while ((requestChunk = request.getReader().readLine()) != null) { stringBuilder.append(requestChunk); }
        Gson gson = new Gson();
        JsonObject requestBody = gson.fromJson(stringBuilder.toString(), JsonObject.class);
        
        Map<String,Object> body = new HashMap<String,Object>();
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		HttpGet authRequest = new HttpGet(AppConfig.authServiceEndpoint + "/token");
		authRequest.setHeader("Authorization", request.getHeader("Authorization"));
		HttpResponse authResponse = HTTP.execute(authRequest);
		String authJson = EntityUtils.toString(authResponse.getEntity());
		if (authResponse.getStatusLine().getStatusCode() != 200) {
			response.setStatus(authResponse.getStatusLine().getStatusCode());
    		out.println(authJson);
    		out.close();
    		return;				
		}
		JsonObject authData = gson.fromJson(authJson, JsonObject.class);
		
		String 	user_id = authData.get("id").getAsString();
		String fileName = requestBody.get("filename").getAsString();

        String url = null;
		try {
			url = AppConfig.getSAS("images/"+user_id+"/"+fileName);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
        
		body.put("url", url);

		out.println(gson.toJson(body));	
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Gson gson = new Gson();
        Map<String,Object> body = new HashMap<String,Object>();
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		HttpGet authRequest = new HttpGet(AppConfig.authServiceEndpoint + "/token");
		authRequest.setHeader("Authorization", request.getHeader("Authorization"));
		HttpResponse authResponse = HTTP.execute(authRequest);
		String authJson = EntityUtils.toString(authResponse.getEntity());
		if (authResponse.getStatusLine().getStatusCode() != 200) {
			response.setStatus(authResponse.getStatusLine().getStatusCode());
    		out.println(authJson);
    		out.close();
    		return;				
		}
		try {
			ContainerURL containerURL = AppConfig.getContainerURL();
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
		body.put("message", "All Files deleted successfully");
		out.println(gson.toJson(body));	
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Gson gson = new Gson();
        Map<String,Object> body = new HashMap<String,Object>();
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		HttpGet authRequest = new HttpGet(AppConfig.authServiceEndpoint + "/token");
		authRequest.setHeader("Authorization", request.getHeader("Authorization"));
		HttpResponse authResponse = HTTP.execute(authRequest);
		String authJson = EntityUtils.toString(authResponse.getEntity());
		if (authResponse.getStatusLine().getStatusCode() != 200) {
			response.setStatus(authResponse.getStatusLine().getStatusCode());
    		out.println(authJson);
    		out.close();
    		return;				
		}

        String objectName = request.getParameter("delete");

		if (objectName != null) {
	        BlockBlobURL blobURL;
			try {
				blobURL = AppConfig.getContainerURL().createBlockBlobURL(objectName);
				blobURL.delete().blockingGet();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}
	        body.put("message", "File deleted successfully");
		} else {
			body.put("error", "INSUFFICIENT_PARAMETERS");
			response.setStatus(400);
    	}

		out.println(gson.toJson(body));	
	}
}
