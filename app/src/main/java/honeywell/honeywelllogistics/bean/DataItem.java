package honeywell.honeywelllogistics.bean;

/**
 * Created by zhujunyu on 2018/5/24.
 */

public class DataItem {
    private String gateMessage;
    private String locDetail;
    private String vin;

    public void setGateMessage(String gateMessage) {
        this.gateMessage = gateMessage;
    }

    public void setLocDetail(String locDetail) {
        this.locDetail = locDetail;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getGateMessage() {
        return gateMessage;
    }

    public String getLocDetail() {
        return locDetail;
    }

    public String getVin() {
        return vin;
    }
}
