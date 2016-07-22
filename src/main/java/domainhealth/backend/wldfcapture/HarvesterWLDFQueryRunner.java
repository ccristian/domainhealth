//Copyright (C) 2008-2013 Paul Done . All rights reserved.
//This file is part of the DomainHealth software distribution. Refer to the  
//file LICENSE in the root of the DomainHealth distribution.
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE 
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//POSSIBILITY OF SUCH DAMAGE.
package domainhealth.backend.wldfcapture;

import javax.management.ObjectName;

import domainhealth.backend.wldfcapture.data.DataRecordsCollection;
import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.WebLogicMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;

/**
 * Locates the JMX Runtime for a specific Server's WLDF Harvester Archive and 
 * executes the provided WLDF query on this archive, collecting the rows of
 * results together and passing this result set back to the caller. 
 */
public class HarvesterWLDFQueryRunner {
	/**
	 * 
	 * @param conn Connection to the admin server's MBean tree
	 * @param serverName The name of the server to perform the WLDF query on
	 * @param wldfQuery The text of the WLDF query to run
	 * @throws WebLogicMBeanException Indicates problem accessing the server to retrieve the data
	 */
	public HarvesterWLDFQueryRunner(WebLogicMBeanConnection conn, String serverName, String wldfQuery, int queryIntervalMillis) throws WebLogicMBeanException {
		this.conn = conn;
		this.wldfQuery = wldfQuery;
		this.serverName = serverName;
		this.queryIntervalMillis = queryIntervalMillis;
		this.queryTimeoutMillis = (int) (QUERY_TIMEOUT_FACTOR * queryIntervalMillis);
		ObjectName newHarvesterArchiveRuntime = null;
		
		try {
			newHarvesterArchiveRuntime = new ObjectName(String.format(HARVESTER_ARCHIVE_OBJ_NAME_TEMPLATE, serverName, serverName));
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}

		this.harvesterArchiveRuntime = newHarvesterArchiveRuntime;
	}

	/**
	 * Runs the WLDF query against the Server's Harvester Archive, collecting 
	 * together the results set.
	 *  
	 * For reference: The raw rows of data retrieved from the WLDF Harvester 
	 * Archive have the following structure:
	 * 
	 *   Column 0: RECORDID (java.lang.Long)
	 *   Column 1: TIMESTAMP (java.lang.Long)
	 *   Column 2: DOMAIN (java.lang.String)
	 *   Column 3: SERVER (java.lang.String)
	 *   Column 4: TYPE (java.lang.String)
	 *   Column 5: NAME (java.lang.String)
	 *   Column 6: ATTRNAME (java.lang.String)
	 *   Column 7: ATTRTYPE (java.lang.Integer)
	 *   Column 8: ATTRVALUE (java.lang.Object)
	 * 
	 * @return The collection of retrieved records
	 * @throws WebLogicMBeanException Indicates problem accessing the server to retrieve the data
	 */
	public DataRecordsCollection retrieveDataRecords() throws WebLogicMBeanException {
		DataRecordsCollection dataRecords = new DataRecordsCollection();
		String cursorId = null;
		
		try {
			cursorId = createCursor();

			while (checkHasMoreData(cursorId)) {
				Object[] setOfRecords = getNextDataChunk(cursorId);
				
				for (Object record : setOfRecords) {
					Object[] recordElements = (Object[]) record;
					dataRecords.addElement((String) recordElements[POSTN_TYPE], (String) recordElements[POSTN_NAME],
							(Long)recordElements[POSTN_TIMESTAMP], (String) recordElements[POSTN_ATTRNAME],
							recordElements[POSTN_ATTRVAL].toString());
				}
			}
		} catch (WebLogicMBeanException e) {
			//AppLog.getLogger().debug("ERROR - For server '" + serverName + "', WLDF Query captured " + dataRecords.getTotalNumberReocrds() + " results with query: " + wldfQuery + "  - Exception message: " + e);
			AppLog.getLogger().error("ERROR - For server '" + serverName + "', WLDF Query captured " + dataRecords.getTotalNumberReocrds() + " results with query: " + wldfQuery + "  - Exception message: " + e);
			throw e;
		} finally {
			if (cursorId != null) {
				closeCursor(cursorId);
			}
		}
				
		AppLog.getLogger().debug("For server '" + serverName + "', WLDF Query captured " + dataRecords.getTotalNumberReocrds() + " results with query: " + wldfQuery);
		return dataRecords;
	}
	
