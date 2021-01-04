package com.aaa.lib.map.imp.model;

public class RobotStatusBean {
    /**
     * "status"	工作状态 enum
     * eStatusNoWork	无工作
     * eStatusPausing	暂停工作
     * eStatusWorking	工作中
     * eStatusStopWorking	停止工作
     * "mode" 清扫模式 enum
     * eModeAutoClean	自动清扫
     * eModeAreaClean	划区清扫
     * eModeRoomClean	按房间清扫
     * eModeLocationClean	定点清扫
     * eModeRCClean	遥控清扫
     * eModeStandby	待机模式
     * eModeDocking	回充中
     * eModeCharging 	充电中
     * eModeError	错误模式
     * "noDisturbSwitch"	勿扰开关 bool
     * TRUE	勿扰打开
     * FALSE	勿扰关闭
     * "noDisturbFrom"	string		开始时间
     * "noDisturbTo"			结束时间
     */
    private int status;
    private int mode;
    private boolean noDisturbSwitch;
    private String noDisturbFrom;
    private String noDisturbTo;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean getNoDisturbSwitch() {
        return noDisturbSwitch;
    }

    public void setNoDisturbSwitch(boolean noDisturbSwitch) {
        this.noDisturbSwitch = noDisturbSwitch;
    }

    public String getNoDisturbFrom() {
        return noDisturbFrom;
    }

    public void setNoDisturbFrom(String noDisturbFrom) {
        this.noDisturbFrom = noDisturbFrom;
    }

    public String getNoDisturbTo() {
        return noDisturbTo;
    }

    public void setNoDisturbTo(String noDisturbTo) {
        this.noDisturbTo = noDisturbTo;
    }

}
