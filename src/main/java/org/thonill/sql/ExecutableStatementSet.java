package org.thonill.sql;

import java.util.*;

import org.thonill.excel.WriteCSVFile;
import org.thonill.excel.WriteExcelFile;
import org.thonill.values.ArrayValue;

import java.sql.*;

public class ExecutableStatementSet extends ArrayList<ExecutableStatement> {

    public ExecutableStatementSet() {
        super();
    }

    public void checkQuerys(Connection conn) throws Exception {
        for (ExecutableStatement statement : this) {
            statement.checkQuery(conn);
        }
    }

    public ResultOfStatments execute(Connection conn) throws Exception {
        ResultOfStatments results = new ResultOfStatments();
        for (ExecutableStatement statement : this) {
            statement.exportToResults(conn, results);
        }
        return results;
    }

    public void close() {
        for (ExecutableStatement statement : this) {
            statement.close();
        }
    }

    public void writeToOutputFile(Connection conn, String ausgabeDatei, String vorlageDatei)
            throws Exception {

        checkQuerys(conn);
        ResultOfStatments results = execute(conn);
        
        if (ausgabeDatei.endsWith(".csv")) {
            ArrayValue arrayValue = results.getArrays().iterator().next();
            WriteCSVFile.writeResultSetToCSV(ausgabeDatei, false,arrayValue);
        }
        if (ausgabeDatei.endsWith(".xls") || ausgabeDatei.endsWith(".xlsx")) {
            WriteExcelFile writer = new WriteExcelFile();
            writer.writeResultSetToExcel(ausgabeDatei, vorlageDatei, results);
        }
        close();
    }

}
