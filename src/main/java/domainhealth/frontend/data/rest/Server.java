package domainhealth.frontend.data.rest;

import domainhealth.frontend.data.DateAmountDataSet;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiovcr on 05/12/2014.
 */
@XmlRootElement
public class Server {

    private String serverName;

    private List<DateAmountDataSet> statistics= new ArrayList<DateAmountDataSet>();

    public List<DateAmountDataSet> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<DateAmountDataSet> statistics) {
        this.statistics = statistics;
    }

    public Server(String serverName){
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
