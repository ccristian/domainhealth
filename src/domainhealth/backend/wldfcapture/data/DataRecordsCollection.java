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
package domainhealth.backend.wldfcapture.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Holds collections of records obtained when using the WLDF Archive Access 
 * API to retrieve data from a Harvester Archive. The records are grouped by
 * MBean type. 
 */
public class DataRecordsCollection {
	/**
	 * Adds a new MBean object attribute key-value pair retrieved statistic.
	 * 
	 * @param mbeanTypeName The type of the MBean
	 * @param mbeanName The name of the MBean object instance
	 * @param timestamp The data-time that the statistic was retrieved
	 * @param attrName The name of the attribute retrieved
	 * @param attrValue The value of the attribute retrieved
	 */
	public void addElement(String mbeanTypeName, String mbeanName, long timestamp, String attrName, String attrValue) {
		TypeDataRecord typeRecord = typeDataRecords.get(mbeanTypeName);
		
		if (typeRecord == null) {
			typeRecord = new TypeDataRecord(mbeanTypeName);
			typeDataRecords.put(mbeanTypeName, typeRecord);
		}

		typeRecord.addElement(mbeanName, timestamp, attrName, attrValue);
		recordCount++;
	}

	/**
	 * Gets a list of all the MBean types that there are records for.
	 * 
	 * @return List of known MBean types
	 */
	public Iterator<String> getTypeRecordNames() {
		return typeDataRecords.keySet().iterator();
	}

	/**
	 * Get an MBean type record by name (this may in turn have a set of 
	 * objects of that MBean type hanging off it).
	 * 
	 * @param mbeanTypeName The MBean type name to lookup
	 * @return The MBean type record
	 */
	public TypeDataRecord getTypeDataRecord(String mbeanTypeName) {
		return typeDataRecords.get(mbeanTypeName);
	}

	/**
	 * Get total number of different MBean types which there are records for.
	 * 
	 * @return Count of number of different know MBean types
	 */
	public int getTotalNumberReocrds() {
		return recordCount;
	}

	// Members
	private final Map<String, TypeDataRecord> typeDataRecords = new HashMap<String, TypeDataRecord>();
	private int recordCount = 0;
}
