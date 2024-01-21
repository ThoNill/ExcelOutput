package org.thonill;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.thonill.checks.DefaultMapCheck;
import org.thonill.checks.MapCheck;

class DefaultMapCheckTest {

	@Test
	void test() {
		test("123", null,null, true);
		test("123,12", null,null, true);
		test(null, "12",null, true);
		test(null, "12,1",null, true);
		test("123,12","12,1","2004,2012",true);

	}

	void test(String kunden, String monat, String jahr, boolean b) {

		HashMap<String, String> map = new HashMap<>();
		map.put("kunden", "^ *[0-9]+ *(, *[0-9]+)* *$"); 
		map.put("monat", "^ *(1[0-2]|[0-9]) *(,1[0-2]|,[1-9])* *$"); 
		map.put("jahr", "^ *20[0-9][0-9] *(,20[0-9][0-9])* *$");

		MapCheck check = new DefaultMapCheck(map);

		HashMap<String, String> daten = new HashMap<>();
		if (kunden != null) {
			daten.put("kunden", kunden);
		}
		if (monat != null) {
			daten.put("monat", monat);
		}
		if (jahr != null) {
			daten.put("jahr", jahr);
		}
		assertEquals(b, check.check(daten));

	}

}
