package com.hmdm.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONObject;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateEntry implements Serializable {

    private static final long serialVersionUID = 514589555335932784L;

    public static final String WEB_PKG = "web";
    public static final String LAUNCHER_PKG = "com.hmdm.launcher";

    public static final String DISABLED_CUSTOM = "custom";
    public static final String DISABLED_NOT_MASTER = "not_master";
    public static final String DISABLED_MULTIPLE = "multiple";
    public static final String DISABLED_ERROR = "error";
    public static final String DISABLED_DOWNLOAD = "download";

    private String name;
    private String pkg;
    private String currentVersion;
    private String version;
    private String url;
    private boolean downloaded;
    private boolean updateDisabled;
    private String updateDisableReason;
    private boolean outdated;

    public UpdateEntry() {}

    public UpdateEntry(JSONObject json) {
        pkg = json.getString("pkg");
        version = json.getString("version");
        url = json.getString("url");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public boolean isUpdateDisabled() {
        return updateDisabled;
    }

    public void setUpdateDisabled(boolean updateDisabled) {
        this.updateDisabled = updateDisabled;
    }

    public String getUpdateDisableReason() {
        return updateDisableReason;
    }

    public void setUpdateDisableReason(String updateDisableReason) {
        this.updateDisableReason = updateDisableReason;
    }

    public boolean isOutdated() {
        return outdated;
    }

    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
    }
}
