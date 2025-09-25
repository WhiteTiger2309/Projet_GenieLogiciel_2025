package com.compression;

/**
 * Classe utilitaire pour mesurer le temps d'exécution
 * des différentes méthodes de compression.
 */
public class Benchmark {
    public static long mesurerCompression(Compression c, int[] tableau) {
        long debut = System.nanoTime();
        c.compresser(tableau);
        long fin = System.nanoTime();
        return fin - debut;
    }

    public static long mesurerDecompression(Compression c, int[] compresse) {
        long debut = System.nanoTime();
        c.decompresser(compresse);
        long fin = System.nanoTime();
        return fin - debut;
    }

    public static long mesurerAcces(Compression c, int index) {
        long debut = System.nanoTime();
        c.get(index);
        long fin = System.nanoTime();
        return fin - debut;
    }
}
