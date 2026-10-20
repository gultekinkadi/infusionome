package com.hk.dropapp.Public;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Calculate {
    public static Map<String,String> calculateSonuc(double siviHacmiD, double ilacHacmiD, double ilacMiktariD,
                                                    double kiloD, double dozD, double dropD){
        //Formül = ( (kilo * doz) * (sıvı hacmi + ilaç hacmi) * drop ) / ilaç miktarı )

        double sonuc = ( (kiloD * dozD) * (siviHacmiD + ilacHacmiD) * dropD) / ilacMiktariD;

        Map<String,String> map = new HashMap<>();
        map.put("sonuc",formatTheDouble(sonuc));
        map.put("aralik",formatTheDoubleAralik(60/sonuc));
        return map;
    }

    private static String formatTheDouble(double d){
        DecimalFormat formatter = new DecimalFormat("##.##");
        return formatter.format(d);
    }
    private static String formatTheDoubleAralik(double d){
        DecimalFormat formatter = new DecimalFormat("##.#########");
        return formatter.format(d);
    }
}
