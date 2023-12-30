package org.thonill.values;

/**
 * Value interface defines methods to get the value of an object
 * in different formats like String, double, boolean etc.
 */
public interface Value {
    String getName();

    String getString();

    double getDouble();

    boolean getBoolean();

    String getCSVValue();
}
