package com.aaa.lib.map.imp.model;


import android.content.Context;

public class RobotStatus {
    /**
     * 机器清扫状态
     * 0.待机
     * 1.清扫中
     * 2.清扫暂停
     * 3.回充
     * 4.回充暂停
     * 5.充电
     * 6.休眠
     */
    public static final WorkState STANDBY = new WorkState(0);
    public static final WorkState CLEANING_PAUSE = new WorkState(2);
    public static final WorkState RECHARGE = new WorkState(3);
    public static final WorkState RECHARGE_PAUSE = new WorkState(4);
    public static final WorkState CHARGING = new ChargingState();
    public static final WorkState ERROR = new ErrorState();
    public static final WorkState DORMANT = new WorkState(7);

    /**
     * 清扫模式
     * <p>
     * 自动清扫
     * 全局清扫
     * 划区清扫
     * 选区清扫
     * 定点清扫
     * 延边清扫
     * 遥控清扫
     */
    public static final WorkState AUTO = new CleaningState(10);
    public static final WorkState GLOBAL = new CleaningState(11);
    public static final WorkState CUSTOM = new CleaningState(12);
    public static final WorkState ROOM = new CleaningState(13);
    public static final WorkState DESIGNATED = new CleaningState(14);
    public static final WorkState BORDER = new CleaningState(15);
    public static final WorkState CONTROL = new CleaningState(16);

    private WorkState workState;
    private int battery;
    private int water;


    public WorkState getWorkState() {
        return workState;
    }

    public void setWorkState(WorkState workState) {
        this.workState = workState;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public static class WorkState {
        int state;

        private WorkState(int state) {
            this.state = state;
        }

        public static WorkState getBy(int state) {

            //非清扫状态
            if (state == STANDBY.get()) {
                return STANDBY;
            } else if (state == CLEANING_PAUSE.get()) {
                return CLEANING_PAUSE;
            } else if (state == RECHARGE.get()) {
                return RECHARGE;
            } else if (state == RECHARGE_PAUSE.get()) {
                return RECHARGE_PAUSE;
            } else if (state == CHARGING.get()) {
                return CHARGING;
            } else if (state == ERROR.get()) {
                return ERROR;
            } else if (state == DORMANT.get()) {
                return DORMANT;
            }
            //清扫状态
            else if (state == AUTO.get()) {
                return GLOBAL;
            } else if (state == GLOBAL.get()) {
                return GLOBAL;
            } else if (state == CUSTOM.get()) {
                return CUSTOM;
            } else if (state == ROOM.get()) {
                return ROOM;
            } else if (state == DESIGNATED.get()) {
                return DESIGNATED;
            } else if (state == BORDER.get()) {
                return BORDER;
            } else if (state == CONTROL.get()) {
                return BORDER;
            } else {
                return STANDBY;
            }
        }

        public int get() {
            return state;
        }
    }

    public static class ErrorState extends WorkState {
        private int errorCode;
        private String errorInfo="";
        private String errorDesc;

        private ErrorState() {
            super(6);
        }


        public void setError(String error) {
            errorInfo = error;
        }

        public String getErrorInfo() {
            return errorInfo;
        }
    }

    public static class CleaningState extends WorkState {

        /**
         * 清扫模式
         * <p>
         * 自动清扫
         * 全局清扫
         * 划区清扫
         * 选区清扫
         * 定点清扫
         * 延边清扫
         * 遥控清扫
         */
        private int cleanMode;

        private CleaningState(int mode) {
            super(1);
            this.cleanMode = mode;
        }

        public int getCleanMode(){
            return cleanMode;
        }
    }


    public static class ChargingState extends WorkState {
        boolean isFull = false;

        private ChargingState() {
            super(5);
        }

        public boolean isFull() {
            return isFull;
        }

        public void setFull(boolean full) {
            isFull = full;
        }

    }


    /*

    public static class CleanMode implements WorkState {
        private static CleanMode AUTO = new CleanMode(0);
        private static CleanMode GLOBAL = new CleanMode(1);
        private static CleanMode CUSTOM = new CleanMode(2);
        private static CleanMode ROOM = new CleanMode(3);
        private static CleanMode DESIGNATED = new CleanMode(4);
        private static CleanMode BORDER = new CleanMode(5);
        private static CleanMode CONTROL = new CleanMode(6);

        int mode;

        CleanMode(int mode) {
            this.mode = mode;
        }

        public static CleanMode getBy(int mode) {
            if (mode == AUTO.get()) {
                return AUTO;
            } else if (mode == GLOBAL.get()) {
                return GLOBAL;
            } else if (mode == CUSTOM.get()) {
                return CUSTOM;
            } else if (mode == ROOM.get()) {
                return ROOM;
            } else if (mode == DESIGNATED.get()) {
                return DESIGNATED;
            } else if (mode == BORDER.get()) {
                return BORDER;
            } else if (mode == CONTROL.get()) {
                return BORDER;
            } else {
                return AUTO;
            }
        }

        public int get() {
            return mode;
        }
    }


    public static class WorkState {
        private static WorkState STANDBY(0),CLEANING(1),

        CLEANING_PAUSE(2),RECHARGE(3),

        RECHARGE_PAUSE(4),CHARGING(5),

        ERROR(6),DORMANT(7);
        int mode;

        WorkState(int mode) {
            this.mode = mode;
        }

        public static WorkState getBy(int mode) {
            if (mode == STANDBY.get()) {
                return STANDBY;
            } else if (mode == CLEANING.get()) {
                return CLEANING;
            } else if (mode == CLEANING_PAUSE.get()) {
                return CLEANING_PAUSE;
            } else if (mode == RECHARGE.get()) {
                return RECHARGE;
            } else if (mode == RECHARGE_PAUSE.get()) {
                return RECHARGE_PAUSE;
            } else if (mode == CHARGING.get()) {
                return CHARGING;
            } else if (mode == ERROR.get()) {
                return ERROR;
            } else if (mode == DORMANT.get()) {
                return DORMANT;
            } else {
                return STANDBY;
            }
        }

        public int get() {
            return mode;
        }
    }*/
}


