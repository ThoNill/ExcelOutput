package org.thonill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.thonill.replace.ReplaceAccumulator;
import org.thonill.replace.ReplaceDescription;

public class ReplaceAccumulatorTest {
    @Test
    void testAccumulate() {
        replaceAndCompare(" 123 ", "{kundenid}","123");
        replaceAndCompare("kundenid + 123 ", "kundenid + {kundenid}","123");
        replaceAndCompare("kundenid = 123 ", "kundenid = {kundenid}","123");
        replaceAndCompare("kundenid  in (123,456) ", "kundenid = {kundenid}","123,456");
        withException( "kundenid + {kundenid}","123,345");
    }

    private void replaceAndCompare(String replaced, String original,String fieldValue) {

        ReplaceAccumulator ra = new ReplaceAccumulator(original);
        ra.accumulate(new ReplaceDescription("kundenid", fieldValue));

        assertEquals(replaced, ra.getText());

    }

     private void withException(String original,String fieldValue) {
        try {
        ReplaceAccumulator ra = new ReplaceAccumulator(original);
        ra.accumulate(new ReplaceDescription("kundenid", fieldValue));
        fail("Without Exception");
        } catch (Exception e) {

        }

    }
}
