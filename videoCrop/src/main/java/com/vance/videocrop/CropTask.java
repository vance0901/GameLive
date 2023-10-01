package com.vance.videocrop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class CropTask {

    private final String path;
    private final Video video;

    public CropTask(String path, Video video) {
        this.path = path;
        this.video = video;
    }

    public void start() {
        String dir = video.videoName.substring(0, video.videoName.length() - 4);
        File file = new File(path, dir);
        file.mkdirs();
        File videoPath = new File(path, video.videoName);

        for (Video.Section section : video.sections) {
            File outFile = new File(file, section.name);
            StringBuilder sb = new StringBuilder("ffmpeg -i \"");
            sb.append(videoPath.getAbsolutePath());
            sb.append("\"  -acodec copy -vcodec copy -ss ");
            sb.append(section.start);
            sb.append(" -to ");
            sb.append(section.end);
            sb.append(" \"");
            sb.append(outFile.getAbsolutePath());
            sb.append(".mp4\" -y");
            System.out.println(sb.toString());
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(sb.toString());
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                process.destroy();
            }
        }


    }
}
