package com.sk89q.mntfs;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class IOUtil {
    
    private IOUtil() {
    }
    
    public static boolean close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                return false;
            }
        }
        
        return true;
    }
    
    public static void append(File file, String text) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, true);
            BufferedWriter out = new BufferedWriter(writer);
            out.write(text);
        } finally {
            close(writer);
        }
    }

}
