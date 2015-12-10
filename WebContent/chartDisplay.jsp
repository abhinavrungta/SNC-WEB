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
    google.load('visualization', '1.0', {
        'packages' : [ 'corechart' ]
    });
 
    google.setOnLoadCallback(init);
    
    function init() {
    	var query = new google.visualization.Query('spreadservlet');
        query.send(handleSimpleDsResponse);
        
        var query2 = new google.visualization.Query('accuracy');
        query2.send(handleAccuracyResponse);
        
        var query3 = new google.visualization.Query('ratings');
        query3.send(handleRatingsResponse);
        
        var query4 = new google.visualization.Query('timestamps');
     	query4.send(handleTimestampResponse);

        drawToolbar();
      }
 
    function handleSimpleDsResponse(response) {
    	var data = response.getDataTable();
 
        // Set chart options
        var options = {
          title: 'Expected Coverage Vs Seed Set Size',
          curveType: 'function',
          legend: { position: 'bottom' },
          vAxis: {title: "Coverage"}
        };
 
        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
        chart.draw(data, options);
    }
    
    function handleAccuracyResponse(response) {
    	var data = response.getDataTable();
 
        // Set chart options
        var options = {
          title: 'Actual Coverage For Different Models',
          curveType: 'function',
          legend: { position: 'bottom' },
          vAxis: {title: "Coverage"}
        };
 
        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div2'));
        chart.draw(data, options);
    }
    
    function handleRatingsResponse(response) {
    	var data = response.getDataTable();
 
        // Set chart options
        var options = {
          title: 'Average Rating By All Users Vs Average Rating By Seed Set Users',
          curveType: 'function',
          legend: { position: 'bottom' },
          hAxis: {title: "Users"},
          vAxis: {title: "Average Rating"}
        };
 
        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div3'));
        chart.draw(data, options);
    }
    
    function handleTimestampResponse(response) {
    	var data = response.getDataTable();
 
        // Set chart options
        var options = {
          title: 'Relative Timestamps of Ratings By Seed Set Users Vs Rest.',
          curveType: 'function',
          legend: { position: 'bottom' },
          hAxis: {title: "Relative TimeStamp"},
          vAxis: {title: "Node Id"}
        };
 
        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.ScatterChart(document.getElementById('chart_div4'));
        chart.draw(data, options);
    }
</script>
</head>
<body>
	<div style="width: 100%;">
		<div id="chart_div"></div>
		<div id="chart_div2"></div>
		<div id="chart_div3"></div>
		<div id="chart_div4"></div>
	</div>
</body>
</html>