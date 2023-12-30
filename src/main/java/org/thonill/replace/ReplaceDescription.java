package org.thonill.replace;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Matcher;

/**
 * This class represents a description for replacing a field value.
 * It contains the field name, original value, replacement pattern, and
 * replacement value.
 */

public class ReplaceDescription {

    private String fieldName;
    private String fieldValue;
    private String fieldPattern;

    public ReplaceDescription(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        checkNotNull(fieldName, "ReplaceDescription.constructor: fieldName is null");
        checkNotNull(fieldValue, "ReplaceDescription.constructor: fieldValue is null");

        calculatePattern(fieldName, fieldValue);
    }

    protected void calculatePattern(String fieldName, String fieldValue) {
        checkNotNull(fieldName, "ReplaceDescription.calculatePattern: fieldName is null");
        checkNotNull(fieldValue, "ReplaceDescription.calculatePattern: fieldValue is null");

        this.fieldPattern = "(^|[^ ]) *\\{" + fieldName + "\\}";
    }

    public String getName() {
        return fieldName;
    }

    public String getPattern() {
        return fieldPattern;
    }

    public String getValue() {
        return fieldValue;
    }

    public String getReplace(Matcher matcher) {
        String fieldReplace = "";
        String c = matcher.group(1);
        if (fieldValue.contains(",")) {
            if ("=".equals(c) ) {
            fieldReplace = " in (" + fieldValue + ") ";
            } else {
                throw new IllegalArgumentException("ReplaceDescription.getReplace: invalid replace value: " + fieldValue + " because c= " + c);
            }
        } else {
            fieldReplace = c + " " + fieldValue + " ";
        }
        return fieldReplace;
    }

    public void setValue(String value) {
        checkNotNull(value, "ReplaceDescription.setValue: value is null");
        this.fieldValue = value;
        calculatePattern(fieldName, fieldValue);
    }

}
