package com.skaas.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;

import com.microsoft.azure.storage.blob.ContainerSASPermission;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.SASProtocol;
import com.microsoft.azure.storage.blob.SASQueryParameters;
import com.microsoft.azure.storage.blob.ServiceSASSignatureValues;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;

public class AppConfig {
	
	public static final String 
			accountName = (System.getenv("SA_NAME") != null) ? System.getenv("SA_NAME") : "YOUR_STORAGE_ACCOUNT_NAME",
			accountKey = (System.getenv("SA_KEY") != null) ? System.getenv("SA_KEY") : "YOUR_STORAGE_ACCOUNT_KEY",
			container = (System.getenv("SA_CONTAINER") != null) ? System.getenv("SA_CONTAINER") : "YOUR_STORAGE_ACCOUNT_CONTAINER";
	public static final String authServiceEndpoint = "http://localhost:8080/authservice";
	
	
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
		values.withPermissions((new ContainerSASPermission().withRead(true).withWrite(true)).toString());
		SASQueryParameters serviceParams = values.generateSASQueryParameters(new SharedKeyCredentials(AppConfig.accountName, AppConfig.accountKey));

		
		String url = "https://" + AppConfig.accountName + ".blob.core.windows.net/" + AppConfig.container + "/" + blobName + serviceParams.encode();
		return url;
	}
}
