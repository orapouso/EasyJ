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

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests EasyRESTHttpClient
 *
 * @author Rafael Raposo
 * @since 1.0.0
 */
public class EasyRESTHttpClientTest {

    EasyRESTHttpClient client;

    private final String VALID_NAME = "validName";
    private final String VALID_VALUE = "validValue";

    public EasyRESTHttpClientTest() {
    }

    @Before
    public void setUp() {
        client = new EasyRESTHttpClient();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of addRequestHeader method
     */
    @Test
    public void testAddRequestHeader() {
        System.out.println("addRequestHeader(String, Object)");
        String headerName = null;
        Object headerValue = null;

        System.out.println("null headerName");
        client.addRequestHeader(headerName, headerValue);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("empty headerName");
        headerName = "";
        client.addRequestHeader(headerName, headerValue);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("null headerValue");
        headerName = VALID_NAME;
        client.addRequestHeader(headerName, headerValue);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("valid");
        headerValue = VALID_VALUE;
        client.addRequestHeader(headerName, headerValue);
        assertEquals(1, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(headerName));
        assertEquals(headerValue, client.getRequestHeaders().get(headerName));

        System.out.println("same headerName");
        headerValue = VALID_VALUE + "value2";
        client.addRequestHeader(headerName, headerValue);
        assertEquals(1, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(headerName));
        assertEquals(headerValue, client.getRequestHeaders().get(headerName));

        System.out.println("another headerName");
        headerName = VALID_NAME + "header2";
        client.addRequestHeader(headerName, headerValue);
        assertEquals(2, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(VALID_NAME));
        assertTrue(client.getRequestHeaders().containsKey(headerName));
        assertEquals(headerValue, client.getRequestHeaders().get(headerName));
    }

    /**
     * Test of addRequestHeaders method with String Array parameters
     *
     * String format: headerName=headerValue
     */
    @Test
    public void testAddRequestHeaders_StringArr() {
        System.out.println("addRequestHeaders(String...)");
        String emptyHeaderName = "=" + VALID_VALUE;
        String emptyHeaderValue = VALID_NAME + "=";
        String validHeader1 = VALID_NAME + "=" + VALID_VALUE;
        String validHeader2 = VALID_NAME + "1" + "=" + VALID_VALUE + "1";
        String validHeader3 = VALID_NAME + "2" + "=" + VALID_VALUE + "2";

        System.out.println("empty string");
        client.addRequestHeaders("");
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("wrong format");
        client.addRequestHeaders("wrongFormat");
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("empty headerName");
        client.addRequestHeaders(emptyHeaderName);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("empty headerValue");
        client.addRequestHeaders(emptyHeaderValue);
        assertEquals(1, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(VALID_NAME));
        assertTrue(((String)client.getRequestHeaders().get(VALID_NAME)).isEmpty());

        System.out.println("valid");
        client.addRequestHeaders(validHeader1);
        assertEquals(1, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(VALID_NAME));
        assertEquals(VALID_VALUE, client.getRequestHeaders().get(VALID_NAME));

        System.out.println("n headers");
        client.addRequestHeaders(validHeader2, validHeader3);
        assertEquals(3, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(VALID_NAME + "1"));
        assertTrue(client.getRequestHeaders().containsKey(VALID_NAME + "2"));
        assertEquals(VALID_VALUE + "1", client.getRequestHeaders().get(VALID_NAME + "1"));
        assertEquals(VALID_VALUE + "2", client.getRequestHeaders().get(VALID_NAME + "2"));
    }

    /**
     * Test of addRequestHeaders method with a {@code List&lt;String&gt;} parameter
     *
     * String format: headerName=headerValue
     */
    @Test
    public void testAddRequestHeaders_List() {
        System.out.println("addRequestHeaders(List<String>)");
        final String emptyHeaderName = "=" + VALID_VALUE;
        final String emptyHeaderValue = VALID_NAME + "=";
        final String validHeader1 = VALID_NAME + "=" + VALID_VALUE;
        final String validHeader2 = VALID_NAME + "1" + "=" + VALID_VALUE + "1";
        final String validHeader3 = VALID_NAME + "2" + "=" + VALID_VALUE + "2";
        List<String> headerPairs = null;

        System.out.println("null");
        headerPairs = new ArrayList<String>(){{
            add(null);
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("empty string");
        headerPairs = new ArrayList<String>(){{
            add("");
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("wrong format");
        headerPairs = new ArrayList<String>(){{
            add("wrongFormat");
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("empty headerName");
        headerPairs = new ArrayList<String>(){{
            add(emptyHeaderName);
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("empty headerValue");
        headerPairs = new ArrayList<String>(){{
            add(emptyHeaderValue);
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(1, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(VALID_NAME));
        assertTrue(((String)client.getRequestHeaders().get(VALID_NAME)).isEmpty());

        System.out.println("valid");
        headerPairs = new ArrayList<String>(){{
            add(validHeader1);
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(1, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(VALID_NAME));
        assertEquals(VALID_VALUE, client.getRequestHeaders().get(VALID_NAME));

        System.out.println("n headers");
        headerPairs = new ArrayList<String>(){{
            add(validHeader2);
            add(validHeader3);
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(3, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(VALID_NAME + "1"));
        assertTrue(client.getRequestHeaders().containsKey(VALID_NAME + "2"));
        assertEquals(VALID_VALUE + "1", client.getRequestHeaders().get(VALID_NAME + "1"));
        assertEquals(VALID_VALUE + "2", client.getRequestHeaders().get(VALID_NAME + "2"));
    }

    /**
     * Test of addRequestHeaders method with a Map&lt;String, Object&gt; parameter
     */
    @Test
    public void testAddRequestHeaders_Map() {
        System.out.println("addRequestHeaders(Map<String, Object>)");
        Map<String, Object> headerPairs = null;
        final String validName1 = VALID_NAME;
        final String validName2 = VALID_NAME + "1";
        final String validName3 = VALID_NAME + "2";
        final String validValue1 = VALID_VALUE;
        final String validValue2 = VALID_VALUE + "1";
        final String validValue3 = VALID_VALUE + "2";

        System.out.println("null");
        headerPairs = new HashMap<String, Object>(){{
            put(null, null);
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("empty headerName");
        headerPairs = new HashMap<String, Object>(){{
            put("", null);
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("null headerValue");
        headerPairs = new HashMap<String, Object>(){{
            put(validName1, null);
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(0, client.getRequestHeaders().size());

        System.out.println("valid");
        headerPairs = new HashMap<String, Object>(){{
            put(validName1, validValue1);
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(1, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(validName1));
        assertEquals(validValue1, client.getRequestHeaders().get(validName1));

        System.out.println("n headers");
        headerPairs = new HashMap<String, Object>(){{
            put(validName2, validValue2);
            put(validName3, validValue3);
        }};
        client.addRequestHeaders(headerPairs);
        assertEquals(3, client.getRequestHeaders().size());
        assertTrue(client.getRequestHeaders().containsKey(validName2));
        assertTrue(client.getRequestHeaders().containsKey(validName3));
        assertEquals(validValue2, client.getRequestHeaders().get(validName2));
        assertEquals(validValue3, client.getRequestHeaders().get(validName3));
    }

    /**
     * Test of addParameter method
     */
    @Test
    public void testAddParameter_String_Object() {
        System.out.println("addParameter(String, Object)");
        String paramName = null;
        Object paramValue = null;

        System.out.println("null paramName");
        client.addParameter(paramName, paramValue);
        assertEquals(0, client.getParameters().size());

        System.out.println("empty paramName");
        paramName = "";
        client.addParameter(paramName, paramValue);
        assertEquals(0, client.getParameters().size());

        System.out.println("null paramValue");
        paramName = VALID_NAME;
        client.addParameter(paramName, paramValue);
        assertEquals(0, client.getParameters().size());

        System.out.println("valid");
        paramValue = VALID_VALUE;
        client.addParameter(paramName, paramValue);
        assertEquals(1, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(paramName));
        assertEquals(paramValue, client.getParameters().get(paramName));

        System.out.println("same paramName");
        paramValue = VALID_VALUE + "value2";
        client.addParameter(paramName, paramValue);
        assertEquals(1, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(paramName));
        assertEquals(paramValue, client.getParameters().get(paramName));

        System.out.println("another paramName");
        paramName = VALID_NAME + "param2";
        client.addParameter(paramName, paramValue);
        assertEquals(2, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(VALID_NAME));
        assertTrue(client.getParameters().containsKey(paramName));
        assertEquals(paramValue, client.getParameters().get(paramName));
    }

    /**
     * Test of addParameter method with {@code NameValuePair} as parameter.
     */
    @Test
    public void testAddParameter_NameValuePair() {
        System.out.println("addParameter");
        NameValuePair param = null;
        String paramName = null;
        String paramValue = null;

        System.out.println("null");
        client.addParameter(param);
        assertEquals(0, client.getParameters().size());

        System.out.println("empty paramName");
        paramName = "";
        param = new BasicNameValuePair(paramName, paramValue);
        client.addParameter(param);
        assertEquals(0, client.getParameters().size());

        System.out.println("null paramValue");
        paramName = VALID_NAME;
        param = new BasicNameValuePair(paramName, paramValue);
        client.addParameter(param);
        assertEquals(0, client.getParameters().size());

        System.out.println("valid");
        paramValue = VALID_VALUE;
        param = new BasicNameValuePair(paramName, paramValue);
        client.addParameter(param);
        assertEquals(1, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(paramName));
        assertEquals(paramValue, client.getParameters().get(paramName));

        System.out.println("same paramName");
        paramValue = VALID_VALUE + "value2";
        param = new BasicNameValuePair(paramName, paramValue);
        client.addParameter(param);
        assertEquals(1, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(paramName));
        assertEquals(paramValue, client.getParameters().get(paramName));

        System.out.println("another paramName");
        paramName = VALID_NAME + "param2";
        param = new BasicNameValuePair(paramName, paramValue);
        client.addParameter(param);
        assertEquals(2, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(VALID_NAME));
        assertTrue(client.getParameters().containsKey(paramName));
        assertEquals(paramValue, client.getParameters().get(paramName));
    }

    /**
     * Test of addParameters method with Map&lt;String, Object&gt; as parameter.
     */
    @Test
    public void testAddParameters_Map() {
        System.out.println("addParameters(Map<String, Object>)");
        Map<String, Object> paramPairs = null;
        final String validName1 = VALID_NAME;
        final String validName2 = VALID_NAME + "1";
        final String validName3 = VALID_NAME + "2";
        final String validValue1 = VALID_VALUE;
        final String validValue2 = VALID_VALUE + "1";
        final String validValue3 = VALID_VALUE + "2";

        System.out.println("null");
        paramPairs = new HashMap<String, Object>(){{
            put(null, null);
        }};
        client.addParameters(paramPairs);
        assertEquals(0, client.getParameters().size());

        System.out.println("empty paramName");
        paramPairs = new HashMap<String, Object>(){{
            put("", null);
        }};
        client.addParameters(paramPairs);
        assertEquals(0, client.getParameters().size());

        System.out.println("null paramValue");
        paramPairs = new HashMap<String, Object>(){{
            put(VALID_NAME, null);
        }};
        client.addParameters(paramPairs);
        assertEquals(0, client.getParameters().size());

        System.out.println("valid");
        paramPairs = new HashMap<String, Object>(){{
            put(validName1, validValue1);
        }};
        client.addParameters(paramPairs);
        assertEquals(1, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(validName1));
        assertEquals(validValue1, client.getParameters().get(validName1));

        System.out.println("n params");
        paramPairs = new HashMap<String, Object>(){{
            put(validName2, validValue2);
            put(validName3, validValue3);
        }};
        client.addParameters(paramPairs);
        assertEquals(3, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(validName2));
        assertTrue(client.getParameters().containsKey(validName3));
        assertEquals(validValue2, client.getParameters().get(validName2));
        assertEquals(validValue3, client.getParameters().get(validName3));
    }

    /**
     * Test of addParameters method with a String Array as parameter.
     */
    @Test
    public void testAddParameters_StringArr() {
        System.out.println("addParameters(String[])");
        String emptyParamName = "=" + VALID_VALUE;
        String emptyParamValue = VALID_NAME + "=";
        String validParam1 = VALID_NAME + "=" + VALID_VALUE;
        String validParam2 = VALID_NAME + "1" + "=" + VALID_VALUE + "1";
        String validParam3 = VALID_NAME + "2" + "=" + VALID_VALUE + "2";

        System.out.println("empty string");
        client.addParameters("");
        assertEquals(0, client.getParameters().size());

        System.out.println("wrong format");
        client.addParameters("wrongFormat");
        assertEquals(0, client.getParameters().size());

        System.out.println("empty paramName");
        client.addParameters(emptyParamName);
        assertEquals(0, client.getParameters().size());

        System.out.println("empty paramValue");
        client.addParameters(emptyParamValue);
        assertEquals(1, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(VALID_NAME));
        assertTrue(((String)client.getParameters().get(VALID_NAME)).isEmpty());

        System.out.println("valid");
        client.addParameters(validParam1);
        assertEquals(1, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(VALID_NAME));
        assertEquals(VALID_VALUE, client.getParameters().get(VALID_NAME));

        System.out.println("n params");
        client.addParameters(validParam2, validParam3);
        assertEquals(3, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(VALID_NAME + "1"));
        assertTrue(client.getParameters().containsKey(VALID_NAME + "2"));
        assertEquals(VALID_VALUE + "1", client.getParameters().get(VALID_NAME + "1"));
        assertEquals(VALID_VALUE + "2", client.getParameters().get(VALID_NAME + "2"));
    }

    /**
     * Test of addParameters method with a List of Strings as argument.
     */
    @Test
    public void testAddParameters_List() {
        System.out.println("addParameters(List<String>)");
        final String emptyParamName = "=" + VALID_VALUE;
        final String emptyParamValue = VALID_NAME + "=";
        final String validParam1 = VALID_NAME + "=" + VALID_VALUE;
        final String validParam2 = VALID_NAME + "1" + "=" + VALID_VALUE + "1";
        final String validParam3 = VALID_NAME + "2" + "=" + VALID_VALUE + "2";
        List<String> paramPairs = null;

        System.out.println("null");
        paramPairs = new ArrayList<String>(){{
            add(null);
        }};
        client.addParameters(paramPairs);
        assertEquals(0, client.getParameters().size());

        System.out.println("empty string");
        paramPairs = new ArrayList<String>(){{
            add("");
        }};
        client.addParameters(paramPairs);
        assertEquals(0, client.getParameters().size());

        System.out.println("wrong format");
        paramPairs = new ArrayList<String>(){{
            add("wrongFormat");
        }};
        client.addParameters(paramPairs);
        assertEquals(0, client.getParameters().size());

        System.out.println("empty paramName");
        paramPairs = new ArrayList<String>(){{
            add(emptyParamName);
        }};
        client.addParameters(paramPairs);
        assertEquals(0, client.getParameters().size());

        System.out.println("empty paramValue");
        paramPairs = new ArrayList<String>(){{
            add(emptyParamValue);
        }};
        client.addParameters(paramPairs);
        assertEquals(1, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(VALID_NAME));
        assertTrue(((String)client.getParameters().get(VALID_NAME)).isEmpty());

        System.out.println("valid");
        paramPairs = new ArrayList<String>(){{
            add(validParam1);
        }};
        client.addParameters(paramPairs);
        assertEquals(1, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(VALID_NAME));
        assertEquals(VALID_VALUE, client.getParameters().get(VALID_NAME));

        System.out.println("n params");
        paramPairs = new ArrayList<String>(){{
            add(validParam2);
            add(validParam3);
        }};
        client.addParameters(paramPairs);
        assertEquals(3, client.getParameters().size());
        assertTrue(client.getParameters().containsKey(VALID_NAME + "1"));
        assertTrue(client.getParameters().containsKey(VALID_NAME + "2"));
        assertEquals(VALID_VALUE + "1", client.getParameters().get(VALID_NAME + "1"));
        assertEquals(VALID_VALUE + "2", client.getParameters().get(VALID_NAME + "2"));
    }

    /**
     * Test of removeRequesHeaders method with a List of Strings as parameter.
     */
    @Test
    public void testRemoveRequesHeaders_List() {
        System.out.println("removeRequesHeaders(List<String>)");
        final String headerName1 = VALID_NAME + "1";
        final String headerName2 = VALID_NAME + "2";
        final String headerName3 = VALID_NAME + "3";
        client.addRequestHeader(headerName1, VALID_VALUE + "1");
        client.addRequestHeader(headerName2, VALID_VALUE + "2");
        client.addRequestHeader(headerName3, VALID_VALUE + "3");

        List<String> headerNames = null;

        System.out.println("null");
        client.removeRequesHeaders(headerNames);
        assertEquals(3, client.getRequestHeaders().size());

        System.out.println("missing headerName");
        headerNames = new ArrayList<String>(){{
            add("missing");
        }};
        client.removeRequesHeaders(headerNames);
        assertEquals(3, client.getRequestHeaders().size());

        System.out.println("valid headerName");
        headerNames = new ArrayList<String>(){{
            add(headerName1);
        }};
        client.removeRequesHeaders(headerNames);
        assertEquals(2, client.getRequestHeaders().size());
        assertNull(client.getRequestHeaders().get(headerName1));

        System.out.println("n valid headerNames");
        headerNames = new ArrayList<String>(){{
            add(headerName2);
            add(headerName3);
        }};
        client.removeRequesHeaders(headerNames);
        assertEquals(0, client.getRequestHeaders().size());
        assertNull(client.getRequestHeaders().get(headerName2));
        assertNull(client.getRequestHeaders().get(headerName3));
    }

    /**
     * Test of removeRequesHeaders method with a String Array as parameter.
     */
    @Test
    public void testRemoveRequesHeaders_StringArr() {
        System.out.println("removeRequesHeaders(String[]");
        String headerName1 = VALID_NAME + "1";
        String headerName2 = VALID_NAME + "2";
        String headerName3 = VALID_NAME + "3";
        client.addRequestHeader(headerName1, VALID_VALUE + "1");
        client.addRequestHeader(headerName2, VALID_VALUE + "2");
        client.addRequestHeader(headerName3, VALID_VALUE + "3");

        assertEquals(3, client.getRequestHeaders().size());

        System.out.println("missing headerName");
        client.removeRequesHeaders("missing");
        assertEquals(3, client.getRequestHeaders().size());

        System.out.println("valid headerName");
        client.removeRequesHeaders(headerName1);
        assertEquals(2, client.getRequestHeaders().size());
        assertNull(client.getRequestHeaders().get(headerName1));

        System.out.println("n valid headerName");
        client.removeRequesHeaders(headerName2, headerName3);
        assertEquals(0, client.getRequestHeaders().size());
        assertNull(client.getRequestHeaders().get(headerName2));
        assertNull(client.getRequestHeaders().get(headerName3));
    }

    /**
     * Test of removeParameters method, of class EasyRESTHttpClient.
     */
    @Test
    public void testRemoveParameters_List() {
        System.out.println("removeParameters(List<String>)");
        final String paramName1 = VALID_NAME + "1";
        final String paramName2 = VALID_NAME + "2";
        final String paramName3 = VALID_NAME + "3";
        client.addParameter(paramName1, VALID_VALUE + "1");
        client.addParameter(paramName2, VALID_VALUE + "2");
        client.addParameter(paramName3, VALID_VALUE + "3");

        List<String> paramNames = null;

        System.out.println("null");
        client.removeParameters(paramNames);
        assertEquals(3, client.getParameters().size());

        System.out.println("missing paramName");
        paramNames = new ArrayList<String>(){{
            add("missing");
        }};
        client.removeParameters(paramNames);
        assertEquals(3, client.getParameters().size());

        System.out.println("valid paramName");
        paramNames = new ArrayList<String>(){{
            add(paramName1);
        }};
        client.removeParameters(paramNames);
        assertEquals(2, client.getParameters().size());
        assertNull(client.getParameters().get(paramName1));

        System.out.println("n valid paramNames");
        paramNames = new ArrayList<String>(){{
            add(paramName2);
            add(paramName3);
        }};
        client.removeParameters(paramNames);
        assertEquals(0, client.getParameters().size());
        assertNull(client.getParameters().get(paramName2));
        assertNull(client.getParameters().get(paramName3));
    }

    /**
     * Test of removeParameters method, of class EasyRESTHttpClient.
     */
    @Test
    public void testRemoveParameters_StringArr() {
        System.out.println("removeParameters");
        String paramName1 = VALID_NAME + "1";
        String paramName2 = VALID_NAME + "2";
        String paramName3 = VALID_NAME + "3";
        client.addParameter(paramName1, VALID_VALUE + "1");
        client.addParameter(paramName2, VALID_VALUE + "2");
        client.addParameter(paramName3, VALID_VALUE + "3");

        System.out.println("null");
        assertEquals(3, client.getParameters().size());

        System.out.println("missing paramName");
        client.removeParameters("missing");
        assertEquals(3, client.getParameters().size());

        System.out.println("valid paramName");
        client.removeParameters(paramName1);
        assertEquals(2, client.getParameters().size());
        assertNull(client.getParameters().get(paramName1));

        System.out.println("n valid paramName");
        client.removeParameters(paramName2, paramName3);
        assertEquals(0, client.getParameters().size());
        assertNull(client.getParameters().get(paramName2));
        assertNull(client.getParameters().get(paramName3));
    }

    /**
     * Test of buildURI method.
     * Just check if the URI has http or not, on it.
     */
    @Test
    public void testBuildURI() {
        System.out.println("buildURI");
        String result;

        System.out.println("null");
        result = client.buildURI(null);
        assertNull(result);

        System.out.println("empty");
        result = client.buildURI("");
        assertEquals("", result);

        System.out.println("invalid");
        result = client.buildURI("invalid");
        assertTrue(result.indexOf("http") > -1);

        System.out.println("valid from host");
        result = client.buildURI("/localhost/valid");
        assertTrue(result.indexOf("http") > -1);

        System.out.println("valid");
        result = client.buildURI("http://localhost/valid");
        assertTrue(result.indexOf("http") > -1);
    }

    /**
     * Test of toQueryString method. Receives a Map&lt;String, Object&gt; and returns
     * a String in format key1=value1[&key2=value2...]
     */
    @Test
    public void testToQueryString() {
        System.out.println("toQueryString");
        Map<String, Object> pairs = null;
        final String key1 = VALID_NAME + "1";
        final String key2 = VALID_NAME + "2";
        final String key3 = VALID_NAME + "3";
        final String value1 = VALID_VALUE + "1";
        final String value2 = VALID_VALUE + "2";
        final String value3 = VALID_VALUE + "3";
        Pattern compile = Pattern.compile("^(([\\w]+\\=[\\w]+)?(?:\\&[\\w]+\\=[\\w]+)*)$");
        Matcher match;

        String result;

        System.out.println("null");
        result = client.toQueryString(pairs);
        assertEquals("", result);

        System.out.println("key null");
        pairs = new HashMap<String, Object>(){{
            put(key1, value1);
            put(null, value3);
            put(key2, value2);
        }};
        result = client.toQueryString(pairs);
        match = compile.matcher(result);
        assertTrue(match.matches());
        assertTrue(result.indexOf("null") == -1);
    }

    /**
     * Test of toMap method. Converts Strings in format key=value to a Map<String, Object>
     */
    @Test
    public void testToMap_List() {
        System.out.println("toMap(List<String>)");
        List<String> pairs = null;
        Map result;

        System.out.println("null");
        result = client.toMap(pairs);
        assertNull(result);

        System.out.println("all tests");
        pairs = new ArrayList<String>(){{
            add(null);
            add("");
            add("invalid");
            add(VALID_NAME + "=" + VALID_VALUE);
        }};
        result = client.toMap(pairs);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_VALUE, result.get(VALID_NAME));
    }

    /**
     * Test of toMap method, of class EasyRESTHttpClient.
     */
    @Test
    public void testToMap_StringArr() {
        System.out.println("toMap");
        Map result;

        System.out.println("all tests");
        result = client.toMap(null, "", "invalid", VALID_NAME + "=" + VALID_VALUE, VALID_NAME + "=" + VALID_VALUE);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_VALUE, result.get(VALID_NAME));
    }

}