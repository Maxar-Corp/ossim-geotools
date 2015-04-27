<%--
  Created by IntelliJ IDEA.
  User: sbortman
  Date: 4/22/15
  Time: 10:16 AM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Parse WMTS</title>
    <meta name="layout" content="main"/>
    <asset:stylesheet src="ol3/ol.css"/>
</head>

<body>
<div class="nav">
    <ul>
        <li><g:link uri="/" class="home">Home</g:link></li>
    </ul>
</div>

<div class="content">
    <h1>WMTS GetCapabilities</h1>
    <pre><code id="log"></code></pre>
</div>
<asset:javascript src="ol3/ol.js"/>
<asset:javascript src="jquery.js"/>
<g:javascript>
    $( document ).ready( function ()
    {
        var parser = new ol.format.WMTSCapabilities();

        $.ajax( '/tilestore/wmts/getCapabilities' ).then( function ( response )
        {
            var result = parser.read( response );
            $( '#log' ).html( window.JSON.stringify( result, null, 2 ) );
        } );
    } );
</g:javascript>
</body>
</html>