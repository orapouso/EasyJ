/*
 *  Copyright 2009-2012 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.easyj.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that wraps HttpClient and HttpMethodBase in the same place.
 * It also enables chaining for quick configuration and execution
 *
 * @author Rafael Raposo
 * @since 1.0.0
 */
public class EasyRESTHttpClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private HttpClient client;
    private HttpRequestBase method;
    private HttpResponse response;
    private Map<String, Object> requestHeaders;
    private Map<String, Object> parameters;
    private List<Integer> ignoreRedirectStatuses;
    private boolean ignoreRedirect;
    private HttpEntity entity;
    private String uri;

    private Throwable exception;
    private String message;
    private String responseString;

    /**
     * Creates a new instance of EasyRESTHttpClient wrapping the HttpClient and Method in the same class.
     */
    public EasyRESTHttpClient() {
         client = new DefaultHttpClient();
         requestHeaders = new HashMap<String, Object>();
         parameters = new HashMap<String, Object>();
         ignoreRedirectStatuses = new ArrayList<Integer>();
         response = null;
         entity = null;
         exception = null;
         responseString = "";
         
         ((DefaultHttpClient) client).setRedirectStrategy(new DefaultRedirectStrategy(){
             @Override
             public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)  {
                boolean isRedirect = false;
                int responseCode = response.getStatusLine().getStatusCode();
                if(!isIgnoreRedirect() && !ignoreRedirectStatuses.contains(responseCode)) {
                    try {
                        isRedirect = super.isRedirected(request, response, context);
                    } catch (ProtocolException e) {}
                }
                
                return isRedirect;
            }
         });
    }

    /**
     * Adds a single header to the request
     *
     * @param headerName Request header name
     * @param headerValue Request header value
     * @return own instance for chaining
     */
    public EasyRESTHttpClient addRequestHeader(String headerName, Object headerValue) {
        if(headerName != null && headerValue != null) {
            requestHeaders.put(headerName, headerValue);
        }
        return this;
    }

    /**
     * Adds one or more header pairs to the request
     *
     * @param headerPairs header pair in format: headerName=headerValue
     * @return own instance for chaining
     */
    public EasyRESTHttpClient addRequestHeaders(String... headerPairs) {
        addRequestHeaders(toMap(headerPairs));
        return this;
    }

    /**
     * Adds a {@code List<String>} of header pairs to the request
     *
     * @param headerPairs {@code List<String>} of header pairs in format: headerName=headerValue
     * @return own instance for chaining
     */
    public EasyRESTHttpClient addRequestHeaders(List<String> headerPairs) {
        addRequestHeaders(toMap(headerPairs));
        return this;
    }

    /**
     * Adds a {@code Map<String, Object>} of header pairs to the request
     *
     * @param headerPairs {@code Map<String, Object>} that represents the header.
     * The key being the headerName and the value being the headerValue.
     * The value can be any object. It will be validated as headerValue.toString()
     * @return own instance for chaining
     */
    public EasyRESTHttpClient addRequestHeaders(Map<String, Object> headerPairs) {
        if(headerPairs != null) {
            for(String key : headerPairs.keySet()) {
                addRequestHeader(key, headerPairs.get(key));
            }
        }
        return this;
    }

    /**
     * Adds a single parameter to the request.
     * Doesn't matter what method is going to be used.
     *
     * @param paramName Parameter name
     * @param paramValue Parameter value
     * @return own instance for chaining
     */
    public EasyRESTHttpClient addParameter(String paramName, Object paramValue) {
        if(paramName != null && !paramName.isEmpty() && paramValue != null) {
            this.parameters.put(paramName, paramValue);
        }
        return this;
    }

    /**
     * Adds a single parameter to the request, wrapped in a {@code NamedValuePair}.
     * Doesn't matter what method is going to be used.
     *
     * @param param Parameter to be added
     * @return own instance for chaining
     */
    public EasyRESTHttpClient addParameter(NameValuePair param) {
        if(param != null) {
            addParameter(param.getName(), param.getValue());
        }
        return this;
    }

    /**
     * Adds a {@code Map<String, Object>} of parameter pairs to the request.
     * Doesn't matter what method is going to be used.
     *
     * @param paramPairs {@code Map<String, Object>} that represents the parameter.
     * The key being the paramName and the value being the paramValue.
     * The value can be any object. It will be validated as paramValue.toString()
     * @return own instance for chaining
     */
    public EasyRESTHttpClient addParameters(Map<String, Object> paramPairs) {
        if(paramPairs != null) {
            for(String key : paramPairs.keySet()) {
                addParameter(key, paramPairs.get(key));
            }
        }
        return this;
    }

    /**
     * Adds one or more parameter pairs to the request.
     * Doesn't matter what method is going to be used.
     *
     * @param paramPairs parameter pair in the format: paramName=paramValue
     * @return own instance for chaining
     */
    public EasyRESTHttpClient addParameters(String... paramPairs) {
        addParameters(toMap(paramPairs));
        return this;
    }

    /**
     * Adds a {@code List<String>} of parameter pairs to the request.
     * Doesn't matter what method is going to be used.
     *
     * @param paramPairs {@code List<String>} of parameter pairs in the format: paramName=paramValue
     * @return own instance for chaining
     */
    public EasyRESTHttpClient addParameters(List<String> paramPairs) {
        addParameters(toMap(paramPairs));
        return this;
    }

    /**
     * Removes headers from the request.
     * 
     * @param headerNames {@code List<String>} of header names
     * @return own instance for chaining
     */
    public EasyRESTHttpClient removeRequesHeaders(List<String> headerNames) {
        if(headerNames != null) {
            removeRequesHeaders(headerNames.toArray(new String[0]));
        }
        return this;
    }

    /**
     * Removes headers from the request.
     *
     * @param headerNames One or more header names to be removed
     * @return own instance for chaining
     */
    public EasyRESTHttpClient removeRequesHeaders(String... headerNames) {
        if(headerNames != null) {
            for(String headerName : headerNames) {
                requestHeaders.remove(headerName);
            }
        }
        return this;
    }

    /**
     * Removes parameters from the request.
     *
     * @param paramNames {@code List<String>} of parameter names
     * @return own instance for chaining
     */
    public EasyRESTHttpClient removeParameters(List<String> paramNames) {
        if(paramNames != null) {
            removeParameters(paramNames.toArray(new String[0]));
        }
        return this;
    }

    public EasyRESTHttpClient setEntity(HttpEntity entity) {
        this.entity = entity;
        return this;
    }

    /**
     * Removes parameters from the request.
     *
     * @param paramNames One or more parameter names to be removed
     * @return own instance for chaining
     */
    public EasyRESTHttpClient removeParameters(String... paramNames) {
        if(paramNames != null) {
            for(String paramName : paramNames) {
                parameters.remove(paramName);
            }
        }
        return this;
    }

    /**
     * Executes a GET HTTP request and returns the body response as {@code String}
     *
     * @param uri Uri of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public EasyRESTHttpClient get(String uri) {
        method = new HttpGet();
        return execute(uri);
    }

    /**
     * Executes a POST HTTP request and returns the body response as {@code String}
     *
     * @param uri Uri of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public EasyRESTHttpClient post(String uri) {
        method = new HttpPost();
        return execute(uri);
    }

    /**
     * Executes a PUT HTTP request and returns the body response as {@code String}
     *
     * @param uri Uri of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public EasyRESTHttpClient put(String uri) {
        method = new HttpPut();
        return execute(uri);
    }

    /**
     * Executes a DELETE HTTP request and returns the body response as {@code String}
     *
     * @param uri URI of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public EasyRESTHttpClient delete(String uri) {
        method = new HttpDelete();
        return execute(uri);
    }

    /**
     * Executes a HEAD HTTP request and returns the body response as {@code String}
     *
     * @param uri URI of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public EasyRESTHttpClient head(String uri) {
        method = new HttpHead();
        return execute(uri);
    }

    /**
     * Executes a TRACE HTTP request and returns the body response as {@code String}
     *
     * @param uri URI of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public EasyRESTHttpClient trace(String uri) {
        method = new HttpTrace();
        return execute(uri);
    }

    /**
     * Executes a OPTIONS HTTP request and returns the body response as {@code String}
     *
     * @param uri URI of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public EasyRESTHttpClient options(String uri) {
        method = new HttpOptions();
        return execute(uri);
    }

    public void shutdown() {
        client.getConnectionManager().shutdown();
    }

    /**
     * Executes the wrapped method after configuring the request headers and parameters.
     * Builds a {@code EasyRESTResponse} and returns it.
     *
     * @param uri URI of the resource to be requested
     * @return Wrapped response for the resource requested
     */
    protected EasyRESTHttpClient execute(String uri) {

        setURI(uri);
        setMethodRequestHeaders();
        setMethodParameters();

        if (!method.containsHeader("Accept")) {
            method.addHeader("Accept", "application/json");
        }

        try {
            if(method.getURI() != null) {
                response = client.execute(method);
            }
        } catch (ClientProtocolException ex) {
            logger.error("Http Error while trying to connect to [{}]", method.getURI(), ex);
            setException("Http Error while trying to connect to [" + method.getURI() + "]", ex);
        } catch (IOException ex) {
            logger.error("IO Error while trying to connect to [{}]", method.getURI(), ex);
            setException("IO Error while trying to connect to [" + method.getURI() + "]", ex);
        }
        return this;
    }

    /**
     * Returns the http response body of the method executed.
     *
     * @return http response content as {@code String}
     */
    public String consumeAsString() {
        HttpEntity responseEntity = response.getEntity();
        if(responseEntity != null && responseString.isEmpty()) {
            try {
                responseString = EntityUtils.toString(responseEntity);
                responseEntity.getContent().close();
            } catch(IOException ex) {
                logger.error("Problem consuming entity as String: [{}]", this.uri);
            } catch(IllegalArgumentException ex) {
                logger.error("Problem consuming entity as String: [{}] - Entity null or too big", this.uri);
            }
        }
        return responseString;
    }
    
    /**
     * Sets the statuses not to redirect
     *
     * @param status HttpStatus to ignore redirect
     */
    public EasyRESTHttpClient ignoreRedirect(int status) {
        ignoreRedirectStatuses.add(status);
        return this;
    }

    /**
     * Globally ignores all redirect statuses
     * 
     * @return if globally ignoring all redirects
     */
    public boolean isIgnoreRedirect() {
        return ignoreRedirect;
    }

    /**
     * Sets whether to ignore all redirects
     * 
     * @param ignoreRedirect 
     */    
    public EasyRESTHttpClient setIgnoreRedirect(boolean ignoreRedirect) {
        this.ignoreRedirect = ignoreRedirect;
        return this;
    }

    /**
     * Sets the uri for the http request
     *
     * @param uri URI of the resource to be requested
     */
    protected void setURI(String uri) {
        try {
            this.uri = uri;
            method.setURI(new URI(buildURI(uri)));
        } catch (URISyntaxException ex) {
            logger.error("Could not build the desired URI: [{}]", uri, ex);
        }
    }

    /**
     * Corrects the uri if it was not passed the right way
     *
     * @param uri URI to the request
     * @return Returns the right URI
     */
    protected String buildURI(String uri) {
        if(uri == null) return null;

        if(!uri.isEmpty() && uri.indexOf("http") == -1) {
            if(uri.charAt(0) != '/') {
                uri = "/" + uri;
            }
            uri = "http:/" + uri;
        }
        return uri;
    }

    /**
     * Sets request headers map on actual method implementation for execution
     */
    protected void setMethodRequestHeaders() {
        Object header;
        for(String headerName : requestHeaders.keySet()) {
            header = requestHeaders.get(headerName);
            if(header != null) {
                method.addHeader(headerName, header.toString());
            }
        }
    }

    /**
     * Sets parameters map on actual method implementation for execution.
     * For GET, TRACE, HEAD, OPTIONS it appends the parameters to que query string.
     */
    protected void setMethodParameters() {
        if(method instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase req = (HttpEntityEnclosingRequestBase) method;
            try {
                if(this.entity != null) {
                    req.setEntity(this.entity);
                } else {
                    req.setEntity(new UrlEncodedFormEntity(prepareParameters(), HTTP.UTF_8));
                }
            } catch (UnsupportedEncodingException ex) {
                logger.error("Encoding Not Supported while setting entity parameters: ", ex);
            }
        } else {
            setMethodQueryString(toQueryString(parameters));
        }
    }

    protected List<NameValuePair> prepareParameters() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Object paramValue;
        for(String paramName: getParameters().keySet()) {
            paramValue = getParameters().get(paramName);
            params.add(new BasicNameValuePair(paramName, paramValue != null ? paramValue.toString() : ""));
        }
        return params;
    }

    /**
     * Sets the query string on actual method implementation for execution
     */
    protected void setMethodQueryString(String querystring) {
        
    }

    /**
     * Returns un unmodifiable version of the request headers as a {@code Map<String, Object>} for read only purposes.
     *
     * @return un unmodifiable version of the request headers
     * @see java.util.Collections#unmodifiableMap(java.util.Map)
     */
    public Map<String, Object> getRequestHeaders() {
        return Collections.unmodifiableMap(requestHeaders);
    }

    /**
     * Returns un unmodifiable version of the parameters as a {@code Map<String, Object>} for read only purposes.
     *
     * @return un unmodifiable version of the parameters
     * @see java.util.Collections#unmodifiableMap(java.util.Map)
     */
    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Returns the wrapped {@code HttpClient}
     * @return the {@code HttpClient} instance
     * @see org.apache.commons.httpclient.HttpClient
     */
    public HttpClient getHttpClient() {
        return this.client;
    }

    /**
     * Returns the wrapped {@code HttpRequestBase}
     * @return the {@code HttpRequestBase} instance
     * @see org.apache.http.client.methods.HttpRequestBase
     */
    public HttpRequestBase getMethod() {
        return this.method;
    }

    /**
     * Returns the wrapped {@code HttpBaseMethod}
     * @return the {@code HttpBaseMethod} instance
     * @see org.apache.commons.httpclient.HttpMethodBase
     */
    public HttpResponse getResponse() {
        return this.response;
    }

    /**
     * Transforms a {@code Map<String, Object>} into query string format.
     *
     * @param pairs {@code Map<String, Object>} of value pairs
     * @return the query string
     */
    protected String toQueryString(Map<String, Object> pairs) {
        String qs = "";
        if(pairs != null && pairs.size() > 0) {
            Object value;
            for(String key : pairs.keySet()) {
                if(key != null) {
                    value = pairs.get(key);
                    qs +=  "&" + key + "=" + (value == null ? "" : value);
                }
            }
            qs = qs.substring(1);
        }
        return qs;
    }

    /**
     * Transforms a {@code List<String>} formatted as name=value into a {@code Map<String, Object>}
     * 
     * @param pairs {@code List<String>} of nameValue pairs
     * @return {@code Map<String, Object>} of nameValue pairs
     */
    protected Map<String, Object> toMap(List<String> pairs) {
        if(pairs != null) {
            return toMap(pairs.toArray(new String[0]));
        }
        return null;
    }

    /**
     * Transforms a String array formatted as name=value or a single string in query string format into a {@code Map<String, Object>}
     *
     * @param pairs A single query string or string array in format name=value
     * @return {@code Map<String, Object>} of nameValue pairs
     */
    protected Map<String, Object> toMap(String... pairs) {
        if(pairs == null || pairs.length == 0) return null;
        if(pairs.length == 1 && pairs[0] == null) return null;

        Map<String, Object> map = new HashMap<String, Object>();
        int i, j;
        String pair, key, value;
        for(i = 0; i < pairs.length; i++) {
            key = "";
            value = "";
            pair = pairs[i];
            if(pair == null) continue;

            if((j = pair.indexOf("=")) > -1) {
                key = pair.substring(0, j);
                value = pair.substring(j+1);
            }
            if(!key.isEmpty()) {
                map.put(key, value);
            }
        }

        return map;
    }

    /**
     * Gets the http response status returned by the execution of a method.
     *
     * @return http response status
     */
    public int getStatus() {
        int statusCode = -1;
        if(exception != null) {
            statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } else if(response != null) {
            statusCode = response.getStatusLine().getStatusCode();
        }
        return statusCode;
    }

    /**
     * Returns any exception ocurred during the execution of a method
     *
     * @return exception returned or null if successful
     */
    public Throwable getException() {
        return this.exception;
    }

    /**
     * Sets any exception that ocurred
     *
     * @param message custom message for the exception
     * @param ex exception thrown during http process
     */
    private void setException(String message, Throwable ex) {
        setException(ex);
        setMessage(message);
    }

    /**
     * Sets any exception that ocurred
     *
     * @param ex exception thrown during http process
     */
    private void setException(Throwable ex) {
        this.exception = ex;
    }

    /**
     * Returns a custom message
     *
     * @return custom message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets any custom message to return to the client
     *
     * @param message custom message to return to the client
     */
    private void setMessage(String message) {
        this.message = message;
    }

    /**
     * Clears client leaving it like a new instance
     */
    public void clear() {
        this.requestHeaders.clear();
        this.parameters.clear();
        this.method = null;
        this.message = null;
        this.exception = null;
        this.entity = null;
        this.uri = null;
        this.responseString = "";
        if(this.response != null && this.response.getEntity() != null) {
            try {
                this.response.getEntity().getContent().close();
            } catch (Exception ex) {
                //silent ignore
            }
        }
        this.response = null;
    }

}