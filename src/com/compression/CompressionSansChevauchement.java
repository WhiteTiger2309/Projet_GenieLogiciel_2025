package com.compression;

/**
 * Compression SANS chevauchement :
 * On range les valeurs dans des cases fixes de largeur k bits,
 * alignées dans les int de 32 bits (pas de chevauchement entre deux int).
 */
public class CompressionSansChevauchement implements Compression {
    private int[] donneesCompressees;
    private int largeurBits;  
    private int tailleOriginale;

    CompressionSansChevauchement() {}

    @Override
    public int[] compresser(int[] tableau) {
        tailleOriginale = tableau.length;

        // Trouver la largeur en bits nécessaire
        int max = 0;
        for (int val : tableau) if (val > max) max = val;
        largeurBits = 32 - Integer.numberOfLeadingZeros(max);
        if (largeurBits == 0) largeurBits = 1;

        // Nombre de valeurs par int
        int valeursParInt = 32 / largeurBits;
        int tailleCompressee = (int) Math.ceil((double) tableau.length / valeursParInt);
        donneesCompressees = new int[tailleCompressee];

        //System.out.println("=== DEBUG CompressionSansChevauchement ===");
        //System.out.println("Largeur (bits) = " + largeurBits);
        //System.out.println("Valeurs par int = " + valeursParInt);
        //System.out.println("Taille compressée = " + tailleCompressee);

        // Remplissage sans chevauchement
        int index = 0;
        int posDansInt = 0;
        for (int val : tableau) {
            int shift = posDansInt * largeurBits;
            int contribution = (val << shift);

            /*System.out.printf("Placer %d (binaire=%s) dans int[%d], position=%d, shift=%d → contribution=%s%n",
                    val,
                    Integer.toBinaryString(val),
                    index,
                    posDansInt,
                    shift,
                    Integer.toBinaryString(contribution));*/

            donneesCompressees[index] |= contribution;

            posDansInt++;
            if (posDansInt == valeursParInt) {
                //System.out.printf("int[%d] final = %s%n", index,
                        //String.format("%32s", Integer.toBinaryString(donneesCompressees[index])).replace(' ', '0'));
                posDansInt = 0;
                index++;
            }
        }

        // Dernier int si incomplet
        /*if (index < donneesCompressees.length) {
            System.out.printf("int[%d] final = %s%n", index,
                    String.format("%32s", Integer.toBinaryString(donneesCompressees[index])).replace(' ', '0'));
        }*/

        return donneesCompressees;
    }

    @Override
    public int[] decompresser(int[] compresse) {
        int[] resultat = new int[tailleOriginale];
        int mask = (1 << largeurBits) - 1;

        int index = 0;
        int posDansInt = 0;
        int valeursParInt = 32 / largeurBits;

        for (int i = 0; i < tailleOriginale; i++) {
            int shift = posDansInt * largeurBits;
            resultat[i] = (compresse[index] >>> shift) & mask;

            //System.out.printf("Lire int[%d], shift=%d → valeur=%d%n", index, shift, resultat[i]);

            posDansInt++;
            if (posDansInt == valeursParInt) {
                posDansInt = 0;
                index++;
            }
        }
        return resultat;
    }

    @Override
    public int get(int i) {
        int mask = (1 << largeurBits) - 1;
        int valeursParInt = 32 / largeurBits;

        int index = i / valeursParInt;
        int posDansInt = i % valeursParInt;

        int shift = posDansInt * largeurBits;
        int val = (donneesCompressees[index] >>> shift) & mask;

        //System.out.printf("Accès direct get(%d) → int[%d], shift=%d → %d%n", i, index, shift, val);

        return val;
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
