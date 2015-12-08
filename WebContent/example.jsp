<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Model Comparison</title>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
  <script type="text/javascript">

  // Load the Visualization API and the ready-made Google table visualization.
  google.load('visualization', '1', {'packages':['table,piechart,orgchart,barchart']});

  // Set a callback to run when the API is loaded.
  google.setOnLoadCallback(init);

  // Send the queries to the data sources.
  function init() {

    var query = new google.visualization.Query('simpleexample');
    query.setQuery("select name,population");
    query.send(handleSimpleDsResponse);

    drawToolbar();
  }

  // Handle the simple data source query response
  function handleSimpleDsResponse(response) {
	 <% System.out.println("And here?");%>
    if (response.isError()) {
      alert('Error in query: ' + response.getMessage() + ' ' + response.getDetailedMessage());
      return;
    }

    var data = response.getDataTable();
    var chart = new google.visualization.PieChart(document.getElementById('simple_div'));
    chart.draw(data, {width: 600, height: 150, is3D: true});
  }

  // Draw the toolbar.
  function drawToolbar() {
    var components = [
        {type: 'html', datasource: 'http://localhost:8080/myWebApp/simpleexample'},
        {type: 'csv', datasource: 'http://localhost:8080/myWebApp/simpleexample'},
    ];

    var container = document.getElementById('toolbar_div');
    google.visualization.drawToolbar(container, components);
  }

  </script>
</head>
<body>
    <div style="width: 600px;">
        <div id="simple_div"></div>
    </div>
</body>
</html>