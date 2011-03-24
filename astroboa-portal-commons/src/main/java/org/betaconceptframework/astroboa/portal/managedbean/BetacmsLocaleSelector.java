package org.betaconceptframework.astroboa.portal.managedbean;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.international.LocaleSelector;

@Scope(ScopeType.PAGE)
@Name("org.betaconceptframework.astroboa.portal.managedbean.betacmsLocaleSelector")
public class BetacmsLocaleSelector {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3616862876819306551L;

	@RequestParameter
	private String locale;
	
	@In(create=true)
	private LocaleSelector localeSelector;
	
	public boolean enableLocale(){
		
		try{
			if (StringUtils.isNotBlank(locale) && localeSelector != null){
				localeSelector.setLocaleString(locale);
				localeSelector.select();
			}
			
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public String createURLForLocale(String newLocale, String url){
		if (StringUtils.isNotBlank(url)){
			String locale = localeSelector.getLocaleString();
			
			if (! StringUtils.equals(locale, newLocale) && url.startsWith("/"+locale)){
				return url.replaceFirst("/"+locale, "/"+newLocale);
			}
			
			return url;
		}
		else{
			return "/"+newLocale;
		}
		
		
	}
}
