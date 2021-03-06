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
			title : 'Rating Distribution',
			curveType : 'function',
			legend : {
				position : 'bottom'
			},
			vAxis : {
				title : "Number Of Ratings"
			}
		};

		// Instantiate and draw our chart, passing in some options.
		var chart = new google.visualization.ColumnChart(document
				.getElementById('chart_div'));
		chart.draw(data, options);
	}

	function handleUserResponse(response) {
		var data = response.getDataTable();

		// Set chart options
		var options = {
			title : 'User Rating Distribution',
			curveType : 'function',
			legend : {
				position : 'bottom'
			},
			vAxis : {
				title : "Number Of Ratings"
			}
		};

		// Instantiate and draw our chart, passing in some options.
		var chart = new google.visualization.ColumnChart(document
				.getElementById('chart_div2'));
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
	<br />
	<div align=center>
		<h3>Great, now choose the model(s)</h3>
	</div>
	<div align=center>
		<form action="input" method="post">
			<table>
				<tr>
					<td>LT Color</td>
					<td><input type="checkbox" name="color" checked /></td>
				</tr>
				<tr>
					<td>LT Classical</td>
					<td><input type="checkbox" name="classic" checked /></td>
				</tr>
				<tr>
					<td>LT Ratings</td>
					<td><input type="checkbox" name="rating" checked /></td>
				</tr>
				<tr>
					<td>LT Tattle</td>
					<td><input type="checkbox" name="tattle" checked /></td>
				</tr>
				<tr>
					<td>Intended Number of Seed Set</td>
					<td><input type="text" name="seed_set" /></td>
				</tr>
				<tr>
					<td>Monte Carlo Simulations to be run</td>
					<td><input type="text" name="monte_carlo" /></td>
				</tr>
				<tr>
					<td align=right><input type="submit" value="submit" /></td>
					<td><input type="submit" value="Home" formaction="index.jsp" />
					</td>
				</tr>
			</table>
		</form>
	</div>
</body>
</html>