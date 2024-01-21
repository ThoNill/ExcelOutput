package org.thonill.checks;

import java.util.Map;

public interface MapCheck {

	boolean check(Map<String, String> testMe);
	
	void checkWithException(Map<String, String> testMe);

}