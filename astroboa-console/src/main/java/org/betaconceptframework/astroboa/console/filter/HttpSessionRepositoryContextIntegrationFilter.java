/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.console.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.RepositoryContext;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Populates the {@link RepositoryContextHolder} with information obtained from
 * the <code>HttpSession</code>.
 * <p/>
 * <p/>
 * The <code>HttpSession</code> will be queried to retrieve the
 * <code>RepositoryContext</code> that should be stored against the
 * <code>RepositoryContextHolder</code> for the duration of the web request. At
 * the end of the web request, any updates made to the
 * <code>RepositoryContextHolder</code> will be persisted back to the
 * <code>HttpSession</code> by this filter.
 * </p>
 * <p/>
 * No <code>HttpSession</code> will be created by this filter if one does not
 * already exist. 
 * <p/>
 * <p/>
 * This filter MUST be executed BEFORE any authentication processing mechanisms.
 * Authentication processing mechanisms (eg BASIC, CAS processing filters etc)
 * expect the <code>SecurityContextHolder</code> to contain a valid
 * <code>SecurityContext</code> by the time they execute.
 * </p>
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 */
public class HttpSessionRepositoryContextIntegrationFilter extends OncePerRequestFilter {

    protected static final Log logger = LogFactory.getLog(HttpSessionRepositoryContextIntegrationFilter.class);

    static final String FILTER_APPLIED = "__astroboa_client_context_session_integration_filter_applied";

    public static final String ASTROBOA_CLIENT_CONTEXT_MAP_KEY = "ASTROBOA_CLIENT_CONTEXT_MAP";
    public static final String ACTIVE_ASTROBOA_CLIENT_CONTEXT_KEY = "ACTIVE_ASTROBOA_CLIENT_CONTEXT";

    public HttpSessionRepositoryContextIntegrationFilter() throws ServletException {

    }

	@Override
	protected String getAlreadyFilteredAttributeName() {
		return FILTER_APPLIED;
	}


	@Override
	protected void doFilterInternal(HttpServletRequest request,	HttpServletResponse response, FilterChain filterChain)	throws ServletException, IOException {

        HttpSession httpSession = null;

        try {
            httpSession = request.getSession(false);
        }
        catch (IllegalStateException ignored) {
        }

        boolean httpSessionExistedAtStartOfRequest = httpSession != null;

        Map<String, AstroboaClientContext> clientContextMapBeforeChainExecution = readClientContextMapFromSession(httpSession);
        AstroboaClientContext  activeClientContextBeforeChainExecution  = readActiveClientContextFromSession(httpSession);
        
        // Make the HttpSession null, as we don't want to keep a reference to it lying
        // around in case chain.doFilter() invalidates it.
        httpSession = null;

        int contextHashBeforeChainExecution = 0;
        
        if (clientContextMapBeforeChainExecution != null) {
        	contextHashBeforeChainExecution = clientContextMapBeforeChainExecution.hashCode();
        }
        
        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);

        // Create a wrapper that will eagerly update the session with the repository context
        // if anything in the chain does a sendError() or sendRedirect().
        // See SEC-398

        OnRedirectUpdateSessionResponseWrapper responseWrapper =
                new OnRedirectUpdateSessionResponseWrapper( response, request,
                    httpSessionExistedAtStartOfRequest, contextHashBeforeChainExecution );

        // Proceed with chain

