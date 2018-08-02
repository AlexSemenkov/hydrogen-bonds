package com.asemenkov.experiments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.particles.GmxAnglePredicate;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.tests.GmxAbstractTest;
import com.asemenkov.waterinargon.GmxResidueH2O;

/**
 * @author asemenkov
 * @since Jun 10, 2018
 */
@Test
public class GmxWaterDimerExperiment extends GmxAbstractTest {

    private static final Path WATER_DIMER = Paths.get("D:", "gromacs", "water-dimer");
    private static final String WATER_DIMER_GRO = "water-dimer.gro";

    @Test
    public void setupWaterDimer() {
        GmxAtom[] atoms = new GmxAtom[6];
        GmxResidue[] waters = new GmxResidue[2];

        GmxAtom[] atoms1 = residueAtomsFactory.get(GmxResidueH2O.class, new float[] { 0.f, 0.f, 0.f });
        GmxAtom[] atoms2 = residueAtomsFactory.get(GmxResidueH2O.class, new float[] { 0.f, 0.f, 0.f });

        waters[0] = residueFactory.get(GmxResidueH2O.class, 1, atoms1);
        waters[1] = residueFactory.get(GmxResidueH2O.class, 2, atoms2);

        System.arraycopy(atoms1, 0, atoms, 0, 3);
        System.arraycopy(atoms2, 0, atoms, 3, 3);
        IntStream.range(0, 6).forEach(i -> atoms[i].setAtomNo(i));

        waters[0].shift(3.f, 3.f, 3.f);
        waters[1].shift(3.3f, 3.f, 3.f);

        GmxAnglePredicate predicate1 = anglePredicateFactory.get(waters[0].getDonorAtoms()[0], waters[0].getAcceptorAtom(), waters[1].getAcceptorAtom(), -1.0, 0.01);
        boolean result1 = waters[0].rotateWhile(predicate1);
        Assert.assertTrue(result1, "Rotation of the 1st molecule H2O failed.");

        GmxAnglePredicate predicate2 = anglePredicateFactory.get(waters[1].getAcceptorAtom(), waters[1].getDonorAtoms()[0], waters[0].getDonorAtoms()[0], -0.616, 0.1);
        GmxAnglePredicate predicate3 = anglePredicateFactory.get(waters[1].getAcceptorAtom(), waters[1].getDonorAtoms()[1], waters[0].getDonorAtoms()[0], -0.616, 0.1);
        boolean result2 = waters[1].rotateWhile(predicate2, predicate3);
        Assert.assertTrue(result2, "Rotation of the 2nd molecule H2O failed.");

        GmxFrameStructure frameStructure = frameStructureFromArraysBuilderSupplier.get() //
                .withDescription("Water Dimer") //
                .withBox(new float[] { 7.f, 7.f, 7.f }) //
                .withResiduesArray(waters) //
                .withAtomsArray(atoms) //
                .build();

        GmxFrameCoordinates frameCoordinates = frameCoordinatesBuilderSupplier.get() //
                .withFrameNo(1) //
                .withAtomsArray(atoms) //
                .buildFromArray();

        GmxFrame frame = frameFactory.get(frameStructure, frameCoordinates);
        groFileReaderAndWriter.writeGroFile(frame, WATER_DIMER, WATER_DIMER_GRO);
    }
}
