package org.thonill.Ole.COMInterfaces;


import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;

@ComInterface(iid = "{00024500-0000-0000-C000-000000000046}")
public interface ExcelApplication {
    @ComProperty
    int getVisible();

    @ComProperty
    void setVisible(boolean value);

    @ComMethod
    void Quit();

    @ComProperty
    List<Workbook> getWorkbooks();
}