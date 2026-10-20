package com.hk.dropapp.Public;

/**
 * Damla hızı hesaplama çekirdeği. Android'e bağımlı değildir; birim testleriyle doğrulanır.
 *
 * Formül:
 *   doz hızı (temel birim/dk) = doz × [kilo] × birim çarpanı
 *   ml/dk   = doz hızı × toplam hacim (ml) ÷ ilaç miktarı (temel birim)
 *   damla/dk = ml/dk × damla faktörü (damla/ml)
 *   ml/saat = ml/dk × 60
 *   aralık (sn) = 60 ÷ damla/dk
 *
 * Temel birim: kütle için mcg, ünite için Ü.
 */
public final class Calculate {

    private Calculate() {
    }

    /** İlaç miktarı birimleri. Değerler temel birime (mcg veya Ü) çevirme çarpanıdır. */
    public enum AmountUnit {
        MCG(1.0, false),
        MG(1000.0, false),
        G(1_000_000.0, false),
        UNIT(1.0, true);

        public final double toBase;
        public final boolean isUnits;

        AmountUnit(double toBase, boolean isUnits) {
            this.toBase = toBase;
            this.isUnits = isUnits;
        }
    }

    /** Doz birimleri. toBasePerMin: girilen değeri temel birim/dk'ya çeviren çarpan. */
    public enum DoseUnit {
        MCG_KG_MIN(1.0, true, false),
        MCG_MIN(1.0, false, false),
        MCG_KG_HOUR(1.0 / 60.0, true, false),
        MG_KG_HOUR(1000.0 / 60.0, true, false),
        MG_HOUR(1000.0 / 60.0, false, false),
        MG_MIN(1000.0, false, false),
        UNIT_KG_HOUR(1.0 / 60.0, true, true),
        UNIT_HOUR(1.0 / 60.0, false, true),
        UNIT_MIN(1.0, false, true);

        public final double toBasePerMin;
        public final boolean perKg;
        public final boolean isUnits;

        DoseUnit(double toBasePerMin, boolean perKg, boolean isUnits) {
            this.toBasePerMin = toBasePerMin;
            this.perKg = perKg;
            this.isUnits = isUnits;
        }
    }

    /** Hesaplama sırasında ortaya çıkabilecek hatalar. */
    public enum Error {
        NONE,
        NON_POSITIVE_VALUE,
        UNIT_MISMATCH
    }

    public static final class Result {
        public final Error error;
        public final double dropsPerMin;
        public final double secondsPerDrop;
        public final double mlPerHour;
        /** Temel birim / ml (mcg/ml veya Ü/ml). */
        public final double concentrationPerMl;
        public final boolean concentrationIsUnits;

        private Result(Error error, double dropsPerMin, double secondsPerDrop,
                       double mlPerHour, double concentrationPerMl, boolean concentrationIsUnits) {
            this.error = error;
            this.dropsPerMin = dropsPerMin;
            this.secondsPerDrop = secondsPerDrop;
            this.mlPerHour = mlPerHour;
            this.concentrationPerMl = concentrationPerMl;
            this.concentrationIsUnits = concentrationIsUnits;
        }

        static Result error(Error e) {
            return new Result(e, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false);
        }

        public boolean isOk() {
            return error == Error.NONE;
        }
    }

    /**
     * @param fluidVolumeMl  sıvı (taşıyıcı) hacmi, ml
     * @param drugVolumeMl   ilacın hacmi, ml (0 olabilir)
     * @param drugAmount     ilaç miktarı, amountUnit cinsinden
     * @param amountUnit     ilaç miktarı birimi
     * @param weightKg       hasta kilosu; doz /kg değilse yok sayılır
     * @param dose           istenen doz, doseUnit cinsinden
     * @param doseUnit       doz birimi
     * @param dropFactor     set damla faktörü (damla/ml)
     */
    public static Result calculate(double fluidVolumeMl, double drugVolumeMl,
                                   double drugAmount, AmountUnit amountUnit,
                                   double weightKg, double dose, DoseUnit doseUnit,
                                   double dropFactor) {
        if (amountUnit.isUnits != doseUnit.isUnits) {
            return Result.error(Error.UNIT_MISMATCH);
        }
        double totalVolume = fluidVolumeMl + drugVolumeMl;
        if (!(fluidVolumeMl >= 0) || !(drugVolumeMl >= 0) || !(totalVolume > 0)
                || !(drugAmount > 0) || !(dose > 0) || !(dropFactor > 0)
                || (doseUnit.perKg && !(weightKg > 0))) {
            return Result.error(Error.NON_POSITIVE_VALUE);
        }

        double amountBase = drugAmount * amountUnit.toBase;
        double doseBasePerMin = dose * doseUnit.toBasePerMin * (doseUnit.perKg ? weightKg : 1.0);
        double concentration = amountBase / totalVolume;
        double mlPerMin = doseBasePerMin / concentration;
        double dropsPerMin = mlPerMin * dropFactor;

        return new Result(Error.NONE, dropsPerMin, 60.0 / dropsPerMin, mlPerMin * 60.0,
                concentration, amountUnit.isUnits);
    }

    /** Türkçe klavyedeki virgülü de kabul eden sayı okuyucu. Geçersizse NaN döner. */
    public static double parse(String s) {
        if (s == null) return Double.NaN;
        String t = s.trim().replace(',', '.');
        if (t.isEmpty()) return Double.NaN;
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
