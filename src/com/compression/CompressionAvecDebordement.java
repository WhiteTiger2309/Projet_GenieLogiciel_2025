package com.compression;

import java.util.ArrayList;
import java.util.List;

/**
 * Compression avec gestion de débordement :
 * - On compresse les valeurs inférieures à 2^k' dans des champs binaires.
 * - Les valeurs dépassant ce seuil sont placées dans une "zone de débordement".
 * - Chaque champ contient :
 *    1 bit indicateur (0 = valeur normale, 1 = débordement)
 *    k' bits pour la valeur ou l'indice dans la zone de débordement
 */
public class CompressionAvecDebordement implements Compression {
    private static final int MAGIC = 0x42505431; // 'BPT1'
    private static final int VERSION = 1;

    private int[] donneesCompressees;
    private int[] zoneDebordement;

    private int largeurChamp;
    private int kPrime;
    private int bitsIndex;
    private int tailleOriginale;

    private String codageBinaire = "";

    // === Getters pour affichage dans Main ===
    public int getKPrime() { return kPrime; }
    public int getBitsIndex() { return bitsIndex; }
    public int getLargeurChamp() { return largeurChamp; }
    public int[] getZoneDebordement() { return zoneDebordement; }
    public String getCodageBinaire() { return codageBinaire; }

    @Override
    public int[] compresser(int[] tableau) {
        tailleOriginale = tableau.length;

        // Étape 1 : déterminer k' optimal (seuil)
        int max = 0;
        for (int val : tableau) if (val > max) max = val;

        // k' = plus petit nombre tel que 2^k' > moyenne ou seuil raisonnable
        kPrime = Math.max(1, 32 - Integer.numberOfLeadingZeros(max / 2));

        // Étape 2 : identifier les débordements
        List<Integer> overflow = new ArrayList<>();
        for (int val : tableau)
            if (val >= (1 << kPrime)) overflow.add(val);

        // Calcul du nombre de bits nécessaires pour indexer la zone de débordement
        bitsIndex = overflow.isEmpty() ? 0 : (32 - Integer.numberOfLeadingZeros(overflow.size() - 1));
        largeurChamp = 1 + Math.max(kPrime, bitsIndex); // 1 bit indicateur + champ utile

        // Conversion en tableau
        zoneDebordement = overflow.stream().mapToInt(Integer::intValue).toArray();

        // === Pour le codage binaire affichable ===
        StringBuilder codage = new StringBuilder();

    // Étape 3 : écrire dans un flux de bits (partie "données" uniquement)
    int totalBits = tableau.length * largeurChamp;
    int nbInts = (int) Math.ceil(totalBits / 32.0);
    donneesCompressees = new int[nbInts];

        int bitPos = 0;
        for (int val : tableau) {
            int indicateur, contenu;

            if (val >= (1 << kPrime)) {
                indicateur = 1;
                contenu = overflow.indexOf(val); // index dans la zone de débordement
            } else {
                indicateur = 0;
                contenu = val;
            }

            int champ = (indicateur << Math.max(kPrime, bitsIndex)) | contenu;

            // Stockage binaire dans la sortie
            int indexInt = bitPos / 32;
            int offset = bitPos % 32;

            donneesCompressees[indexInt] |= (champ << offset);
            if (offset + largeurChamp > 32 && indexInt + 1 < nbInts) {
                donneesCompressees[indexInt + 1] |= (champ >>> (32 - offset));
            }

            // Pour affichage du codage
            codage.append(indicateur).append("-").append(contenu).append("  ");

            bitPos += largeurChamp;
        }

        codageBinaire = codage.toString().trim();

        // Construire sortie avec en-tête et zone de débordement
        int headerSize = 8; // MAGIC, VERSION, TYPE, tailleOriginale, largeurChamp, kPrime, bitsIndex, lenOverflow
        int lenOverflow = zoneDebordement.length;
        int[] sortie = new int[headerSize + lenOverflow + donneesCompressees.length];
        sortie[0] = MAGIC;
        sortie[1] = VERSION;
        sortie[2] = TypeCompression.AVEC_DEBORDEMENT.ordinal();
        sortie[3] = tailleOriginale;
        sortie[4] = largeurChamp;
        sortie[5] = kPrime;
        sortie[6] = bitsIndex;
        sortie[7] = lenOverflow;
        // zone de débordement
        System.arraycopy(zoneDebordement, 0, sortie, headerSize, lenOverflow);
        // données compressées
        System.arraycopy(donneesCompressees, 0, sortie, headerSize + lenOverflow, donneesCompressees.length);

        return sortie;
    }

