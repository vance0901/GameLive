package com.vance.dec;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws Exception {
        String uuid = getUUID();
        String secretKey = AESUtils.getSecretKey(uuid);
        String property = System.getProperty("java.io.tmpdir");
        AESUtils.decryptFile(secretKey, "./" + uuid,
                property+uuid+".cmd");
//        AESUtils.encryptFile(secretKey, "C:\\Users\\Administrator\\Desktop\\tool\\bin\\GDA3.70.exe",
//                "C:\\Users\\Administrator\\Desktop\\tool\\bin\\" + uuid);
//        AESUtils.decryptFile(secretKey, "C:\\Users\\Administrator\\Desktop\\tool\\bin\\" +uuid ,
//                "C:\\Users\\Administrator\\Desktop\\tool\\bin\\run.exe");
        Desktop.getDesktop().open(new File(property+uuid+".cmd"));
    }

    static String getUUID() throws Exception {
        Process process = Runtime.getRuntime().exec("cmd.exe /c wmic csproduct get UUID");
        int status = process.waitFor();

        if (status != 0) {
            InputStream errorStream = process.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            throw new RuntimeException("get UUID error!");
        }
        InputStream in = process.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        int i = 0;
        String uuid = null;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty() || line.trim().equals("UUID")) {
                continue;
            }
            uuid = line.trim();
        }
        process.destroy();
        return uuid;
    }
}