package edu.ranken.prsmith.nprtechnews.request;

import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class GsonRequest<T> extends Request<T> {
    private static final String LOG_TAG = "NPRTechNews";

    private final Gson gson = new Gson();
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Object requestBody;
    private final Response.Listener<T> listener;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     * @param clazz Relevant class object, for Gson's reflection
     * @param headers Map of request headers
     * @param requestBody JSON data to be posted as the body of the request.
     *                    Or null to skip the request body.
     * @param listener Listener to receive the parsed response
     * @param errorListener Error listener, or null to ignore errors
     */
    public GsonRequest(
        int method,
        String url,
        Class<T> clazz,
        @Nullable Map<String, String> headers,
        @Nullable Object requestBody,
        @Nullable Response.Listener<T> listener,
        @Nullable Response.ErrorListener errorListener
    ) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.headers = headers;
        this.requestBody = requestBody;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers != null ? headers : Collections.emptyMap();
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }

    @Override
    public byte[] getBody() {
        try {
            if (requestBody != null) {
                return gson.toJson(requestBody).getBytes(StandardCharsets.UTF_8);
            } else {
                return null;
            }
        } catch (Exception ex) {
            //throw new VolleyError("Failed to serialize request body", ex);
            Log.e(LOG_TAG, "Failed to serialize request body", ex);
            return null;
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String charset = HttpHeaderParser.parseCharset(response.headers, "utf-8");
            String json = new String(response.data, charset);
            T obj = gson.fromJson(json, clazz);

            Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
            return Response.success(obj, cacheEntry);
        } catch (Exception ex) {
            return Response.error(new ParseError(ex));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        if (listener != null) {
            listener.onResponse(response);
        }
    }
}
