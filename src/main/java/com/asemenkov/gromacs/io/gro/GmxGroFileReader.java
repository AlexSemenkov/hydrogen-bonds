package com.asemenkov.gromacs.io.gro;

import com.asemenkov.utils.FileUtils;
import com.asemenkov.utils.Logger;
import com.asemenkov.utils.RegexPatterns;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author asemenkov
 * @since Apr 15, 2018
 */
public class GmxGroFileReader {

    private static final String EXTENSION = ".gro";
    private static final int DESCRIPTION_INDEX = 0;
    private static final int ATOMS_NO_INDEX = 1;
    private static final int ATOMS_START_INDEX = 2;

    public String readGroFileDescription(Path groFile) {
        FileUtils.verifyExtension(groFile, EXTENSION);
        return FileUtils.readWholeFile(groFile).get(DESCRIPTION_INDEX);
    }

    public Integer readGroFileAtomsNum(Path groFile) {
        FileUtils.verifyExtension(groFile, EXTENSION);
        List<String> lines = FileUtils.readWholeFile(groFile);
        return Integer.valueOf(lines.get(ATOMS_NO_INDEX).trim());
    }

    public float[] readGroFileBox(Path groFile) {
        FileUtils.verifyExtension(groFile, EXTENSION);
        List<String> lines = FileUtils.readWholeFile(groFile);
        Integer atomsNum = Integer.valueOf(lines.get(ATOMS_NO_INDEX).trim());
        return readBox(lines.get(ATOMS_START_INDEX + atomsNum));
    }

    public List<GmxGroFileAtomLine> readGroFileAtomLines(Path groFile) {
        FileUtils.verifyExtension(groFile, EXTENSION);
        List<String> lines = FileUtils.readWholeFile(groFile);
        Integer atomsNum = Integer.valueOf(lines.get(ATOMS_NO_INDEX).trim());

        Logger.log("Reading file: " + groFile);
        Logger.log("Number of atoms lines: " + atomsNum);

        return lines.subList(ATOMS_START_INDEX, ATOMS_START_INDEX + atomsNum).parallelStream() //
                .map(GmxGroFileAtomLine::fromStringLine) //
                .collect(Collectors.toList());
    }

    private float[] readBox(String line) {
        Matcher matcher = RegexPatterns.FLOAT_PATTERN.matcher(line);
        float[] toReturn = new float[3];
        if (matcher.find()) toReturn[0] = Float.valueOf(matcher.group(1));
        if (matcher.find()) toReturn[1] = Float.valueOf(matcher.group(1));
        if (matcher.find()) toReturn[2] = Float.valueOf(matcher.group(1));
        return toReturn;
    }

}
