package com.skaas.webapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.DownloadResponse;
import com.microsoft.rest.v2.util.FlowableUtil;
import com.skaas.core.AppConfig;

import io.reactivex.Flowable;

/**
 * Servlet implementation class Galleryservlet
 */

@MultipartConfig(fileSizeThreshold=1024*1024*2, // 2MB
maxFileSize=1024*1024*10,      // 10MB
maxRequestSize=1024*1024*50)   // 50MB
public class Galleryservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
			
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Galleryservlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String user_id = AppConfig.getUserId(request.getCookies()); 
		if(user_id != null){  
	        String objectName = URLDecoder.decode(request.getPathInfo(), "UTF-8").substring(1);
	        
			if (request.getParameterMap().containsKey("delete")) {

				try {
			        BlockBlobURL blobURL = AppConfig.getContainerURL().createBlockBlobURL(objectName);
			        blobURL.delete().blockingGet();
				} catch (Exception e) {
					throw new ServletException(e.getMessage());
				}
				response.sendRedirect(request.getContextPath()+"/gallery.jsp");
	    	} else {

	    		byte[] body = new byte[0];
				try {
			        BlockBlobURL blobURL = AppConfig.getContainerURL().createBlockBlobURL(objectName);
			        DownloadResponse downloadResponse = blobURL.download().blockingGet();
			        body = FlowableUtil.collectBytesInBuffer(downloadResponse.body(null)).blockingGet().array();
				} catch (Exception e) {
					throw new ServletException(e.getMessage());
				}
		        
		    	response.setContentType("application/octet-stream");
		    	response.setContentLength((int) body.length);

		    	OutputStream fos = response.getOutputStream();
	
		    	try {
	    	       fos.write(body);
		    	} catch (Exception e) {
		    	    e.printStackTrace();
		    	} finally {
		    	    fos.close();
		    	}
	    	}	    	
		} else {
			PrintWriter out = response.getWriter();
			out.println("You're not logged in");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String user_id = AppConfig.getUserId(request.getCookies());  
		if(user_id != null){	    	
			Part filePart = request.getPart("file");
	    	String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
	        byte[] fileBytes = toByteArray(filePart.getInputStream());

			try {
		        BlockBlobURL blobURL = AppConfig.getContainerURL().createBlockBlobURL("images/"+user_id+"/"+fileName);
		        blobURL.upload(Flowable.just(ByteBuffer.wrap(fileBytes)), filePart.getSize()).blockingGet();		        
			} catch (Exception e) {
				throw new ServletException(e.getMessage());
			}

			response.sendRedirect("gallery.jsp");
		} else {
			PrintWriter out = response.getWriter();
			out.println("You're not logged in");
		}
	}
	
	public static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
			os.write(buffer, 0, len);
		}
		return os.toByteArray();
	}
}
