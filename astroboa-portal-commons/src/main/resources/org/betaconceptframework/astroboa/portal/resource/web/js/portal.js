/* 
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
 * 
 * 		  This file is part of Astroboa.
 * 		 
 * 		  Astroboa is free software: you can redistribute it and/or modify
 * 		  it under the terms of the GNU Lesser General Public License as published by
 * 		  the Free Software Foundation, either version 3 of the License, or
 * 		  (at your option) any later version.
 * 		 
 * 		  Astroboa is distributed in the hope that it will be useful,
 * 		  but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 		  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * 		  GNU Lesser General Public License for more details.
 * 		 
 * 		  You should have received a copy of the GNU Lesser General Public License
 * 		  along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 * 		 
 * Authors 
 * 		  Gregory Chomatas (gchomatas@betaconcept.com)
 * 		  Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
*/

/*
 * This file is provided as part of the astroboa-portal-common module of astroboa platform.
 * It contains utility javascript functions that can be utilised
 * from web applications that use the astroboa-portal-commons module to
 * publish content stored in astroboa.
 * 
 * This file is already included in page templates layouts provided as part of
 * astroboa-portal-commons module.
 * If you want to include to your pages add the following in page head:
 * 	<script type="text/javascript"
 * 		src="#{portalContext}/astroboa/resource/jar/js/portal.js"></script>
 */
 
  
/* The following code is used to read the web application context path 
 * in a global variable that is used by various javascript functions
 * included in this file.
 * The context path is the path under which the web application runs
 * inside the application server
 * For this code to run add the following to your web page or page template:
 * 
 * 	At page head put:
 *	<script type="text/javascript"
 *		src="#{portalContext}/astroboa/resource/remoting/resource/remote.js"></script>
 *
 *	<script type="text/javascript"
 * 		src="#{portalContext}/astroboa/resource/remoting/interface.js?portalRemoting"></script>
 * 
 * 	At the end of the page body (inside the body tags) put:
 * 	<script type="text/javascript">
 *		startSessionTimeoutCheck();
 *		setPortalContext();
 *	</script>
 * 	
*/
var portalContext;
var portalRemoting = Seam.Component.getInstance("portalRemoting");

function setPortalContext() {
	portalRemoting.getPortalContext(setPortalContextCallback);
}

function setPortalContextCallback(result) {
	portalContext = result;
}



/*
 * The following code asynchronously accesses the 'httpSessionChecker' Seam bean
 * to check if the user session has expired
 * If the session has expired a modal panel is displayed with an appropriate message
 * that instructs the user to press a link to be directed in the home page and renew the session
 * The purpose of this code is to prevent the user accessing ajax calls to the server through a
 * page that has expired. It thus prevents user frustration that might occur when she makes ajax calls to the server 
 * and nothing happens because the session has expired.
 * Use this function when your page contains ajax calls to the backend to force the user to renew the session
 * before using any ajax calls in expired pages.
 * 
 * To use the facility you need to add the following into the head of your page:
 * 
 * 	<script type="text/javascript"
 * 		src="#{portalContext}/astroboa/resource/remoting/resource/remote.js"></script>
 *
 *	<script type="text/javascript"
 * 		src="#{portalContext}/astroboa/resource/remoting/interface.js?httpSessionChecker&amp;portalRemoting"></script>
 * 
 * Notice that the last script tag loads both the 'httpSessionChecker' and the 'portalRemoting' seam beans. Use it like this because
 * the 'portalRemoting' is always required for proper functioning of several javascript functions included in this file
 * 
 * At the end of the page body (inside the body tags) put:
 * 	<script type="text/javascript">
 *		startSessionTimeoutCheck(sessionTimeoutInMillis);
 *		setPortalContext();
 *	</script>
 *
 * Notice that you start both the httpSessionTimeoutCheck and the set up of the portalContext variable
 * which is always required for proper functioning of several javascript functions included in this file
 * 
 * The code assumes that session expires in 30 minutes if you provide a null timeout
*/ 

var sessionChecker = Seam.Component.getInstance("httpSessionChecker");

var sessionTimeoutInterval = null;

function startSessionTimeoutCheck(userTimeoutMillis) {
	var defaultTimeoutMillis = 30*60*1000+3000;
	if (userTimeoutMillis == null) {
		userTimeoutMillis = defaultTimeoutMillis;
	}
	else {
		userTimeoutMillis = userTimeoutMillis + 3000;
	}
	sessionTimeoutInterval = setInterval('sessionChecker.isNewSession(alertTimeout)', userTimeoutMillis);
}

function stopSessionTimeoutCheck() {
	if (sessionTimeoutInterval) {
		clearInterval(sessionTimeoutInterval);
	}
}

