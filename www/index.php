<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Практическая работа №5</title>
<meta name="keywords" content="" />
<meta name="description" content="" />

<link href="css/tooplate_style.css" rel="stylesheet" type="text/css" />

<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.lightbox-0.5.js"></script>
<link rel="stylesheet" type="text/css" href="css/jquery.lightbox-0.5.css" media="screen" />
<script type='text/javascript' src='js/jquery.min.js'></script>
<script type='text/javascript' src='js/jquery.scrollTo-min.js'></script>
<script type='text/javascript' src='js/jquery.localscroll-min.js'></script>
<script type="text/javascript" src="js/jquery.lightbox-0.5.js"></script> 

<script type="text/javascript">
$(function() {
	$.localScroll();
    $('#map a').lightBox();
});
</script>

</head>
<body>

<span id="top"></span>
<div id="tooplate_body_wrapper">
<div id="tooplate_wrapper">
	<div id="tooplate_header">
        <div id="site_title">
            <h1><a href="#">Blue Spark</a></h1>
        </div>
        <div id="tooplate_menu">
            <ul>
                <li><a href="#home">Home</a></li>
                <li><a href="#aboutus">About Us</a></li>
                <li><a href="#blog">Blog</a></li>
                <li><a href="#portfolio">Portfolio</a></li>
                <li class="last"><a href="#contactus">Contact</a></li>
            </ul>    	
        </div> <!-- end of tooplate_menu -->
	</div> <!-- end of header -->
	  <div id="tooplate_main">
    
		<div id="home" class="content_top"></div>
    	<div class="content_box">
        	<div class="content_title content_ct"><h2>Welcome to Blue Spark Theme</h2></div>
            <div class="content">
            	
				<form method="POST" action="http://127.0.0.1"> 
					<input name="name" type="text" />
					<input type="submit" />
				</form>
                <p>
					<a href="#">
						<?php 
						echo '<h4>' . $_SERVER['REQUEST_METHOD'] . '</h4>';
						if ($_SERVER['REQUEST_METHOD'] == 'POST') {
							echo $_POST['name']; 
						} else {
							echo $_GET['name'];
						}
						?> Free sasotos
					</a> 
					for phott link when you use this template. Phasellus tempus ullamcorper lectus at 
					mollis. Fusce sit amet tristique magna facilisis.
				</p>
    			<div class="cleaner h30"></div>

                
                <div class="cleaner"></div>
            </div>

		</div> 
        
        <div id="aboutus" class="content_top"></div>
    	<div class="content_box">
        	<div class="content_title content_ct"><h2>Ab Us</h2></div>

            
            <div class="content_bottom content_cb"><a href="#top" class="gototop">Go To Top</a></div>
		</div>    
        
        <div id="blog" class="content_top"></div>

        
        <div id="portfolio" class="content_top"></div>
    	<div class="content_box">
        	<div class="content_title content_ct"><h2>Portfolio</h2></div>
            <div class="content">
            	
                <div class="cleaner"></div>
                <p>Cras condimentum lorem nec augue dictum pretium. Maecenas tincidunt aliquet vestibulum. Vivamus rutrum tellus eu tellus sagittis elementum. Quisque orci diam, vestibulum quis porttitor sit amet.</p>
                <div class="cleaner h30"></div>

                
                <div class="cleaner"></div>
            </div>
            
            <div class="content_bottom content_cb"><a href="#top" class="gototop">Go To Top</a></div>
		</div> 
        
        <div id="contactus" class="content_top"></div>
    	<div class="content_box">
        	<div class="content_title content_ct"><h2>Contact Information</h2></div>

            
            <div class="content_bottom content_cbf"><a href="#top" class="gototop">Go To Top</a></div>
		</div> 
    </div>
	</div>
</div>
<?php 
						if (isset($_GET["name"])) {
							echo $_GET["name"] . ", welcome ";
						} else {
							echo "Welcome ";
						} 
					?>
					</body>
					</html>