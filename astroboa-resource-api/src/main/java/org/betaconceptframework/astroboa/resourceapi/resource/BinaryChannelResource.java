package org.betaconceptframework.astroboa.resourceapi.resource;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryChannel.ContentDispositionType;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.resourceapi.utility.ContentApiUtils;
import org.betaconceptframework.astroboa.resourceapi.utility.IndexExtractor;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.utility.FilenameUtils;
import org.betaconceptframework.utility.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryChannelResource extends AstroboaResource{

	private final Logger logger = LoggerFactory.getLogger(getClass()); 
	private BinaryProperty binaryProperty;
	private int binaryPropertyValueIndex;
	private ContentObject contentObject;
	
	private String binaryPropertyValueIdentifier;

	public BinaryChannelResource(AstroboaClient astroboaClient) {
		super(astroboaClient);
	}

	public BinaryChannelResource(
			AstroboaClient astroboaClient,
			ContentObject contentObject,
			BinaryProperty binaryProperty,
			String binaryPropertyValueIdentifier) {
		
		super(astroboaClient);
		
		this.contentObject = contentObject;
		this.binaryProperty = binaryProperty;
		this.binaryPropertyValueIdentifier = binaryPropertyValueIdentifier;
	}

	public BinaryChannelResource(
			AstroboaClient astroboaClient,
			ContentObject contentObject,
			BinaryProperty binaryProperty,
			int binaryPropertyValueIndex) {
		
		super(astroboaClient);
		
		this.contentObject = contentObject;
		this.binaryProperty = binaryProperty;
		this.binaryPropertyValueIndex = binaryPropertyValueIndex;
	}
	

	// This will be called when the API call is through the content object resource and contentObjectIdOrName, propertyPath have been already consumed
	// This may also be called if an old style API call is received with empty content object id and empty property path, i.e. /resource/binaryChannel
	// In the latter case an exception is thrown.
	// The API call always returns a single binary file even if the binary property is multivalue.
	// This means that for multivalue binary properties you should provide a value index (between brackets) at the end of the path,
	// e.g. myfiles.file[0] or mySlideshow[1].image[0]
	//If the value index is omitted, the first value (i.e. the first file) is always returned
	@GET
	@Path("/")
	public Response getContentObjectBinaryChannelPropertyValue(
			@QueryParam("contentDispositionType") String contentDispositionType,
			@QueryParam("width") String width,
			@QueryParam("height") String height,
			@QueryParam("aspectRatio") String aspectRatio,
			@QueryParam("cropPolicy") String cropPolicy) {
		
		if (contentObject == null || binaryProperty == null) { // an old style API call with empty content object id and empty property path have been received, i.e. /resource/binaryChannel
			logger.warn("an old style API call with empty content object id and empty property path have been received");
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
		if (StringUtils.isBlank(contentDispositionType) || ! ContentDispositionType.ATTACHMENT.toString().equals(contentDispositionType.toUpperCase())){
			contentDispositionType = ContentDispositionType.INLINE.toString();
		}
		else {
			contentDispositionType = contentDispositionType.toUpperCase();
		}

		if (binaryPropertyValueIdentifier != null){
			return getBinaryFileInBinaryChannelPropertyUsingBinaryChannelId(binaryProperty, binaryPropertyValueIdentifier, contentDispositionType, width, height, aspectRatio, cropPolicy);
		}
		else{
			return getBinaryFileInBinaryChannelProperty(binaryProperty, binaryPropertyValueIndex, contentDispositionType, width, height, aspectRatio, cropPolicy);
		}
	}

	@GET
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" + "/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}")
	public Response getBinaryFileInBinaryChannelProperty(
			@PathParam("contentObjectIdOrSystemName") String contentObjectIdOrSystemName,
			@PathParam("propertyPath") String binaryChannelPropertyValuePath
			) {

		return getBinaryFileInBinaryChannelProperty(contentObjectIdOrSystemName, binaryChannelPropertyValuePath, ContentDispositionType.INLINE.toString());
	}
	
	/*
	@PUT
	@Path("/{contentObjectIdOrSystemName}/{binaryChannelPropertyValuePath}")
	public Response setBinaryFileInBinaryChannelProperty(
			@PathParam("contentObjectIdOrSystemName") String contentObjectIdOrSystemName,
			@PathParam("binaryChannelPropertyValuePath") String binaryChannelPropertyValuePath
			) {

		return putBinaryFileInBinaryChannelProperty(contentObjectIdOrSystemName, binaryChannelPropertyValuePath);
	}
	*/
	
	//binaryChannelPropertyValuePath is the full path to a property value
	//for example myfiles.file[0] or mySlideshow[1].image[0]
	// The API call always returns a single binary file even if the binary property is multivalue.
	//This means that for multivalue binary properties you should provide a value index (between brackets) at the end of the path
	//If the value index is omitted, the first value (i.e. the first file) is always returned
	
	@GET
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" + "/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}" + "/{contentDispositionType}")
	public Response getBinaryFileInBinaryChannelProperty(
			@PathParam("contentObjectIdOrSystemName") String contentObjectIdOrSystemName,
			@PathParam("propertyPath") String binaryChannelPropertyValuePath,
			@PathParam("contentDispositionType") String contentDispositionType) {

		return getBinaryFileInBinaryChannelProperty(contentObjectIdOrSystemName, binaryChannelPropertyValuePath, contentDispositionType, "-1", "-1");
	}

	@GET
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" + "/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}" + "/{contentDispositionType}/{width}")
	public Response getBinaryFileInBinaryChannelPropertyWithContentDispositionAndWidth(
			@PathParam("contentObjectIdOrSystemName") String contentObjectIdOrSystemName,
			@PathParam("propertyPath") String binaryChannelPropertyValuePath,
			@PathParam("contentDispositionType") String contentDispositionType,
			@PathParam("width") String width) {
		return getBinaryFileInBinaryChannelProperty(contentObjectIdOrSystemName, binaryChannelPropertyValuePath, contentDispositionType, width, "-1");
	}

	@GET
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" + "/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}" + "/{contentDispositionType}/{height}")
	public Response getBinaryFileInBinaryChannelPropertyWithContentDispositionAndHeight(
			@PathParam("contentObjectIdOrSystemName") String contentObjectIdOrSystemName,
			@PathParam("propertyPath") String binaryChannelPropertyValuePath,
			@PathParam("contentDispositionType") String contentDispositionType,
			@PathParam("height") String height) {
		return getBinaryFileInBinaryChannelProperty(contentObjectIdOrSystemName, binaryChannelPropertyValuePath, contentDispositionType, "-1", height);
	}

	@GET
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" + "/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}" + "/{height}")
	public Response getBinaryFileInBinaryChannelPropertyWithHeight(
			@PathParam("contentObjectIdOrSystemName") String contentObjectIdOrSystemName,
			@PathParam("propertyPath") String binaryChannelPropertyValuePath,
			@PathParam("height") String height) {
		return getBinaryFileInBinaryChannelProperty(contentObjectIdOrSystemName, binaryChannelPropertyValuePath, null, "-1", height);
	}

	@GET
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" + "/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}" + "/{width}")
	public Response getBinaryFileInBinaryChannelPropertyWithWidth(
			@PathParam("contentObjectIdOrSystemName") String contentObjectIdOrSystemName,
			@PathParam("propertyPath") String binaryChannelPropertyValuePath,
			@PathParam("width") String width) {
		return getBinaryFileInBinaryChannelProperty(contentObjectIdOrSystemName, binaryChannelPropertyValuePath, null, width, "-1");
	}
	
	@GET
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" + "/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}" + "/{width}/{height}")
	public Response getBinaryFileInBinaryChannelPropertyWithWidthAndHeight(
			@PathParam("contentObjectIdOrSystemName") String contentObjectIdOrSystemName,
			@PathParam("propertyPath") String binaryChannelPropertyValuePath,
			@PathParam("width") String width,
			@PathParam("height") String height) {
		return getBinaryFileInBinaryChannelProperty(contentObjectIdOrSystemName, binaryChannelPropertyValuePath, null, width, height);
	}

	
	@GET
	@Path("/{contentObjectIdOrName: " + CmsConstants.UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "}" + "/{propertyPath: " + CmsConstants.PROPERTY_PATH_REG_EXP_FOR_RESTEASY + "}" + "/{contentDispositionType}/{width}/{height}")
	public Response getBinaryFileInBinaryChannelProperty(
			@PathParam("contentObjectIdOrSystemName") String contentObjectIdOrSystemName,
			@PathParam("propertyPath") String binaryChannelPropertyValuePath,
			@PathParam("contentDispositionType") String contentDispositionType,
			@PathParam("width") String width,
			@PathParam("height") String height) {


		try {
			

			if (StringUtils.isBlank(contentObjectIdOrSystemName)){
				logger.warn("The provided content object id {} is invalid ", contentObjectIdOrSystemName);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			ContentObject contentObject  = astroboaClient.getContentService().getContentObject(contentObjectIdOrSystemName, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, CacheRegion.NONE, null, false);
				
			if (contentObject == null) {
				logger.warn("The provided content object id {} does not correspond to a content object or you do not have permission to access the requested object", contentObjectIdOrSystemName);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			//ContentDisposition Type could either be attachment or inline
			if (StringUtils.isBlank(contentDispositionType) || ! ContentDispositionType.ATTACHMENT.toString().equals(contentDispositionType.toUpperCase())){
				contentDispositionType = ContentDispositionType.INLINE.toString();
			}
			
			//Extract Index from path
			IndexExtractor indexExtractor = new IndexExtractor(binaryChannelPropertyValuePath);

			String binaryChannelPropertyPath = indexExtractor.getPropertyPathWithoutIndex();

			int valueIndex = indexExtractor.getIndex();

			//Load Property according to property path
			BinaryProperty binaryChannelProperty = null;

			try{
				binaryChannelProperty = (BinaryProperty) contentObject.getCmsProperty(binaryChannelPropertyPath);
			}
			catch(Exception e){
				logger.warn("Could not load provided property using path "+ binaryChannelPropertyPath+" from contentObject "+contentObjectIdOrSystemName, e);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			if (binaryChannelProperty == null) {
				logger.warn("The provided property {} does not exist",binaryChannelPropertyPath);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			return getBinaryFileInBinaryChannelProperty(binaryChannelProperty, valueIndex, contentDispositionType, width, height, null, null);
			
		}
		catch (WebApplicationException e) {	
			throw e;
		}
		catch (Exception e) {
			logger.error("A problem occured while connecting repository client to Astroboa Repository", e);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}

	}

	
	private Response getBinaryFileInBinaryChannelProperty( 
			BinaryProperty binaryProperty, 
			int binaryPropertyValueIndex, 
			String contentDispositionType, 
			String width,
			String height, String aspectRatio, String cropPolicy) {

		try {
			//Load resource
			if ((binaryProperty).hasNoValues()){
				logger.warn("Content Object name: {}. The provided property {} does not have any values",  contentObject.getSystemName(), binaryProperty.getPath());
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			BinaryChannel binaryChannel = null;

			if (binaryProperty.getPropertyDefinition().isMultiple()){
				try{
					binaryChannel = binaryProperty.getSimpleTypeValues().get(binaryPropertyValueIndex);
				}
				catch(Exception e){
					logger.warn("Content Object name: " + contentObject.getSystemName() + ". Unable to retrieve value from binary channel property "+binaryProperty.getPath()+ " for index "+ binaryPropertyValueIndex, e);
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}
			}
			else{

				if (binaryPropertyValueIndex != 0){
					logger.warn("Content Object name: {}. You have provided a value index for the single value property {}. You will get back the value of the property but avoid to use indexes for single value properties", contentObject.getSystemName(), binaryProperty.getPath());
				}

				binaryChannel = binaryProperty.getSimpleTypeValue();
			}

			if (binaryChannel == null) {
				logger.warn("Content Object name: {}. BinaryChannel does not exist in path {}", contentObject.getSystemName(), binaryProperty.getPath());
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			String mimeType = binaryChannel.getMimeType();

			if (StringUtils.isBlank(mimeType) && StringUtils.isNotBlank(binaryChannel.getSourceFilename())){
				mimeType = new MimetypesFileTypeMap().getContentType(binaryChannel.getSourceFilename());
			}

			if (StringUtils.isBlank(mimeType) ){
				mimeType = "application/octet-stream";
			}


			String binaryFileName = 
				FilenameUtils.convertFilenameGreekCharactersToEnglishAndReplaceInvalidCharacters(binaryChannel.getSourceFilename());


			if (StringUtils.isNotBlank(mimeType) && mimeType.startsWith("image/") && 
					(! StringUtils.equals(width, "-1") || ! StringUtils.equals(height, "-1"))
			){

				byte[] resourceByteArray = binaryChannel.getContent();

				resourceByteArray = resizeImageResource(resourceByteArray, mimeType, width, height, aspectRatio, cropPolicy);
				
				return ContentApiUtils.createBinaryResponse(
						resourceByteArray, 
						mimeType, 
						ContentDispositionType.valueOf(contentDispositionType), 
						(width != null ? "W"+width : "")
						+(height != null ?"H"+height: "")
						+(width != null || height != null ? "-":"")
						+binaryFileName,
						binaryChannel.getModified() != null ? binaryChannel.getModified().getTime() : Calendar.getInstance().getTime());

			}
			else{

				InputStream resourceStream = binaryChannel.getContentAsStream();

				if (resourceStream == null) {
					logger.warn("No content found for binaryChannel in path "+ binaryProperty.getPath());
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}
				
				return ContentApiUtils.createBinaryResponseFromStream(
						resourceStream, 
						mimeType, 
						ContentDispositionType.valueOf(contentDispositionType), 
						binaryFileName, 
						binaryChannel.getModified() != null ? binaryChannel.getModified().getTime() : Calendar.getInstance().getTime(), 
						binaryChannel.getSize());
				
				
			}

		}
		catch (WebApplicationException e) {	
			throw e;
		}
		catch (Exception e) {
			logger.error("Content Object name: " + contentObject.getSystemName() + 
					"binary channel property: " + binaryProperty.getPath() + " property index "+ binaryPropertyValueIndex + ". A problem occured while retrieving binary channel", e);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
	}

	private byte[] resizeImageResource(byte[] resourceByteArray, String mimeType, String width, String height, String aspectRatio, String cropPolicy) 
	throws NumberFormatException, Exception {

		int imageWidth = 0;
		int imageHeight = 0;

		if (StringUtils.isNotBlank(width)){
			imageWidth = Integer.valueOf(width);
		}

		if (StringUtils.isNotBlank(height)){
			imageHeight = Integer.valueOf(height);
		}

		if (imageWidth != 0 && imageHeight != 0) {
			return ImageUtils.bufferedImageToByteArray(ImageUtils.resize(resourceByteArray, imageWidth, imageHeight), mimeType);
		}

		if (imageWidth != 0 && imageHeight == 0) {
			
			if (StringUtils.isNotBlank(aspectRatio)){
				return ImageUtils.changeAspectRatioAndResize(resourceByteArray, mimeType, imageWidth, imageHeight, Double.valueOf(aspectRatio), cropPolicy);
			}
			
			return ImageUtils.bufferedImageToByteArray(ImageUtils.scaleToWidth(resourceByteArray, imageWidth), mimeType);
		}

		if (imageWidth == 0 && imageHeight != 0) {

			if (StringUtils.isNotBlank(aspectRatio)){
				return ImageUtils.changeAspectRatioAndResize(resourceByteArray, mimeType, imageWidth, imageHeight, Double.valueOf(aspectRatio), cropPolicy);
			}

			return ImageUtils.bufferedImageToByteArray(ImageUtils.scaleToHeight(resourceByteArray, imageHeight), mimeType);
		}
		
		if (imageWidth == 0 && imageHeight == 0 && StringUtils.isNotBlank(aspectRatio)) {
			return ImageUtils.bufferedImageToByteArray(ImageUtils.changeAspectRatio(resourceByteArray, Double.valueOf(aspectRatio), cropPolicy), mimeType);
		}

		return resourceByteArray;
	}

	private Response getBinaryFileInBinaryChannelPropertyUsingBinaryChannelId( 
			BinaryProperty binaryProperty, 
			String binaryChannelId, 
			String contentDispositionType, 
			String width,
			String height, String aspectRatio, String cropPolicy) {

		try {
			//Load resource
			if ((binaryProperty).hasNoValues()){
				logger.warn("Content Object name: {}. The provided property {} does not have any values",  contentObject.getSystemName(), binaryProperty.getPath());
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			if (StringUtils.isBlank(binaryChannelId)){
				logger.warn("Content Object name: {}. The provided binary channel id {} is blank", contentObject.getSystemName(), binaryChannelId);
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			BinaryChannel binaryChannel = null;

			if (binaryProperty.getPropertyDefinition().isMultiple()){

				try{
					for (BinaryChannel binaryPropertyValue : binaryProperty.getSimpleTypeValues()){
						if (StringUtils.equals(binaryPropertyValue.getId(),binaryChannelId)){
							binaryChannel = binaryPropertyValue;
							break;
						}
					}
				}
				catch(Exception e){
					logger.warn("Content Object name: " + contentObject.getSystemName() + ". Unable to retrieve value from binary channel property "+binaryProperty.getPath()+ " for index "+ binaryPropertyValueIndex, e);
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}
			}
			else{

				if (binaryPropertyValueIndex != 0){
					logger.warn("Content Object name: {}. You have provided a value index for the single value property {}. You will get back the value of the property but avoid to use indexes for single value properties", contentObject.getSystemName(), binaryProperty.getPath());
				}
				
				if (StringUtils.equals(binaryProperty.getSimpleTypeValue().getId(), binaryChannelId)){
					binaryChannel = binaryProperty.getSimpleTypeValue();
				}
			}

			if (binaryChannel == null) {
				logger.warn("Content Object name: {}. BinaryChannel with id {} is not a value of the property in in path {}", 
						new Object[]{contentObject.getSystemName(), binaryChannelId, binaryProperty.getPath()});
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			String mimeType = binaryChannel.getMimeType();

			if (StringUtils.isBlank(mimeType) && StringUtils.isNotBlank(binaryChannel.getSourceFilename())){
				mimeType = new MimetypesFileTypeMap().getContentType(binaryChannel.getSourceFilename());
			}

			if (StringUtils.isBlank(mimeType) ){
				mimeType = "application/octet-stream";
			}


			String binaryFileName = 
				FilenameUtils.convertFilenameGreekCharactersToEnglishAndReplaceInvalidCharacters(binaryChannel.getSourceFilename());


			if (StringUtils.isNotBlank(mimeType) && mimeType.startsWith("image/") && 
					(! StringUtils.equals(width, "-1") || ! StringUtils.equals(height, "-1"))
			){

				byte[] resourceByteArray = binaryChannel.getContent();

				resourceByteArray = resizeImageResource(resourceByteArray, mimeType, width, height, aspectRatio, cropPolicy);
				
				return ContentApiUtils.createBinaryResponse(
						resourceByteArray, 
						mimeType, 
						ContentDispositionType.valueOf(contentDispositionType), 
						(width != null ? "W"+width : "")
						+(height != null ?"H"+height: "")
						+(width != null || height != null ? "-":"")
						+binaryFileName,
						binaryChannel.getModified() != null ? binaryChannel.getModified().getTime() : Calendar.getInstance().getTime());

			}
			else{

				InputStream resourceStream = binaryChannel.getContentAsStream();

				if (resourceStream == null) {
					logger.warn("No content found for binaryChannel in path "+ binaryProperty.getPath());
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}
				
				return ContentApiUtils.createBinaryResponseFromStream(
						resourceStream, 
						mimeType, 
						ContentDispositionType.valueOf(contentDispositionType), 
						binaryFileName, 
						binaryChannel.getModified() != null ? binaryChannel.getModified().getTime() : Calendar.getInstance().getTime(), 
						binaryChannel.getSize());
				
				
			}

		}
		catch (WebApplicationException e) {	
			throw e;
		}
		catch (Exception e) {
			logger.error("Content Object name: " + contentObject.getSystemName() + 
					"binary channel property: " + binaryProperty.getPath() + " property index "+ binaryPropertyValueIndex + " and binary channel id "+ binaryChannelId+ 
					". A problem occured while retrieving binary channel", e);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
	}
}

