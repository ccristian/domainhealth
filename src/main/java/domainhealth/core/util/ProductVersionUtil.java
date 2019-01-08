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

/**
 * Provides Version Text related utility functions mainly to process software 
 * product version numbers like "10.3", "10.3.5", "12.1", "12.1.2".
 */
public class ProductVersionUtil {
	/**
	 * Utility method to determine the first product version text is greater
	 * than the second product version text. Version number in WebLogic are 
	 * represented like "10.3", "10.3.5", "12.1", "12.1.2". 
	 * 
	 * @param x The first version number text - X
	 * @param y The second version number text - Y
	 * @return True if X >= Y, otherwise False if X < Y
	 */
	public static boolean isVersion_X_GreaterThanOrEqualTo_Y(String x, String y) {
		boolean answer = true;				
		String[] xList = x.trim().split("\\.");
		String[] yList = y.trim().split("\\.");
		int index = 0;		
		int maxIndex = Math.max(xList.length, yList.length) - 1;
	
		while (index <= maxIndex) {
			if (index >= xList.length) {
				// not always true cos remaining part of Y may have a point version greater than zero
				if (maxValueInRemainingListElements(yList, index) == 0) {
					answer = true;
				} else {
					answer = false;
				}
	
				break;
			}
	
			if (index >= yList.length) {
				// always true cos the remaining part of X is at least equal if not greater
				answer = true;
				break;
			}
	
			int xElmnt = 0;
			int yElmnt = 0;
			
			if (xList[index].length() > 0) {
				xElmnt = Integer.parseInt(xList[index]);
			}
	
			if (yList[index].length() > 0) {
				yElmnt = Integer.parseInt(yList[index]);
			}

			if (xElmnt > yElmnt) {
				answer = true;
				break;
			}
					
			if (xElmnt < yElmnt) {
				answer = false;
				break;
			}
	
			index += 1;
		}

		return answer;
	}

	/**
	 * For an array of numbers, finds the max individual value from these 
	 * numbers. For example if the input is "12,1,2,0,5" and initial index is
	 * 3, will find the highest number from "0,5" which is "5". If there are
	 * no numbers found, returns 0.
	 * 
	 * @param numberList The list of individual numbers
	 * @param initialIndex The index of the first number to start checking onwards from
	 * @return The maximum number found in the list, from position of the initial index 
	 */
	private static int maxValueInRemainingListElements(String[] numberList, int initialIndex) {
		int maxVal = 0;
		int index = initialIndex;

		while (index < numberList.length) {
			if (numberList[index].length() > 0) {
				maxVal = Math.max(maxVal, Integer.parseInt(numberList[index]));
			}

			index += 1;
		}

		return maxVal;
	}	
}