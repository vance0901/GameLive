package com.vance.videocrop;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class MyClass {

    public static void main(String[] args) throws Exception {
        String xls = args[0];
        File Inputfile = new File(xls);
        FileInputStream fileInputStream = new FileInputStream(Inputfile);
        Workbook workbook = Workbook.getWorkbook(fileInputStream);
        Sheet readfirst = workbook.getSheet(0);
        int rows = readfirst.getRows();
        Map<String, List<Video>> data = new HashMap<>();
        String key = null;
        Video video = null;
        for (int i = 0; i < rows; i++) {
            Cell cell = readfirst.getCell(0, i);
            if (!isEmpty(cell)) {
                key = cell.getContents();
                data.put(key, new ArrayList<Video>());
            }
            cell = readfirst.getCell(1, i);
            List<Video> videos = data.get(key);
            if (!isEmpty(cell)) {
                video = new Video();
                video.videoName = cell.getContents();
                videos.add(video);
            }
            Video.Section section = new Video.Section();
            cell = readfirst.getCell(2, i);
            section.start = cell.getContents();
            cell = readfirst.getCell(3, i);
            section.end = cell.getContents();
            cell = readfirst.getCell(4, i);
            section.name = cell.getContents();
            video.sections.add(section);
        }


        Set<String> keySet = data.keySet();
        for (String s : keySet) {
            System.out.println("当前目录：" + s);
            List<Video> videos = data.get(s);
            for (Video video1 : videos) {
                System.out.println("视频名：" + video1.videoName);
                for (Video.Section section : video1.sections) {
                    System.out.println(section.name + ":" + section.start + "->" + section.end);
                }
            }

        }

        for (String s : keySet) {
            List<Video> videos = data.get(s);
            for (Video video1 : videos) {
                CropTask cropTask = new CropTask(s, video1);
                cropTask.start();
            }

        }
    }

    static boolean isEmpty(Cell cell) {
        return cell.getType().toString().equals("Empty");
    }
}