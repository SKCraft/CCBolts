package com.sk89q.mntdfs;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sk89q.mntfs.Path;
import com.sk89q.mntfs.ResourceSupervisor;
import com.sk89q.mntfs.WritableMount;

public class WritableMountTest {
    
    private static final int MAX_OPEN = 1;
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private WritableMount mount;

    @Before
    public void setUp() throws Exception {
        DataOutputStream out;
        File root = tempFolder.getRoot();
        File subfolder;

        out = new DataOutputStream(new FileOutputStream(new File(root, "accounts.lst")));
        out.writeInt(1234567890);
        out.close();

        out = new DataOutputStream(new FileOutputStream(new File(root, "receivable.txt")));
        out.writeShort(0);
        out.close();

        (subfolder = new File(root, "files")).mkdir();

        out = new DataOutputStream(new FileOutputStream(new File(subfolder, "papers.dat")));
        out.writeByte(0);
        out.close();

        (subfolder = new File(root, "to_delete")).mkdir();

        out = new DataOutputStream(new FileOutputStream(new File(subfolder, "file1.txt")));
        out.writeByte(0);
        out.close();

        out = new DataOutputStream(new FileOutputStream(new File(subfolder, "delete_me.txt")));
        out.writeByte(0);
        out.close();

        (subfolder = new File(subfolder, "subfolder")).mkdir();

        out = new DataOutputStream(new FileOutputStream(new File(subfolder, "subfile.txt")));
        out.writeByte(0);
        out.close();

        (subfolder = new File(root, "temp")).mkdir();

        out = new DataOutputStream(new FileOutputStream(new File(subfolder, "temp1.txt")));
        out.writeInt(1234567890);
        out.close();

        out = new DataOutputStream(new FileOutputStream(new File(subfolder, "temp2.txt")));
        out.writeInt(1234567890);
        out.close();

        ResourceSupervisor supervisor = new ResourceSupervisor(MAX_OPEN);
        supervisor.setMaxFileSize(1024); // bytes
        mount = new WritableMount(supervisor, root);
    }
    
    @Test
    public void testIsWritable() throws IOException {
        assertTrue(mount.isWritable(Path.root()));
        assertTrue(mount.isWritable(Path.parse("test")));
        assertTrue(mount.isWritable(Path.parse("accounts.lst")));
        assertTrue(mount.isWritable(Path.parse("files")));
    }
    
    @Test
    public void testList() throws IOException {
        Path[] rootList;;
        Path[] expected;
        
        rootList = mount.list(Path.root());
        expected = new Path[] {
                Path.parse("accounts.lst"),
                Path.parse("receivable.txt"),
                Path.parse("files"),
                Path.parse("to_delete"),
                Path.parse("temp"),
            };

        Arrays.sort(rootList);
        Arrays.sort(expected);
        
        assertArrayEquals(expected, rootList);
        
        rootList = mount.list(Path.parse("files"));
        expected = new Path[] {
                Path.parse("files/papers.dat"),
            };

        Arrays.sort(rootList);
        Arrays.sort(expected);
        
        assertArrayEquals(expected, rootList);
    }
    
    @Test(expected = FileNotFoundException.class)
    public void testListMissing1() throws IOException {
        mount.list(Path.parse("missing"));
    }
    
    @Test(expected = FileNotFoundException.class)
    public void testListMissing2() throws IOException {
        mount.list(Path.parse("files/missing"));
    }

    @Test
    public void testExists() throws IOException {
        assertTrue(mount.exists(Path.parse("/accounts.lst")));
        assertTrue(mount.exists(Path.parse("//accounts.lst")));
        assertTrue(mount.exists(Path.parse("accounts.lst/")));
        assertTrue(mount.exists(Path.parse("/accounts.lst/")));
        assertFalse(mount.exists(Path.parse("/missing.dat")));
        assertFalse(mount.exists(Path.parse("/missing.dat")));
        assertFalse(mount.exists(Path.parse("/accounts.lst/missing.dat")));
        assertTrue(mount.exists(Path.parse("receivable.txt")));
        assertTrue(mount.exists(Path.parse("files")));
        assertTrue(mount.exists(Path.parse("files/papers.dat")));
        assertFalse(mount.isDirectory(Path.parse("/files/missing/subfolder")));
        assertFalse(mount.exists(Path.parse("files/missing.dat")));
    }
    
    @Test
    public void testIsDirectory() throws IOException {
        assertFalse(mount.isDirectory(Path.parse("/accounts.lst")));
        assertFalse(mount.isDirectory(Path.parse("/receivable.txt")));
        assertTrue(mount.isDirectory(Path.parse("/")));
        assertTrue(mount.isDirectory(Path.parse("/files")));
        assertFalse(mount.isDirectory(Path.parse("/files/missing/subfolder")));
        assertFalse(mount.isDirectory(Path.parse("missing")));
    }
    
    @Test
    public void testSize() throws IOException {
        assertEquals(mount.getSize(Path.parse("accounts.lst")), 4);
        assertEquals(mount.getSize(Path.parse("receivable.txt")), 2);
        assertEquals(mount.getSize(Path.parse("files/papers.dat")), 1);
    }

    @Test(expected = FileNotFoundException.class)
    public void testSizeMissing() throws IOException {
        mount.getSize(Path.parse("missing.txt"));
    }
    
