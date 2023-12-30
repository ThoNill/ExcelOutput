package org.thonill;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.thonill.actions.AusgabeSteuerItem;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Test class for AusgabeSteuerItem
 */
public class AusgabeSteuerItemTest extends SqlTest {

    @BeforeAll
    public static void AusgabeSteuerItemTestInit() {
        AppTestinit();
    }

    @Test
    void testCreateAusgabeDateien() {
        try {
            try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
                AusgabeSteuerItem.createAusgabeDateien("src\\test\\resources\\Steuerung.xls", conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Creations of files failed");

        }
    }
}
