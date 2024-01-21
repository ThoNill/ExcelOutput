package org.thonill.replace;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

/**
 * Accumulator interface for accumulating objects of type C. Provides default
 * implementation of perform() to accumulate a list of objects.
 */

public interface Accumulator<C> {
	void accumulate(C object);

	default void perform(List<C> list) {
		checkNotNull(list, "Accumulator.perform: list is null");
		for (C object : list) {
			accumulate(object);
		}
	}
}
