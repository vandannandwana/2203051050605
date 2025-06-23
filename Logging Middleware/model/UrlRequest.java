package com.exp.LoggingMiddleware.model;

public class UrlRequest {

    private String url;
    private int validity;
    private String shortcode;

    public UrlRequest() {
    }

    public UrlRequest(String url, int validity, String shortcode) {
        this.url = url;
        this.validity = validity;
        this.shortcode = shortcode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getValidity() {
        return validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    public String getShortcode() {
        return shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }
}
