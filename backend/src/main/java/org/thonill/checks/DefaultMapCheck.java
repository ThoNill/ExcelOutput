package org.thonill.checks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.thonill.exceptions.ApplicationException;

public class DefaultMapCheck implements MapCheck {

	private Map<String, Pattern> patterns = new HashMap<>();

	public DefaultMapCheck(Map<String, String> stringPattern) {
		for (Entry<String, String> e : stringPattern.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	public void put(String key, String value) {
		patterns.put(key, Pattern.compile(value));
	}

	@Override
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

	@Override
	public void checkWithException(Map<String, String> testMe) {
		for (Entry<String, String> e : testMe.entrySet()) {
			String key = e.getKey();
			if (patterns.containsKey(key)) {
				CharSequence s = e.getValue();
				if(!patterns.get(key).matcher(s).matches()) {
					throw new ApplicationException("Der Schluessel "+ key + " passt nicht zu " + s);
				}
			} else {
				throw new ApplicationException("Der Schluessel "+ key + " is unbekannt");
			}
		}	

	}
}
