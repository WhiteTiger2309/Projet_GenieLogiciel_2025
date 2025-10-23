package com.compression;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        /*int[] tableau = {5, 12, 31, 7, 15, 1023, 2000, 999999};
        int[] tableau2 = {5, 12, 31, 7, 15, 1023};

        // ==========================================
        // Test 1 : COMPRESSION SANS CHEVAUCHEMENT
        // ==========================================
        Compression compresseur1 = CompressionFactory.create(TypeCompression.SANS_CHEVAUCHEMENT);

        System.out.println("=== Compression SANS chevauchement ===");
        MesurePerformance.mesurer(compresseur1, tableau2);
        System.out.println();


        // ==========================================
        // Test 2 : COMPRESSION AVEC CHEVAUCHEMENT
        // ==========================================
        Compression compresseur2 = CompressionFactory.create(TypeCompression.AVEC_CHEVAUCHEMENT);

        System.out.println("=== Compression AVEC chevauchement ===");
        MesurePerformance.mesurer(compresseur2, tableau);
        System.out.println();


        // ==========================================
        // Test 3 : COMPRESSION AVEC DÉBORDEMENT
        // ==========================================
        Compression compresseur3 = CompressionFactory.create(TypeCompression.AVEC_DEBORDEMENT);

        System.out.println("=== Compression AVEC débordement ===");
        MesurePerformance.mesurer(compresseur3, tableau);

        // Affiche la zone de débordement si présente
        if (compresseur3 instanceof CompressionAvecDebordement) {
            CompressionAvecDebordement c = (CompressionAvecDebordement) compresseur3;
            System.out.println("Zone de débordement : " + Arrays.toString(c.getZoneDebordement()));
        }*/




        // --- ⚙️ Appel du benchmark complet ---
        System.out.println("\n\n=== LANCEMENT DU BENCHMARK COMPLET ===\n");
        BenchmarkCompression.debut_benchmark(null);  // <--- appelle directement le benchmark
    }
}
