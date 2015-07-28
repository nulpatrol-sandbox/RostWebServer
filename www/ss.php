<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Практическая работа №5</title>
<meta name="keywords" content="" />
<meta name="description" content="" />

<link href="css/tooplate_style.css" rel="stylesheet" type="text/css" />

<!-- Arquivos utilizados pelo jQuery lightBox plugin -->
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.lightbox-0.5.js"></script>
<link rel="stylesheet" type="text/css" href="css/jquery.lightbox-0.5.css" media="screen" />
<!-- / fim dos arquivos utilizados pelo jQuery lightBox plugin -->
<script type='text/javascript' src='js/jquery.min.js'></script>
<script type='text/javascript' src='js/jquery.scrollTo-min.js'></script>
<script type='text/javascript' src='js/jquery.localscroll-min.js'></script>
<script type="text/javascript" src="js/jquery.lightbox-0.5.js"></script> 
<!-- Ativando o jQuery lightBox plugin -->
<script type="text/javascript">
$(function() {
	$.localScroll();
    $('#map a').lightBox();
});
</script>

</head>
<body>
<form method="POST" action="http://127.0.0.1/ss.php"> 
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