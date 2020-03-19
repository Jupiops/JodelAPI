package com.fr31b3u73r.jodel;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.stream.Collectors.toMap;

public class JodelHttpResponse {

    private int statusCode;
    private byte[] content;
    private Map<String, String> headers = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    public JodelHttpResponse(int statusCode, byte[] content, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.content = content;
        this.headers = headers;
    }

    public JodelHttpResponse() {
        this(0, null, null);
    }

    /**
     * Returns the HTTP status code (ex: 200, 404, etc) associated with this
     * response.
     *
     * @return The HTTP status code associated with this response.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the HTTP status code that was returned with this response.
     *
     * @param statusCode The HTTP status code (ex: 200, 404, etc) associated with this
     *                   response.
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns the byte array containing the response content.
     *
     * @return The byte array containing the response content.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the byte array containing the response content.
     *
     * @param content The byte array containing the response content.
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    /**
     * Returns the HTTP headers returned with this response.
     *
     * @return The set of HTTP headers returned with this HTTP response.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> String.join(", ", e.getValue())));
    }

    /**
     * Looks up a header by name and returns its value. Does case insensitive comparison.
     *
     * @param headerName Name of header to get value for.
     * @return The header value of the given header. Null if header is not present.
     */
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }
}
