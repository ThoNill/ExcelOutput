package org.thonill.replace;

import java.util.ArrayList;
import java.util.List;
import org.thonill.gui.FieldDescription;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Accumulates FieldDescription instances that match a given pattern.
 */

public class PatternAccumulator implements Accumulator<FieldDescription> {
    private List<FieldDescription> usedObjects = new ArrayList<FieldDescription>();
    private String text;

    public PatternAccumulator(String text) {
        this.text = text;
    }

    public void accumulate(FieldDescription value) {
        checkNotNull(value, "PatternAccumulator.accumulate: value is null");
        if (text.matches(value.getPattern())) {
            usedObjects.add(value);
        }
    }

}
