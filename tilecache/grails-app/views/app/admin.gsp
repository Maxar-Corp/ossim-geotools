<!DOCTYPE html>
<html>

	<head>
		
		<meta charset="utf-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge" />
		<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />

		<title>RBT | Tiles Administrator</title>

		<link rel="stylesheet" href="../bower_components/ol3/dist/ol.css" />
		<link rel="stylesheet" href="../bower_components/bootstrap/dist/css/bootstrap.css" />
		<link rel="stylesheet" href="../bower_components/fontawesome/css/font-awesome.css" />
		<link rel="stylesheet" href="../bower_components/datatables/media/css/jquery.dataTables.css" />

		<link rel="stylesheet" href="assets/css/styles.css" />
   
	</head>
  
	<body>

    	<div class="row text-center security-level-top">Unclassified</div>
	    <div class="container">
			<nav style="top:28px" class="navbar navbar-fixed-top navbar-default" role="navigation">
				<div class="container-fluid">
					<div class="navbar-header">
						<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-navbar-collapse-1">
							<span class="sr-only">Toggle navigation</span>
							<span class="icon-bar"></span>
							<span class="icon-bar"></span>
							<span class="icon-bar"></span>
						</button>
						<img class="pull-left" style="width: 40px; height: 40px; padding-top: 10px;" src="../assets/img/rbt_symbol.png" alt="RBT Logo">
						<a class="navbar-brand" href="#">RBT | Tiles Administrator</a>
					</div>
					<div class="collapse navbar-collapse" id="bs-navbar-collapse-1">
						<ul class="nav navbar-nav">
							<li>
								<a href="#" class="fa fa-gear" data-toggle="modal" data-target="#exportGeopackageModal">&nbsp;Options</a>
							</li>
						</ul>
						<ul class="nav navbar-nav navbar-right">
							<li class="dropdown">
								<a href="#" class="dropdown-toggle fa fa-question-circle" data-toggle="dropdown">&nbsp;Help <b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li>
										<a class="fa fa-tachometer" href="#">&nbsp;&nbsp;View Dashboard</a>
									</li>
									<li>
										<a class="fa fa-file-text-o" href="#">&nbsp;&nbsp;View Log File</a>
									</li>
								</ul>
							</li>
							 <li><a href="">About</a></li>
						</ul>
					</div><!-- /.navbar-collapse -->
				</div><!-- /.container-fluid -->
			</nav>
		</div><!-- /.container" -->

		<div class="navbar-offset"></div>
		
			<div id="titlesRow" class="row">
				
				<div class="col-md-2 text-center">
					<h4>OMAR Layers</h4>
				</div>

				<div class="col-md-5 text-center">
					<h4>OMAR Map</h4>
				</div>

				<div class="col-md-5 text-center">
					<h4>Tile Server</h4>
				</div>

			</div>

			<div id="mapsRow" class="row">
				
				<div id="toc" class="col-md-2">
					<ul>
						<li>Image 1</li>
						<li>Image 2</li>
						<li>Image 3</li>
						<li>Image 4</li>
						<li>Image 5</li>
					</ul>
				</div>

				<div id="mapOmar" class="col-md-5"></div>
				
				<div id="mapTile" class="col-md-5"></div>

			</div>

		<nav class="navbar navbar-default navbar-fixed-bottom text-center security-level-bottom ">
			<div class="container " style="min-height:">
				Unclassified		
			</div>
		</nav>

		<!-- Vendor libs -->
		<script type="text/javascript" src="../bower_components/jquery/dist/jquery.js"></script>
		<script type="text/javascript" src="../bower_components/bootstrap/dist/js/bootstrap.js"></script>
		<script type="text/javascript" src="../bower_components/ol3/dist/ol.js"></script>

		<!-- App specific libs -->
		<script type="text/javascript" src="assets/js/addLayersAdmin.js"></script>
		<script type="text/javascript" src="assets/js/appAdmin.js"></script>	
	</body>

</html>
