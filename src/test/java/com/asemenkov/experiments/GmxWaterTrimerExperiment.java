package com.asemenkov.experiments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.GmxFrameStructure;
import com.asemenkov.gromacs.particles.GmxAnglePredicate;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.tests.GmxAbstractTest;
import com.asemenkov.waterinargon.GmxResidueH2O;

/**
 * @author asemenkov
 * @since Jun 9, 2018
 */
@Test
public class GmxWaterTrimerExperiment extends GmxAbstractTest {

    private static final Path GRO_WATER_TRIMER = Paths.get("D:", "gromacs", "water-trimer");
    private static final String WATER_CYCLIC_TRIMER = "water-cyclic-trimer.gro";
    private static final String WATER_CHAIN_TRIMER_D_AD_A = "water-chain-trimer-d-ad-a.gro";
    private static final String WATER_CHAIN_TRIMER_D_AA_D = "water-chain-trimer-d-aa-d.gro";
    private static final String WATER_CHAIN_TRIMER_A_DD_A = "water-chain-trimer-a-dd-a.gro";
    private static final float[] WATER_TRIMER_BOX = new float[] { 7.f, 7.f, 7.f };

    private GmxAnglePredicate h1Predicate, h2Predicate;
    private boolean result;

    private GmxResidueH2O[] waters = new GmxResidueH2O[3];
    private GmxAtom[] atoms = new GmxAtom[9];
    private GmxAtom o1, o2, o3, hDonor1, hDonor2, hDonor3, hFree1, hFree2, hFree3;

    @BeforeMethod
    public void setupResidues() {
        GmxAtom[] atoms1 = residueAtomsFactory.get(GmxResidueH2O.class, new float[] { 3.f, 3.f, 3.f });
        GmxAtom[] atoms2 = residueAtomsFactory.get(GmxResidueH2O.class, new float[] { 3.f, 3.f, 3.f });
        GmxAtom[] atoms3 = residueAtomsFactory.get(GmxResidueH2O.class, new float[] { 3.f, 3.f, 3.f });

        waters[0] = (GmxResidueH2O) residueFactory.get(GmxResidueH2O.class, 1, atoms1);
        waters[1] = (GmxResidueH2O) residueFactory.get(GmxResidueH2O.class, 2, atoms2);
        waters[2] = (GmxResidueH2O) residueFactory.get(GmxResidueH2O.class, 3, atoms3);

        o1 = atoms[0] = waters[0].getAcceptorAtom();
        hDonor1 = atoms[1] = waters[0].getDonorAtoms()[0];
        hFree1 = atoms[2] = waters[0].getDonorAtoms()[1];

        o2 = atoms[3] = waters[1].getAcceptorAtom();
        hDonor2 = atoms[4] = waters[1].getDonorAtoms()[0];
        hFree2 = atoms[5] = waters[1].getDonorAtoms()[1];

        o3 = atoms[6] = waters[2].getAcceptorAtom();
        hDonor3 = atoms[7] = waters[2].getDonorAtoms()[0];
        hFree3 = atoms[8] = waters[2].getDonorAtoms()[1];

        IntStream.range(0, 9).forEach(i -> atoms[i].setAtomNo(i));
    }

    @Test
    public void setupCyclicTrimer() {
        waters[1].shiftX(0.3f);
        waters[2].shiftX(0.15f);
        waters[2].shiftY((float) Math.sqrt(0.3 * 0.3 + 0.15 * 0.15));

        h1Predicate = anglePredicateFactory.get(hDonor1, o1, o2, -1.0, 0.01);
        h2Predicate = anglePredicateFactory.get(hFree1, o1, o3, 0.966, 0.01);
        result = waters[0].rotateWhile(h1Predicate, h2Predicate);
        Assert.assertTrue(result, "Rotation of the 1st molecule H2O failed.");

        h1Predicate = anglePredicateFactory.get(hDonor2, o2, o3, -1.0, 0.01);
        h2Predicate = anglePredicateFactory.get(hFree2, o2, o1, 0.966, 0.01);
        result = waters[1].rotateWhile(h1Predicate, h2Predicate);
        Assert.assertTrue(result, "Rotation of the 2nd molecule H2O failed.");

        h1Predicate = anglePredicateFactory.get(hDonor3, o3, o1, -1.0, 0.01);
        h2Predicate = anglePredicateFactory.get(hFree3, o3, o2, 0.966, 0.01);
        result = waters[2].rotateWhile(h1Predicate, h2Predicate);
        Assert.assertTrue(result, "Rotation of the 3rd molecule H2O failed.");

        writeTrimerIntoGroFile("Cyclic Trimer", WATER_CYCLIC_TRIMER);
    }

