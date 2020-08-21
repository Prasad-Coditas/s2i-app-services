package com.snap2buy.themobilebackend.model;

/**
 * @author Harshal Patil
 */
public class UserAppConfig {

    private String userId;

    private String appVersionCode;

    private String appVersionName;

    private String cameraResolution;

    private String deviceBrand;

    private String deviceModel;

    private String displayDPI;

    private String displayDensity;

    private String installationDate;

    private String manufacturer;

    private String osName;

    private String screenResolution;

    private String sdkVersion;

    private String updationDate;
    
    private String platform;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(String appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }

    public String getCameraResolution() {
        return cameraResolution;
    }

    public void setCameraResolution(String cameraResolution) {
        this.cameraResolution = cameraResolution;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public void setDeviceBrand(String deviceBrand) {
        this.deviceBrand = deviceBrand;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDisplayDPI() {
        return displayDPI;
    }

    public void setDisplayDPI(String displayDPI) {
        this.displayDPI = displayDPI;
    }

    public String getDisplayDensity() {
        return displayDensity;
    }

    public void setDisplayDensity(String displayDensity) {
        this.displayDensity = displayDensity;
    }

    public String getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(String installationDate) {
        this.installationDate = installationDate;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getScreenResolution() {
        return screenResolution;
    }

    public void setScreenResolution(String screenResolution) {
        this.screenResolution = screenResolution;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getUpdationDate() {
        return updationDate;
    }

    public void setUpdationDate(String updationDate) {
        this.updationDate = updationDate;
    }

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}
}
