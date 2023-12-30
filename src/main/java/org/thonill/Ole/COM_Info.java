package org.thonill.Ole;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.sun.jna.platform.win32.COM.COMUtils.COMInfo;
import com.sun.jna.platform.win32.COM.COMUtils;
import java.io.File;

import java.util.logging.Logger;

public class COM_Info {
    private static final Logger LOG = Logger.getLogger(COM_Info.class.getName());

    public static void main(String[] args) {
        FileWriter writer = null;
        try {
            String filename = new File(".\\build\\tmp", "CLSIDs.txt").getAbsolutePath();
            ArrayList<COMInfo> comInfos = COMUtils.getAllCOMInfoOnSystem();
            writer = new FileWriter(filename);

            for (COMInfo comInfo : comInfos) {
                if (comInfo.progID != null) {
                    String result = "CLSID: " + comInfo.clsid + "\n";
                    result += "InprocHandler32: " + comInfo.inprocHandler32 + "\n";
                    result += "InprocServer32: " + comInfo.inprocServer32 + "\n";
                    result += "LocalServer32: " + comInfo.localServer32 + "\n";
                    result += "ProgID: " + comInfo.progID + "\n";
                    result += "ProgTypeLibID: " + comInfo.typeLib + "\n";

                    writer.write(result + "\n");
                }
            }

            LOG.info("file written to: " + filename);
            LOG.info("Found CLSID`s on the system: "
                    + comInfos.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
