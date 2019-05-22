package com.asemenkov.gromacs.io.gro;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.io.exceptions.GmxIoException;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.utils.io.FileUtils;
import com.asemenkov.utils.io.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * @author asemenkov
 * @since Apr 15, 2018
 */
public class GmxGroFileWriter {

    private static final String EXTENSION = ".gro";
    private static final String BOX_FORMAT = "\r\n%8.3f%8.3f%8.3f";
    private static final String ATOMS_NUM_FORMAT = "\r\n  %d";

    public Path writeGroFile(GmxFrame frame, Path directory, String fileName) {
        if (!fileName.endsWith(EXTENSION)) fileName += EXTENSION;

        FileUtils.createDirectoryIfNotExists(directory);
        Path path = Paths.get(directory.toString(), fileName);
        BufferedWriter writer = FileUtils.getBufferedWriter(path, true, true);
        Logger.log("Writing file: " + path);
        if (writer == null) throw new GmxIoException("Cannot initialize BufferedWriter.");

        try {
            writer.write(frame.getDescription());
            writer.write(String.format(ATOMS_NUM_FORMAT, frame.getAtomsNum()));

            GmxResidue[] residues = frame.getResidues();
            GmxAtom[] atoms = Arrays.copyOf(frame.getAtoms(), frame.getAtomsNum());

            Arrays.stream(residues).parallel() //
                    .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                    .forEach(atom -> atoms[atom.getAtomNo()] = null);

            Arrays.stream(atoms) //
                    .filter(Objects::nonNull) //
                    .map(GmxGroFileAtomLine::fromFreeAtom) //
                    .forEach(atomLine -> {
                        try {
                            writer.write(atomLine.toString());
                        } catch (IOException exception) {
                            FileUtils.close(writer);
                            throw new GmxIoException(exception);
                        }
                    });

            Arrays.stream(residues) //
                    .flatMap(residue -> Arrays.stream(GmxGroFileAtomLine.fromResidue(residue))) //
                    .forEach(atomLine -> {
                        try {
                            writer.write(atomLine.toString());
                        } catch (IOException exception) {
                            FileUtils.close(writer);
                            throw new GmxIoException(exception);
                        }
                    });

            float[] box = frame.getBox();
            writer.write(String.format(Locale.US, BOX_FORMAT, box[0], box[1], box[2]));
            writer.write("\n");

        } catch (IOException exception) {
            throw new GmxIoException(exception);
        } finally {
            FileUtils.close(writer);
        }

        return path;
    }

}
