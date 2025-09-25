package com.compression;

import java.util.Random;

/**
 * Point d'entrée du programme.
 * 
 * - Génère un tableau aléatoire
 * - Crée un compresseur via la fabrique
 * - Mesure les performances
 */
public class Main {
    public static void main(String[] args) {
        // Exemple : créer un tableau aléatoire
        int n = 1000;
        int[] tableau = new int[n];
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            tableau[i] = rand.nextInt(1024); // valeurs entre 0 et 1023
        }

        // Choisir un type de compression
        Compression c = CompressionFactory.creer("chevauchement");

        // Compression
        long tCompresser = Benchmark.mesurerCompression(c, tableau);
        System.out.println("Temps compresser() : " + tCompresser + " ns");

        // Décompression
        int[] compresse = c.compresser(tableau);
        long tDecompresser = Benchmark.mesurerDecompression(c, compresse);
        System.out.println("Temps decompresser() : " + tDecompresser + " ns");

        // Accès direct
        long tGet = Benchmark.mesurerAcces(c, 10);
        System.out.println("Temps get(10) : " + tGet + " ns");
    }
}
