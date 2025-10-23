package com.compression;

import java.util.Arrays;

public class MesurePerformance {

    public static void mesurer(Compression algo, int[] tableau) {
        // --- Mesure du temps de compression ---
        long debutComp = System.nanoTime();
        int[] compresse = algo.compresser(tableau);
        long finComp = System.nanoTime();
        long tempsCompression = finComp - debutComp;

        // --- Mesure du temps de décompression ---
        long debutDecomp = System.nanoTime();
        int[] decompresse = algo.decompresser(compresse);
        long finDecomp = System.nanoTime();
        long tempsDecompression = finDecomp - debutDecomp;

        // --- Affichage des résultats ---
        System.out.println("Tableau original        : " + Arrays.toString(tableau));
        System.out.println("Compressé (ints)        : " + Arrays.toString(compresse));
        System.out.println("Décompressé             : " + Arrays.toString(decompresse));
        System.out.println("Temps compression       : " + tempsCompression + " ns");
        System.out.println("Temps décompression     : " + tempsDecompression + " ns");

        // --- Calcul du seuil de rentabilité ---
        double tailleOriginale = tableau.length;
        double tailleCompressee = compresse.length;

        if (tailleCompressee < tailleOriginale) {
            double gain = tailleOriginale - tailleCompressee;
            double tRentable = (double) tempsCompression / gain;
            System.out.printf("Compression rentable si la latence t > %.2f ns/unité%n", tRentable);
        } else {
            System.out.println("⚠️  Aucune rentabilité : la compression ne réduit pas la taille !");
        }
    }
}
