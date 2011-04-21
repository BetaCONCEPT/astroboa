package org.betaconceptframework.astroboa.resourceapi.resource;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel.ContentDispositionType;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.resourceapi.utility.ContentApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePropertyResource extends AstroboaResource{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final static String DATE_WITH_TIME_FORMAT_PATTERN = "dd-MM-yyyy HH:mm:ss z";
	private SimpleCmsProperty property;
	private int valueIndex;
	private ContentObject contentObject;
	
	public SimplePropertyResource (
			AstroboaClient astroboaClient,
			ContentObject contentObject,
			SimpleCmsProperty property,
			int valueIndex) {
		
		super(astroboaClient);
		
		this.contentObject = contentObject;
		this.property = property;
		this.valueIndex = valueIndex;
	}
	
	// This will be called  through the content object resource. The contentObjectIdOrName, propertyPath have been already consumed and the requested
	// property and value index are passed through the class constructor.
	// The API call always returns a single value even if the property is multivalue.
	// This means that for multivalue properties you should provide a value index (between brackets) at the end of the path,
	// e.g. departments.department[0] or departments.department[0].jobPositions.jobPosition[0]
	//If the value index is omitted, the first value is always returned
	@GET
	@Path("/")
	public Response getContentObjectSimplePropertyValue(
			@QueryParam("contentDispositionType") String contentDispositionType,
			@QueryParam("callback") String callback) {

		if (property == null) {
			logger.warn("The property passed through the class constructor is null");
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}

		if (StringUtils.isBlank(contentDispositionType) || ! ContentDispositionType.ATTACHMENT.toString().equals(contentDispositionType.toUpperCase())){
			contentDispositionType = ContentDispositionType.INLINE.toString();
		}
		else {
			contentDispositionType = contentDispositionType.toUpperCase();
		}


		try{

			if (property.hasNoValues()){
				logger.warn("Content Object name: {}. The provided property {} does not have any values", contentObject.getSystemName(), property.getPath());
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			Object propertyValue;

			if (property.getPropertyDefinition().isMultiple()){
				try{
					propertyValue = property.getSimpleTypeValues().get(valueIndex);
				}
				catch(Exception e){
					logger.warn("Content Object name: " + contentObject.getSystemName() + ". Unable to retrieve value from property "+ property.getPath() + " for index "+ valueIndex, e);
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}
			}
			else{

				if (valueIndex != 0){
					logger.warn("Content Object name: {}. A value index greater than 0 has been provided but the property {} is a single value property. You will get back the value but you should avoid to specify a value index for single value properties", contentObject.getSystemName(), property.getPath());
				}

				propertyValue = property.getSimpleTypeValue();
			}

			if (propertyValue == null) {
				logger.warn("Content Object name: {}. The property value does not exist in path {}. Check if the value index you provided is correct", contentObject.getSystemName(), property.getPath());
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}

			// now that we have the value lets typecast it and convert it to string in order to generate the response
			String propertyValueAsString = null;

			switch (property.getValueType()) {
			case String:
				propertyValueAsString = (String) propertyValue;
				break;
			case Date:
				propertyValueAsString = convertDateToString(((Calendar) propertyValue).getTime(), DATE_WITH_TIME_FORMAT_PATTERN);
				break;
			case Boolean:
				propertyValueAsString = ((Boolean) propertyValue).toString();
				break;
			case Long:
				propertyValueAsString = ((Long) propertyValue).toString();
				break;
			case Double:
				propertyValueAsString = ((Double) propertyValue).toString();
				break;
			case ObjectReference:
				propertyValueAsString = ((ContentObject) propertyValue).getId(); // when values are references to cms entities the id of the entity is returned
				break;
			case TopicReference:
				propertyValueAsString = ((Topic) propertyValue).getId(); // when values are references to cms entities the id of the entity is returned
				break;
			default:
				logger.warn("Content Object name: " + contentObject.getSystemName() + ". The provided property " + property.getPath() + " has the value type " + property.getValueType().toString() + " which is not supported by the current astroboa resource api version");
				throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
			}
			
			if (StringUtils.isNotBlank(callback)) {
				propertyValueAsString = callback + "('" + StringEscapeUtils.escapeJavaScript(propertyValueAsString) + "')" + ";";
			}

			ResponseBuilder responseForPropertyValue = null;

			String responseFileName = contentObject.getSystemName() + "-" + property.getPath();


			responseForPropertyValue = Response.ok(propertyValueAsString);
			responseForPropertyValue.type(MediaType.TEXT_PLAIN + "; charset=utf-8");
			
			// in some special cases we need to return a different mime type
			if ("cascadingStyleSheetObject".equals(contentObject.getContentObjectType())) {
				responseForPropertyValue.type("text/css; charset=utf-8");
				responseFileName = responseFileName + ".css";
			}
			else if ("scriptObject".equals(contentObject.getContentObjectType())) {
				StringProperty scriptLanguageProperty = (StringProperty) contentObject.getCmsProperty("scriptLanguage");
				if (scriptLanguageProperty != null && scriptLanguageProperty.hasValues()) {
					String scriptLanguage = scriptLanguageProperty.getFirstValue();
					if ("javascript".equals(scriptLanguage)) {
						responseForPropertyValue.type("text/javascript; charset=utf-8");
						responseFileName = responseFileName + ".js";
					}
					else {
						responseFileName = responseFileName + scriptLanguage;
					}
				}
			}
			
			ContentApiUtils.addContentDispositionHeaderToResponse(responseForPropertyValue, ContentDispositionType.valueOf(contentDispositionType), responseFileName);
			ContentApiUtils.addLastModifiedAndETagHeaderToResponse(responseForPropertyValue,
					((CalendarProperty)contentObject.getCmsProperty("profile.modified")).getSimpleTypeValueAsDate(),
					propertyValueAsString.length());



			return responseForPropertyValue.build();

		}
		catch (WebApplicationException e) {	
			throw e;
		}
		catch (Exception e) {
			logger.error("A problem occured while connecting repository client to Astroboa Repository", e);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
	}


	
	private String convertDateToString(Date date, String dateFormatPattern) {
		SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance();
		dateFormat.setLenient(false); // be strict in the formatting
		// apply accepted pattern
		dateFormat.applyPattern(dateFormatPattern);
		
		return dateFormat.format(date);
	}
}