    @Test
    public void testMkdirs() throws IOException {
        String pathName = "records/names/by/id";
        assertFalse(new File(tempFolder.getRoot(), pathName).isDirectory());
        mount.mkdirs(Path.parse(pathName));
        assertTrue(new File(tempFolder.getRoot(), pathName).isDirectory());
        
        pathName = "files";
        assertTrue(new File(tempFolder.getRoot(), pathName).isDirectory());
        mount.mkdirs(Path.parse(pathName));
        assertTrue(new File(tempFolder.getRoot(), pathName).isDirectory());
    }
    
    @Test(expected = IOException.class)
    public void testMkdirsOnFile1() throws IOException {
        mount.mkdirs(Path.parse("accounts.lst"));
    }
    
    @Test(expected = IOException.class)
    public void testMkdirsOnFile2() throws IOException {
        mount.mkdirs(Path.parse("accounts.lst/folder"));
    }
    
    @Test
    public void testDelete() throws IOException {
        assertTrue(new File(tempFolder.getRoot(), "to_delete/delete_me.txt").exists());
        assertTrue(new File(tempFolder.getRoot(), "to_delete/file1.txt").exists());
        assertTrue(new File(tempFolder.getRoot(), "to_delete/subfolder/subfile.txt").exists());
        assertTrue(new File(tempFolder.getRoot(), "accounts.lst").exists());
        
        mount.delete(Path.parse("to_delete/delete_me.txt"));
        
        assertFalse(new File(tempFolder.getRoot(), "to_delete/delete_me.txt").exists());
        assertTrue(new File(tempFolder.getRoot(), "to_delete/file1.txt").exists());
        assertTrue(new File(tempFolder.getRoot(), "to_delete/subfolder/subfile.txt").exists());
        assertTrue(new File(tempFolder.getRoot(), "accounts.lst").exists());
        
        mount.delete(Path.parse("to_delete"));
        
        assertFalse(new File(tempFolder.getRoot(), "to_delete/delete_me.txt").exists());
        assertFalse(new File(tempFolder.getRoot(), "to_delete/file1.txt").exists());
        assertFalse(new File(tempFolder.getRoot(), "to_delete/subfolder/subfile.txt").exists());
        assertTrue(new File(tempFolder.getRoot(), "accounts.lst").exists());
    }
    
    @Test(expected = FileNotFoundException.class)
    public void testDeleteMissing1() throws IOException {
        assertFalse(new File(tempFolder.getRoot(), "missing").exists());
        mount.delete(Path.parse("files/missing"));
    }
    
    @Test(expected = FileNotFoundException.class)
    public void testDeleteMissing2() throws IOException {
        assertFalse(new File(tempFolder.getRoot(), "missing").exists());
        mount.delete(Path.parse("missing"));
    }
    
    @Test
    public void testMove() throws IOException {
        String from = "temp/temp1.txt";
        String to = "temp/temp1_moved.txt";

        assertTrue(new File(tempFolder.getRoot(), from).exists());
        assertFalse(new File(tempFolder.getRoot(), to).exists());
        
        mount.move(Path.parse(from), Path.parse(to));

        assertFalse(new File(tempFolder.getRoot(), from).exists());
        assertTrue(new File(tempFolder.getRoot(), to).exists());
    }

    @Test(expected = FileNotFoundException.class)
    public void testMoveMissing() throws IOException {
        mount.move(Path.parse("missing"), Path.parse("missing2"));
    }

    @Test(expected = IOException.class)
    public void testMoveToExisting() throws IOException {
        mount.move(Path.parse("accounts.lst"), Path.parse("receivable.txt"));
    }
    
    @Test
    public void testCopy() throws IOException {
        String from = "temp/temp2.txt";
        String to = "temp/temp2_copied.txt";

        assertTrue(new File(tempFolder.getRoot(), from).exists());
        assertFalse(new File(tempFolder.getRoot(), to).exists());
        
        mount.copy(Path.parse(from), Path.parse(to));

        assertTrue(new File(tempFolder.getRoot(), from).exists());
        assertTrue(new File(tempFolder.getRoot(), to).exists());
    }
    
    @Test
    public void testGetInputStream() throws IOException {
        InputStream in = mount.getInputStream(Path.parse("accounts.lst"));
        DataInputStream dataIn = new DataInputStream(in);
        assertEquals(dataIn.readInt(), 1234567890);
        assertEquals(dataIn.read(), -1);
        in.close();
    }

    @Test(expected = FileNotFoundException.class)
    public void testGetInputStreamMissing() throws IOException {
        mount.getInputStream(Path.parse("missing"));
    }

    @Test(expected = IOException.class)
    public void testGetInputStreamDirectory() throws IOException {
        mount.getInputStream(Path.parse("files"));
    }
    
    @Test
    public void testGetOutputStream() throws IOException {
        OutputStream out = mount.getOutputStream(Path.parse("temp/out.txt"), false);
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeInt(415167);
        out.close();
        
        InputStream in = new FileInputStream(new File(tempFolder.getRoot(), "temp/out.txt"));
        DataInputStream dataIn = new DataInputStream(in);
        assertEquals(dataIn.readInt(), 415167);
        assertEquals(dataIn.read(), -1);
        in.close();

        out = mount.getOutputStream(Path.parse("temp/out.txt"), true);
        dataOut = new DataOutputStream(out);
        dataOut.writeInt(65278);
        out.close();
        
        in = new FileInputStream(new File(tempFolder.getRoot(), "temp/out.txt"));
        dataIn = new DataInputStream(in);
        assertEquals(dataIn.readInt(), 415167);
        assertEquals(dataIn.readInt(), 65278);
        assertEquals(dataIn.read(), -1);
        in.close();
    }

}
