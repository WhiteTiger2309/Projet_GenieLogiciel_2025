package com.compression;

/**
 * Implémentation de la compression avec chevauchement.
 * 
 * Ici, les entiers compressés peuvent être répartis sur deux entiers
 * machine consécutifs.
 */
public class CompressionAvecChevauchement implements Compression {
    private int[] donneesCompressees;
    private int largeurBits; // nombre de bits nécessaires par entier

    @Override
    public int[] compresser(int[] tableau) {
        // TODO: implémenter la compression avec chevauchement
        return null;
    }

    @Override
    public int[] decompresser(int[] compresse) {
        // TODO: implémenter la décompression avec chevauchement
        return null;
    }

    @Override
    public int get(int i) {
        // TODO: accès direct
        return 0;
    }
}
