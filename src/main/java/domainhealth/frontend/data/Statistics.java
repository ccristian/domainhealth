package domainhealth.frontend.data;

import org.joda.time.Interval;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by cristianchiovari on 11/8/15.
 */

@XmlRootElement(name="stats")
public class Statistics {

    public Statistics(Interval interval){
        this.interval = interval;
    }

    @XmlElement(name="xxx")
    Interval interval;

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }
}
