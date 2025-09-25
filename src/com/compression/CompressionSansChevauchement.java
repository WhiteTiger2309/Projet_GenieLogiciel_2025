package com.compression;

/**
 * Implémentation de la compression sans chevauchement.
 * 
 * Chaque entier compressé doit tenir entièrement dans un entier machine
 * de sortie, ce qui simplifie l’algorithme mais peut être légèrement
 * moins optimal en espace.
 */
public class CompressionSansChevauchement implements Compression {
    private int[] donneesCompressees;
    private int largeurBits;

    @Override
    public int[] compresser(int[] tableau) {
        // TODO: implémenter la compression sans chevauchement
        return null;
    }

    @Override
    public int[] decompresser(int[] compresse) {
        // TODO: implémenter la décompression sans chevauchement
        return null;
    }

    @Override
    public int get(int i) {
        // TODO: accès direct
        return 0;
    }
}
