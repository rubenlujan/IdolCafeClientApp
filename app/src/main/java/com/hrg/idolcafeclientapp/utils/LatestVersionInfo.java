package com.hrg.idolcafeclientapp.utils;

public class LatestVersionInfo {
    private int versionCode;
    private String versionName;
    private String apkUrl;
    private String whatsNew; // Puede ser null

    // Constructor vacío (útil para librerías de parseo JSON como Gson o Jackson)
    public LatestVersionInfo() {}

    // Getters
    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public String getWhatsNew() {
        return whatsNew;
    }

    // Opcional: Setters si necesitas crear objetos manualmente
    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public void setWhatsNew(String whatsNew) {
        this.whatsNew = whatsNew;
    }
}
