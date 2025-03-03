package model;

public class Alerts 
{
    int alertId;
    String alertMessage;
    String alertDate;

    Alerts(){}

    public Alerts(int alertId, String alertMessage, String alertDate) {
        this.alertId = alertId;
        this.alertMessage = alertMessage;
        this.alertDate = alertDate;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getAlertDate() {
        return alertDate;
    }

    public void setAlertDate(String alertDate) {
        this.alertDate = alertDate;
    };
    
    @Override
    public String toString() {
        return "Alerts [alertId=" + alertId + ", alertMessage=" + alertMessage + ", alertDate=" + alertDate +  "]";
    }


}