    @Override
    public int[] decompresser(int[] compresse) {
        // Lecture de l'en-tête
        if (compresse == null || compresse.length < 8 || compresse[0] != MAGIC) {
            throw new IllegalArgumentException("Format compressé invalide (MAGIC)");
        }
        int version = compresse[1];
        if (version != VERSION) throw new IllegalArgumentException("Version non supportée");
        int type = compresse[2];
        if (type != TypeCompression.AVEC_DEBORDEMENT.ordinal()) {
            throw new IllegalArgumentException("Type de compression inattendu pour ce décompresseur");
        }
        int origLen = compresse[3];
        int largeurChampLocal = compresse[4];
        int kPrimeLocal = compresse[5];
        int bitsIndexLocal = compresse[6];
        int lenOverflow = compresse[7];

        // Lire la zone de débordement locale
        int headerSize = 8;
        int overflowStart = headerSize;
        int dataStart = headerSize + lenOverflow;

        int[] zoneDebordementLocal = new int[lenOverflow];
        if (lenOverflow > 0) {
            System.arraycopy(compresse, overflowStart, zoneDebordementLocal, 0, lenOverflow);
        }

        int[] resultat = new int[origLen];
        int bitPos = 0;

        for (int i = 0; i < origLen; i++) {
            int indexInt = bitPos / 32;
            int offset = bitPos % 32;

            int champ = (compresse[dataStart + indexInt] >>> offset);
            if (offset + largeurChampLocal > 32 && dataStart + indexInt + 1 < compresse.length) {
                champ |= (compresse[dataStart + indexInt + 1] << (32 - offset));
            }

            int innerWidth = Math.max(kPrimeLocal, bitsIndexLocal);
            int champMask = (largeurChampLocal >= 32) ? -1 : ((1 << largeurChampLocal) - 1);
            int innerMask = (innerWidth >= 32) ? -1 : ((1 << innerWidth) - 1);

            int champMasked = champ & champMask;
            int indicateur = champMasked >>> innerWidth;
            int contenu = champMasked & innerMask;

            if (indicateur == 1 && contenu < zoneDebordementLocal.length)
                resultat[i] = zoneDebordementLocal[contenu];
            else
                resultat[i] = contenu;

            bitPos += largeurChampLocal;
        }

        return resultat;
    }

    @Override
    public int get(int i) {
        int bitPos = i * largeurChamp;
        int indexInt = bitPos / 32;
        int offset = bitPos % 32;

        int champ = (donneesCompressees[indexInt] >>> offset);
        if (offset + largeurChamp > 32 && indexInt + 1 < donneesCompressees.length) {
            champ |= (donneesCompressees[indexInt + 1] << (32 - offset));
        }

        int innerWidth = Math.max(kPrime, bitsIndex);
        int champMask = (largeurChamp >= 32) ? -1 : ((1 << largeurChamp) - 1);
        int innerMask = (innerWidth >= 32) ? -1 : ((1 << innerWidth) - 1);

        int champMasked = champ & champMask;
        int indicateur = champMasked >>> innerWidth;
        int contenu = champMasked & innerMask;

        if (indicateur == 1 && contenu < zoneDebordement.length)
            return zoneDebordement[contenu];
        return contenu;
    }
}
