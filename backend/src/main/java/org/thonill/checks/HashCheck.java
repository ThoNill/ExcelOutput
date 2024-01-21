package org.thonill.checks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class HashCheck {

	private Map<String, Pattern> patterns = new HashMap<>();

	HashCheck(Map<String, String> stringPattern) {
		for (Entry<String, String> e : stringPattern.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	public void put(String key, String value) {
		patterns.put(key, Pattern.compile(value));
	}

	public boolean check(Map<String, String> testMe) {
		for (Entry<String, String> e : testMe.entrySet()) {
			String key = e.getKey();
			if (patterns.containsKey(key)) {
				CharSequence s = e.getValue();
				if(!patterns.get(key).matcher(s).matches()) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

}
