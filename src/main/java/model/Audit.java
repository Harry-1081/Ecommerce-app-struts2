package model;

public class Audit 
{
    int auditId;
    String time;
    String action;
    String status;
    String doneBy;
    String parameter;

    Audit(){}

    public Audit(int auditId,String time, String action, String status, String doneBy, String parameter) {
        this.auditId = auditId;
        this.time = time;
        this.action = action;
        this.status = status;
        this.doneBy = doneBy;
        this.parameter = parameter;
    }

    public int getAuditId() {
        return auditId;
    }

    public void setAuditId(int auditId) {
        this.auditId = auditId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDoneBy() {
        return doneBy;
    }

    public void setDoneBy(String doneBy) {
        this.doneBy = doneBy;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    };

}
