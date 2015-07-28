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
echo "Status" . $_POST["msg"];
?>
</body>
</html>