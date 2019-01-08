package domainhealth.rest;

/**
 * Created by cristianchiovari on 7/26/15.
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.ws.rs.WebApplicationException;

public class RESTTimestampParam {

    private SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
    private java.sql.Timestamp timestamp;

    public RESTTimestampParam( String timestampStr ) throws WebApplicationException {
        try {
            timestamp = new java.sql.Timestamp( df.parse( timestampStr ).getTime() );
        } catch ( final ParseException ex ) {
            throw new WebApplicationException( ex );
        }
    }

    public java.sql.Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        if ( timestamp != null ) {
            return timestamp.toString();
        } else {
            return "";
        }
    }
}
