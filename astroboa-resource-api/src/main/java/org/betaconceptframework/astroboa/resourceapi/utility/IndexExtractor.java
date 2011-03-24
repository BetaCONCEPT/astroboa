package org.betaconceptframework.astroboa.resourceapi.utility;

import java.net.HttpURLConnection;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexExtractor {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private int index = 0;
	private String propertyPathWithoutIndex;

	public IndexExtractor(String propertyPath){

		if (propertyPath != null && propertyPath.endsWith("]")){

			propertyPathWithoutIndex = StringUtils.substringBeforeLast(propertyPath, "[");

			String indexAsString = StringUtils.substringAfterLast(propertyPath, "[").replaceAll("]", "");
			if (indexAsString != null){
				try{
					index = Integer.parseInt(indexAsString);
				}
				catch(Exception e){
					logger.warn("Could not create int from String", e);
					throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
				}
			}
		}
		else{
			propertyPathWithoutIndex = propertyPath;
		}
	}

	public int getIndex(){
		return index;
	}

	public String getPropertyPathWithoutIndex(){
		return propertyPathWithoutIndex;
	}
}