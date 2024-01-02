package org.thonill.ole;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.thonill.logger.LOG;

import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.COMUtils.COMInfo;

public class MyCOMInfo {

	public static void main(String[] args) {
		FileWriter writer = null;
		try {
			String filename = new File(".\\build\\tmp", "CLSIDs.txt").getAbsolutePath();
			ArrayList<COMInfo> comInfos = COMUtils.getAllCOMInfoOnSystem();
			writer = new FileWriter(filename);

			for (COMInfo comInfo : comInfos) {
				if (comInfo.progID != null) {
					StringBuilder result = new StringBuilder("CLSID: " + comInfo.clsid + "\n");
					result.append("InprocHandler32: " + comInfo.inprocHandler32 + "\n");
					result.append("InprocServer32: " + comInfo.inprocServer32 + "\n");
					result.append("LocalServer32: " + comInfo.localServer32 + "\n");
					result.append("ProgID: " + comInfo.progID + "\n");
					result.append("ProgTypeLibID: " + comInfo.typeLib + "\n\n");

					writer.write(result.toString());
				}
			}

			LOG.info("file written to: {0}", filename);
			LOG.info("Found CLSID`s on the system: {0}", comInfos.size());
		} catch (IOException e) {
			LOG.severe(e.getLocalizedMessage());
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				LOG.severe(e.getLocalizedMessage());
			}
		}
	}

}
