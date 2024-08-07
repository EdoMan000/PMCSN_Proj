package org.pmcsn.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FileUtils {

    public static void deleteDirectory(String dir) {
        File root = new File(dir);
        List<File> directories = new ArrayList<>();
        List<File> queue = new Stack<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            File d = queue.removeLast();
            directories.add(d);
            File[] files = d.listFiles();
            if (files != null) {
                List<File> children = List.of(files);
                children.stream().filter(File::isFile).toList().forEach(File::delete);
                queue.addAll(children.stream().filter(File::isDirectory).toList());
            }
        }
        directories.reversed().forEach(File::delete);
    }

    public static void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
