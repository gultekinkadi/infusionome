package com.hk.dropapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.hk.dropapp.Public.Calculate;
import com.hk.dropapp.Public.Calculate.AmountUnit;
import com.hk.dropapp.Public.Calculate.DoseUnit;
import com.hk.dropapp.Public.Calculate.Result;

import org.junit.Test;

public class CalculateTest {

    private static final double EPS = 1e-6;

    /** v1.0 formülü: kilo × doz × (sıvı + ilaç hacmi) × damla faktörü ÷ ilaç miktarı (mcg). */
    private static double legacy(double sivi, double ilacHacmi, double ilacMcg, double kilo, double doz, double drop) {
        return kilo * doz * (sivi + ilacHacmi) * drop / ilacMcg;
    }

    @Test
    public void matchesOriginalFormula() {
        Result r = Calculate.calculate(250, 10, 400000, AmountUnit.MCG, 70, 5, DoseUnit.MCG_KG_MIN, 60);
        assertTrue(r.isOk());
        assertEquals(legacy(250, 10, 400000, 70, 5, 60), r.dropsPerMin, EPS);
    }

    @Test
    public void mgEqualsThousandMcg() {
        Result mcg = Calculate.calculate(250, 10, 400000, AmountUnit.MCG, 70, 5, DoseUnit.MCG_KG_MIN, 20);
        Result mg = Calculate.calculate(250, 10, 400, AmountUnit.MG, 70, 5, DoseUnit.MCG_KG_MIN, 20);
        assertEquals(mcg.dropsPerMin, mg.dropsPerMin, EPS);
    }

    @Test
    public void dopamineExample() {
        // 400 mg / 250 ml (1600 mcg/ml), 70 kg, 5 mcg/kg/dk → 350 mcg/dk → 0.21875 ml/dk → 13.125 ml/saat
        Result r = Calculate.calculate(250, 0, 400, AmountUnit.MG, 70, 5, DoseUnit.MCG_KG_MIN, 60);
        assertEquals(13.125, r.mlPerHour, EPS);
        assertEquals(13.125, r.dropsPerMin, EPS); // mikroset: damla/dk = ml/saat
        assertEquals(1600, r.concentrationPerMl, EPS);
        assertEquals(60 / 13.125, r.secondsPerDrop, EPS);
    }

    @Test
    public void heparinUnitsPerHour() {
        // 25000 Ü / 500 ml (50 Ü/ml), 1000 Ü/saat → 20 ml/saat; 20 damla/ml set → 6.667 damla/dk
        Result r = Calculate.calculate(500, 0, 25000, AmountUnit.UNIT, 0, 1000, DoseUnit.UNIT_HOUR, 20);
        assertTrue(r.isOk());
        assertEquals(20, r.mlPerHour, EPS);
        assertEquals(20.0 / 3.0, r.dropsPerMin, EPS);
        assertTrue(r.concentrationIsUnits);
    }

    @Test
    public void midazolamMgPerHourNoWeightNeeded() {
        // 50 mg / 50 ml, 5 mg/saat → 5 ml/saat
        Result r = Calculate.calculate(40, 10, 50, AmountUnit.MG, 0, 5, DoseUnit.MG_HOUR, 60);
        assertTrue(r.isOk());
        assertEquals(5, r.mlPerHour, EPS);
    }

    @Test
    public void unitMismatchRejected() {
        Result r = Calculate.calculate(250, 0, 400, AmountUnit.MG, 70, 5, DoseUnit.UNIT_HOUR, 20);
        assertEquals(Calculate.Error.UNIT_MISMATCH, r.error);
    }

    @Test
    public void zeroAndMissingWeightRejected() {
        assertEquals(Calculate.Error.NON_POSITIVE_VALUE,
                Calculate.calculate(250, 0, 0, AmountUnit.MG, 70, 5, DoseUnit.MCG_KG_MIN, 20).error);
        assertEquals(Calculate.Error.NON_POSITIVE_VALUE,
                Calculate.calculate(250, 0, 400, AmountUnit.MG, 0, 5, DoseUnit.MCG_KG_MIN, 20).error);
        assertEquals(Calculate.Error.NON_POSITIVE_VALUE,
                Calculate.calculate(0, 0, 400, AmountUnit.MG, 70, 5, DoseUnit.MCG_KG_MIN, 20).error);
    }

    @Test
    public void parseAcceptsComma() {
        assertEquals(2.5, Calculate.parse("2,5"), EPS);
        assertEquals(2.5, Calculate.parse(" 2.5 "), EPS);
        assertTrue(Double.isNaN(Calculate.parse("")));
        assertTrue(Double.isNaN(Calculate.parse("abc")));
    }
}
