package com.compression;

/**
 * Implémentation de la compression avec zone de débordement.
 * 
 * - Les entiers "petits" sont stockés dans une zone compacte avec un bit indicateur.
 * - Les entiers "trop grands" sont stockés dans une zone séparée de débordement.
 */
public class CompressionAvecDebordement implements Compression {
    private int[] zoneCompacte;
    private int[] zoneDebordement;
    private int largeurBits;

    @Override
    public int[] compresser(int[] tableau) {
        // TODO: implémenter compression avec zone de débordement
        return null;
    }

    @Override
    public int[] decompresser(int[] compresse) {
        // TODO: implémenter décompression avec zone de débordement
        return null;
    }

    @Override
    public int get(int i) {
        // TODO: accès direct
        return 0;
    }
}
