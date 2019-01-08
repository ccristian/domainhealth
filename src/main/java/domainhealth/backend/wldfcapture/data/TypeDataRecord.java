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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * For a specific MBean type, holds a set of MBean object instances which are 
 * of that MBean type.
 */
public class TypeDataRecord {
	/**
	 * Creates new instance of MBean type record
	 * 
	 * @param mbeanTypeName The MBean type to store records for
	 */
	public TypeDataRecord(String mbeanTypeName) {
		this.mbeanTypeName = mbeanTypeName;
	}

	/**
	 * Adds a new Object name element of this MBean type element.
	 * 
	 * @param mbeanName Name of MBean object being added
	 * @param timestamp Date-time of MBean object data retrieval
	 * @param attrName The name of the attribute statistic retrieved
	 * @param attrValue The value of the attribute statistic retrieved
	 */
	public void addElement(String mbeanName, long timestamp, String attrName, String attrValue) {
		String objectName = extractMBeanObjectName(mbeanName);
		InstanceDataRecord objectRecord = instanceDataRecords.get(objectName);
		
		if (objectRecord == null) {
			objectRecord = new InstanceDataRecord(objectName, timestamp);
			instanceDataRecords.put(objectName, objectRecord);
		} 
		
		objectRecord.addElement(attrName, attrValue);		
	}

	/**
	 * Get this MBean type name.
	 * 
	 * @return This MBean type's name
	 */
	public String getMbeanTypeName() {
		return mbeanTypeName;
	}

	/**
	 * Get list of MBean object instances of this MBean type.
	 * 
	 * @return List of MBean object instances of this MBean type
	 */
	public Iterator<String> getInstanceNames() {
		return instanceDataRecords.keySet().iterator();
	}

	/**
	 * Get specific object statistic data record from list with given object 
	 * name.
	 * 
	 * @param objectName The name of the object to retrieve the data record for
	 * @return The object's data record
	 */
	public InstanceDataRecord getInstanceDataRecord(String objectName) {
		return instanceDataRecords.get(objectName);
	}

	/**
	 * Utility method for extracting the name of an object from its full MBean
	 * object name.
	 *  
	 * @param mbeanName The MBean name
	 * @return The name of the object extracted from the MBean
	 */
	public static String extractMBeanObjectName(String mbeanName) {
		Matcher matcher = MBEAN_NAME_EXTRACTOR_PATTERN.matcher(mbeanName);		
		boolean found = matcher.find();
		
		if (!found) {
			throw new IllegalStateException("Failed to extact MBean name part from full MBean canonical name of: " + mbeanName);			
		}

		return matcher.group(1);
	}
	
	// Constants
	private final static Pattern MBEAN_NAME_EXTRACTOR_PATTERN = Pattern.compile(".+[:,][Nn]ame=([^,$]+)");	
	
	// Members
	private final Map<String, InstanceDataRecord> instanceDataRecords = new HashMap<String, InstanceDataRecord>();
	private final String mbeanTypeName;
}