function resetSessionTimeoutCheck() {
	stopSessionTimeoutCheck();
	startSessionTimeoutCheck();
}

function alertTimeout(newSession) {
	if (newSession) {
		clearInterval(sessionTimeoutInterval);
		showExpirationMessageWithRichModalPanel();
	}
}

/*
 * This function uses rich faces javascript libraries to
 * present a modal panel that informs the user about
 * session expiration and forces her to go to some page to
 * initialize a new session
 * The panel xhtml code should be included in the page that
 * utilizes the http  session checker facility (see above).
 * To included in your page add the following code at the end of
 * your page body (inside body tags):
 * 
 *		<ui:include
 *			src="#{portalContext}/astroboa/resource/jar/facelet/sessionExpirationMessage.xhtml" />
 * 
 */
function showExpirationMessageWithRichModalPanel(){
	Richfaces.showModalPanel('expirationMessagePanel');
}


//Use this utility function to properly run javascript code
//on page load. The function takes into account 
//onload events that other scripts may have register
function addLoadEvent(func) {
	var oldonload = window.onload;
	if (typeof window.onload != 'function') {
		window.onload = func;
	} else {
		window.onload = function() {
			if (oldonload) {
				oldonload();
			}
			func();
		}
	}
}

/* 
 * This function is used to encode and decode URLs
 * It is mainly used to encode text search urls that contain UTF-8 characters
 * 
*/
var Url = {

	// public method for url encoding
	encode : function (string) {
		return escape(this._utf8_encode(string));
	},

	// public method for url decoding
	decode : function (string) {
		return this._utf8_decode(unescape(string));
	},

	// private method for UTF-8 encoding
	_utf8_encode : function (string) {
		string = string.replace(/\r\n/g,"\n");
		var utftext = "";

		for (var n = 0; n < string.length; n++) {

			var c = string.charCodeAt(n);

			if (c < 128) {
				utftext += String.fromCharCode(c);
			}
			else if((c > 127) && (c < 2048)) {
				utftext += String.fromCharCode((c >> 6) | 192);
				utftext += String.fromCharCode((c & 63) | 128);
			}
			else {
				utftext += String.fromCharCode((c >> 12) | 224);
				utftext += String.fromCharCode(((c >> 6) & 63) | 128);
				utftext += String.fromCharCode((c & 63) | 128);
			}

		}

		return utftext;
	},

	// private method for UTF-8 decoding
	_utf8_decode : function (utftext) {
		var string = "";
		var i = 0;
		var c = c1 = c2 = 0;

		while ( i < utftext.length ) {

			c = utftext.charCodeAt(i);

			if (c < 128) {
				string += String.fromCharCode(c);
				i++;
			}
			else if((c > 191) && (c < 224)) {
				c2 = utftext.charCodeAt(i+1);
				string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
				i += 2;
			}
			else {
				c2 = utftext.charCodeAt(i+1);
				c3 = utftext.charCodeAt(i+2);
				string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
				i += 3;
			}

		}

		return string;
	}

}

/*
 * The function getObjNN4(obj,name) returns the object for "name". It starts the search in "obj"
 * This function is needed to find nested elements in a page.
*/
function getObjNN4(obj,name)
{
	var x = obj.layers;
	var foundLayer;
	for (var i=0;i<x.length;i++)
	{
		if (x[i].id == name)
		 	foundLayer = x[i];
		else if (x[i].layers.length)
			var tmp = getObjNN4(x[i],name);
		if (tmp) foundLayer = tmp;
	}
	return foundLayer;
}

//The function returns the style object
function getStyleObject(objectId) {
	if(document.getElementById && document.getElementById(objectId)) {
		return document.getElementById(objectId).style;
	} else if (document.all && document.all(objectId)) {
		return document.all(objectId).style;
	} else if (document.layers && document.layers[objectId]) {
		return getObjNN4(document,objectId);
	} else {
		return false;
	}
} 

/*
 * The function hides or shows a page element.
 * show an element
 * 	changeObjectVisibility('myObjectId', 'block');
 * hide an element
 * 	changeObjectVisibility('myObjectId', 'none');
*/ 
function changeObjectVisibility(objectId, newVisibility) {
  var styleObject = getStyleObject(objectId, document);
  if(styleObject) {
		styleObject.display = newVisibility;
		return true;
  } 
  else {
		return false;
  }
}

/*
 * This function checks if the passed key event
 * corresponds to the enter key and if this is true it 
 * returns the outcome of the passed function (called with the passed argument)
 */
function callFunctionOnEnterKey(e, func, arg) {
	var evt=(e)?e:(window.event)?window.event:null;
 	if(evt){
        var key=(evt.charCode)?evt.charCode:((evt.keyCode)?evt.keyCode:((evt.which)?evt.which:0));
       // alert("key pressed" + key);
        if(key==13){
        return func(arg);
		}	
	
    }
}

