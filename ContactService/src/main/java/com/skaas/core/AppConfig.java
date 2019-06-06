package com.skaas.core;

public class AppConfig {
	
	public static final String dbEndpoint = (System.getenv("DB_ENDPOINT") != null) ? System.getenv("DB_ENDPOINT") : "localhost";
	public static final String dbPort = (System.getenv("DB_PORT") != null) ? System.getenv("DB_PORT") : "9042";
	public static final String authServiceEndpoint = (System.getenv("AUTH_SERVICE") != null) ? System.getenv("AUTH_SERVICE") : "http://localhost:8080/authservice";
	
}
