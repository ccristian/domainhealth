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
package domainhealth.core.env;

import commonj.work.Work;

/**
 * Base class for work schedule on the work manager which sets the context 
 * class loader for the scheduled work (ie. the run() method) to the same
 * context as the thread which instantiated this work object. This is required 
 * because invariably we want the work to be running in the same context as 
 * the code that scheduled it, to enable things like JNDI looks on 
 * javaLcomp/env entries to work properly.
 */
public abstract class ContextAwareWork implements Work {
	/**
	 * Creates new instance capture the originators context class-loader.
	 */
	public ContextAwareWork() {
		contextClassloader = Thread.currentThread().getContextClassLoader();
	}

	/**
	 * Returns false indicating that work is not daemon and should be allowed 
	 * to finish.
	 * 
	 * @return False
	 */
	public boolean isDaemon() {
		return false;
	}

	/**
	 * Release any resources.
	 */
	public void release() {
	}

	/**
	 * Switch the current threads context class loader to the one stored 
	 * earlier before executing main work in doRun() method.
	 */
	public final void run() {
		Thread currentThread = Thread.currentThread();
		ClassLoader originalClassLoader = currentThread.getContextClassLoader();
		
		//it seems in weblogic 12c at least this is not necessary but i just let it like this for now
		//for backward compatibility.
		currentThread.setContextClassLoader(contextClassloader);
		
		try {
			doRun();
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("JVM error occurred. " + t, t);
		} finally {
			currentThread.setContextClassLoader(originalClassLoader);
		}
	}

	/**
	 * Override this with an implementation which performs the main business 
	 * logic in a scheduled work manager thread
	 */
	public abstract void doRun();

	// Members
	private final ClassLoader contextClassloader;
}
