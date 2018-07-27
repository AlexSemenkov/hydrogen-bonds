package com.asemenkov.utils;

import com.asemenkov.gromacs.exceptions.GmxIoException;

import static java.nio.file.StandardOpenOption.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author asemenkov
 * @since Apr 13, 2018
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
public class FileUtils {

    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private final static String USERNAME = System.getProperty("user.name");

    private FileUtils() {
    }

    public static Charset getEncoding() {
        return ENCODING;
    }

    public static String getUsername() {
        return USERNAME;
    }

    // ======== DIRECTORY ACTIONS ========

    public static void createDirectoryIfNotExist(Path path) {
        if (fileExists(path)) return;

        try {
            Files.createDirectories(path);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static List<Path> getFoldersInCurrentDirectory(Path path) {
        checkDir(path);
        File[] files = path.toFile().listFiles();
        if (files == null) return new ArrayList<>();
        return Arrays.stream(files) //
                .filter(File::isDirectory) //
                .map(File::toPath) //
                .collect(Collectors.toList());
    }

    public static List<Path> getFilesInCurrentDirectory(Path path) {
        checkDir(path);
        File[] files = path.toFile().listFiles();
        if (files == null) return new ArrayList<>();
        return Arrays.stream(files) //
                .filter(File::isFile) //
                .map(File::toPath) //
                .collect(Collectors.toList());
    }

    public static List<Path> getNestedFilesInCurrentDirectory(Path path) {
        checkDir(path);
        List<Path> toReturn = new ArrayList<>(getFilesInCurrentDirectory(path));

        List<Path> folders = getFoldersInCurrentDirectory(path);
        if (folders.isEmpty()) folders.stream().map(FileUtils::getNestedFilesInCurrentDirectory) //
                .forEach(toReturn::addAll);

        return toReturn;
    }

    // ======== FILE ACTIONS ========

    public static boolean fileExists(Path path) {
        File file = path.toFile();
        return file.exists();
    }

    public static void verifyExtension(Path path, String extension) {
        checkFile(path);

        String lower = extension.toLowerCase();
        if (!lower.startsWith(".")) lower = "." + lower;

        if (!path.toString().toLowerCase().endsWith(lower)) //
            throw new GmxIoException("Extension of " + path + " is not as expected - " + extension);
    }

    public static void copyFile(Path from, Path to) {
        checkFile(from);
        checkFile(to);

        try {
            Files.copy(from, to);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static void moveFile(Path from, Path to) {
        checkFile(from);
        checkFile(to);

        try {
            Files.move(from, to);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static void renameFile(Path path, String newName) {
        checkFile(path);

        try {
            Files.move(path, path.resolveSibling(newName));
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static void deleteFileIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    // ======== FILE READING ========

    public static List<String> readWholeFile(Path path) {
        checkFile(path);

        if (!path.toFile().canRead()) throw new FileUtilsException("File cannot be read: " + path);

        if (path.toFile().length() > 512 * 1024 * 1024) throw new FileUtilsException("File is to large to be read at a time: " + path);

        try (BufferedReader reader = Files.newBufferedReader(path, ENCODING)) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static BufferedReader getBufferedReader(Path path) {
        checkFile(path);

        if (!path.toFile().canRead()) throw new FileUtilsException("File cannot be read: " + path);

        try {
            return Files.newBufferedReader(path, ENCODING);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    // ======== FILE WRITING ========

    public static void writeWholeFile(Path path, Collection<String> lines, //
            boolean createFileIfItDoesntExist, boolean eraseDataIfFileExists) {

        if (path.toFile().exists()) {
            if (!path.toFile().canWrite()) throw new FileUtilsException("File cannot be written: " + path);

            if (eraseDataIfFileExists) writeData(path, lines, TRUNCATE_EXISTING);
            else writeData(path, lines, APPEND);

        } else {
            if (createFileIfItDoesntExist) writeData(path, lines, CREATE_NEW);
        }
    }

    public static BufferedWriter getBufferedWriter(Path path, boolean createFileIfItDoesntExist, boolean eraseDataIfFileExists) {

        if (path.toFile().exists()) {
            if (!path.toFile().canWrite()) throw new FileUtilsException("File cannot be written: " + path);

            if (eraseDataIfFileExists) return getWriter(path, TRUNCATE_EXISTING);
            else return getWriter(path, APPEND);

        } else {
            if (createFileIfItDoesntExist) return getWriter(path, CREATE_NEW);
            else return null;
        }
    }

    public static void close(BufferedWriter bufferedWriter) {
        if (bufferedWriter != null) try {
            bufferedWriter.close();
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static void close(BufferedReader bufferedReader) {
        if (bufferedReader != null) try {
            bufferedReader.close();
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    // ======== SUPPORT METHODS ========

    private static void checkFile(Path path) {
        if (!fileExists(path)) throw new FileUtilsException("File not found: " + path);

        File file = path.toFile();

        if (!file.isFile()) throw new FileUtilsException("It's not a file: " + path);
    }

    private static void checkDir(Path path) {
        if (!fileExists(path)) throw new FileUtilsException("Directory not found: %s" + path);

        File file = path.toFile();
        if (!file.isDirectory()) throw new FileUtilsException("It's not a directory: " + path);
    }

    private static void writeData(Path path, Collection<String> lines, OpenOption option) {
        try {
            Files.write(path, lines, ENCODING, WRITE, option);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    private static BufferedWriter getWriter(Path path, OpenOption option) {
        try {
            return Files.newBufferedWriter(path, ENCODING, WRITE, option);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    // ======== EXCEPTION CLASS ========

    public static class FileUtilsException extends RuntimeException {

        private static final long serialVersionUID = -7280624581521195779L;

        public FileUtilsException(String message) {
            super(message);
        }

        public FileUtilsException(Throwable throwable) {
            super(throwable);
        }

        public FileUtilsException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}
