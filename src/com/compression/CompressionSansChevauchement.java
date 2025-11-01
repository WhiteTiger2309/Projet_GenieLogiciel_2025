package com.compression;

/**
 * Compression SANS chevauchement :
 * On range les valeurs dans des cases fixes de largeur k bits,
 * alignées dans les int de 32 bits (pas de chevauchement entre deux int).
 */
public class CompressionSansChevauchement implements Compression {
    private static final int MAGIC = 0x42505431; // 'BPT1'
    private static final int VERSION = 1;

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

        // Construire la sortie avec en-tête auto-portant
        int headerSize = 5; // MAGIC, VERSION, TYPE, tailleOriginale, largeurBits
        int[] sortie = new int[headerSize + donneesCompressees.length];
        sortie[0] = MAGIC;
        sortie[1] = VERSION;
        sortie[2] = TypeCompression.SANS_CHEVAUCHEMENT.ordinal();
        sortie[3] = tailleOriginale;
        sortie[4] = largeurBits;
        System.arraycopy(donneesCompressees, 0, sortie, headerSize, donneesCompressees.length);

        return sortie;
    }


    @Override
    public int[] decompresser(int[] compresse) {
        // Lecture de l'en-tête
        if (compresse == null || compresse.length < 5 || compresse[0] != MAGIC) {
            throw new IllegalArgumentException("Format compressé invalide (MAGIC)");
        }
        int version = compresse[1];
        if (version != VERSION) throw new IllegalArgumentException("Version non supportée");
        int type = compresse[2];
        if (type != TypeCompression.SANS_CHEVAUCHEMENT.ordinal()) {
            // On pourrait tolérer, mais on signale une incohérence de type
            throw new IllegalArgumentException("Type de compression inattendu pour ce décompresseur");
        }
        int origLen = compresse[3];
        int k = compresse[4];

        int[] resultat = new int[origLen];
        int mask = (k >= 32) ? -1 : (1 << k) - 1;

        int index = 0;
        int posDansInt = 0;
        int valeursParInt = Math.max(1, 32 / Math.max(1, k));
        int dataStart = 5;

        for (int i = 0; i < origLen; i++) {
            int shift = posDansInt * k;
            int val = (compresse[dataStart + index] >>> shift) & mask;
            resultat[i] = val;

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
