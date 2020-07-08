package com.th.netty_server.entity;

public class ChinaFlowMeterStatus {
    private String pressLowCheck;
    private String pressHighCheck;
    private String tempHighCheck;
    private String tempLowCheck;
    private String tempSensorCheck;
    private String pressSensorCheck;
    private String magenetCheck;

    public ChinaFlowMeterStatus() {
    }

    public ChinaFlowMeterStatus(String pressLowCheck, String pressHighCheck, String tempHighCheck, String tempLowCheck, String tempSensorCheck, String pressSensorCheck, String magenetCheck) {
        this.pressLowCheck = pressLowCheck;
        this.pressHighCheck = pressHighCheck;
        this.tempHighCheck = tempHighCheck;
        this.tempLowCheck = tempLowCheck;
        this.tempSensorCheck = tempSensorCheck;
        this.pressSensorCheck = pressSensorCheck;
        this.magenetCheck = magenetCheck;
    }

    public String getPressLowCheck() {
        return pressLowCheck;
    }

    public void setPressLowCheck(String pressLowCheck) {
        this.pressLowCheck = pressLowCheck;
    }

    public String getPressHighCheck() {
        return pressHighCheck;
    }

    public void setPressHighCheck(String pressHighCheck) {
        this.pressHighCheck = pressHighCheck;
    }

    public String getTempHighCheck() {
        return tempHighCheck;
    }

    public void setTempHighCheck(String tempHighCheck) {
        this.tempHighCheck = tempHighCheck;
    }

    public String getTempLowCheck() {
        return tempLowCheck;
    }

    public void setTempLowCheck(String tempLowCheck) {
        this.tempLowCheck = tempLowCheck;
    }

    public String getTempSensorCheck() {
        return tempSensorCheck;
    }

    public void setTempSensorCheck(String tempSensorCheck) {
        this.tempSensorCheck = tempSensorCheck;
    }

    public String getPressSensorCheck() {
        return pressSensorCheck;
    }

    public void setPressSensorCheck(String pressSensorCheck) {
        this.pressSensorCheck = pressSensorCheck;
    }

    public String getMagenetCheck() {
        return magenetCheck;
    }

    public void setMagenetCheck(String magenetCheck) {
        this.magenetCheck = magenetCheck;
    }
}
