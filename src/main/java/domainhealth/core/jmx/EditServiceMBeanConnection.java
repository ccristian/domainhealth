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
package domainhealth.core.jmx;

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Creates an WebLogic JMX Connection to WebLogic's Edit Service Tree. See 
 * description of WebLogicMBeanConnection for more info. Used primarily to
 * control and track new Edit sessions for making configuration changes and
 * then activating them.
 *  
 * @see WebLogicMBeanConnection
 */
public class EditServiceMBeanConnection extends WebLogicMBeanConnection {
	/**
	 * Create a new connection to a REMOTE WebLogic server's Edit Service M
	 * Bean Tree.
	 * 
	 * @see WebLogicMBeanException
	 * 
	 * @param protocol The admin server connection protocol (ie. 't3' or 't3s')
	 * @param host The hostname/ip-address of the admin server
	 * @param port The listen admin port of the admin server
	 * @param username A WebLogic Administrator username to connect with 
	 * @param password A WebLogic Administrator password to connect with
	 * @throws WebLogicMBeanException Indicates that a JMX connection to server could not be made
	 */
	public EditServiceMBeanConnection(String protocol, String host, int port, String username, String password) throws WebLogicMBeanException {
		super(protocol, host, port, username, password, EDIT_SERVICE_NAME);
	}

	/**
	 * Create a new connection to the LOCAL WebLogic server's Edit Service
	 * MBean Tree.
	 * 
	 * @see WebLogicMBeanException
	 *
	 * @throws WebLogicMBeanException Indicates that a JMX connection to server could not be made
	 */
	public EditServiceMBeanConnection() throws WebLogicMBeanException {
		super(EDIT_SERVICE_NAME);
	}

	/**
	 * Get a handle onto the Edit Service's Edit Configuration Manager with 
	 * which one can then start and activate edit sessions to make 
	 * configuration changes within.
	 * 
	 * @return The Edit Configuration Manager
	 * @throws WebLogicMBeanException Indicates problem occurred in trying to retrieve mbean
	 */
	public ObjectName getEditConfigurationMgr() throws WebLogicMBeanException {
		try {
			return (ObjectName) getConn().getAttribute(editServiceMBean, CONFIGURATION_MGR);
		} catch (Exception e) {
			throw new WebLogicMBeanException(e.toString(), e);
		}
	}

	/**
	 * Start a new Edit Session
	 * 
	 * @return The domain configuration object from with which configuration changes can be made
	 * @throws WebLogicMBeanException Indicates problem occurred in trying create a new edit session
	 */
	public ObjectName startEdit() throws WebLogicMBeanException {
		if (isEditing) {
			throw new WebLogicMBeanException("An edit session has already been started - can't start a new one");
		}
		
		ObjectName domainConfig =  (ObjectName) invoke(getEditConfigurationMgr(), START_EDIT_OPERTN, 
			new Object[] { NO_WAIT, LOCK_TIMEOUT_MILLIS, NON_EXCLUSIVE_LOCK }, START_EDIT_PARAMTYPES);
		
		if (domainConfig == null) {
			throw new WebLogicMBeanException("Unable to get edit lock for domain - check if an administrator already has an admin lock (eg. using the admin console) and release it");
		}

		isEditing = true;
		return domainConfig;
	}

	/**
	 * Save and activate the current edit session and alls its changes.
	 * 
	 * @throws WebLogicMBeanException Indicates problem occurred in trying save and activate an edit session
	 */
	public void saveAndActivate() throws WebLogicMBeanException {		
		if (!isEditing) {
			return;
		}
		
		invoke(getEditConfigurationMgr(), SAVE_OPERTN, null, SAVE_PARAMTYPES);
		invoke(getEditConfigurationMgr(), ACTIVATE_OPERTN, new Object[] {NO_TIMEOUT}, ACTIVATE_PARAMTYPES);
	}

	/**
	 * Cancels any currently active edit session.
	 * 
	 * @throws WebLogicMBeanException Indicates problem occurred in trying to cancel an edit session
	 */
	public void cancelEdit() throws WebLogicMBeanException {
		try {
			invoke(getEditConfigurationMgr(), CANCEL_EDIT_OPERTN, null, CANCEL_EDIT_PARAMTYPES);
		} finally {
			isEditing = false;			
		}
	}

	/**
	 * Stop the current edit session without activate any changes
	 * 
	 * @throws WebLogicMBeanException Indicates problem occurred in trying to stop an edit session
	 */
	public void stopEdit() throws WebLogicMBeanException {
		if (!isEditing) {
			return;
		}
		
		try {
			invoke(getEditConfigurationMgr(), STOP_EDIT_OPERTN,	null, STOP_EDIT_PARAMTYPES);
		} finally {
			isEditing = false;			
		}
	}

	/**
	 * Set an MBean's target property to point to (target) another MBean.
	 * 
	 * @param parent The MBean to set targetting for
	 * @param newTarget The mBean to target
	 * @throws WebLogicMBeanException Indicates a problem in setting the target property
	 */
	public void addTarget(ObjectName parent, ObjectName newTarget) throws WebLogicMBeanException {
		invoke(parent, ADD_TARGET_OPERTN, new Object[] {newTarget}, ADD_TARGET_PARAMTYPES);
	}

	// Constants
	private static final String EDIT_SERVICE_NAME = "weblogic.management.mbeanservers.edit";
	private static final String START_EDIT_OPERTN = "startEdit";
	private static final String[] START_EDIT_PARAMTYPES = new String [] {Integer.class.getCanonicalName(), Integer.class.getCanonicalName(), Boolean.class.getCanonicalName()};
	private static final String SAVE_OPERTN = "save";
	private static final String[] SAVE_PARAMTYPES = null;
	private static final String ACTIVATE_OPERTN = "activate";
	private static final String[] ACTIVATE_PARAMTYPES = new String [] {Long.class.getCanonicalName()};
	private static final String CANCEL_EDIT_OPERTN = "cancelEdit";
	private static final String[] CANCEL_EDIT_PARAMTYPES = null;
	private static final String STOP_EDIT_OPERTN = "stopEdit";
	private static final String[] STOP_EDIT_PARAMTYPES = null;
	private static final String ADD_TARGET_OPERTN = "addTarget";
	private static final String[] ADD_TARGET_PARAMTYPES = new String [] {ObjectName.class.getCanonicalName()};
	private static final int NO_WAIT = 0; 
	private static final int NO_TIMEOUT = -1; 
	private static final int LOCK_TIMEOUT_MILLIS = 20 * 1000; 
	private static final boolean NON_EXCLUSIVE_LOCK = false; 
	private static final ObjectName editServiceMBean; 

	static {
		try {
			editServiceMBean = new ObjectName("com.bea:Name=EditService,Type=weblogic.management.mbeanservers.edit.EditServiceMBean");
		} catch (MalformedObjectNameException e) {
			throw new AssertionError(e.toString());
		}
	}	
	
	// Members
	private boolean isEditing = false;
}
