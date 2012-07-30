package com.sk89q.mntfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Path implements Comparable<Path> {
    
    private final static int MAX_PATH_LEN = 60;
    private final static int MAX_PATH_DEPTH = 10;
    
    private final String[] parts;
    
    private Path(String[] parts) {
        this.parts = Arrays.copyOf(parts, parts.length);
    }
    
    private Path(List<String> partsList) {
        String[] parts = new String[partsList.size()];
        partsList.toArray(parts);
        this.parts = parts;
    }
    
    public String[] parts() {
        return Arrays.copyOf(parts, parts.length);
    }
    
    public String get(int index) {
        return parts[index];
    }
    
    public boolean isRoot() {
        return parts.length == 0;
    }
    
    public Path parent() {
        if (parts.length == 0) {
            return new Path(new String[0]);
        }
        
        String[] parentParts = new String[parts.length - 1];
        for (int i = 0; i < parts.length - 1; i++) {
            parentParts[i] = parts[i];
        }
        
        return new Path(parentParts);
    }
    
    public Path combine(String part) throws IOException {
        if (part.length() == 0) {
            throw new IOException("Invalid path length");
        } else if (part.equals(".")) {
            return this;
        } else if (part.equals("..")) {
            if (parts.length == 0) {
                throw new IOException("Directory traversal error in path");
            } else {
                return parent();
            }
        } else {
            checkPart(part);
            String[] newParts = Arrays.copyOf(parts, parts.length + 1);
            newParts[newParts.length - 1] = part;
            return new Path(newParts);
        }
    }
    
    public Path relative(Path child) {
        if (child.size() < size()) {
            return null;
        }
        
        List<String> newParts = new ArrayList<String>();
        boolean matching = true;
        
        for (int i = 0; i < child.size(); i++) {
            if (matching && !child.parts[i].equals(parts[i])) {
                matching = false;
            }
            
            if (!matching) {
                newParts.add(child.parts[i]);
            }
        }
        
        return new Path(newParts);
    }
    
    public boolean contains(Path other) {
        if (parts.length > other.parts.length) {
            return false;
        }
        
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].equals(other.parts[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    public int size() {
        return parts.length;
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
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Path)) return false;
        Path other = (Path) obj;
        return toString().equals(other.toString());
    }
    
    public static Path root() {
        return new Path(new String[0]);
    }
    
    public static Path parse(String uncleanPath) throws IOException {
        if (uncleanPath.length() > MAX_PATH_LEN) {
            throw new IOException("Path is too long");
        }
        
        LinkedList<String> parts = new LinkedList<String>();
        
        // Remove last and first slash
        uncleanPath = uncleanPath.replaceAll("[\\/]+$", "");
        uncleanPath = uncleanPath.replaceAll("^[\\/]+", "");
        
        if (uncleanPath.length() == 0) {
            return Path.root();
        }
        
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
        if (part.indexOf('\0') != -1 || part.indexOf('\\') != -1 || part.indexOf('/') != -1) {
            throw new IOException("Invalid path");
        }
    }

    @Override
    public int compareTo(Path other) {
        int len = parts.length <= other.parts.length ? parts.length : other.parts.length;
        
        for (int i = 0; i < len; i++) {
            int val = parts[i].compareTo(other.parts[i]);
            if (val != 0) {
                return val;
            }
        }
        
        if (parts.length == other.parts.length) {
            return 0;
        } else if (parts.length > other.parts.length) {
            return 1;
        } else {
            return -1;
        }
    }
    
}
