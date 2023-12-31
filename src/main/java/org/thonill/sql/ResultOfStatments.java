package org.thonill.sql;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import org.thonill.values.ArrayValue;
import org.thonill.values.Value;

/**
 * The ResultOfStatments class represents the results of executing SQL
 * statements. It stores singular result values in a HashMap and array/multiple
 * results in another HashMap. Provides methods to add results and retrieve them
 * by name.
 */
public class ResultOfStatments {
	private static final Logger LOG = Logger.getLogger(ResultOfStatments.class.getName());

	HashMap<String, Value> singularResults;
	HashMap<String, ArrayValue> multipleResults;

	public ResultOfStatments() {
		singularResults = new HashMap<>();
		multipleResults = new HashMap<>();
	}

	public void putSingle(String name, Value value) {
		checkNotNull(name, "ResultOfStatments name is null");
		checkNotNull(value, "ResultOfStatments value is null");

		LOG.info("putSingle " + name + " " + value.toString());
		singularResults.put(name, value);
	}

	public void putArray(String name, ArrayValue value) {
		checkNotNull(name, "ResultOfStatments name is null");
		checkNotNull(value, "ResultOfStatments value is null");
		multipleResults.put(name, value);
	}

	public Value getSingleObject(String name) {
		checkNotNull(name, "ResultOfStatments name is null");
		if (singularResults.containsKey(name)) {
			LOG.info("getSingle " + name + " " + singularResults.get(name).toString());
			return singularResults.get(name);
		} else {
			LOG.info("getSingle " + name + " not found");
		}
		return null;
	}

	public ArrayValue getArray(String name) {
		checkNotNull(name, "ResultOfStatments name is null");
		ArrayValue areaValue = multipleResults.get(name);
		if (areaValue != null) {
			LOG.info("getArray " + name + " " + areaValue.toString());
			return areaValue;
		} else {
			LOG.info("getArray " + name + " not found");
		}
		return null;
	}

	public Collection<ArrayValue> getArrays() {
		return multipleResults.values();
	}
}
