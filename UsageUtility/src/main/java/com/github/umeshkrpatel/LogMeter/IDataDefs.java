package com.github.umeshkrpatel.LogMeter;

public interface IDataDefs {
    String kPackageName = "com.github.umeshkrpatel.LogMeter";
    String kAuthority = kPackageName + ".data";
    enum Type {
        TYPE_NONE,
        TYPE_BILLPERIOD,
        TYPE_MIXED,
        TYPE_CALL,
        TYPE_SMS,
        TYPE_MMS,
        TYPE_DATA_MOBILE,
        TYPE_DATA_WIFI,
        TYPE_UNKNOWN;
        public int toInt() {
            return this.ordinal();
        }
        public static Type fromInt(int type) {
            return Type.values()[type];
        }
    };

    enum BillPeriod {
        DAY01,
        WEEK01,
        DAY14,
        DAY15,
        DAY28,
        DAY30,
        DAY31,
        DAY60,
        DAY90,
        MONTH01,
        MONTH1DAY1,
        MONTH02,
        MONTH03,
        MONTH04,
        MONTH05,
        MONTH06,
        MONTH12,
        BPINF;
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

    interface ILogs {
        String TABLE = "logs";
        int INDEX_ID = 0;
        int INDEX_PLAN_ID = 1;
        int INDEX_RULE_ID = 2;
        int INDEX_TYPE = 3;
        int INDEX_DIRECTION = 4;
        int INDEX_DATE = 5;
        int INDEX_AMOUNT = 6;
        int INDEX_BILL_AMOUNT = 7;
        int INDEX_REMOTE = 8;
        int INDEX_ROAMED = 9;
        int INDEX_COST = 10;
        int INDEX_FREE = 11;
        int INDEX_MYNUMBER = 12;
        int INDEX_PLAN_NAME = 13;
        int INDEX_RULE_NAME = 14;
        int INDEX_PLAN_TYPE = 15;
        String ID = "_id";
        String PLAN_ID = "_plan_id";
        String RULE_ID = "_rule_id";
        String TYPE = "_type";
        String DIRECTION = "_direction";
        String DATE = "_date";
        String AMOUNT = "_amount";
        String BILL_AMOUNT = "_bill_amount";
        String REMOTE = "_remote";
        String ROAMED = "_roamed";
        String COST = "_logs_cost";
        String FREE = "_logs_cost_free";
        String MYNUMBER = "_mynumber";
    }
}
