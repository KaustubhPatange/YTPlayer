
package com.kpstv.youtube.models.spotify;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Artist_ {

    @SerializedName("external_urls")

    private ExternalUrls__ externalUrls;
    @SerializedName("href")

    private String href;
    @SerializedName("id")

    private String id;
    @SerializedName("name")

    private String name;
    @SerializedName("type")

    private String type;
    @SerializedName("uri")

    private String uri;

    public ExternalUrls__ getExternalUrls() {
        return externalUrls;
    }

    public void setExternalUrls(ExternalUrls__ externalUrls) {
        this.externalUrls = externalUrls;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
