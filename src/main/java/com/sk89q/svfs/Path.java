package com.sk89q.svfs;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Path {
    
    private final static int MAX_PATH_LEN = 60;
    private final static int MAX_PATH_DEPTH = 10;
    
    private final String[] parts;
    
    public Path(String[] parts) {
        this.parts = Arrays.copyOf(parts, parts.length);
    }
    
    public Path(List<String> partsList) {
        String[] parts = new String[partsList.size()];
        partsList.toArray(parts);
        this.parts = parts;
    }
    
    public String[] parts() {
        return parts;
    }
    
    public boolean isRoot() {
        return parts.length == 0;
    }
    
    public Path getParent() {
        if (parts.length == 0) {
            return new Path(new String[0]);
        }
        String[] parentParts = new String[parts.length - 1];
        for (int i = 0; i < parts.length - 1; i++) {
            parentParts[i] = parts[i];
        }
        return new Path(parentParts);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String part : parts) {
            if (!first) {
                builder.append("/");
            }
            builder.append(part);
            first = false;
        }
        return builder.toString();
    }
    
    public static Path parse(String uncleanPath) throws IOException {
        if (uncleanPath.length() > MAX_PATH_LEN) {
            throw new IOException("Path is too long");
        }
        
        LinkedList<String> parts = new LinkedList<String>();
        
        // Remove last slash
        uncleanPath = uncleanPath.replaceAll("[\\/]+$", "");
        
        for (String part : uncleanPath.split("[\\/]+")) {
            part = part.trim();
            
            if (part.length() == 0) {
                throw new IOException("Invalid path length");
            } else if (part.equals(".")) {
                continue;
            } else if (part.equals("..")) {
                if (parts.size() == 0) {
                    throw new IOException("Directory traversal error in path");
                } else {
                    parts.removeLast();
                }
            } else {
                checkPart(part);
                parts.add(part);
            }
        }
        
        if (parts.size() == 0) {
            throw new IOException("Path has no elements");
        }
        
        if (parts.size() >= MAX_PATH_DEPTH) {
            throw new IOException("Path is too deep");
        }
        
        return new Path(parts);
    }

    private static void checkPart(String part) throws IOException {
        if (part.indexOf('\0') != -1) {
            throw new IOException("Invalid path");
        }
    }
    
}
