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
package domainhealth.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import domainhealth.core.env.AppLog;


/**
 * Provides File related utility functions mainly to enable files and 
 * directories to be easily created and retrieved
 */
public class FileUtil {
	/**
	 * Locate a file for a given path, returning the file handle if it exists 
	 * or null if it does not exist
	 * 
	 * @param filePath The path of the file
	 * @return The file handle of the file or null
	 * @throws IOException Indicates a problem accessing the filesystem
	 */
	public static File retrieveFile(String filePath) throws IOException, FileNotFoundException {
		File file = new File(filePath);
		//System.out.println(filePath);
		if (file.exists()) {
			return file;
		} else {
			return null;
		}
	}

	/**
	 * Locate a file for a given path, if it exists and if it does not 
	 * exist, create the file. 
	 * 
	 * @param filePath The path of the file
	 * @return The file handle of the retrieved or created file
	 * @throws IOException Indicates a problem accessing the filesystem
	 */
	public static File createOrRetrieveFile(String filePath) throws IOException {
		File file = new File(filePath);
		
		if (!file.exists()) {
			boolean created = false;
			
			try {
				created = file.createNewFile();
			} catch (Exception e) {
				throw new IOException("Error creating file '" + file.getAbsolutePath() + ". Cause: " + e.toString());
			}
			
			if (!created) {							
				throw new IOException("Unable to create file '" + file.getAbsolutePath());				
			}
		}
		
		return file;
	}
	
	/**
	 * Locate a directory for a given path, returning the directory file 
	 * handle if it exists or null if it does not exist
	 * 
	 * @param dirPath The path of the directory
	 * @return The file handle of the directory or null
	 * @throws IOException Indicates a problem accessing the filesystem
	 */
	public static File retrieveDir(String dirPath) throws IOException, FileNotFoundException {
		File dir = new File(dirPath);
		
		if (dir.exists()) {
			return dir;
		} else {
			return null;
		}
	}

	/**
	 * Locate a directory for a given path, if it exists and if it does not 
	 * exist, create the directory. 
	 * 
	 * @param dirPath The path of the directory
	 * @return The file handle of the retrieved or created directory
	 * @throws IOException Indicates a problem accessing the filesystem
	 */
	public static File createOrRetrieveDir(String dirPath) throws IOException {
		File dir = new File(dirPath);
		
		if (!dir.exists()) {
			boolean created = false;
			
			try {
				created = dir.mkdirs();
			} catch (Exception e) {
				throw new IOException("Error reading/writing to directory '" + dir.getAbsolutePath() + ". Cause: " + e.getMessage());
			}
			
			if (!created) {							
				throw new IOException("Unable to read and write to directory '" + dir.getAbsolutePath());				
			}
		}
		
		return dir;
	}

	/**
	 * Remove current path resource (file or directory), but if it is a 
	 * directory, recursively remove that directories contents first.
	 * 
	 * @param path The path of the file/directory
	 * @return True if resource path exists and can be deleted
	 */
    public static boolean deleteRecursive(File path) {
        if (!path.exists()) {
        	return false;
        }
        
        boolean success = true;
        
        if (path.isDirectory()){
        	AppLog.getLogger().debug("Removing directory: " + path);
        	
            for (File f : path.listFiles()){
                success = success && deleteRecursive(f);
            }
        }
        
        return success && path.delete();
    }
}
