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
 * For a specific MBean object name, holds a set of attribute name-value pairs
 * for the retrieved statistics for the object.
 */
public class InstanceDataRecord {
	/**
	 * Creates new instance of MBean object record
	 * 
	 * @param objectName The MBean object name
	 * @param timestamp The date-time of the retrieved data record
	 */
	public InstanceDataRecord(String objectName, long timestamp) {
		this.objectName = objectName;
		this.timestamp = timestamp;  // Assume all attributes in object data record have same timestamp (they should be within a second of each other)
	}
	
	/**
	 * Add a new attribute key-value pair retrieved statistic.
	 * 
	 * @param attrName The attribute name
	 * @param attrValue The attribute value
	 */
	public void addElement(String attrName, String attrValue) {
		attributes.put(attrName, attrValue);
	}
	
	/**
	 * Gets the name of the object.
	 * 
	 * @return The name of the object
	 */
	public String getInstanceObjectName() {
		return objectName;
	}
	
	/**
	 * Gets the date-time of the retrieved statistic.
	 * 
	 * @return The date-time of the retrieved statistic
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * The list of names of statistic attribute stored.
	 *  
	 * @return The list of statistic attribute names
	 */	
	public Iterator<String> getAttrNames() {
		return attributes.keySet().iterator();
	}

	/**
	 * Get the value of a stored attribute by name.
	 * 
	 * @param attrName The name of the attribute to lookup
	 * @return THe value of the lookup attribute
	 */
	public String getAttrValue(String attrName) {
		return attributes.get(attrName);
	}

	// Members
	private final Map<String, String> attributes = new HashMap<String, String>();	
	private final String objectName;
	private final long timestamp;
}