	/**
	 * Uses the WLS JMX API to invoke the WLDF create cursor operation.
	 * 
	 * @return The id of the created cursor containing thw query results
	 * @throws WebLogicMBeanException Indicates problem accessing the server to retrieve the data
	 */
	private String createCursor() throws WebLogicMBeanException {
		long currentTime = System.currentTimeMillis();		
		return (String) conn.invoke(harvesterArchiveRuntime, OPEN_CURSOR_OPERTN, new Object [] {(currentTime - queryIntervalMillis), currentTime, wldfQuery, queryTimeoutMillis}, OPEN_CURSOR_PARAMTYPES);		
	}

	/**
	 * Uses the WLS JMX API to invoke the WLDF has more data operation.
	 * 
	 * @param cursorId The id of the current cursor containing the query results
	 * @return True if more rows exists; false if no more left
	 * @throws WebLogicMBeanException Indicates problem accessing the server to retrieve the data
	 */
	private boolean checkHasMoreData(String cursorId) throws WebLogicMBeanException {
		return ((Boolean) conn.invoke(harvesterArchiveRuntime, HAS_DATA_OPERTN, new Object [] {cursorId}, HAS_DATA_PARAMTYPES)).booleanValue();
	}

	/**
	 * Uses the WLS JMX API to invoke the WLDF get next set of records operation.
	 * 
	 * @param cursorId The id of the current cursor containing the query results
	 * @return The set of weakly typed result rows 
	 * @throws WebLogicMBeanException Indicates problem accessing the server to retrieve the data
	 */
	private Object[] getNextDataChunk(String cursorId) throws WebLogicMBeanException { 
		return (Object[]) conn.invoke(harvesterArchiveRuntime, FETCH_OPERTN, new Object [] {cursorId, MAX_RECORDS_FETCH}, FETCH_PARAMTYPES);
	}
	
	/**
	 * Uses the WLS JMX API to invoke the WLDF close cursor operation.
	 * 
	 * @param cursorId The id of the current cursor containing the query results
	 * @throws WebLogicMBeanException Indicates problem accessing the server to retrieve the data
	 */
	private void closeCursor(String cursorId) throws WebLogicMBeanException {
		conn.invoke(harvesterArchiveRuntime, CLOSE_CURSOR_OPERTN, new Object [] {cursorId}, CLOSE_CURSOR_PARAMTYPES);		
	}
	
	// Constants
	private static final String HARVESTER_ARCHIVE_OBJ_NAME_TEMPLATE = "com.bea:ServerRuntime=%s,Name=HarvestedDataArchive,Type=WLDFDataAccessRuntime,Location=%s,WLDFAccessRuntime=Accessor,WLDFRuntime=WLDFRuntime";
	private static final String OPEN_CURSOR_OPERTN = "openCursor";
	private static final String[] OPEN_CURSOR_PARAMTYPES = new String [] {Long.class.getCanonicalName(), Long.class.getCanonicalName(), String.class.getCanonicalName(), Long.class.getCanonicalName()};
	private static final String HAS_DATA_OPERTN = "hasMoreData";
	private static final String[] HAS_DATA_PARAMTYPES = new String [] {String.class.getCanonicalName()};
	private static final String FETCH_OPERTN = "fetch";
	private static final String[] FETCH_PARAMTYPES = new String [] {String.class.getCanonicalName(), Integer.class.getCanonicalName()};
	private static final String CLOSE_CURSOR_OPERTN = "closeCursor";
	private static final String[] CLOSE_CURSOR_PARAMTYPES = new String [] {String.class.getCanonicalName()};	
	private static final int MAX_RECORDS_FETCH = 200;
	private static final int POSTN_TYPE = 4;
	private static final int POSTN_NAME = 5;
	private static final int POSTN_TIMESTAMP = 1;
	private static final int POSTN_ATTRNAME = 6;
	private static final int POSTN_ATTRVAL = 8;
	
	// Members
	private final WebLogicMBeanConnection conn;
	private final String wldfQuery; 	
	private final ObjectName harvesterArchiveRuntime;
	private final int queryIntervalMillis;
	private final int queryTimeoutMillis;
	private final String serverName;
	
	// Constants
	private static final float QUERY_TIMEOUT_FACTOR = 0.3F;
}
