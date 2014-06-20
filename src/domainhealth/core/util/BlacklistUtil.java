package domainhealth.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import domainhealth.core.env.AppProperties;
import domainhealth.core.env.AppProperties.PropKey;

public class BlacklistUtil {
	
	private final static String BLACKLIST_TOKENIZER_PATTERN = ",\\s*";
	private List<String> componentBlacklist;
	
	public BlacklistUtil(AppProperties appProps) {
		componentBlacklist = tokenizeBlacklistText(appProps.getProperty(PropKey.COMPONENT_BLACKLIST_PROP));
	}

	/**
	 * Gets list of names of web-app and ejb components which should not have 
	 * statistics collected and shown.
	 * 
	 * @param blacklistText The text containing comma separated list of names to ignore
	 * @return A strongly type list of names to ignore
	 */
	private List<String> tokenizeBlacklistText(String blacklistText) {
		List<String> blacklist = new ArrayList<String>();
		String[] blacklistArray = null;
		
		if (blacklistText != null) {
			blacklistArray = blacklistText.split(BLACKLIST_TOKENIZER_PATTERN);
		}
		
		if ((blacklistArray != null) && (blacklistArray.length > 0)) {
			blacklist = Arrays.asList(blacklistArray);
		} else {
			blacklist = new ArrayList<String>();
		}
				
		return blacklist;
	}
	
	/**
	 * Returns the list of component names to be ignored (the blacklist) 
	 * 
	 * @return The blacklist of component names
	 */
	public List<String> getComponentBlacklist() {
		return componentBlacklist;
	}
}
