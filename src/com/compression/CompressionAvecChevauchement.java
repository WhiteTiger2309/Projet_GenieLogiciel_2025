package com.compression;

/**
 * Compression AVEC chevauchement :
 * On écrit les entiers dans un flux de bits continu.
 * Un entier peut être découpé entre deux int de 32 bits.
 */
public class CompressionAvecChevauchement implements Compression {
    private int[] donneesCompressees;
    private int largeurBits;
    private int tailleOriginale;

    CompressionAvecChevauchement() {}

    @Override
    public int[] compresser(int[] tableau) {
        tailleOriginale = tableau.length;

        // Trouver la largeur en bits
        int max = 0;
        for (int val : tableau) if (val > max) max = val;
        largeurBits = 32 - Integer.numberOfLeadingZeros(max);
        if (largeurBits == 0) largeurBits = 1;

        int totalBits = tableau.length * largeurBits;
        int tailleCompressee = (int) Math.ceil(totalBits / 32.0);
        donneesCompressees = new int[tailleCompressee];

        //System.out.println("=== DEBUG CompressionAvecChevauchement ===");
        //System.out.println("Largeur (bits) = " + largeurBits);
        //System.out.println("Total bits = " + totalBits);
        //System.out.println("Taille compressée (ints) = " + tailleCompressee);

        int bitPos = 0;
        for (int val : tableau) {
            int indexInt = bitPos / 32;
            int offset = bitPos % 32;

            //System.out.printf("Placer %d (binaire=%s) à partir du bitPos=%d → int[%d], offset=%d%n", val, Integer.toBinaryString(val), bitPos, indexInt, offset);

            // Ecriture dans le int courant
            donneesCompressees[indexInt] |= (val << offset);

            // Si ça dépasse 32 bits, on écrit la suite dans le suivant
            if (offset + largeurBits > 32) {
                donneesCompressees[indexInt + 1] |= (val >>> (32 - offset));
                //System.out.printf(" ↳ Débordement → int[%d] reçoit les bits restants%n", indexInt + 1);
            }

           // System.out.printf("int[%d] après = %s%n", indexInt,
                    //String.format("%32s", Integer.toBinaryString(donneesCompressees[indexInt])).replace(' ', '0'));

            bitPos += largeurBits;
        }

        return donneesCompressees;
    }

    @Override
    public int[] decompresser(int[] compresse) {
        int[] resultat = new int[tailleOriginale];
        int mask = (1 << largeurBits) - 1;

        int bitPos = 0;
        for (int i = 0; i < tailleOriginale; i++) {
            int indexInt = bitPos / 32;
            int offset = bitPos % 32;

            int val = (compresse[indexInt] >>> offset);

            if (offset + largeurBits > 32) {
                val |= (compresse[indexInt + 1] << (32 - offset));
                //System.out.printf("Lire i=%d chevauchant int[%d] et int[%d]%n", i, indexInt, indexInt + 1);
            } else {
                //System.out.printf("Lire i=%d dans int[%d], offset=%d%n", i, indexInt, offset);
            }

            resultat[i] = val & mask;
            //System.out.printf(" → valeur décompressée = %d%n", resultat[i]);

            bitPos += largeurBits;
        }

        return resultat;
    }

    @Override
    public int get(int i) {
        int mask = (1 << largeurBits) - 1;
        int bitPos = i * largeurBits;
        int indexInt = bitPos / 32;
        int offset = bitPos % 32;

        int val = (donneesCompressees[indexInt] >>> offset);
        if (offset + largeurBits > 32) {
            val |= (donneesCompressees[indexInt + 1] << (32 - offset));
            //System.out.printf("Accès get(%d) chevauche int[%d] et int[%d]%n", i, indexInt, indexInt + 1);
        } else {
            //System.out.printf("Accès get(%d) dans int[%d], offset=%d%n", i, indexInt, offset);
        }

        return val & mask;
    }

    // Getters utiles
    public int[] getDonneesCompressees() {
        return donneesCompressees;
    }

    public int getLargeurBits() {
        return largeurBits;
    }

    public int getTailleOriginale() {
        return tailleOriginale;
    }
}
