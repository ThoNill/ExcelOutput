package org.thonill.replace;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Accumulates text replacements.
 */

public class ReplaceAccumulator extends TextAccumulator {

    public ReplaceAccumulator(String text) {
        super(text);
        checkNotNull(text, "ReplaceAccumulator.constructor: text is null");
    }

    public void accumulate(ReplaceDescription description) {
        checkNotNull(description, "ReplaceAccumulator.accumulate: description is null");
        text = replaceAll(description, text);
    }

    private String replaceAll(ReplaceDescription description, String original) {
        int lastIndex = 0;
        StringBuilder output = new StringBuilder();
        Pattern pattern = Pattern.compile(description.getPattern());

        Matcher matcher = pattern.matcher(original);
        while (matcher.find()) {
            output.append(original, lastIndex, matcher.start())
                    .append(description.getReplace(matcher));

            lastIndex = matcher.end();
        }
        if (lastIndex < original.length()) {
            output.append(original, lastIndex, original.length());
        }
        return output.toString();
    }

}