package com.compression;

/**
 * Compression AVEC chevauchement :
 * On écrit les entiers dans un flux de bits continu.
 * Un entier peut être découpé entre deux int de 32 bits.
 */
public class CompressionAvecChevauchement implements Compression {
    private static final int MAGIC = 0x42505431; // 'BPT1'
    private static final int VERSION = 1;

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

        // Construire sortie avec en-tête auto-portant
        int headerSize = 5; // MAGIC, VERSION, TYPE, tailleOriginale, largeurBits
        int[] sortie = new int[headerSize + donneesCompressees.length];
        sortie[0] = MAGIC;
        sortie[1] = VERSION;
        sortie[2] = TypeCompression.AVEC_CHEVAUCHEMENT.ordinal();
        sortie[3] = tailleOriginale;
        sortie[4] = largeurBits;
        System.arraycopy(donneesCompressees, 0, sortie, headerSize, donneesCompressees.length);

        return sortie;
    }

    @Override
    public int[] decompresser(int[] compresse) {
        // Lecture de l'en-tête
        if (compresse == null || compresse.length < 5 || compresse[0] != 0x42505431) {
            throw new IllegalArgumentException("Format compressé invalide (MAGIC)");
        }
        int version = compresse[1];
        if (version != VERSION) throw new IllegalArgumentException("Version non supportée");
        int type = compresse[2];
        if (type != TypeCompression.AVEC_CHEVAUCHEMENT.ordinal()) {
            throw new IllegalArgumentException("Type de compression inattendu pour ce décompresseur");
        }
        int origLen = compresse[3];
        int k = compresse[4];

        int[] resultat = new int[origLen];
        int mask = (k >= 32) ? -1 : (1 << k) - 1;

        int dataStart = 5;
        int bitPos = 0;
        for (int i = 0; i < origLen; i++) {
            int globalBitPos = bitPos;
            int indexInt = globalBitPos / 32;
            int offset = globalBitPos % 32;

            int val = (compresse[dataStart + indexInt] >>> offset);

            if (offset + k > 32) {
                val |= (compresse[dataStart + indexInt + 1] << (32 - offset));
            }

            resultat[i] = val & mask;
            bitPos += k;
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