/*
 * This function retrieves the selected value from a selection menu
 * whose selection items have been generated by a seam "select items" tag 
 */
function getValueFromSelectOneMenu(menuId){

	var selectOneMenu  = document.getElementById(menuId);
	if (selectOneMenu != null){
		var selectedValue = selectOneMenu.value;
								
		if (selectedValue == 'org.jboss.seam.ui.NoSelectionConverter.noSelectionValue'){
				selectedValue = '0';
		}
	}
	else{
		selectedValue = '0';
	}
	
	return selectedValue;
}


/*
 * This function retrieves the selected date value from a rich faces 
 * date component. If no date has been set it returns a default 
 * hard coded date
 */
function getValueFromDateField(dateComponentId){
	var dateField = document.getElementById(dateComponentId);
	if (dateField == null){
		return '1900-01-01';
	}
	var date  = document.getElementById(dateComponentId).value;
	if (date == '' || date == null){
		date = '1900-01-01';
	}
	
	return date;
}


/*
 *	This function validates the text strings entered by the
 *	user in the text search box of the web site.
 *  The string must be at least 3 characters long and
 *  should not contain special characters such as '*', '[', etc.
 *  TODO: provide a way to internationalize javascript messages 
*/
function validateTextSearched(textSearched) {
	
	if (textSearched == null) {
		alert('Η λέξη αναζήτησης πρέπει να έχει τουλάχιστον 3 χαρακτήρες');
		return false;
	}
	
	//Trim leading and trailing spaces
	textSearched = textSearched.replace(/^\s*/, "").replace(/\s*$/, "");

	//Check if lenght is less than 3 characters if there is a value
	//Must escape less than
	if (textSearched.length < 3){
		alert('Η λέξη αναζήτησης πρέπει να έχει τουλάχιστον 3 χαρακτήρες');
		return false;
	}
											
	//Search for invalid characters
	if (textSearched.indexOf('[') != -1 ||
		textSearched.indexOf(']') != -1 ||
		textSearched.indexOf('\\') != -1 ||
		textSearched.indexOf('/') != -1 ||
		textSearched.indexOf('^') != -1 ||
		textSearched.indexOf('$') != -1 ||
		textSearched.indexOf('.') != -1 ||
		textSearched.indexOf('|') != -1 ||
		textSearched.indexOf('?') != -1 ||
		textSearched.indexOf('*') != -1 ||
		textSearched.indexOf('+') != -1 ||
		textSearched.indexOf(')') != -1 ||
		textSearched.indexOf('(') != -1 ||
		textSearched.indexOf('{') != -1 ||
		textSearched.indexOf('}') != -1 ||
		textSearched.indexOf('&amp;') != -1) {
			alert('Η λέξη αναζήτησης περιέχει μη επιτρεπτούς χαρακτήρες:[,],{,},\\,/,^,$,.,|,?,*,+,(,),&amp;');
			return false;
	}
	
	return true;
}



/*
 * This function reads the text value from a text input field with the provided id,
 * it validates the text value, and generates a astroboa portal-api URL that performs
 * a full text search in the astroboa repository,
 * i.e. the generated URL returns the content objects that their string or binary properties (those containing pdf, word, excel, ppt files)
 * contain the requested string.
 * The following is sample code to use in your pages in order to allow full text search of your web site content 
 * (i.e. the published content stored in astroboa). The code handles submitting the generated URL by pressing the enter key or using a search button :
 *			<label for="search-box">Αναζήτηση:</label>
			<input id="search-box" 
					value="#{textSearched}" 
					type="text" 
					onkeypress="var queryURL = callFunctionOnEnterKey(event, generateTextSearchURL('search-box')); 
								if (queryURL &amp;&amp; queryURL != 'minLengthNotValid') {location.href=queryURL; return false;} 
								else if (queryURL == 'minLengthNotValid') {return false;}"/>
			<a href="#" onclick="var queryURL = generateTextSearchURL('search-box'); 
								if (queryURL &amp;&amp; queryURL != 'minLengthNotValid') {location.href=queryURL; return false;} 
								else if (queryURL == 'minLengthNotValid') {return false;}"> Go </a>
 */
function generateTextSearchURL(textInputFieldId){
	
	var textSearched=document.getElementById(textInputFieldId).value;

	if (validateTextSearched(textSearched)) {					
		textSearched = Url.encode(textSearched);
			
		var textSearchURL = portalContext+'/resource/contentObject/textSearched/'+textSearched;
		return textSearchURL;
	}
	
	// if value is not valid return a special string instead of the URL
	return 'minLengthNotValid';
}
