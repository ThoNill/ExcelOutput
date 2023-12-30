package org.thonill.values;


/**
 * ArrayValue defines an interface for accessing elements in an array-like
 * structure.
 * 
 * It provides methods to get the name, size, and elements at a given position.
 * It also has a next() method to iterate through elements.
 * 
 * The default getValues() method returns all elements as a Value[] array.
 */
public interface ArrayValue {
    String getName();

    Value getPosition(int i);

    int size();

    boolean next();

    default Value[] getValues() {
        int numColumns = size();

        Value[] values = new Value[numColumns];

        for (int i = 0; i < numColumns; i++) {
            values[i] = getPosition(i);
        }
        return values;
    }
}
