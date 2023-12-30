package org.thonill.gui;

import org.thonill.replace.ReplaceDescription;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * FieldDescription represents a field in a form, with a name, value and label.
 * It extends ReplaceDescription to support replacing values in the field.
 */
public class FieldDescription extends ReplaceDescription {

    private String fieldLabel;

    public FieldDescription(String fieldName, String fieldValue, String fieldLabel) {
        super(fieldName, fieldValue);
        checkNotNull(fieldLabel, "FieldDescription.constructor: fieldLabel is null");
        this.fieldLabel = fieldLabel;

        calculatePattern(fieldName, fieldValue);
    }

    public String getLabel() {

        return fieldLabel;
    }

}
