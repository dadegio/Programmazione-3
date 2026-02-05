package CommonResources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Email implements Serializable {

    private static final long serialVersionUID = 1L; // Identificatore univoco
    private String dest;
    private List<String> destsEmail = new ArrayList<>();    private String mittEmail, argEmail, testoEmail;
    private int priorEmail;
    private String dataSpedEmail;
    boolean isRead;
    int cntEmail;

    public boolean getIsRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public List<String> getDestsEmail() {
        return new ArrayList<>(destsEmail); // Return a copy to avoid external modification
    }

    public void setDestsEmail(List<String> destsEmail) {
        this.destsEmail = new ArrayList<>(destsEmail); // Copy to ensure immutability
    }

    public String getDest() {
        return dest;
    }

    public String getArgEmail() {
        return argEmail;
    }

    public void setArgEmail(String argEmail) {
        this.argEmail = argEmail;
    }

    public String getTestoEmail() {
        return testoEmail;
    }

    public void setTestoEmail(String testoEmail) {
        this.testoEmail = testoEmail;
    }

    public int getPriorEmail() {
        return priorEmail;
    }

    public void setPriorEmail(int priorEmail) {
        this.priorEmail = priorEmail;
    }

    public String getMittEmail() {
        return mittEmail;
    }

    public void setMittEmail(String mittEmail) {
        this.mittEmail = mittEmail;
    }

    public String getCcString() {
        return String.join(",", destsEmail); // Concatenate emails with commas
    }

    public String getDataSpedEmail() {
        return dataSpedEmail;
    }

    public void setDataSpedEmail(String dataSpedEmail) {
        this.dataSpedEmail = dataSpedEmail;
    }

    public String toString() {
        return "commonResources.Email{" +
                "destsEmail=" + destsEmail +
                ", mittEmail='" + mittEmail + '\'' +
                ", argEmail='" + argEmail + '\'' +
                ", testoEmail='" + testoEmail + '\'' +
                ", priorEmail=" + priorEmail +
                ", dataSpedEmail=" + dataSpedEmail +
                ", getIsRead=" + isRead +
                '}';
    }

    public void setCntEmail(int cntEmail) {
        this.cntEmail = cntEmail;
    }

    public int getCntEmail() {
        return cntEmail;
    }

    public void setDest(String emailField) {
        this.dest = emailField;
    }
}
