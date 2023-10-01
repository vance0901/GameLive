package com.vance.videocrop;

import java.util.ArrayList;
import java.util.List;

public class Video {
    public String videoName;
    public List<Section> sections = new ArrayList<>();

    public Video() {

    }

    public static class Section {
        public String start;
        public String end;
        public String name;
    }

}
