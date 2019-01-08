package domainhealth.frontend.data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by cristianchiovari on 11/8/15.
 */

@XmlRootElement(name="stats")
public class Statistics {

    private String a;

    private String b;

    public Statistics(){

    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }
}
