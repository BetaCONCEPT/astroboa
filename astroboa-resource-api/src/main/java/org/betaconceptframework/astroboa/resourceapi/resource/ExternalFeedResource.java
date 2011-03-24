package org.betaconceptframework.astroboa.resourceapi.resource;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.resourceapi.utility.SyndicationFeedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.SyndFeedOutput;


@Path("/externalFeed")
public class ExternalFeedResource {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	@GET
    //@Path("/{feedURL}/toFeedType/{toFeedType}")
    //@Produces(value="application/rss+xml")
    public String getExternalFeed(
    		@QueryParam("feedURL") String feedURL, 
    		@QueryParam("toFeedType") String tofeedType,
    		@QueryParam("toFeedTitle") String tofeedTitle) {
		
		if (StringUtils.isBlank(feedURL)) {
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
		if (StringUtils.isBlank(tofeedType)) {
			tofeedType = SyndicationFeedType.RSS_VERSION_2_0;
		}
		
		try {
			FeedFetcher fetcher = new HttpURLFeedFetcher();
			SyndFeed feed = fetcher.retrieveFeed(new URL(feedURL));
			feed.setFeedType(tofeedType);
			if (StringUtils.isNotBlank(tofeedTitle)) {
				feed.setTitle(tofeedTitle);
			}
			
			
			SyndFeedOutput feedOutput = new SyndFeedOutput();
			return feedOutput.outputString(feed);
		}
		catch (Exception e) {
			logger.error("The SyndFeeder failed to retrieve the requested feed", e);
			throw new WebApplicationException(HttpURLConnection.HTTP_NOT_FOUND);
		}
		
	}

	
}

