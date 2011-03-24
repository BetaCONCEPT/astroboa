var componentIdsWithHtmlEditorAttached = new Array();

function attachHtmlEditorToTextAreasBySelector(selector){
	var i;
	componentIdsWithHtmlEditorAttached=getElementsBySelector(selector);
	
	for(i=0;i<componentIdsWithHtmlEditorAttached.length;i++) {
    	tinyMCE.execCommand('mceAddControl', true, componentIdsWithHtmlEditorAttached[i].id);
    } 
}

function removeHtmlEditorFromTextAreasBySelector(selector){
	var i;
	
	if (componentIdsWithHtmlEditorAttached.length > 0) {
		tinyMCE.triggerSave(true,true);
		for(i=0;i<componentIdsWithHtmlEditorAttached.length;i++){
    		tinyMCE.execCommand('mceRemoveControl', false, componentIdsWithHtmlEditorAttached[i].id);
    	}
    	componentIdsWithHtmlEditorAttached = new Array();	
    }
}

function attachHtmlEditorToTextAreasByEditorInit() {
	tinyMCE.init({
				mode : "none",
				plugins : "paste,table,preview,iespell,advlink,advimage",
				theme_advanced_buttons1 : "bold,italic,underline,sub,sup,formatselect,fontsizeselect,styleselect,seperator,justifyleft,justifycenter,justifyright,justifyfull,separator,outdent,indent",
				theme_advanced_buttons2 : "bullist,numlist,separator,tablecontrols,image,separator,link,unlink,separator,hr",
				theme_advanced_buttons3 : "cleanup,removeformat,separator,pastetext,pasteword,selectall,undo,separator,iespell,code,preview",
				theme_advanced_styles : "Header 1=header1;Header 2=header2;Header 3=header3;Σύνδεσμος Ψηφιακού Καναλιού=binaryChannelLink",
				theme : "advanced",
				theme_advanced_toolbar_location : "top",
				theme_advanced_toolbar_align : "left",
				theme_advanced_statusbar_location : "none",
				editor_selector : "mceEditor",
				editor_deselector : "mceNoEditor",
				entities : "160,nbsp,38,amp,162,cent,163,pound,165,yen,169,copy,174,reg,8482,trade,8240,permil,60,lt,62,gt,8804,le,8805,ge,176,deg,8722,minus",
				convert_newlines_to_brs : true,
				convert_urls : false,
				extended_valid_elements : "a[name|href|target|title|onclick|class]",
				width : "600",
				height : "400"
			});
	attachHtmlEditorToTextAreasBySelector('textarea.mceEditor');	
}
