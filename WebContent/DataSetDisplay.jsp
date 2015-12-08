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
    	var query = new google.visualization.Query('datasetanalysis');
    	query.setQuery("select rating, count");
        query.send(handleCountResponse);
        
        query = new google.visualization.Query('datasetanalysis');
        query.setQuery("select rating, user");
        query.send(handleUserResponse);

        drawToolbar();
      }
 
    function handleCountResponse(response) {
    	var data = response.getDataTable();
 
        // Set chart options
        var options = {
          title: 'Rating Distribution',
          curveType: 'function',
          legend: { position: 'bottom' }
        };
 
        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));
        chart.draw(data, options);
    }
    
    function handleUserResponse(response) {
    	var data = response.getDataTable();
 
        // Set chart options
        var options = {
          title: 'User Rating Distribution',
          curveType: 'function',
          legend: { position: 'bottom' }
        };
 
        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div2'));
        chart.draw(data, options);
    }
</script>
</head>
<body>
   <div style="display: table; width: 100%;">
   	<div style="display: table-row">
   		<div id="chart_div" style="width: 50%; display: table-cell;"></div>
        <div id="chart_div2" style="width: 50%; display: table-cell;"></div>
   	</div>
   </div>
    <div>
    <h3> Great, now choose the model</h3>
		<form action="input" method="post">
        	LT Color <input type="checkbox" name="color" /> 
            LT Classical <input type="checkbox"	name="classical" /> 
            LT Ratings <input type="checkbox"	name="rating" /> 
            LT Tattle <input type="checkbox" name="tattle" />
            <br />
            Intended Number of Seed Set <input type="text" name="seed_set" />
            <br />
            Monte Carlo Simulations to be run <input type="text" name="monte_carlo" />
            <br />
            Let's Go <input type="submit" value="submit" />
           </form>          
        </div>
</body>
</html>