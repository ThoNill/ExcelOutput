package org.thonill.ole.cominterfaces;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WinDef.LCID;
import com.sun.jna.platform.win32.COM.util.Factory;

public class WinOLEExcel {

	public static void main(String[] args) {

		// Initialize COM Subsystem
		Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
		// Initialize Factory for COM object creation
		Factory fact = new Factory();
		// Set LCID for calls to english locale. Without this formulas need
		// to be specified in the users locale.
		fact.setLCID(new LCID(0x0409));

		ExcelApplication excelApp = fact.createObject(ExcelApplication.class);

		excelApp.setVisible(true);

		// Hole ein Workbook mit JNA

		excelApp.Quit();
	}

}
