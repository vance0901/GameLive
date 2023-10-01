package com.vance.tool;

import java.io.File;
import java.io.FileOutputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args == null) {
            throw new RuntimeException("加密失败,请检查是否指定UUID与待加密文件.");
        }
        if (args[0] == null || args[0].isEmpty()) {
            throw new RuntimeException("加密失败,没有指定UUID.");
        }
        if (args[1] == null || args[1].isEmpty()) {
            throw new RuntimeException("加密失败,没有指定待加密文件.");
        }

        if (!new File(args[1]).exists()) {
            throw new RuntimeException("加密失败：" + args[1] + " 不存在.");
        }
        System.out.println("uuid:" + args[0]);
        System.out.println("加密:" + args[1]);
        String uuid = args[0];
        String file = args[1];
        String secretKey = AESUtils.getSecretKey(uuid);
        AESUtils.encryptFile(secretKey, file, "./dec/bin/" + uuid);


        File zipFile = new File("./../out/" + uuid + ".zip");
        if (!zipFile.getParentFile().exists()) {
            zipFile.getParentFile().mkdirs();
        }
        ZipUtil.toZip("./", new FileOutputStream(zipFile));
        new File("./dec/bin/" + uuid).delete();
    }


}