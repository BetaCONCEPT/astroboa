var queryOffset = 0;
var queryLimit = 10;

var repositories = [];
var boxElementList = [];

var textSearchFieldHelp = 'type any word or phrase';

bcmslib.jQuery(document).ready (function(){
	
	bcmslib.jQuery('#searchedText').focus(function(){
		if (bcmslib.jQuery('#searchedText').val() == textSearchFieldHelp) {
			bcmslib.jQuery('#searchedText').val('');
		}
    });
	bcmslib.jQuery('#searchedText').blur(function(){
		if (bcmslib.jQuery('#searchedText').val() == null || bcmslib.jQuery('#searchedText').val() == '') {
			bcmslib.jQuery('#searchedText').val(textSearchFieldHelp);
		}
    });
	
	createAjaxActivityIndicator();
	
	bcmslib.jQuery('.repositorySelectionList option').each(function(i) {
		repositories[i] = bcmslib.jQuery(this).val();
	});
	
	initLoginPanel();
	
	//initMasonry();
	
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
	
	//setTimeout(function() {
	//	bcmslib.jQuery("#searchResults").masonry( { appendedContent: newBoxes });
	 // }, 1000);

	boxElementList = [];
}

function showMoreResults(query, projectionPaths, orderBy) {
	bcmslib.jQuery("#showMore").hide();
	queryOffset = queryOffset + queryLimit;
	
	// clone the repositories array in order to avoid concurrent modification
	var clonedRepositories = repositories.slice(0);
	bcmslib.jQuery.each(clonedRepositories, function(i, repository) {
		getObjectCollection(repository, query, projectionPaths, orderBy, queryOffset, queryLimit, renderElementsFromResults);
	});
	if (repositories.length > 0) {
		bcmslib.jQuery("#showMore").show();
	}
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
						if (data.resourceCollection != null) {
							callback(data.resourceCollection.resource, repository);
						}
						else {
							var idxOfRepoToRemove = repositories.indexOf(repository);
							if (idxOfRepoToRemove != -1) {repositories.splice(idxOfRepoToRemove, 1);}
							bcmslib.jQuery.pnotify({
   								pnotify_text: 'No more results from repository "' + repository + '"',
   								pnotify_type: 'ok'
   							});
						}
					}
	});
}

function renderElementsFromResults(objectList, repository) {
	if (!objectList) {
		return;
	}

	//var columnClass = "singleModeBox singleModeCol";
	var columnClass = "object";
	
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
		var appendedDate = '<p class="date">' + new Date(object.profile.modified) + '  Repository: <span>' + repository + '</span>  Type: <span>' + getObjectTypeLabel(object.contentObjectTypeName) + '</span></p>';
		var appendedThumbnail = (object.thumbnail != undefined ? '<p><img src="' + object.thumbnail.url + '" /></p>' : '');
		
		
		if (object.contentObjectTypeName == 'fileResourceObject' && object.content != undefined) {
			
			if (object.content.mimeType == 'image/jpeg' ||  object.content.mimeType == 'image/png') {
				var appendedImage = '<p><img src="' + object.content.url + '?width=200&aspectRatio=1.0" /></p>';
				
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


function getObjectTypeLabel(objectType) {
	switch (objectType) {
	case 'fileResourceObject':
		return 'File';
		break;
	case 'personObject':
		return 'Person';
		break;
	case 'genericContentResourceObject':
		return 'Text';
		break;
	default:
		return objectType;
		break;
	}
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

/*
 *	This function validates the text strings entered by the
 *	user in the text search box of the web site.
 *  The string must be at least 3 characters long and
 *  should not contain special characters such as '*', '[', etc.
*/
function validateTextSearched(textSearched) {
	
	if (textSearched == null) {
		alert('Please type at least three letters in text field');
		return false;
	}
	
	//Trim leading and trailing spaces
	textSearched = textSearched.replace(/^\s*/, "").replace(/\s*$/, "");

	//Check if lenght is less than 3 characters if there is a value
	//Must escape less than
	if (textSearched.length < 3){
		alert('Please type at least three letters in text field');
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
		//textSearched.indexOf('*') != -1 ||
		textSearched.indexOf('+') != -1 ||
		textSearched.indexOf(')') != -1 ||
		textSearched.indexOf('(') != -1 ||
		textSearched.indexOf('{') != -1 ||
		textSearched.indexOf('}') != -1 ||
		textSearched.indexOf('&amp;') != -1) {
			alert('The words that you have input contain one or more of the following not allowed characters:[,],{,},\\,/,^,$,.,|,?,*,+,(,),&amp;');
			return false;
	}
	
	return true;
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

function textSearch() {
	
	repositories.length = 0;
	bcmslib.jQuery('.repositorySelectionList option').each(function(i) {
		repositories[i] = bcmslib.jQuery(this).val();
	});
	
	bcmslib.jQuery("#showMore").hide();
	var text = bcmslib.jQuery('#searchedText').val();
	if (text != null && !validateTextSearched(text)) {
		return false;
	}
	
	bcmslib.jQuery("#searchResultsHeader h1").html('Searching for "' + text + '"');
	
	var textSearchedQuery = 'textSearched="' + text + '"';
	var projectionPaths = "thumbnail,profile.title,profile.description,profile.modified,content,body,summary,plot,about,aboutMe";
	var orderBy= "profile.modified desc";
	
	//bcmslib.jQuery('#searchResults').masonry('destroy');
	bcmslib.jQuery("#searchResults").empty();
	
	//initMasonry();
	
	// clone the repositories array in order to avoid concurrent modification
	var clonedRepositories = repositories.slice(0);
	bcmslib.jQuery.each(clonedRepositories, function(i, repository) {
		getObjectCollection(repository, textSearchedQuery, projectionPaths, orderBy, queryOffset, queryLimit, renderElementsFromResults);
	});
	
	if (repositories.length > 0) {
		
		bcmslib.jQuery("#showMore").unbind();
		bcmslib.jQuery("#showMore").click(function () {
			showMoreResults(textSearchedQuery, projectionPaths, orderBy);
		});
	
		bcmslib.jQuery("#showMore").show();
	}

	
	return false;
	
}


