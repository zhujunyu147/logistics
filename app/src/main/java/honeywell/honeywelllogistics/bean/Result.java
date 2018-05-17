package honeywell.honeywelllogistics.bean;


import java.util.ArrayList;

import honeywell.honeywelllogistics.interfaces.IData;

/**
 * Created by zhujunyu on 2017/3/16.
 */

public class Result {
    public static final int RESULT_OK = 0;
    public static final int RESULT_ERROR = -1;
    public static final int RESULT_EXCEPTION = -2;

    public ArrayList<IData> resultList = new ArrayList<IData>();

    public Object[] strArray;

    public int resultCode;
}