        try {
            // This is the only place in this class where RepositoryContextHolder.setContext() is called
            AstroboaClientContextHolder.setClientContextMap(clientContextMapBeforeChainExecution);
            AstroboaClientContextHolder.setActiveClientContext(activeClientContextBeforeChainExecution);

            filterChain.doFilter(request, responseWrapper);
        }
        finally {
            // This is the only place in this class where RepositoryContextHolder.getContext() is called
        	Map<String, AstroboaClientContext> clientContextMapAfterChainExecution = AstroboaClientContextHolder.getClientContextMap();
            AstroboaClientContext  activeClientContextAfterChainExecution  = AstroboaClientContextHolder.getActiveClientContext();
            
            // Crucial removal of RepositoryContextHolder contents - do this before anything else.
            AstroboaClientContextHolder.clearContext();

            request.removeAttribute(FILTER_APPLIED);

            // storeRepositoryContextInSession() might already be called by the response wrapper
            // if something in the chain called sendError() or sendRedirect(). This ensures we only call it
            // once per request.
            if ( !responseWrapper.isSessionUpdateDone() ) {
              storeClientContextInSession(clientContextMapAfterChainExecution, activeClientContextAfterChainExecution, request,
                      httpSessionExistedAtStartOfRequest, contextHashBeforeChainExecution);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("RepositoryContextHolder now cleared, as request processing completed");
            }
        }
    }

    private AstroboaClientContext readActiveClientContextFromSession(HttpSession httpSession) {
    	 if (httpSession == null) {
             if (logger.isDebugEnabled()) {
                 logger.debug("No HttpSession currently exists");
             }

             return null;
         }

         // Session exists, so try to obtain a context from it.

         Object activeClientContextFromSessionObject = httpSession.getAttribute(ACTIVE_ASTROBOA_CLIENT_CONTEXT_KEY);

         if (activeClientContextFromSessionObject == null) {
             if (logger.isDebugEnabled()) {
                 logger.debug("HttpSession returned null object for "+ACTIVE_ASTROBOA_CLIENT_CONTEXT_KEY);
             }

             return null;
         }

         // We now have the repository context object from the session.

         if (!(activeClientContextFromSessionObject instanceof AstroboaClientContext)) {
             if (logger.isWarnEnabled()) {
                 logger.warn(ACTIVE_ASTROBOA_CLIENT_CONTEXT_KEY +" did not contain a AstroboaClientContext but contained: '"
                         + activeClientContextFromSessionObject
                         + "'; are you improperly modifying the HttpSession directly "
                         + "(you should always use AstroboaClientContextHolder) or using the HttpSession attribute "
                         + "reserved for this class?");
             }

             return null;
         }

         // Everything OK. The only non-null return from this method.

         return (AstroboaClientContext) activeClientContextFromSessionObject;
	}

	/**
     * Gets the repository context from the session (if available) and returns it.
     * <p/>
     * If the session is null, the context object is null or the context object stored in the session
     * is not an instance of RepositoryContext it will return null.
     * <p/>
     * If <tt>cloneFromHttpSession</tt> is set to true, it will attempt to clone the context object
     * and return the cloned instance.
     *
     * @param httpSession the session obtained from the request.
     */
    private Map<String, AstroboaClientContext> readClientContextMapFromSession(HttpSession httpSession) {
        if (httpSession == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No HttpSession currently exists");
            }

            return null;
        }

        // Session exists, so try to obtain a context from it.

        Object clientContextMapFromSessionObject = httpSession.getAttribute(ASTROBOA_CLIENT_CONTEXT_MAP_KEY);

        if (clientContextMapFromSessionObject == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("HttpSession returned null object for "+ASTROBOA_CLIENT_CONTEXT_MAP_KEY);
            }

            return null;
        }

        // We now have the repository context object from the session.

        if (!(clientContextMapFromSessionObject instanceof Map)) {
            if (logger.isWarnEnabled()) {
                logger.warn(ASTROBOA_CLIENT_CONTEXT_MAP_KEY + " did not contain a Map but contained: '"
                        + clientContextMapFromSessionObject
                        + "'; are you improperly modifying the HttpSession directly "
                        + "(you should always use Astroboa ClienContextHolder) or using the HttpSession attribute "
                        + "reserved for this class?");
            }

            return null;
        }

        // Everything OK. The only non-null return from this method.

        return (Map<String, AstroboaClientContext>) clientContextMapFromSessionObject;
    }

    /**
     * Stores the supplied repository context in the session (if available) and if it has changed since it was
     * set at the start of the request.
     *
     * @param clientContextMapAfterChainExecution the context object obtained from the RepositoryContextHolder after the request has
     *        been processed by the filter chain. RepositoryContextHolder.getContext() cannot be used to obtain
     *        the context as it has already been cleared by the time this method is called.
     * @param request the request object (used to obtain the session, if one exists).
     * @param httpSessionExistedAtStartOfRequest indicates whether there was a session in place before the
     *        filter chain executed. If this is true, and the session is found to be null, this indicates that it was
     *        invalidated during the request and a new session will now be created.
     * @param contextHashBeforeChainExecution the hashcode of the context before the filter chain executed.
     *        The context will only be stored if it has a different hashcode, indicating that the context changed
     *        during the request.
     *
     */
    private void storeClientContextInSession(Map<String, AstroboaClientContext> clientContextMapAfterChainExecution,
    											AstroboaClientContext activeClientContextAfterChainExecution,
                                               HttpServletRequest request,
                                               boolean httpSessionExistedAtStartOfRequest,
                                               int contextHashBeforeChainExecution) {
        HttpSession httpSession = null;

        try {
            httpSession = request.getSession(false);
        }
        catch (IllegalStateException ignored) {
        }

        if (httpSession == null) {
            if (httpSessionExistedAtStartOfRequest) {
                if (logger.isDebugEnabled()) {
                    logger.debug("HttpSession is now null, but was not null at start of request; "
                            + "session was invalidated, so do not create a new session");
                }
            }
        }

        // If HttpSession exists, store current RepositoryContextHolder contents but only if
        // the RepositoryContext has actually changed (see JIRA SEC-37)
        if (httpSession != null && (clientContextMapAfterChainExecution == null || clientContextMapAfterChainExecution.hashCode() != contextHashBeforeChainExecution)) {
            httpSession.setAttribute(ASTROBOA_CLIENT_CONTEXT_MAP_KEY, clientContextMapAfterChainExecution);
            httpSession.setAttribute(ACTIVE_ASTROBOA_CLIENT_CONTEXT_KEY, activeClientContextAfterChainExecution);

            if (logger.isDebugEnabled()) {
                logger.debug("AstroboaClientContext map: '" + clientContextMapAfterChainExecution + ", and AstroboaClientContext stored to HttpSession");
            }
        }
    }

    /**
     * Does nothing. We use IoC container lifecycle services instead.
     */
    public void destroy() {
    }



    //~ Inner Classes ==================================================================================================    

    /**
     * Wrapper that is applied to every request to update the <code>HttpSession<code> with
     * the <code>RepositoryContext</code> when a <code>sendError()</code> or <code>sendRedirect</code>
     * happens. See SEC-398. The class contains the fields needed to call
     * <code>storeRepositoryContextInSession()</code>
     */
    private class OnRedirectUpdateSessionResponseWrapper extends HttpServletResponseWrapper {

        HttpServletRequest request;
        boolean httpSessionExistedAtStartOfRequest;
        int contextHashBeforeChainExecution;

        // Used to ensure storeRepositoryContextInSession() is only
        // called once.
        boolean sessionUpdateDone = false;

        /**
         * Takes the parameters required to call <code>storeRepositoryContextInSession()</code> in
         * addition to the response object we are wrapping.
         * @see HttpSessionRepositoryContextIntegrationFilter#storeRepositoryContextInSession(RepositoryContext, ServletRequest, boolean, int)
         */
        public OnRedirectUpdateSessionResponseWrapper(HttpServletResponse response,
                                                      HttpServletRequest request,
                                                      boolean httpSessionExistedAtStartOfRequest,
                                                      int contextHashBeforeChainExecution) {
            super(response);
            this.request = request;
            this.httpSessionExistedAtStartOfRequest = httpSessionExistedAtStartOfRequest;
            this.contextHashBeforeChainExecution = contextHashBeforeChainExecution;
        }

        /**
         * Makes sure the session is updated before calling the
         * superclass <code>sendError()</code>
         */
        public void sendError(int sc) throws IOException {
            doSessionUpdate();
            super.sendError(sc);
        }

        /**
         * Makes sure the session is updated before calling the
         * superclass <code>sendError()</code>
         */
        public void sendError(int sc, String msg) throws IOException {
            doSessionUpdate();
            super.sendError(sc, msg);
        }

        /**
         * Makes sure the session is updated before calling the
         * superclass <code>sendRedirect()</code>
         */
        public void sendRedirect(String location) throws IOException {
            doSessionUpdate();
            super.sendRedirect(location);
        }

        /**
         * Calls <code>storeRepositoryContextInSession()</code>
         */
        private void doSessionUpdate() {
            if (sessionUpdateDone) {
                return;
            }
            Map<String, AstroboaClientContext> clientContextMap = AstroboaClientContextHolder.getClientContextMap();
            AstroboaClientContext activeClientContext = AstroboaClientContextHolder.getActiveClientContext();
            storeClientContextInSession(clientContextMap, activeClientContext, request,
                    httpSessionExistedAtStartOfRequest, contextHashBeforeChainExecution);
            sessionUpdateDone = true;
        }

        /**
         * Tells if the response wrapper has called
         * <code>storeRepositoryContextInSession()</code>.
         */
        public boolean isSessionUpdateDone() {
            return sessionUpdateDone;
        }

    }
}
