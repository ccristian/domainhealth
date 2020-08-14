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
package domainhealth.core.statistics;

import static domainhealth.core.statistics.MonitorProperties.*;

/**
 * Utility class for changing some of the bad characters contains in 
 * resource names for suitable replacements to enable the resource names
 * to be stored in CSV files and used in HTTP links, etc.. Also performs
 * some resource type specific logic (eg. picking out a Distributed 
 * Destination's real name from a physical destination compound name.
 */
public class ResourceNameNormaliser {
	/**
	 * Remove bad characters from a resource name. 
	 * 
	 * @param resourceType The type of resource
	 * @param resourceName The resource's name 
	 * @return The normalised name
	 */
	public static String normalise(String resourceType, String resourceName) {
		String normalisedName = resourceName;
				
		// Updated by gregoan
		//if (resourceType == DESTINATION_RESOURCE_TYPE) {
		if (resourceType == DESTINATION_RESOURCE_TYPE || resourceType == JMSSVR_RESOURCE_TYPE) {
			
			int startPos = resourceName.indexOf(DEST_MODULE_PHYSICAL_SEPERATOR);
			
			if (startPos < 0) {
				startPos = resourceName.indexOf(DEST_SERVER_MODULE_SEPARATOR);
			}
					
			if (startPos > 0) {
				normalisedName = resourceName.substring(startPos + 1);
			}
		// For SAF the relevant information is between last "DEST_SERVER_MODULE_SEPARATOR" and "DEST_MODULE_PHYSICAL_SEPERATOR"
		} else if (resourceType == SAFAGENT_RESOURCE_TYPE) {
			
			int startPos = resourceName.lastIndexOf(DEST_SERVER_MODULE_SEPARATOR);
			int endPos = resourceName.indexOf(DEST_MODULE_PHYSICAL_SEPERATOR);
			
			if (startPos > 0 && endPos > 0) {
				normalisedName = resourceName.substring(startPos +1, endPos);
			}
			else {
				if (startPos < 0) {
					startPos = resourceName.indexOf(DEST_SERVER_MODULE_SEPARATOR);
				}
						
				if (startPos > 0) {
					normalisedName = resourceName.substring(startPos + 1);
				}
			}			
		} else if (resourceType == WEBAPP_RESOURCE_TYPE) {
			
//System.out.println("ResourceNameNormaliser::normalise() - WEB-APP - Processing the resource [" + resourceName + "]");			
//System.out.println("ResourceNameNormaliser::normalise() - WEB-APP - Looking for [" + WEBAPP_SERVER_NAME_SEPARATOR + "]");
			
			int startPos = resourceName.indexOf(WEBAPP_SERVER_NAME_SEPARATOR);
			
			if (startPos > 0) {
				
//System.out.println("ResourceNameNormaliser::normalise() - WEB-APP - Found [" + WEBAPP_SERVER_NAME_SEPARATOR + "] in position [" + startPos + "]");

				normalisedName = resourceName.substring(startPos + WEBAPP_SERVER_NAME_SEPARATOR.length());
				
//System.out.println("ResourceNameNormaliser::normalise() - WEB-APP - startPos > 0 - normalisedName is [" + normalisedName + "]");

			}
		}

		normalisedName = normalisedName.replace(BAD_CHAR_1, GOOD_CHAR);
//System.out.println("ResourceNameNormaliser::normalise() - normalisedName BAD_CHAR_1 is [" + normalisedName + "]");

		normalisedName = normalisedName.replace(BAD_CHAR_2, GOOD_CHAR);
//System.out.println("ResourceNameNormaliser::normalise() - normalisedName BAD_CHAR_2 is [" + normalisedName + "]");

		normalisedName = normalisedName.replace(BAD_CHAR_3, GOOD_CHAR);
//System.out.println("ResourceNameNormaliser::normalise() - normalisedName BAD_CHAR_3 is [" + normalisedName + "]");
		
		
		normalisedName = normalisedName.replace(BAD_CHAR_4, GOOD_CHAR);
//System.out.println("ResourceNameNormaliser::normalise() - normalisedName BAD_CHAR_4 is [" + normalisedName + "]");

		if (normalisedName.endsWith(REDUNDANT_GOOD_STR)) {
			
//System.out.println("ResourceNameNormaliser::normalise() - normalisedName REDUNDANT_GOOD_STR return [" + normalisedName.substring(0, (normalisedName.length() - 1)) + "]");
			
			return normalisedName.substring(0, (normalisedName.length() - 1));			
		}
		
//System.out.println("ResourceNameNormaliser::normalise() - normalisedName final [" + normalisedName.trim() + "]");
//System.out.println("");

		return normalisedName.trim();
	}

	// Constants
	private final static char DEST_MODULE_PHYSICAL_SEPERATOR = '@';
	private final static char DEST_SERVER_MODULE_SEPARATOR = '!';
	private final static String WEBAPP_SERVER_NAME_SEPARATOR = "_/";
	private final static char BAD_CHAR_1 = '/';
	private final static char BAD_CHAR_2 = '[';
	private final static char BAD_CHAR_3 = ']';
	private final static char BAD_CHAR_4 = '.';
	private final static char GOOD_CHAR = '_';
	private final static String REDUNDANT_GOOD_STR = "" + GOOD_CHAR;
}
