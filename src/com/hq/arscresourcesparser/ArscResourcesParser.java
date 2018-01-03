package com.hq.arscresourcesparser;

import com.hq.arscresourcesparser.arsc.ArscFile;
import com.hq.arscresourcesparser.arsc.ResTableEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 */
public class ArscResourcesParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //String fileName = "/home/hq/temp/uni.apk";
        //String fileName = "/home/hq/temp/resources.arsc";
        String fileName = args[0];
        String xml = "";
        try {
            if (fileName.endsWith(".apk")) {
                try (ZipFile zip = new ZipFile(fileName)) {
                    ZipEntry amz;
                    amz = zip.getEntry("resources.arsc");
                    try (InputStream amis = zip.getInputStream(amz)) {
                        int BUFFER_SIZE = (int) (amz.getSize() > 51200 ? 51200 : amz.getSize());
                        byte[] buf = new byte[BUFFER_SIZE];
                        int bytesRead = amis.read(buf);
                        ArscFile arscFile = new ArscFile();
                        arscFile.parse(buf);
                        xml = arscFile.buildPublicXml();
                    }
                }
            } else if (fileName.endsWith(".arsc")) {
                File file = new File(fileName);
                FileInputStream fin = new FileInputStream(file);
                byte buf[] = new byte[(int) file.length()];
                fin.read(buf);
                ArscFile arscFile = new ArscFile();
                arscFile.parse(buf);
                xml = arscFile.buildPublicXml();
                fin.close();
                
                // testing
                int resId = 0x7f030001;
                short packageId = (short) (resId >> 24 & 0xff);
                short typeId = (short) ((resId >> 16) & 0xff);
                int entryIndex = (int) (resId & 0xffff);
                ResTableEntry res = arscFile.getResource(resId);
                if (res != null) {
                    System.out.println(res.toString());
                } else {
                    System.out.println("Resource ID 0x" + String.format("%04x", resId) + " cannot be found.");
                }
                
            } else {
                xml = "Non valide file.";
            }
        } catch (IOException ex) {
            xml = "Non valide file.";
        }
        System.out.println(xml);
    }

}
