package com.asemenkov.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.waterinargon.GmxAtomAr;
import com.asemenkov.waterinargon.GmxAtomH;
import com.asemenkov.waterinargon.GmxAtomO;

/**
 * @author asemenkov
 * @since May 8, 2018
 */
public class GmxAtomTest extends GmxAbstractTest {

    @Test
    public void testAtomCreation() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 2.2f, 3.3f });
        Assert.assertEquals(argon.getAbbreviation(), "Ar", "Wrong atom abbreviation.");
        Assert.assertEquals(argon.getFullName(), "Argon", "Wrong atom full name.");
        verifyAtomCoordinates(argon, 1.1f, 2.2f, 3.3f);
    }

    @Test
    public void testShiftX() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 2.2f, 3.3f });
        argon.shiftX(0.123f);
        verifyAtomCoordinates(argon, 1.223f, 2.2f, 3.3f);
        argon.shiftX(-0.6f);
        verifyAtomCoordinates(argon, 0.623f, 2.2f, 3.3f);
    }

    @Test
    public void testShiftY() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 2.2f, 3.3f });
        argon.shiftY(0.123f);
        verifyAtomCoordinates(argon, 1.1f, 2.323f, 3.3f);
        argon.shiftY(-0.8f);
        verifyAtomCoordinates(argon, 1.1f, 1.523f, 3.3f);
    }

    @Test
    public void testShiftZ() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 2.2f, 3.3f });
        argon.shiftZ(0.0123f);
        verifyAtomCoordinates(argon, 1.1f, 2.2f, 3.3123f);
        argon.shiftZ(-0.8f);
        verifyAtomCoordinates(argon, 1.1f, 2.2f, 2.5123f);
    }

    @Test
    public void testShift() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 2.2f, 3.3f });
        argon.shift(-0.1f, 0.123f, 1.1f);
        verifyAtomCoordinates(argon, 1.0f, 2.323f, 4.4f);
    }

    @Test
    public void testEuclideanDistanceToAtom() {
        GmxAtom hydrogen = atomFactory.get(GmxAtomH.class, "H", 0, new float[] { 1.1f, 2.2f, 3.3f });
        GmxAtom oxygen = atomFactory.get(GmxAtomO.class, "O", 1, new float[] { 1.1f, 2.2f, 3.3f });
        Assert.assertNotNull(hydrogen);
        Assert.assertNotNull(oxygen);
        Assert.assertEquals(hydrogen.euclideanDistanceToAtom(oxygen), 0.0, "Wrong distance.");
        Assert.assertEquals(oxygen.euclideanDistanceToAtom(hydrogen), 0.0, "Wrong distance.");

        hydrogen.setCoordinates(new float[] { 4.1f, 5.123f, 10.0f });
        Assert.assertEquals(hydrogen.euclideanDistanceToAtom(oxygen), 7.901514135193463, "Wrong distance.");
        Assert.assertEquals(oxygen.euclideanDistanceToAtom(hydrogen), 7.901514135193463, "Wrong distance.");

        oxygen.setCoordinates(new float[] { -1.1f, -0.222f, -8.0f });
        Assert.assertEquals(hydrogen.euclideanDistanceToAtom(oxygen), 19.48355739563652, "Wrong distance.");
        Assert.assertEquals(oxygen.euclideanDistanceToAtom(hydrogen), 19.48355739563652, "Wrong distance.");
    }

    @Test
    public void testCosOfAngleBetweenAtoms() {
        float[][] h2oCoordinates = { //
                { 3.970f, 4.455f, 4.689f }, //
                { 4.005f, 4.486f, 4.778f }, //
                { 3.872f, 4.472f, 4.685f } };

        GmxAtom[] water = new GmxAtom[3];
        water[0] = atomFactory.get(GmxAtomO.class, "O", 0, h2oCoordinates[0]);
        water[1] = atomFactory.get(GmxAtomH.class, "H1", 1, h2oCoordinates[1]);
        water[2] = atomFactory.get(GmxAtomH.class, "H2", 2, h2oCoordinates[2]);

        double cosAlpha = water[0].cosOfAngleBetweenTwoAtoms(water[1], water[1]);
        Assert.assertEquals(Math.toDegrees(Math.acos(cosAlpha)), 0.0, "Wrong angle degree.");

        cosAlpha = water[0].cosOfAngleBetweenTwoAtoms(water[1], water[2]);
        Assert.assertEquals(Math.toDegrees(Math.acos(cosAlpha)), 109.0054037847547, "Wrong angle degree.");

        double disOH1 = water[0].euclideanDistanceToAtom(water[1]);
        double disOH2 = water[0].euclideanDistanceToAtom(water[2]);
        cosAlpha = water[0].cosOfAngleBetweenTwoAtoms(water[1], water[2], disOH1, disOH2);
        Assert.assertEquals(Math.toDegrees(Math.acos(cosAlpha)), 109.0054037847547, "Wrong angle degree.");
    }

    @Test
    public void testCosOfAngleToAtomWithZeroVertex() {
        GmxAtom o = atomFactory.get(GmxAtomO.class, "O", 0, new float[] { 3.970f, 4.455f, 4.689f });
        GmxAtom h = atomFactory.get(GmxAtomH.class, "H", 1, new float[] { 4.005f, 4.486f, 4.778f });

        double cosAlpha = o.cosOfAngleToAtomWithZeroVertex(o);
        Assert.assertEquals(Math.toDegrees(Math.acos(cosAlpha)), 0.0, "Wrong angle degree.");

        cosAlpha = o.cosOfAngleToAtomWithZeroVertex(h);
        Assert.assertEquals(Math.toDegrees(Math.acos(cosAlpha)), 0.3116288346870596, "Wrong angle degree.");

        cosAlpha = h.cosOfAngleToAtomWithZeroVertex(o);
        Assert.assertEquals(Math.toDegrees(Math.acos(cosAlpha)), 0.3116288346870596, "Wrong angle degree.");
    }

    @Test
    public void testRadiusVector() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 2.2f, 3.3f });
        Assert.assertNotNull(argon);
        Assert.assertEquals(argon.getRadiusVector(), 4.115822958620668, "Wrong radius-vector.");

        argon.shiftX(0.1f);
        Assert.assertEquals(argon.getRadiusVector(), 4.143669880226216, "Wrong radius-vector.");

        argon.shiftY(0.1f);
        Assert.assertEquals(argon.getRadiusVector(), 4.197618245134349, "Wrong radius-vector.");

        argon.shiftZ(0.1f);
        Assert.assertEquals(argon.getRadiusVector(), 4.276680840111723, "Wrong radius-vector.");

        argon.shift(0.1f, 0.1f, 0.1f);
        Assert.assertEquals(argon.getRadiusVector(), 4.438467860449390, "Wrong radius-vector.");

        argon.setCoordinates(new float[] { 1.1f, 2.2f, 3.3f });
        Assert.assertEquals(argon.getRadiusVector(), 4.115822958620668, "Wrong radius-vector.");
    }

}
