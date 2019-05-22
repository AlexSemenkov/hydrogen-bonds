package com.asemenkov.procedure.simulation;

import com.asemenkov.utils.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * @author asemenkov
 * @since May 16, 2019
 */
public class AbstractProcedure extends InjectionsHolder {

    @PostConstruct
    public void createSimulationPath(){
        FileUtils.createDirectoryIfNotExists(simulationPath);
    }

    // ======== UTILITY METHODS ========

    protected Path copyFileToInputDir(@NotNull Path pathToFile) {
        Path destPath = Paths.get(simulationPath.toString(), pathToFile.getFileName().toString());
        return FileUtils.copyFile(pathToFile, destPath);
    }

    protected Path getPathForOutputFile(@NotNull String fileName) {
        if (fileName.contains(File.separator) || fileName.split("\\.").length != 2) //
            throw new FileUtils.FileUtilsException("Invalid name for output file: " + fileName);
        return Paths.get(simulationPath.toString(), fileName);
    }

    protected Path getLastModifiedFileWithExtension(@NotNull String extension) {
        String ext = extension.replaceFirst("\\.", "").toLowerCase();
        return FileUtils.getFilesInDirectory(simulationPath).stream() //
                .filter(p -> p.toFile().isFile()) //
                .filter(p -> FilenameUtils.getExtension(p.getFileName().toString()).equals(ext)) //
                .max(Comparator.comparing(FileUtils::getLastModifiedAttr)).orElse(null);
    }

}
