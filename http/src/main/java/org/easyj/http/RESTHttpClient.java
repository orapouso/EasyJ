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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that wraps HttpClient and HttpMethodBase in the same place.
 * It also enables chaining for quick configuration and execution
 *
 * @author Rafael Raposo
 * @since 1.0.0
 */
public class RESTHttpClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private HttpClient client;
    private HttpMethodBase method;
    private Map<String, Object> requestHeaders;
    private Map<String, Object> parameters;
    private String queryString;

    /**
     * Creates a new instance of EasyRESTHttpClient wrapping the HttpClient and Method in the same class.
     */
    public RESTHttpClient() {
         client = new HttpClient();
         requestHeaders = new HashMap<String, Object>();
         parameters = new HashMap<String, Object>();
         queryString = "";
    }

    /**
     * Adds a single header to the request
     *
     * @param headerName Request header name
     * @param headerValue Request header value
     * @return own instance for chaining
     */
    public RESTHttpClient addRequestHeader(String headerName, Object headerValue) {
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
    public RESTHttpClient addRequestHeaders(String... headerPairs) {
        addRequestHeaders(toMap(headerPairs));
        return this;
    }

    /**
     * Adds a {@code List<String>} of header pairs to the request
     *
     * @param headerPairs {@code List<String>} of header pairs in format: headerName=headerValue
     * @return own instance for chaining
     */
    public RESTHttpClient addRequestHeaders(List<String> headerPairs) {
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
    public RESTHttpClient addRequestHeaders(Map<String, Object> headerPairs) {
        if(headerPairs != null) {
            requestHeaders.putAll(headerPairs);
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
    public RESTHttpClient addParameter(String paramName, Object paramValue) {
        if(paramName != null) {
            parameters.put(paramName, paramValue);
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
    public RESTHttpClient addParameters(String... paramPairs) {
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
    public RESTHttpClient addParameters(List<String> paramPairs) {
        addParameters(toMap(paramPairs));
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
    public RESTHttpClient addParameters(Map<String, Object> paramPairs) {
        if(paramPairs != null) {
            parameters.putAll(paramPairs);
        }
        return this;
    }

    /**
     * Removes headers from the request.
     * 
     * @param headerNames {@code List<String>} of header names
     * @return own instance for chaining
     */
    public RESTHttpClient removeRequesHeaders(List<String> headerNames) {
        return removeRequesHeaders(headerNames.toArray(new String[0]));
    }

    /**
     * Removes headers from the request.
     *
     * @param headerNames One or more header names to be removed
     * @return own instance for chaining
     */
    public RESTHttpClient removeRequesHeaders(String... headerNames) {
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
    public RESTHttpClient removeParameters(List<String> paramNames) {
        return removeParameters(paramNames.toArray(new String[0]));
    }

    /**
     * Removes parameters from the request.
     *
     * @param paramNames One or more parameter names to be removed
     * @return own instance for chaining
     */
    public RESTHttpClient removeParameters(String... paramNames) {
        if(paramNames != null) {
            for(String paramName : paramNames) {
                parameters.remove(paramName);
            }
        }
        return this;
    }

    /**
     * Sets the query string for the wrapped request overwriting anything that
     * was previously set
     *
     * @param queryString The query string to be added to the request
     * @return The {@code EasyRESTHttpClient} instance for chaining
     */
    public RESTHttpClient setQueryString(String queryString) {
        return setQueryString(queryString, false);
    }

    /**
     * Sets the query string for the wrapped request with a parameter to append to
     * a previously set query string
     *
     * @param queryString The query string to be added to the request
     * @param append {@code true} if the string should be appended. {@code false} otherwise
     * @return The {@code EasyRESTHttpClient} instance for chaining
     */
    public RESTHttpClient setQueryString(String queryString, boolean append) {
        if(queryString != null) {
            this.queryString = ((append) ? this.queryString + (this.queryString.length() > 0 ? "&" : "") : "") + queryString;
        }
        return this;
    }

    /**
     * Executes a GET HTTP request and returns the body response as {@code String}
     *
     * @param uri Uri of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public RESTHttpClient get(String uri) {
        method = new GetMethod();
        return execute(uri);
    }

    /**
     * Executes a POST HTTP request and returns the body response as {@code String}
     *
     * @param uri Uri of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public RESTHttpClient post(String uri) {
        method = new PostMethod();
        return execute(uri);
    }

    /**
     * Executes a PUT HTTP request and returns the body response as {@code String}
     *
     * @param uri Uri of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public RESTHttpClient put(String uri) {
        method = new PutMethod();
        return execute(uri);
    }

    /**
     * Executes a DELETE HTTP request and returns the body response as {@code String}
     *
     * @param uri URI of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public RESTHttpClient delete(String uri) {
        method = new DeleteMethod();
        return execute(uri);
    }

    /**
     * Executes a HEAD HTTP request and returns the body response as {@code String}
     *
     * @param uri URI of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public RESTHttpClient head(String uri) {
        method = new HeadMethod();
        return execute(uri);
    }

    /**
     * Executes a TRACE HTTP request and returns the body response as {@code String}
     *
     * @param uri URI of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public RESTHttpClient trace(String uri) {
        method = new TraceMethod(buildURI(uri));
        return execute(uri);
    }

    /**
     * Executes a OPTIONS HTTP request and returns the body response as {@code String}
     *
     * @param uri URI of the resource to be requested
     * @return The body response of the request as {@code String}
     */
    public RESTHttpClient options(String uri) {
        method = new OptionsMethod();
        return execute(uri);
    }

    /**
     * Executes the wrapped method after configuring the request headers and parameters.
     * Builds a {@code EasyRESTResponse} and returns it.
     *
     * @param uri URI of the resource to be requested
     * @return Wrapped response for the resource requested
     */
    protected RESTHttpClient execute(String uri) {
        status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        responseBody = null;

        setURI(uri);
        setMethodRequestHeaders();
        setMethodQueryString();
        setMethodParameters();

        if(method.getRequestHeader("Accept") == null) {
            method.addRequestHeader("Accept", "application/json");
        }

        try {
            status = client.executeMethod(method);

            if(status == HttpStatus.SC_OK) {
                responseBody = method.getResponseBodyAsString();
            }
        } catch (HttpException ex) {
            logger.error("HttpException: problem executing HTTP Request: [{}]", method, ex);
            exception = ex;
        } catch (IOException ex) {
            logger.error("IOException: problem executing HTTP Request or trying to read the Response: [{}]", method, ex);
            exception = ex;
        }
        return this;
    }

    /**
     * Sets the uri for the http request
     *
     * @param uri URI of the resource to be requested
     */
    protected void setURI(String uri) {
        try {
            method.setURI(new URI(buildURI(uri), false));
        } catch (URIException ex) {
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
        if(uri.indexOf("http") == -1) {
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
                method.addRequestHeader(headerName, header.toString());
            }
        }
    }

    /**
     * Sets parameters map on actual method implementation for execution.
     * For GET, TRACE, HEAD, OPTIONS it appends the parameters to que query string.
     */
    protected void setMethodParameters() {
        if(method instanceof PostMethod) {
            PostMethod post = (PostMethod) method;
            Object param;
            for(String headerName : parameters.keySet()) {
                param = parameters.get(headerName);
                if(param != null) {
                    post.addParameter(headerName, param.toString());
                }
            }
        } else if(method instanceof PutMethod){
            PutMethod put = (PutMethod) method;
            try {
                put.setRequestEntity(new StringRequestEntity(toQueryString(parameters), null, null));
            } catch (UnsupportedEncodingException ex) {}
        } else {
            setQueryString(toQueryString(parameters), true);
            setMethodQueryString();
        }
    }

    /**
     * Sets the query string on actual method implementation for execution
     */
    protected void setMethodQueryString() {
        method.setQueryString(queryString);
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
     * Returns the wrapped {@code HttpBaseMethod}
     * @return the {@code HttpBaseMethod} instance
     * @see org.apache.commons.httpclient.HttpMethodBase
     */
    public HttpMethodBase getMethod() {
        return this.method;
    }

    /**
     * Transforms a {@code Map<String, Object>} into query string format.
     *
     * @param pairs {@code Map<String, Object>} of value pairs
     * @return the query string
     */
    protected String toQueryString(Map<String, Object> pairs) {
        String qs = null;
        if(pairs != null && pairs.size() > 0) {
            qs = "";
            for(String key : pairs.keySet()) {
                qs +=  "&" + key + "=" + pairs.get(key);
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
        return toMap(pairs.toArray(new String[0]));
    }

    /**
     * Transforms a String array formatted as name=value or a single string in query string format into a {@code Map<String, Object>}
     *
     * @param pairs A single query string or string array in format name=value
     * @return {@code Map<String, Object>} of nameValue pairs
     */
    protected Map<String, Object> toMap(String... pairs) {
        Map<String, Object> map = null;

        if(pairs != null && pairs.length > 0) {
            map = new HashMap<String, Object>();
            if(pairs.length == 1 && pairs[0].indexOf("&") > -1)  {
                pairs = pairs[0].split("&");
            }
            int i, j;
            String pair, key, value;
            for(i = 0; i < pairs.length; i++) {
                pair = pairs[i];
                if((j = pair.indexOf("=")) > -1) {
                    key = pair.substring(0, j);
                    value = pair.substring(j+1);
                } else {
                    key = pair;
                    value = pairs[++i];
                }
                map.put(key, value);
            }

        }

        return map;
    }

    private int status;
    private String responseBody;
    private Throwable exception;

    /**
     * Gets the http response status returned by the execution of a method.
     *
     * @return http response status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns the http response body of the method executed.
     *
     * @return http response body as {@code String}
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Returns any exception ocurred during the execution of a method
     *
     * @return exception returned or null if successful
     */
    public Throwable getException() {
        return this.exception;
    }

}