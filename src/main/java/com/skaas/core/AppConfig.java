package com.skaas.core;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;

import javax.servlet.http.Cookie;

import com.microsoft.azure.storage.blob.ContainerSASPermission;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.SASProtocol;
import com.microsoft.azure.storage.blob.SASQueryParameters;
import com.microsoft.azure.storage.blob.ServiceSASSignatureValues;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class AppConfig {

	public static final String 
			accountName = (System.getenv("SA_NAME") != null) ? System.getenv("SA_NAME") : "YOUR_STORAGE_ACCOUNT_NAME",
			accountKey = (System.getenv("SA_KEY") != null) ? System.getenv("SA_KEY") : "YOUR_STORAGE_ACCOUNT_KEY",
			container = (System.getenv("SA_CONTAINER") != null) ? System.getenv("SA_CONTAINER") : "YOUR_STORAGE_ACCOUNT_CONTAINER",
			dbEndpoint = (System.getenv("DB_ENDPOINT") != null) ? System.getenv("DB_ENDPOINT") : "127.0.0.1";
	
			
	public static ContainerURL getContainerURL() throws InvalidKeyException, MalformedURLException {
		ContainerURL containerURL = (
				new ServiceURL(
						new URL("https://" + AppConfig.accountName + ".blob.core.windows.net"), 
						StorageURL.createPipeline(
							new SharedKeyCredentials(AppConfig.accountName, AppConfig.accountKey),
							new PipelineOptions()
						)
					)
				).createContainerURL(AppConfig.container);
		
		return containerURL;
	}

	public static String getSAS(String blobName) throws InvalidKeyException, MalformedURLException {
		ServiceSASSignatureValues values = new ServiceSASSignatureValues()
                .withProtocol(SASProtocol.HTTPS_HTTP).withExpiryTime(OffsetDateTime.now().plusDays(2))
                .withContainerName(AppConfig.container)
                .withBlobName(blobName);
		values.withPermissions((new ContainerSASPermission().withRead(true)).toString());
		SASQueryParameters serviceParams = values.generateSASQueryParameters(new SharedKeyCredentials(AppConfig.accountName, AppConfig.accountKey));

		
		String url = "https://" + AppConfig.accountName + ".blob.core.windows.net/" + AppConfig.container + "/" + blobName + serviceParams.encode();
		return url;
	}

	public static String getUserId(Cookie[] cookies) throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException, UnsupportedEncodingException {
		String jwt = AppConfig.getCookie(cookies, "token");
		String user_id = null;
		if (jwt != null) {
			user_id = Jwts.parser().setSigningKey("secret".getBytes("UTF-8")).parseClaimsJws(jwt).getBody().getSubject();
		}
		return user_id;
	}
	
	public static String getCookie(Cookie[] cookies, String cookieName) {
		if (cookies != null)
	    {
	      for (int i=0; i<cookies.length; i++)
	      {
	        Cookie cookie = cookies[i];
	        if (cookieName.equals(cookie.getName()))
	        {
	          return cookie.getValue();
	        }
	      }
	    }
		return null;
	}
	
}
