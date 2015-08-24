package io.kickflip.sdk.interceptor;

import retrofit.RequestInterceptor;

public class KickflipInterceptor implements RequestInterceptor {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TYPE_JSON = "application/json";

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader(CONTENT_TYPE, TYPE_JSON);
    }
}
