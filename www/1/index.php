<html>
<head>
	<title>My homepage</title>
	<link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>

<form method="POST" action="http://127.0.0.1"> 
	<input name="name" type="text" />
	<input type="submit" />
	<!--#include file="file.inc" -->
</form>
<?
if ($_SERVER['REQUEST_METHOD'] == 'GET') {
	echo "<h1>Hello, " . $_GET["name"] . "</h1>";
} else {
	echo "<h1>Hello, " . $_POST["name"] . "</h1>";
}
?>
</body>
</html>