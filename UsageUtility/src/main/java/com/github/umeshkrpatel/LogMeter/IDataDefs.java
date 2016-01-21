package com.github.umeshkrpatel.LogMeter;

public interface IDataDefs {
    String kPackageName = "com.github.umeshkrpatel.LogMeter";
    String kAuthority = kPackageName + ".data";
    enum Type {
        TYPE_NONE(-1),
        TYPE_TITLE(0),
        TYPE_SPACING(1),
        TYPE_BILLPERIOD(2),
        TYPE_MIXED(3),
        TYPE_CALL(4),
        TYPE_SMS(5),
        TYPE_MMS(6),
        TYPE_DATA_MOBILE(7),
        TYPE_DATA_WIFI(8),
        TYPE_UNKNOWN(9);
        private Integer eValue;
        Type (Integer value) {
            eValue = value;
        }
        public int toInt() {
            return eValue;
        }
        public static Type fromInt(int type) {
            return Type.values()[type];
        }
    };

    enum BillPeriod {
        DAY01(0),
        WEEK01(1),
        DAY14(2),
        DAY15(3),
        DAY28(17),
        DAY30(4),
        DAY31(5),
        DAY60(6),
        DAY90(7),
        MONTH01(8),
        MONTH1DAY1(9),
        MONTH02(10),
        MONTH03(11),
        MONTH04(12),
        MONTH05(13),
        MONTH06(14),
        MONTH12(15),
        BPINF(16);
        private int eValue;
        BillPeriod(int value) {
            eValue = value;
        }
        public int toInt() {
            return this.ordinal();
        }
        public static BillPeriod fromInt(int bp) {
            return BillPeriod.values()[bp];
        }
    }

    /**
     * Direction of log: in.
     */
    int DIRECTION_IN = 0;
    /**
     * Direction of log: out.
     */
    int DIRECTION_OUT = 1;
    /**
     * Type of limit: none.
     */
    int LIMIT_TYPE_NONE = 0;
    /**
     * Type of limit: units.
     */
    int LIMIT_TYPE_UNITS = 1;
    /**
     * Type of limit: cost.
     */
    int LIMIT_TYPE_COST = 2;
    /**
     * Plan/rule id: not yet calculated.
     */
    int NO_ID = -1;
    /**
     * Plan/rule id: no plan/rule found.
     */
    int NOT_FOUND = -2;
}
