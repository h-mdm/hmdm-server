package com.hmdm.rest.json;

import java.util.List;

public class SummaryResponse {
    private List<ChartItem> statusSummary;
    private List<ChartItem> installSummary;
    private long devicesTotal;
    private long devicesEnrolled;
    private long devicesEnrolledLastMonth;
    private List<ChartItem> devicesEnrolledMonthly;

    private List<String> topConfigs;

    private List<Integer> statusOfflineByConfig;
    private List<Integer> statusIdleByConfig;
    private List<Integer> statusOnlineByConfig;

    private List<Integer> appFailureByConfig;
    private List<Integer> appMismatchByConfig;
    private List<Integer> appSuccessByConfig;


    public List<ChartItem> getStatusSummary() {
        return statusSummary;
    }

    public void setStatusSummary(List<ChartItem> statusSummary) {
        this.statusSummary = statusSummary;
    }

    public List<ChartItem> getInstallSummary() {
        return installSummary;
    }

    public void setInstallSummary(List<ChartItem> installSummary) {
        this.installSummary = installSummary;
    }

    public long getDevicesTotal() {
        return devicesTotal;
    }

    public void setDevicesTotal(long devicesTotal) {
        this.devicesTotal = devicesTotal;
    }

    public long getDevicesEnrolled() {
        return devicesEnrolled;
    }

    public void setDevicesEnrolled(long devicesEnrolled) {
        this.devicesEnrolled = devicesEnrolled;
    }

    public long getDevicesEnrolledLastMonth() {
        return devicesEnrolledLastMonth;
    }

    public void setDevicesEnrolledLastMonth(long devicesEnrolledLastMonth) {
        this.devicesEnrolledLastMonth = devicesEnrolledLastMonth;
    }

    public List<ChartItem> getDevicesEnrolledMonthly() {
        return devicesEnrolledMonthly;
    }

    public void setDevicesEnrolledMonthly(List<ChartItem> devicesEnrolledMonthly) {
        this.devicesEnrolledMonthly = devicesEnrolledMonthly;
    }

    public List<String> getTopConfigs() {
        return topConfigs;
    }

    public void setTopConfigs(List<String> topConfigs) {
        this.topConfigs = topConfigs;
    }

    public List<Integer> getStatusOfflineByConfig() {
        return statusOfflineByConfig;
    }

    public void setStatusOfflineByConfig(List<Integer> statusOfflineByConfig) {
        this.statusOfflineByConfig = statusOfflineByConfig;
    }

    public List<Integer> getStatusIdleByConfig() {
        return statusIdleByConfig;
    }

    public void setStatusIdleByConfig(List<Integer> statusIdleByConfig) {
        this.statusIdleByConfig = statusIdleByConfig;
    }

    public List<Integer> getStatusOnlineByConfig() {
        return statusOnlineByConfig;
    }

    public void setStatusOnlineByConfig(List<Integer> statusOnlineByConfig) {
        this.statusOnlineByConfig = statusOnlineByConfig;
    }

    public List<Integer> getAppFailureByConfig() {
        return appFailureByConfig;
    }

    public void setAppFailureByConfig(List<Integer> appFailureByConfig) {
        this.appFailureByConfig = appFailureByConfig;
    }

    public List<Integer> getAppMismatchByConfig() {
        return appMismatchByConfig;
    }

    public void setAppMismatchByConfig(List<Integer> appMismatchByConfig) {
        this.appMismatchByConfig = appMismatchByConfig;
    }

    public List<Integer> getAppSuccessByConfig() {
        return appSuccessByConfig;
    }

    public void setAppSuccessByConfig(List<Integer> appSuccessByConfig) {
        this.appSuccessByConfig = appSuccessByConfig;
    }
}
