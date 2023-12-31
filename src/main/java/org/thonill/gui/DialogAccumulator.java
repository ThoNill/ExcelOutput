package org.thonill.gui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.thonill.replace.Accumulator;

/**
 * DialogAccumulator provides a Swing dialog UI for accumulating input field
 * values.
 */

public class DialogAccumulator implements Accumulator<FieldDescription> {
	private JDialog dialog;
	private JPanel vertikal;
	private List<FieldDescription> fields;

	public DialogAccumulator(String text) {
		checkNotNull(text, "DialogAccumulator.constructor: text is null");

		fields = new ArrayList<>();
		dialog = new JDialog();
		dialog.setTitle(text);
		vertikal = new JPanel();
		vertikal.setLayout(new BoxLayout(vertikal, BoxLayout.Y_AXIS));
		dialog.add(vertikal);
	}

	@Override
	public void accumulate(FieldDescription value) {
		checkNotNull(value, "DialogAccumulator.accumulate: value is null");

		JTextField textField = new JTextField(value.getValue());
		textField.setName(value.getName());
		JLabel label = new JLabel(value.getLabel());
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(label);
		panel.add(textField);
		vertikal.add(panel);

	}

	public void showDialog() {
		dialog.setModal(true);
		dialog.pack();
		dialog.setVisible(true);

	}

	private HashMap<String, String> getHashMapFromDialog() {
		HashMap<String, String> result = new HashMap<>();
		for (Component c : vertikal.getComponents()) {
			if (c instanceof JPanel) {
				JPanel panel = (JPanel) c;
				for (Component cc : panel.getComponents()) {
					if (cc instanceof JTextField) {
						JTextField textField = (JTextField) cc;
						String value = textField.getText();
						String key = textField.getName();
						result.put(key, value);
					}
				}
			}
		}
		return result;
	}

	private void setValuesFromHashMap(List<FieldDescription> fields, HashMap<String, String> values) {
		for (FieldDescription field : fields) {
			String value = values.get(field.getName());
			if (value != null) {
				field.setValue(value);
			}
		}
	}

	public void setValues() {
		setValuesFromHashMap(fields, getHashMapFromDialog());
	}

}
