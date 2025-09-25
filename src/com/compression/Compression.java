package com.compression;


/**
 * Interface commune pour toutes les méthodes de compression Bit Packing.
 * 
 * Chaque implémentation doit permettre :
 * - la compression d’un tableau d’entiers,
 * - la décompression du tableau compressé,
 * - l’accès direct au i-ème élément sans tout décompresser.
 */
public interface Compression {
    int[] compresser(int[] tableau);      // Compression
    int[] decompresser(int[] compresse);  // Décompression
    int get(int i);                       // Accès direct
}
