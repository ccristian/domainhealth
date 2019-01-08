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

import java.io.File;
import java.io.IOException;

import domainhealth.core.util.FileUtil;

import junit.framework.TestCase;

/**
 * Test-case class for: domainhealth.util.FileUtil
 * 
 * @see domainhealth.util.FileUtil
 */
public class FileUtilTest extends TestCase {
	/**
	 * Test method
	 */	
    public void testFileNotExists() {
    	try {
    		assertNull(FileUtil.retrieveFile("/here/there/nofile"));
    	} catch (IOException e) {
			fail(e.getMessage());
		}
    }

	/**
	 * Test method
	 */	
    public void testFileExists() {
    	try {
    		assertNotNull(FileUtil.retrieveFile("pom.xml"));
    	} catch (IOException e) {
			fail(e.getMessage());
		}
    }

	/**
	 * Test method
	 */	
    public void testDirNotExists() {
    	try {
    		assertNull(FileUtil.retrieveDir("/here/there/nodir"));
    	} catch (IOException e) {
			fail(e.getMessage());
		}
    }

	/**
	 * Test method
	 */	
    public void testDirExists() {
    	try {
    		assertNotNull(FileUtil.retrieveDir("src"));
    	} catch (IOException e) {
			fail(e.getMessage());
		}
    }
    
	/**
	 * Test method
	 */	
    public void testCreateAndRetrieveDir() {
    	File file = new File(JAVA_TEMP_DIR, "abc123");
    	
    	try {
    		assertFalse(file.exists());
    		assertNull(FileUtil.retrieveDir(file.getAbsolutePath()));
    		assertNotNull(FileUtil.createOrRetrieveDir(file.getAbsolutePath()));
    		file.delete();
    		assertNull(FileUtil.retrieveDir(file.getAbsolutePath()));    		
    	} catch (IOException e) {
			fail(e.getMessage());
		}    	
    }    
    
    // Constants
    private final static String JAVA_TEMP_DIR = System.getProperty("java.io.tmpdir");
}   
