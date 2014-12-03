package domainhealth.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;

/**
 * Created by chiovcr on 02/12/2014.
 */
// The Java class will be hosted at the URI path "/helloworld"
@Path("/storage")
public class StorageService {
    // The Java method will process HTTP GET requests
    @GET
    // The Java method will produce content identified by the MIME Media type "text/plain"
    @Produces("text/plain")
    public String getClichedMessage() {
        // Return some cliched textual content
        return "Domainhealth RS";
    }

}
