jQuery(document).ready(function() {
	jQuery('#layer1').draggable(
		{
			zIndex: 	20,
			ghosting:	false,
			opacity: 	0.7,
			cursor: 'crosshair',
			scroll: true,
			handle:	'#layer1_handle'
		}
	);	
				
	jQuery("#layer1").show();
	
	jQuery('#close').click(function()
		{
			jQuery("#layer1").hide();
		});

		
});
	
