package org.thonill.gui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JDialog;

import org.thonill.replace.Accumulator;

/**
 * DialogAccumulator provides a Swing dialog UI for accumulating input field
 * values.
 */

public class DialogAccumulator implements Accumulator<FieldDescription> {
	private JDialog dialog;
	private JPanelAccumulator vertikal;

	public DialogAccumulator(String text) {
		checkNotNull(text, "DialogAccumulator.constructor: text is null");
		dialog = new JDialog();
		dialog.setTitle(text);
		vertikal = new JPanelAccumulator();
		vertikal.setLayout(new BoxLayout(vertikal, BoxLayout.Y_AXIS));
		dialog.add(vertikal);
	}

	@Override
	public void accumulate(FieldDescription value) {
		checkNotNull(value, "DialogAccumulator.accumulate: value is null");
		vertikal.accumulate(value);

	}

	public void showDialog() {
		dialog.setModal(true);
		dialog.pack();
		dialog.setVisible(true);

	}

	public HashMap<String, String> getHashMapFromDialog() {
		return vertikal.getHashMapFromDialog();
	}

	public void setValues() {
		vertikal.setValues();
	}

}
