package com.asemenkov.gromacs.frame.coordinates;

import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.particles.utils.GmxResidueReflectionData;
import com.asemenkov.utils.io.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * @author asemenkov
 * @since Aug 12, 2018
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GmxFrameCoordinatesFromScratchBuilder
        extends GmxAbstractFrameCoordinatesBuilder<GmxFrameCoordinatesFromScratchBuilder> {

    private @Autowired GmxResidueReflectionData residueReflectionData;
    private GmxFrameStructure frameStructure;

    // ======== INTERFACE ========

    public GmxFrameCoordinatesFromScratchBuilder withFrameStructure(GmxFrameStructure frameStructure) {
        this.frameStructure = frameStructure;
        return this;
    }

    @Override
    public GmxFrameCoordinates build() {
        validateFrameStructure();
        float[] box = frameStructure.getBox();
        int atomsNum = frameStructure.getAtomsSequence().length;
        int residuesNum = frameStructure.getResidueIndexesMap().values().stream().mapToInt(a -> a.length)
                .sum();
        int residueAtomsNum = frameStructure.getResidueAtomsMap().values().stream().mapToInt(a -> a.length)
                .sum();
        int freeAtomsNum = atomsNum - residueAtomsNum;
        int coordinatesNum = freeAtomsNum + residuesNum;

        float[][] rawCoordinates = getRawCoordinates(box, coordinatesNum);
        float[][] fullCoordinates = getFullCoordinates(rawCoordinates, atomsNum);

        GmxFrameCoordinates toReturn = new GmxFrameCoordinates(fullCoordinates, frameNo);
        Logger.log("Frame coordinates successfully created from scratch");
        return toReturn;
    }

    // ======== VALIDATION METHODS ========

    private void validateFrameStructure() {
        if (frameStructure == null) throw new GmxFrameException("Frame structure is missing.");
    }

    // ======== SUPPORT METHODS ========

    private float[][] getRawCoordinates(float[] box, int coordinatesNum) {
        double volume = box[0] * box[1] * box[2];
        double interval = Math.cbrt(coordinatesNum / volume);

        int xNum = (int) (box[0] * interval);
        if (xNum == 0) xNum = 1;
        int yNum = (int) (box[1] * interval);
        if (yNum == 0) yNum = 1;
        int zNum = coordinatesNum / (xNum * yNum) + 1;

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

    private float[][] getFullCoordinates(float[][] rawCoordinates, int atomsNum) {
        float[][] fullCoordinates = new float[atomsNum][];
        AtomicInteger index = new AtomicInteger(0);

        frameStructure.getResidueIndexesMap().forEach((key, value) -> {
            float[][] deltas = residueReflectionData.getResiduePivotDeltas(key);

            IntStream.of(value).parallel().forEach(i -> {
                float[] pivotCoordinate = rawCoordinates[index.getAndIncrement()];
                int[] atomIndexes = frameStructure.getResidueAtomsMap().get(i);

                IntStream.range(0, atomIndexes.length).forEach(j ->  //
                        fullCoordinates[atomIndexes[j]] = new float[] { //
                                pivotCoordinate[0] + deltas[j][0], //
                                pivotCoordinate[1] + deltas[j][1], //
                                pivotCoordinate[2] + deltas[j][2] });
            });
        });

        IntStream.range(0, fullCoordinates.length).parallel() //
                .filter(i -> fullCoordinates[i] == null) //
                .forEach(i -> fullCoordinates[i] = rawCoordinates[index.getAndIncrement()]);

        return fullCoordinates;
    }
}
