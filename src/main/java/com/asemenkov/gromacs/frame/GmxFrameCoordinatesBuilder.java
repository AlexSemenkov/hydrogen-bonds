package com.asemenkov.gromacs.frame;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;

import com.asemenkov.gromacs.exceptions.GmxFrameException;
import com.asemenkov.gromacs.exceptions.GmxIoException;
import com.asemenkov.gromacs.io.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.utils.Logger;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public class GmxFrameCoordinatesBuilder {

    private @Autowired Map<Class<? extends GmxResidue>, float[][]> residuePivotDeltasMap;

    private int frameNo;
    private List<GmxGroFileAtomLine> groFileAtomLines;
    private GmxFrameStructure frameStructure;
    private GmxAtom[] atoms;

    // ======== INTERFACE ========

    public GmxFrameCoordinatesBuilder withFrameNo(int frameNo) {
        this.frameNo = frameNo;
        return this;
    }

    public GmxFrameCoordinatesBuilder withGroFileAtomLines(List<GmxGroFileAtomLine> groFileAtomLines) {
        this.groFileAtomLines = groFileAtomLines;
        return this;
    }

    public GmxFrameCoordinatesBuilder withFrameStructure(GmxFrameStructure frameStructure) {
        this.frameStructure = frameStructure;
        return this;
    }

    public GmxFrameCoordinatesBuilder withAtomsArray(GmxAtom[] atoms) {
        this.atoms = new GmxAtom[atoms.length];
        System.arraycopy(atoms, 0, this.atoms, 0, atoms.length);
        return this;
    }

    // ======== BUILD METHODS ========

    public GmxFrameCoordinates buildFromGroFile() {
        validateGroFileAtomLines();
        float[][] coordinates = groFileAtomLines.parallelStream() //
                .map(GmxGroFileAtomLine::getCoordinates) //
                .toArray(float[][]::new);

        GmxFrameCoordinates toReturn = new GmxFrameCoordinates(coordinates, frameNo);
        Logger.log("Frame coordinates successfully created from .gro file");
        resetBuilder();
        return toReturn;
    }

    public GmxFrameCoordinates buildFromScratch() {
        validateFrameStructure();
        float[] box = frameStructure.getBox();
        int atomsNum = frameStructure.getAtomsSequence().length;
        int residuesNum = frameStructure.getResidueIndexesMap().values().stream().mapToInt(a -> a.length).sum();
        int residueAtomsNum = frameStructure.getResidueAtomsMap().values().stream().mapToInt(a -> a.length).sum();
        int freeAtomsNum = atomsNum - residueAtomsNum;
        int coordsNum = freeAtomsNum + residuesNum;

        float[][] rawCoords = getRawCoords(box, coordsNum);
        float[][] fullCoords = getFullCoords(rawCoords, atomsNum);

        GmxFrameCoordinates toReturn = new GmxFrameCoordinates(fullCoords, frameNo);
        Logger.log("Frame coordinates successfully created from scratch");
        resetBuilder();
        return toReturn;
    }

    public GmxFrameCoordinates buildFromArray() {
        validateAtomsArray();
        float[][] coords = Arrays.stream(atoms) //
                .map(GmxAtom::getCoordinates) //
                .toArray(float[][]::new);

        GmxFrameCoordinates toReturn = new GmxFrameCoordinates(coords, frameNo);
        Logger.log("Frame coordinates successfully created from array of atoms");
        resetBuilder();
        return toReturn;
    }

    // ======== SUPPORT METHODS ========

    private void resetBuilder() {
        Logger.log("Resetting frame coordinates builder...");
        frameNo = 0;
        groFileAtomLines = null;
        frameStructure = null;
        atoms = null;
        System.gc();
    }

    private float[][] getRawCoords(float[] box, int coordsNum) {
        double volume = box[0] * box[1] * box[2];
        double interval = Math.cbrt(coordsNum / volume);

        int xNum = (int) (box[0] * interval);
        if (xNum == 0) xNum = 1;
        int yNum = (int) (box[1] * interval);
        if (yNum == 0) yNum = 1;
        int zNum = coordsNum / (xNum * yNum) + 1;

        double xInterval = box[0] / xNum;
        double yInterval = box[1] / yNum;
        double zInterval = box[2] / zNum;

        double[] xStream, yStream, zStream;
        xStream = DoubleStream.iterate(xInterval / 2, x -> x + xInterval).limit(xNum).toArray();
        yStream = DoubleStream.iterate(yInterval / 2, y -> y + yInterval).limit(yNum).toArray();
        zStream = DoubleStream.iterate(zInterval / 2, z -> z + zInterval).limit(zNum).toArray();

        AtomicInteger index = new AtomicInteger(0);
        float[][] rawCoords = new float[xNum * yNum * zNum][3];
        DoubleStream.of(xStream).parallel().forEach(x -> //
                DoubleStream.of(yStream).parallel().forEach(y -> //
                        DoubleStream.of(zStream).parallel().forEach(z -> {
                            int j = index.getAndIncrement();
                            rawCoords[j][0] = (float) x;
                            rawCoords[j][1] = (float) y;
                            rawCoords[j][2] = (float) z;
                        })));

        return rawCoords;
    }

    private float[][] getFullCoords(float[][] rawCoords, int atomsNum) {
        float[][] fullCoords = new float[atomsNum][];
        AtomicInteger index = new AtomicInteger(0);

        frameStructure.getResidueIndexesMap().forEach((key, value) -> {
            float[][] deltas = residuePivotDeltasMap.get(key);

            IntStream.of(value).parallel().forEach(i -> {
                float[] pivotCoord = rawCoords[index.getAndIncrement()];
                int[] atomIndexes = frameStructure.getResidueAtomsMap().get(i);

                IntStream.range(0, atomIndexes.length).forEach(j ->  //
                        fullCoords[atomIndexes[j]] = new float[] { //
                                pivotCoord[0] + deltas[j][0], //
                                pivotCoord[1] + deltas[j][1], //
                                pivotCoord[2] + deltas[j][2] });
            });
        });

        IntStream.range(0, fullCoords.length).parallel() //
                .filter(i -> fullCoords[i] == null) //
                .forEach(i -> fullCoords[i] = rawCoords[index.getAndIncrement()]);

        return fullCoords;
    }

    // ======== VALIDATION METHODS ========

    private void validateGroFileAtomLines() {
        if (groFileAtomLines == null || groFileAtomLines.size() == 0) //
            throw new GmxIoException("Invalid atom lines from .gro file: " + groFileAtomLines);
    }

    private void validateFrameStructure() {
        if (frameStructure == null) throw new GmxFrameException("Frame structure is missing.");
    }

    private void validateAtomsArray() {
        if (atoms == null || atoms.length == 0) throw new GmxFrameException("Atoms array is missing.");
    }
}
