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

        // Étape 3 : écrire dans un flux de bits
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
        return donneesCompressees;
    }

    @Override
    public int[] decompresser(int[] compresse) {
        int[] resultat = new int[tailleOriginale];
        int bitPos = 0;

        for (int i = 0; i < tailleOriginale; i++) {
            int indexInt = bitPos / 32;
            int offset = bitPos % 32;

            int champ = (compresse[indexInt] >>> offset);
            if (offset + largeurChamp > 32 && indexInt + 1 < compresse.length) {
                champ |= (compresse[indexInt + 1] << (32 - offset));
            }

            int indicateur = champ >>> Math.max(kPrime, bitsIndex);
            int contenu = champ & ((1 << Math.max(kPrime, bitsIndex)) - 1);

            if (indicateur == 1 && contenu < zoneDebordement.length)
                resultat[i] = zoneDebordement[contenu];
            else
                resultat[i] = contenu;

            bitPos += largeurChamp;
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

        int indicateur = champ >>> Math.max(kPrime, bitsIndex);
        int contenu = champ & ((1 << Math.max(kPrime, bitsIndex)) - 1);

        if (indicateur == 1 && contenu < zoneDebordement.length)
            return zoneDebordement[contenu];
        return contenu;
    }
}
