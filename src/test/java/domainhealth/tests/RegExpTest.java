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
package domainhealth.tests;

import domainhealth.backend.wldfcapture.HarvesterWLDFModuleCreator;
import domainhealth.backend.wldfcapture.data.TypeDataRecord;
import domainhealth.core.util.ProductVersionUtil;
import junit.framework.TestCase;

/**
 * Test-case class for various Regular Expression utility methods in the 
 * project.
 */
public class RegExpTest extends TestCase {
	/**
	 * Test method
	 */	
    public void testMBeanObjectNameExtractor() {
    	assertEquals(TypeDataRecord.extractMBeanObjectName("com.bea:Name=WLS103MultiSvrAdminServer,Type=ServerRuntime"), 
    			"WLS103MultiSvrAdminServer");
    	assertEquals(TypeDataRecord.extractMBeanObjectName("com.bea:Name=WLS103MultiSvrAdminServer,ServerRuntime=WLS103MultiSvrAdminServer,Type=JVMRuntime"), 
    			"WLS103MultiSvrAdminServer");
    	assertEquals(TypeDataRecord.extractMBeanObjectName("com.bea:Name=ThreadPoolRuntime,ServerRuntime=WLS103MultiSvrServer1,Type=ThreadPoolRuntime"), 
    			"ThreadPoolRuntime");
    	assertEquals(TypeDataRecord.extractMBeanObjectName("com.bea:Name=MyDB,ServerRuntime=WLS103MultiSvrServer2,Type=JDBCDataSourceRuntime"), 
    			"MyDB");
    	assertEquals(TypeDataRecord.extractMBeanObjectName("com.bea:JMSServerRuntime=WLS103MultiSvrServer2_MainJMSRsc2JMSServer,Name=MainJMSRsc2!WLS103MultiSvrServer2_MainJMSRsc2JMSServer@jms.QA,ServerRuntime=WLS103MultiSvrServer2,Type=JMSDestinationRuntime"), 
    			"MainJMSRsc2!WLS103MultiSvrServer2_MainJMSRsc2JMSServer@jms.QA");
    	assertEquals(TypeDataRecord.extractMBeanObjectName("com.bea:ApplicationRuntime=MyOtherDB,Name=weblogic.wsee.mdb.DispatchPolicy,ServerRuntime=WLS103MultiSvrServer2,Type=WorkManagerRuntime"), 
    			"weblogic.wsee.mdb.DispatchPolicy");
    	assertEquals(TypeDataRecord.extractMBeanObjectName("com.bea:Name=weblogic.logging.DomainLogBroadcasterClient,ServerRuntime=WLS103MultiSvrServer2,Type=WorkManagerRuntime"), 
    			"weblogic.logging.DomainLogBroadcasterClient");
    	assertEquals(TypeDataRecord.extractMBeanObjectName("com.bea:ApplicationRuntime=DomainHealth_WLDFHarvesterModule,Name=weblogic.wsee.mdb.DispatchPolicy,ServerRuntime=WLS103MultiSvrServer2,Type=WorkManagerRuntime"), 
    			"weblogic.wsee.mdb.DispatchPolicy");
    }
    
	/**
	 * Test method
	 */	
    public void testWLDFModuleVersionTextVersion() {
    	HarvesterWLDFModuleCreator harvesterWLDFModuleCreator1 = new HarvesterWLDFModuleCreator(60*1000, "0.9.0b3", "10.3.5");
    	assertTrue(harvesterWLDFModuleCreator1.doesModuleDescriptionContainCurrentModuleVersion("WLDF Module for DH. v0.9.0b3."));
    	assertTrue(harvesterWLDFModuleCreator1.doesModuleDescriptionContainCurrentModuleVersion(". v0.9.0b3."));
    	assertFalse(harvesterWLDFModuleCreator1.doesModuleDescriptionContainCurrentModuleVersion(". v0.0.0."));
    	HarvesterWLDFModuleCreator harvesterWLDFModuleCreator2 = new HarvesterWLDFModuleCreator(60*1000, "0.9b3", "10.3.5");
    	assertTrue(harvesterWLDFModuleCreator2.doesModuleDescriptionContainCurrentModuleVersion(". v0.9b3."));
    }
    
	/**
	 * Test method
	 */	
    public void testWebLogicVersionComparer() {
    	// TRUE tests
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.2", "12.1.2"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.2", "12.1.1"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3", "10.3"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3", "10.0"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3", "10.3.0"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3.0", "10.3"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3.1", "10.3"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3.4", "10.3.3"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3.4", "10.3.4"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12", "10.3.3"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.0", "12"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12", "12.0"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.", "12"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12", "12."));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.2", "9.0.1"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.2", "12.1"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.2", "12.1.1"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.2", "12.1.2"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.2 ", "12.1.2.0"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.2.0 ", "12.1.2"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.2.0 ", "12.1.1.2"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("9.1", "9.0"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("9.0.1", "9"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("9", "9.0.0"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("9.0.0", "9"));
    	assertTrue(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("9.1", "9.0.0"));

    	// FALSE tests
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10", "10.3"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("9.0.0", "10.3.0"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.0", "10.3"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.2", "10.3"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3", "10.3.1"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3.0", "10.3.1"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.2", "12.1.3"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.1", "12.1.2"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1", "12.1.2"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.0", "12.1.2"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12", "12.1.2"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3.4", "10.3.5"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("10.3.4", "12"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("9.0.1", "12.1.2"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.1", "12.1.2"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("12.1.1.2 ", "12.1.2.0"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("9.0", "9.1"));
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("9", "9.0.1"));    	
    	assertFalse(ProductVersionUtil.isVersion_X_GreaterThanOrEqualTo_Y("9.0.0", "9.1"));    	
    }
}