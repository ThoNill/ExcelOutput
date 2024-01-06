package org.thonill.gui;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.thonill.replace.Accumulator;

/**
 * DialogAccumulator provides a Swing dialog UI for accumulating input field
 * values.
 */

public class JPanelAccumulator extends JPanel implements Accumulator<FieldDescription> {
	private List<FieldDescription> fields;
	private List<JTextField> textFields;

	public JPanelAccumulator(List<FieldDescription> descriptions) {
		this();
		setLayout(new GridLayout(descriptions.size(), 1, 5, 5));
		perform(descriptions);
	}

	public JPanelAccumulator() {
		super();
		fields = new ArrayList<>();
		textFields = new ArrayList<>();
	}

	@Override
	public void accumulate(FieldDescription fieldDescription) {
		checkNotNull(fieldDescription, "DialogAccumulator.accumulate: value is null");

		JTextField textField = new JTextField(fieldDescription.getValue());
		textFields.add(textField);
		textField.setName(fieldDescription.getName());
		JLabel label = new JLabel(fieldDescription.getLabel());
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(label);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(textField);
		add(panel);
		fields.add(fieldDescription);

	}

	public HashMap<String, String> getHashMapFromDialog() {
		HashMap<String, String> result = new HashMap<>();
		setHashmap(result);
		return result;
	}

	public void setValues() {
		setValuesFromHashMap(fields, getHashMapFromDialog());
	}

	public void setHashmap(HashMap<String, String> map) {
		for (JTextField textField : textFields) {
			String value = textField.getText();
			String key = textField.getName();
			map.put(key, value);
		}
	}

	public void setActiveArgument(ActiveArguments map) {
		for (JTextField textField : textFields) {
			String value = textField.getText();
			String key = textField.getName();
			map.put(key, value);
		}
	}

	public void storeProperties(Properties map) {
		for (JTextField textField : textFields) {
			String value = textField.getText();
			String key = textField.getName();
			map.put(key, value);
		}
	}

	public void loadProperties(Properties map) {
		for (JTextField textField : textFields) {
			String key = textField.getName();
			String value = map.getProperty(key);
			textField.setText((value == null) ? "" : value);
		}
	}

	private void setValuesFromHashMap(List<FieldDescription> fields, HashMap<String, String> values) {
		for (FieldDescription field : fields) {
			String value = values.get(field.getName());
			if (value != null) {
				field.setValue(value);
			}
		}
	}

}
