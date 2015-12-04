package domainhealth.rest;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import domainhealth.frontend.data.Statistics;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

/**
 * Created by cristianchiovari on 11/29/15.
 */
@Provider
@Produces("application/json")
public class JAXBJSONContextResolver implements ContextResolver<JAXBContext> {
    private JAXBContext context;
    public JAXBJSONContextResolver() throws Exception {
        JSONConfiguration.MappedBuilder b = JSONConfiguration.mapped();
        //b.nonStrings("id");
        b.rootUnwrapping(true);
        //b.arrays("distribution");
        context = new JSONJAXBContext(b.build(),Statistics.class);
    }
    @Override
    public JAXBContext getContext(Class<?> objectType) {
        return context;
    }
}