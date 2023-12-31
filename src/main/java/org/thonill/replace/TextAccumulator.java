package org.thonill.replace;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract class for accumulating text for replacements. Subclasses implement
 * specific accumulation logic.
 */

abstract class TextAccumulator implements Accumulator<ReplaceDescription> {
	protected String text;

	public TextAccumulator(String text) {
		this.text = text;
		checkNotNull(text, "ReplaceAccumulator.constructor: text is null");
	}

	public String getText() {
		return text;
	}

}
