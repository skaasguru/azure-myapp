<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ page import="
	com.skaas.core.AppConfig,
	java.lang.Math,
	com.microsoft.azure.storage.blob.BlobURL,
	com.microsoft.azure.storage.blob.ContainerURL,
	com.microsoft.azure.storage.blob.ListBlobsOptions,
	com.microsoft.azure.storage.blob.models.BlobItem,
	com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentResponse
" %>

<%
	String user_id = AppConfig.getUserId(request.getCookies());
	Boolean isLoggedIn = (user_id != null);  
	if ( !isLoggedIn ){
		response.sendRedirect("index.jsp");
	}

%>

<!DOCTYPE html>
<html>
<head>
    <title>My Gallery</title>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <link rel="stylesheet" href="styles/styles.css" type="text/css" media="screen">
</head>
<body>
	<nav class="navbar navbar-default">
		<div class="container-fluid">
			<div class="navbar-header">
			      <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#myNavbar">
			        <span class="icon-bar"></span>
			        <span class="icon-bar"></span>
			        <span class="icon-bar"></span>                        
			      </button>
				<a class="navbar-brand" href="index.jsp">My App</a>
			</div>
		    <div class="collapse navbar-collapse" id="myNavbar">
				<ul class="nav navbar-nav">
					<li><a href="index.jsp">Dashboard</a></li>
					<li><a href="contacts.jsp">My Contacts</a></li>
					<li class="active"><a href="gallery.jsp">My Gallery</a></li>
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<li><a href="loginservlet?logout"><span class="glyphicon glyphicon-log-out"></span> Logout</a></li>
				</ul>
			</div>
		</div>
	</nav>

	<div class="container">
		<h2 class="text-center">My Gallery</h2>
		<div class="row">
			<a class="btn btn-default btn-circle pull-right"
				href="gallery-add.jsp"> <span class="glyphicon glyphicon-plus"></span>
				Add
			</a>
		</div>
		<br>
		
		<div class="row">
			<%
				ContainerURL containerURL = AppConfig.getContainerURL();
				ContainerListBlobFlatSegmentResponse listBlobResponse = containerURL.listBlobsFlatSegment(null, new ListBlobsOptions()).blockingGet();
				if (listBlobResponse.body().segment() != null) {
					for (BlobItem blob : listBlobResponse.body().segment().blobItems()) {
			%>
			<div class="col-md-3">
				<div class="thumbnail thumbnail-height">
					<a href="galleryservlet/<%= blob.name() %>">
						<img class="img-rounded" src="<%= AppConfig.getSAS(blob.name()) %>" alt="Image">
					</a>
					<a class="top-right" href="galleryservlet/<%= blob.name() %>?delete">
						<span class="glyphicon glyphicon-trash"></span>
					</a>
				</div>
			</div>
				<% } 
			} else { %>
				<h3 class="text-center">No Images found. Please upload your images to list here.</h3>
			<% } %>
		</div>
	</div>
</body>
</html>