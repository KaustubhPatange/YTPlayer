
package com.kpstv.youtube.models.spotify;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Tracks {

    @SerializedName("album")

    private Album album;
    @SerializedName("artists")

    private List<Artist_> artists = null;
    @SerializedName("available_markets")

    private List<String> availableMarkets = null;
    @SerializedName("disc_number")

    private Integer discNumber;
    @SerializedName("duration_ms")

    private Integer durationMs;
    @SerializedName("explicit")

    private Boolean explicit;
    @SerializedName("external_ids")

    private ExternalIds externalIds;
    @SerializedName("external_urls")

    private ExternalUrls___ externalUrls;
    @SerializedName("href")

    private String href;
    @SerializedName("id")

    private String id;
    @SerializedName("is_local")

    private Boolean isLocal;
    @SerializedName("name")

    private String name;
    @SerializedName("popularity")

    private Integer popularity;
    @SerializedName("preview_url")

    private Object previewUrl;
    @SerializedName("track_number")

    private Integer trackNumber;
    @SerializedName("type")

    private String type;
    @SerializedName("uri")

    private String uri;

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public List<Artist_> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist_> artists) {
        this.artists = artists;
    }

    public List<String> getAvailableMarkets() {
        return availableMarkets;
    }

    public void setAvailableMarkets(List<String> availableMarkets) {
        this.availableMarkets = availableMarkets;
    }

    public Integer getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(Integer discNumber) {
        this.discNumber = discNumber;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public Boolean getExplicit() {
        return explicit;
    }

    public void setExplicit(Boolean explicit) {
        this.explicit = explicit;
    }

    public ExternalIds getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(ExternalIds externalIds) {
        this.externalIds = externalIds;
    }

    public ExternalUrls___ getExternalUrls() {
        return externalUrls;
    }

    public void setExternalUrls(ExternalUrls___ externalUrls) {
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

    public Boolean getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(Boolean isLocal) {
        this.isLocal = isLocal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public Object getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(Object previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Integer getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(Integer trackNumber) {
        this.trackNumber = trackNumber;
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
