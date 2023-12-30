package org.thonill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.thonill.replace.RawSqlStatement;
import org.thonill.replace.SplitSqlText;

public class SplitSqlTextTest {
    private SplitSqlText splitter = new SplitSqlText();

    @Test
    void testSpliter() {
        String sql = "SELECT * FROM kunden";
        List<RawSqlStatement> sqls = splitter.extractList("SELECT * FROM kunden");
        assertEquals(1, sqls.size());
        assertEquals(sql,sqls.get(0).getQueryString());

        testExtraction(" \n\nSELECT * FROM kunden;SELECT * FROM rechnung"
                , "SELECT * FROM kunden");

        testExtraction("SELECT * FROM kunden;SELECT * FROM rechnung"
                , "SELECT * FROM kunden");

        testExtraction("SELECT * FROM kunden\n  \n SELECT * FROM rechnung"
                , "SELECT * FROM kunden");

        testExtraction("SELECT * FROM kunden -- ein Zeilenkommentar \n  \n SELECT * FROM rechnung"
                , "SELECT * FROM kunden");

        testExtraction("SELECT * FROM kunden /* ein Kommentar */\n  \n SELECT * FROM rechnung"
                , "SELECT * FROM kunden");

        testExtraction("SELECT * FROM kunden /* ein Kom\nmentar */\n  \n SELECT * FROM rechnung"
                , "SELECT * FROM kunden");

        testExtraction("SELECT * /* ein Kom\nmentar */ FROM kunden \n  \n SELECT * FROM rechnung"
                , "SELECT *  FROM kunden");

        testExtraction(
                "SELECT * /* ein Kom\nmentar -- in einem anderen */ FROM kunden \n  \n SELECT * FROM rechnung"
                , "SELECT *  FROM kunden");

        testExtraction(
                "SELECT * -- und /* ein Kommentar -- in einem anderen */ \n FROM kunden \n  \n SELECT * FROM rechnung"
                , "SELECT *  FROM kunden");

        testExtraction(
                "SELECT -- und /* ein Kommentar -- in einem anderen */ \n* FROM kunden \n  \n SELECT * FROM rechnung"
                , "SELECT * FROM kunden");

        testExtraction(
                "SELECT -- und /* ein Kommentar -- in einem anderen */ \na FROM kunden \n  \n SELECT * FROM rechnung"
                , "SELECT a FROM kunden");

        testExtraction(
                "SELECT -- und /* ein Kommentar -- in einem anderen */ \na FROM kunden \n \n \n SELECT * FROM rechnung"
                , "SELECT a FROM kunden");

        testExtraction(
                "SELECT -- und /* ein Kommentar -- in einem anderen */ \na FROM kunden \n\n SELECT * FROM rechnung"
                , "SELECT a FROM kunden");

        testExtraction(
                "SELECT -- und /* ein Kommentar -- in einem anderen */ \na FROM kunden \n;\n SELECT * FROM rechnung"
                , "SELECT a FROM kunden");

        testExtraction(
                "SELECT -- und /* ein Kommentar -- in einem anderen */ \na FROM kunden \n ; \n SELECT * FROM rechnung"
                , "SELECT a FROM kunden");

        testExtraction(
                "SELECT -- und /* ein Kommentar -- in einem anderen */ \na FROM kunden \n ;\n\n SELECT * FROM rechnung"
                , "SELECT a FROM kunden");

        testExtraction(
                "SELECT 'das',-- und /* ein Kommentar -- in einem anderen */ \na FROM kunden \n;\n SELECT * FROM rechnung"
                , "SELECT 'das',a FROM kunden");

        testExtraction(
                "SELECT 'd\"as',-- und /* ein Kommentar -- in einem anderen */ \na FROM kunden \n;\n SELECT * FROM rechnung"
                , "SELECT 'd\"as',a FROM kunden");

        testExtraction(
                "SELECT \"d'as\",-- und /* ein Kommentar -- in einem anderen */ \na FROM kunden \n;\n SELECT * FROM rechnung",
                "SELECT \"d'as\",a FROM kunden");

    }

    private void testExtraction(String sqls, String sql0) {
        List<RawSqlStatement> s = splitter.extractList(sqls);
        assertEquals(2, s.size());
        assertEquals(sql0, s.get(0).getQueryString());
        assertEquals("SELECT * FROM rechnung", s.get(1).getQueryString());
    }
}
