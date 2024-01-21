package org.thonill.replace;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates FieldDescription instances that match a given pattern.
 */

public class PatternAccumulator implements Accumulator<ReplaceDescription> {
	private List<ReplaceDescription> usedObjects = new ArrayList<>();
	private String text;

	public PatternAccumulator(String text) {
		this.text = text;
	}

	@Override
	public void accumulate(ReplaceDescription value) {
		checkNotNull(value, "PatternAccumulator.accumulate: value is null");
		if (text.matches(value.getPattern())) {
			usedObjects.add(value);
		}
	}

}
