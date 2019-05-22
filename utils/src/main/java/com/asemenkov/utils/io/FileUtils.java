package com.asemenkov.utils.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author asemenkov
 * @since Apr 13, 2018
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
public class FileUtils {

    private FileUtils() {
    }

    // ======== DIRECTORY ACTIONS ========

    public static void createDirectoryIfNotExists(Path path) {
        if (pathExists(path)) return;

        try {
            Files.createDirectories(path);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static List<Path> getFoldersInDirectory(Path path) {
        if(!pathExists(path)) return Collections.emptyList();
        File[] files = path.toFile().listFiles();
        if (files == null) return new ArrayList<>();
        return Arrays.stream(files) //
                .filter(File::isDirectory) //
                .map(File::toPath) //
                .collect(Collectors.toList());
    }

    public static List<Path> getFilesInDirectory(Path path) {
        if(!pathExists(path)) return Collections.emptyList();
        File[] files = path.toFile().listFiles();
        if (files == null) return new ArrayList<>();
        return Arrays.stream(files) //
                .filter(File::isFile) //
                .map(File::toPath) //
                .collect(Collectors.toList());
    }

    public static List<Path> getNestedFilesInDirectory(Path path) {
        if(!pathExists(path)) return Collections.emptyList();
        List<Path> toReturn = new ArrayList<>(getFilesInDirectory(path));
        List<Path> folders = getFoldersInDirectory(path);
        if (folders.isEmpty()) folders.stream() //
                .map(FileUtils::getNestedFilesInDirectory) //
                .forEach(toReturn::addAll);
        return toReturn;
    }

    // ======== FILE ACTIONS ========

    public static boolean pathExists(Path path) {
        return path.toFile().exists();
    }

    public static void verifyFileExists(Path path) {
        if (!pathExists(path) || !path.toFile().isFile()) //
            throw new FileUtilsException("File not found: " + path);
    }

    public static void verifyDirExists(Path path) {
        if (!pathExists(path) || path.toFile().isFile()) //
            throw new FileUtilsException("Directory not found: " + path);
    }

    public static void verifyFileInDirExists(Path pathToFile, Path pathToDir) {
        verifyFileExists(pathToFile);
        verifyDirExists(pathToDir);
        if (!pathToFile.getParent().equals(pathToDir)) //
            throw new FileUtilsException("File [" + pathToFile + "] not found in dir: " + pathToDir);
    }

    public static void verifyExtension(Path path, String... extensions) {
        if (Arrays.stream(extensions).map(String::toLowerCase) //
                .map(extension -> extension.replaceFirst("^(?!\\.)", ".")) //
                .noneMatch(extension -> path.toString().toLowerCase().endsWith(extension)))
            throw new FileUtilsException("Extension of " + path + " not in " + Arrays.toString(extensions));
    }

    public static Path copyFile(Path from, Path to) {
        verifyFileExists(from);
        try {
            return Files.copy(from, to);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static Path moveFile(Path from, Path to) {
        verifyFileExists(from);
        try {
            return Files.move(from, to);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static Path renameFile(Path path, String newName) {
        verifyFileExists(path);
        try {
            return Files.move(path, path.resolveSibling(newName));
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static boolean deleteFileIfExists(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    // ======== ATTRIBUTES ========

    public static FileTime getLastModifiedAttr(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class).lastModifiedTime();
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    // ======== FILE READING ========

    public static List<String> readWholeFile(Path path) {
        verifyFileExists(path);

        if (!path.toFile().canRead()) throw new FileUtilsException("File cannot be read: " + path);

        if (path.toFile().length() > 512 * 1024 * 1024) throw new FileUtilsException(
                "File is to large to be read at a time: " + path);

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    public static BufferedReader getBufferedReader(Path path) {
        verifyFileExists(path);

        if (!path.toFile().canRead()) throw new FileUtilsException("File cannot be read: " + path);

        try {
            return Files.newBufferedReader(path, StandardCharsets.UTF_8);
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

    public static BufferedWriter getBufferedWriter(Path path, boolean createFileIfItDoesntExist,
            boolean eraseDataIfFileExists) {

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

    private static void writeData(Path path, Collection<String> lines, OpenOption option) {
        try {
            Files.write(path, lines, StandardCharsets.UTF_8, WRITE, option);
        } catch (IOException exception) {
            throw new FileUtilsException(exception);
        }
    }

    private static BufferedWriter getWriter(Path path, OpenOption option) {
        try {
            return Files.newBufferedWriter(path, StandardCharsets.UTF_8, WRITE, option);
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