    @Test
    public void setupChainTrimerDonorAcceptorDonorAcceptor() {
        waters[1].shiftX(0.3f);
        waters[2].shiftX(0.6f);

        h1Predicate = anglePredicateFactory.get(hDonor1, o1, o2, -1.0, 0.01);
        result = waters[0].rotateWhile(h1Predicate);
        Assert.assertTrue(result, "Rotation of the 1st molecule H2O failed.");

        h1Predicate = anglePredicateFactory.get(hDonor2, o2, o3, -1.0, 0.01);
        result = waters[1].rotateWhile(h1Predicate);
        Assert.assertTrue(result, "Rotation of the 2nd molecule H2O failed.");

        h1Predicate = anglePredicateFactory.get(o3, hDonor3, o2, -0.5, 0.01);
        h2Predicate = anglePredicateFactory.get(o3, hFree3, o2, -0.5, 0.01);
        result = waters[2].rotateWhile(h1Predicate, h2Predicate);
        Assert.assertTrue(result, "Rotation of the 3rd molecule H2O failed.");

        writeTrimerIntoGroFile("Chain Trimer Donor::Acceptor-Donor::Acceptor", WATER_CHAIN_TRIMER_D_AD_A);
    }

    @Test
    public void setupChainTrimerDonorAcceptorAcceptorDonor() {
        waters[1].shiftX(0.237f);
        waters[1].shiftY(0.184f);
        waters[2].shiftX(0.473f);

        h1Predicate = anglePredicateFactory.get(hDonor1, o1, o2, -1.0, 0.01);
        h2Predicate = anglePredicateFactory.get(o1, hFree1, o3, -0.766, 0.01);
        result = waters[0].rotateWhile(h1Predicate, h2Predicate);
        Assert.assertTrue(result, "Rotation of the 1st molecule H2O failed.");

        h1Predicate = anglePredicateFactory.get(o2, hDonor2, o1, -1.0, 0.01);
        h2Predicate = anglePredicateFactory.get(o2, hFree2, o3, -1.0, 0.01);
        result = waters[1].rotateWhile(h1Predicate, h2Predicate);
        Assert.assertTrue(result, "Rotation of the 2nd molecule H2O failed.");

        h1Predicate = anglePredicateFactory.get(hDonor3, o3, o2, -1.0, 0.01);
        h2Predicate = anglePredicateFactory.get(o3, hFree3, o1, -0.766, 0.01);
        result = waters[2].rotateWhile(h1Predicate, h2Predicate);
        Assert.assertTrue(result, "Rotation of the 3rd molecule H2O failed.");

        writeTrimerIntoGroFile("Chain Trimer Donor::Acceptor-Acceptor::Donor", WATER_CHAIN_TRIMER_D_AA_D);
    }

    @Test
    public void setupChainTrimerAcceptorDonorDonorAcceptor() {
        waters[1].shiftX(0.237f);
        waters[1].shiftY(0.184f);
        waters[2].shiftX(0.473f);

        h1Predicate = anglePredicateFactory.get(o1, hDonor1, o2, -0.616, 0.1);
        h2Predicate = anglePredicateFactory.get(o1, hFree1, o2, -0.616, 0.1);
        result = waters[0].rotateWhile(h1Predicate, h2Predicate);
        Assert.assertTrue(result, "Rotation of the 1st molecule H2O failed.");

        h1Predicate = anglePredicateFactory.get(hDonor2, o2, o1, -1.0, 0.01);
        h2Predicate = anglePredicateFactory.get(hFree2, o2, o3, -1.0, 0.01);
        result = waters[1].rotateWhile(h1Predicate, h2Predicate);
        Assert.assertTrue(result, "Rotation of the 2nd molecule H2O failed.");

        h1Predicate = anglePredicateFactory.get(o3, hDonor3, o2, -0.616, 0.1);
        h2Predicate = anglePredicateFactory.get(o3, hFree3, o2, -0.616, 0.1);
        result = waters[2].rotateWhile(h1Predicate, h2Predicate);
        Assert.assertTrue(result, "Rotation of the 3rd molecule H2O failed.");

        writeTrimerIntoGroFile("Chain Trimer Acceptor::Donor-Donor::Acceptor", WATER_CHAIN_TRIMER_A_DD_A);
    }

    private void writeTrimerIntoGroFile(String description, String fileNAme) {
        GmxFrameStructure frameStructure = frameStructureBuilderSupplier.get() //
                .withDescription(description) //
                .withBox(WATER_TRIMER_BOX) //
                .withResiduesArray(waters) //
                .withAtomsArray(atoms) //
                .buildFromArrays();

        GmxFrameCoordinates frameCoordinates = frameCoordinatesBuilderSupplier.get() //
                .withFrameNo(1) //
                .withAtomsArray(atoms) //
                .buildFromArray();

        GmxFrame frame = frameFactory.get(frameStructure, frameCoordinates);
        groFileReaderAndWriter.writeGroFile(frame, GRO_WATER_TRIMER, fileNAme);
    }

}
