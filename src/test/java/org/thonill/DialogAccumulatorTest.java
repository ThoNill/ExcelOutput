package org.thonill;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import org.thonill.gui.DialogAccumulator;
import org.thonill.gui.FieldDescription;

/**
 * Test class for DialogAccumulator
 */

public class DialogAccumulatorTest {

	@Disabled
	@Test
	public void testDialogAccumulator() {

		DialogAccumulator accumulator = new DialogAccumulator("Test Dialog");

		accumulator.accumulate(new FieldDescription("KUNDENIK", "999999999", "Kunden IK"));
		accumulator.accumulate(new FieldDescription("MONAT", "01", "Monat"));

		accumulator.showDialog();

		// Überprüfe, ob der Dialog korrekt angezeigt wird
		// z.B. Anzahl der Panels, Labels und Textfelder
	}
}
