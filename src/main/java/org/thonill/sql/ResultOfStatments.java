package org.thonill.sql;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;

import org.thonill.logger.LOG;
import org.thonill.values.ArrayValue;
import org.thonill.values.Value;

/**
 * The ResultOfStatments class represents the results of executing SQL
 * statements. It stores singular result values in a HashMap and array/multiple
 * results in another HashMap. Provides methods to add results and retrieve them
 * by name.
 */
public class ResultOfStatments {

	HashMap<String, Value> singularResults;
	HashMap<String, ArrayValue> multipleResults;

	public ResultOfStatments() {
		singularResults = new HashMap<>();
		multipleResults = new HashMap<>();
	}

	public void putSingle(String name, Value value) {
		checkNotNull(name, "ResultOfStatments name is null");
		checkNotNull(value, "ResultOfStatments value is null");

		LOG.info("putSingle {0} {1}" , name, value.toString());
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
			LOG.info("getSingle  {0} {1}" , name , singularResults.get(name).toString());
			return singularResults.get(name);
		} else {
			LOG.info("getSingle  {0}  not found", name);
		}
		return null;
	}

	public ArrayValue getArray(String name) {
		checkNotNull(name, "ResultOfStatments name is null");
		ArrayValue areaValue = multipleResults.get(name);
		if (areaValue != null) {
			LOG.info("getArray  {0} {1}" , name , areaValue.toString());
			return areaValue;
		} else {
			LOG.info("getArray  {0}  not found" , name );
		}
		return null;
	}

	public Collection<ArrayValue> getArrays() {
		return multipleResults.values();
	}
}
