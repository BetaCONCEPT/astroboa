var queryOffset = 0;
var queryLimit = 5;

var repositories = [];
var boxElementList = [];


bcmslib.jQuery(document).ready (function(){
	
	createAjaxActivityIndicator();
	
	bcmslib.jQuery('.repositorySelectionList option').each(function(i) {
		repositories[i] = bcmslib.jQuery(this).val();
	});
	
	initLoginPanel();
	
	initMasonry();
	
	var lastPublishedObjectsQuery = "";
	var projectionPaths = "thumbnail,profile.title,profile.description,profile.modified,content,body,summary,plot,about,aboutMe";
	var orderBy= "profile.modified desc";
	
	bcmslib.jQuery.each(repositories, function(i, repository) {
		getObjectCollection(repository, lastPublishedObjectsQuery, projectionPaths, orderBy, queryOffset, queryLimit, renderElementsFromResults);
	});
	
	bcmslib.jQuery("#showMore").click(function () {
		showMoreResults(lastPublishedObjectsQuery, projectionPaths, orderBy);
	});
});

function appendBoxElements() {
	var newBoxes = bcmslib.jQuery(boxElementList);
	bcmslib.jQuery("#searchResults")
	.append(newBoxes);
	
	setTimeout(function() {
		bcmslib.jQuery("#searchResults").masonry( { appendedContent: newBoxes });
	  }, 1000);

	//bcmslib.jQuery(".scaledImage").scale("center", "stretch");

	boxElementList = [];
}

function showMoreResults(query, projectionPaths, orderBy) {
	queryOffset = queryOffset + queryLimit;
	bcmslib.jQuery.each(repositories, function(i, repository) {
		getObjectCollection(repository, query, projectionPaths, orderBy, queryOffset, queryLimit, renderElementsFromResults);
	});
}

function initLoginPanel() {
	//Expand Panel
	bcmslib.jQuery("#open").click(function(){
		bcmslib.jQuery("div#panel").slideDown("slow");	
	});	
	
	// Collapse Panel
	bcmslib.jQuery("#close").click(function(){
		bcmslib.jQuery("div#panel").slideUp("slow");	
	});		
	
	// Switch buttons from "Log In | Register" to "Close Panel" on click
	bcmslib.jQuery("#toggle a").click(function () {
		bcmslib.jQuery("#toggle a").toggle();
	});	
}


function getObjectCollection(repository, cmsQuery, projectionPaths, orderBy, offset, limit, callback) {
	var resourceAPI_GetObjectURL = 'http://'+window.location.hostname+':' + window.location.port + '/resource-api/' + repository + '/contentObject';
	
	bcmslib.jQuery.ajax({
		type: "GET",
		dataType : 'json',
		data: ({
				cmsQuery:			cmsQuery, 
				output:				'json', 
				projectionPaths:	projectionPaths,
				orderBy:			orderBy,
				offset:				offset,
				limit:				limit
				}),				   		
		async : true,
		url : resourceAPI_GetObjectURL,
		success : function(data) {
						callback(data.resourceCollection.resource, repository);
					}
	});
}

function renderElementsFromResults(objectList, repository) {
	if (!objectList) {
		return;
	}
	
	var columnClass = "singleModeBox singleModeCol";
	
	bcmslib.jQuery.each(objectList, function(i, object) {
		
		var newBoxElement = bcmslib.jQuery(document.createElement('div'));
		//var colId =  Math.floor(Math.random()*2); //(i % 5) + 1;
		//var columnClass = "box col" + colId;

		var imageBoxClass = columnClass +  ' image';
		var textBoxClass = columnClass +  ' text';
		var textFileBoxClass = columnClass +  ' textFile';

		var appendedTitle = '<h2>' + object.profile.title + '</h2>';
		var objectText = getObjectText(object);
		if (objectText == undefined) {
			objectText = '';
		}
		var appendedObjectText = '<p>' + objectText + '</p>';
		var appendedDate = '<p class="date">' + object.profile.modified + '<br/>repository: <span>' + repository + '</span></p>';
		var appendedThumbnail = (object.thumbnail != undefined ? '<p><img src="' + object.thumbnail.url + '" /></p>' : '');
		
		
		if (object.contentObjectTypeName == 'fileResourceObject' && object.content != undefined) {
			
			if (object.content.mimeType == 'image/jpeg' ||  object.content.mimeType == 'image/png') {
				var appendedImage = '<p><img src="' + object.content.url + '" /></p>';
				
				bcmslib.jQuery(newBoxElement)
					.addClass(imageBoxClass)
					.append(
						appendedTitle + 
						appendedDate +
						appendedObjectText +
						appendedImage);
			}
			else {
				var appendedDownloadUrl = '<p><a href="' + object.content.url + '">Download</a>';
				
				bcmslib.jQuery(newBoxElement)
				.addClass(textFileBoxClass)
				.append(
						appendedTitle +  
						appendedDate +
						appendedObjectText +
						appendedDownloadUrl);
			}
		}
		else {
			bcmslib.jQuery(newBoxElement)
				.addClass(textBoxClass)
				.append(
					appendedTitle + 
					appendedDate +
					 appendedThumbnail +
					appendedObjectText);
		}
		
		boxElementList.push(newBoxElement[0]);
	});
}

function getObjectText(object) {

	if (object.about != undefined) {
		return object.about;
	}
	else if (object.aboutMe != undefined) {
		return object.aboutMe;
	}
	else if (object.plot != undefined) {
		return object.plot;
	}
	else if (object.summary != undefined) {
		return object.summary;
	}
	else if (object.profile.description != undefined) {	
		return object.profile.description;
	}
	else if (object.body != undefined) {
		// strip html tags and return the first 300 chars
		return stripHTMLMarkup(object.body).substring(0, 300) + '...';
	}
		
}

function stripHTMLMarkup(htmlText) {
	//wrap the provided text with a div to ensure the strip
	// will be applied to all of the text
	htmlText = '<div>' + htmlText + '</div>';
	
	return bcmslib.jQuery(htmlText).text();
}

function initMasonry() {
	
	bcmslib.jQuery('#searchResults').masonry(
			{ 
				singleMode: true,
			//	columnWidth: 220,
				resizeable: true,
				animate: true,
				itemSelector: '.singleModeBox'
			});	
}

function createAjaxActivityIndicator() {
	
	bcmslib.jQuery(document).ajaxStart(function() {
		bcmslib.jQuery("#ajaxActivityIndicator").hide();
		bcmslib.jQuery("#ajaxActivityIndicator").show();
	});
	
	bcmslib.jQuery(document).ajaxStop(function() {
		appendBoxElements();
		bcmslib.jQuery("#ajaxActivityIndicator").hide();
	});
}
