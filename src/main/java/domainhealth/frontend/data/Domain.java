package domainhealth.frontend.data;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiovcr on 05/12/2014.
 */
//@XmlRootElement
public class Domain {



    private String domainName;

    private List<Server> servers = new ArrayList<Server>();


    public Domain(String domainName){
        this.domainName = domainName;
    }

    public void addServer(Server server){
        servers.add(server);
    }


    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }


}
