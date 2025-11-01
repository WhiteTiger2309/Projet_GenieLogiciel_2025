package com.compression;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * BenchmarkCompression
 *
 * Compare les trois stratégies (avec chevauchement, sans chevauchement, avec débordement)
 * sur plusieurs jeux de données et affiche résultats (taille, ratio, temps, rentabilité).
 */
public class BenchmarkCompression {

    private static final Random random = new Random(12345);
    private static final int WARMUP = 5;
    private static final int REPEAT = 50; // augmente si tu veux plus de stabilité

    /**
     * Petit conteneur pour associer un libellé humain à un jeu de données.
     */
    private static class Dataset {
        final String label;
        final int[] data;

        Dataset(String label, int[] data) {
            this.label = label;
            this.data = data;
        }
    }

    public static void debut_benchmark(String[] args) {
        List<TypeCompression> types = List.of(
                TypeCompression.AVEC_CHEVAUCHEMENT,
                TypeCompression.SANS_CHEVAUCHEMENT,
                TypeCompression.AVEC_DEBORDEMENT
        );

        // Jeux de données : tu peux en ajouter/retirer
        List<Dataset> jeux = List.of(
                new Dataset("Aléatoire (n=8, min=0, max=1023)", genererTableauAleatoire(8, 0, 1023)),
                new Dataset("Aléatoire (n=8, min=0, max=1_000_000)", genererTableauAleatoire(8, 0, 1_000_000)),
                new Dataset("Aléatoire (n=32, min=0, max=65_535)", genererTableauAleatoire(32, 0, 65_535)),
                new Dataset("Synthétique (n=256, valeurs ∈ [0..15])", genererTableauAleatoire(256, 0, 15)),
                new Dataset("Synthétique (n=1024, valeurs ∈ [0..3])", genererTableauAleatoire(1024, 0, 3)),
                new Dataset("Fixe [5, 12, 31, 7, 15, 1023, 2000, 999999]", new int[]{5, 12, 31, 7, 15, 1023, 2000, 999999})
        );

        System.out.println("=== BENCHMARK DES MÉTHODES DE COMPRESSION ===\n");
        System.out.println("Nombre de jeux : " + jeux.size());
        System.out.println();

        int casIndex = 1;
        for (Dataset ds : jeux) {
            int[] data = ds.data;
            System.out.println("============================================================");
            System.out.println("Cas #" + casIndex + " " + ds.label);
            System.out.println("------------------------------------------------------------");
            System.out.println("Jeu de données : " + Arrays.toString(data));
            System.out.println("Max = " + Arrays.stream(data).max().orElse(0));
            System.out.println("Longueur = " + data.length);
            System.out.println();

            for (TypeCompression type : types) {
                Compression algo = CompressionFactory.create(type);
                benchmark(type, algo, data);
                System.out.println();
            }

            System.out.println("============================================================\n");
            casIndex++;
        }
    }

    private static int[] genererTableauAleatoire(int taille, int minInclusive, int maxInclusive) {
        int[] arr = new int[taille];
        int range = Math.max(1, maxInclusive - minInclusive + 1);
        for (int i = 0; i < taille; i++) {
            arr[i] = minInclusive + random.nextInt(range);
        }
        return arr;
    }

    private static void benchmark(TypeCompression type, Compression compression, int[] original) {
        System.out.println(">> Type : " + type);

        // Warmup (JIT)
        for (int i = 0; i < WARMUP; i++) {
            int[] tmpC = compression.compresser(original);
            int[] tmpD = compression.decompresser(tmpC);
            if (!Arrays.equals(original, tmpD)) {
                System.out.println("!!! Erreur d'integrité lors du warmup pour " + type);
                return;
            }
        }

        // Mesures répétées (moyenne)
        long totalCompress = 0;
        long totalDecompress = 0;
        int[] lastCompressed = null;
        int[] lastDecompressed = null;

        for (int i = 0; i < REPEAT; i++) {
            long t0 = System.nanoTime();
            lastCompressed = compression.compresser(original);
            long t1 = System.nanoTime();
            lastDecompressed = compression.decompresser(lastCompressed);
            long t2 = System.nanoTime();

            totalCompress += (t1 - t0);
            totalDecompress += (t2 - t1);

            // vérification rapide sur chaque itération (pour détecter corruption)
            if (!Arrays.equals(original, lastDecompressed)) {
                System.out.println("!!! Erreur : décompression incorrecte pour " + type + " (itération " + i + ")");
                return;
            }
        }

        long avgCompressNs = totalCompress / REPEAT;
        long avgDecompressNs = totalDecompress / REPEAT;

        int tailleOriginale = original.length;
        int tailleCompressee = lastCompressed.length;
        double ratio = (double) tailleCompressee / (double) tailleOriginale;
        boolean rentable = tailleCompressee < tailleOriginale;

        System.out.printf("Taille originale   : %d ints%n", tailleOriginale);
        System.out.printf("Taille compressée  : %d ints%n", tailleCompressee);
        System.out.printf("Ratio (comp/orig)  : %.3f%n", ratio);
    // Affichage du tableau compressé (avec en-tête éventuel) pour inspection
    System.out.println("Compressé (ints)   : " + Arrays.toString(lastCompressed));

        // ---- Analyse de l'en-tête (affichage lisible) ----
        printHeaderAnalysis(type, lastCompressed);

        System.out.printf("Compression OK ?   : %s%n", Arrays.equals(original, lastDecompressed) ? "OUI" : "NON");
        System.out.printf("Temps moyen compress   : %d ns%n", avgCompressNs);
        System.out.printf("Temps moyen decompress : %d ns%n", avgDecompressNs);

        if (rentable) {
            double gain = (double) (tailleOriginale - tailleCompressee); // en ints
            double tRentableNsPerInt = (double) avgCompressNs / gain;
            System.out.printf("Compression rentable si latence t > %.2f ns/int (environ %.3f µs/int)%n",
                    tRentableNsPerInt, tRentableNsPerInt / 1000.0);
        } else {
            System.out.println("Compression non rentable : la compression n'a pas réduit la taille !");
        }
    }

    private static void printHeaderAnalysis(TypeCompression type, int[] comp) {
        final int MAGIC = 0x42505431;
        if (comp == null || comp.length < 5 || comp[0] != MAGIC) {
            System.out.println("(Info) Pas d'en-tête détecté ou format inconnu.");
            return;
        }

        int originalLen = comp[3];
        if (type == TypeCompression.AVEC_CHEVAUCHEMENT || type == TypeCompression.SANS_CHEVAUCHEMENT) {
            if (comp.length < 5) {
                System.out.println("(Info) En-tête incomplet.");
                return;
            }
            int k = comp[4];
            int headerInts = 5;
            int dataInts = Math.max(0, comp.length - headerInts);
            int payloadBits = originalLen * k;
            int payloadIntsLower = (int) Math.ceil(payloadBits / 32.0);
            double headerCostPct = 100.0 * headerInts / comp.length;

            System.out.println("Résumé header/payload:");
            System.out.printf("- Header          : %d ints%n", headerInts);
            System.out.printf("- Payload (data)  : %d ints%n", dataInts);
            System.out.printf("- Coût header     : %.1f%%%n", headerCostPct);
            System.out.printf("- k (bits/valeur) : %d%n", k);
            System.out.printf("- Payload théorique: %d bits (≈%d ints)%n", payloadBits, payloadIntsLower);
            if (k < 32) {
                double nThreshold = headerInts / (1.0 - (k / 32.0));
                System.out.printf("- Seuil n (approx): > %.2f éléments (header inclus)%n", nThreshold);
            }
        } else if (type == TypeCompression.AVEC_DEBORDEMENT) {
            if (comp.length < 8) {
                System.out.println("(Info) En-tête débordement incomplet.");
                return;
            }
            int largeurChamp = comp[4];
            int kPrime = comp[5];
            int bitsIndex = comp[6];
            int lenOverflow = comp[7];
            int headerInts = 8;
            int overflowInts = lenOverflow;
            int dataInts = Math.max(0, comp.length - headerInts - overflowInts);
            double headerCostPct = 100.0 * headerInts / comp.length;
            int payloadBits = (originalLen * largeurChamp) + (overflowInts * 32);
            int payloadIntsLower = (int) Math.ceil(payloadBits / 32.0);

            System.out.println("Résumé header/payload:");
            System.out.printf("- Header                 : %d ints%n", headerInts);
            System.out.printf("- Overflow (zone)        : %d ints%n", overflowInts);
            System.out.printf("- Data (flux principal)  : %d ints%n", dataInts);
            System.out.printf("- Payload total (théorie): %d bits (≈%d ints)%n", payloadBits, payloadIntsLower);
            System.out.printf("- Coût header            : %.1f%%%n", headerCostPct);
            System.out.printf("- Largeur champ          : %d bits = 1 + max(k'=%d, bitsIndex=%d)%n",
                    largeurChamp, kPrime, bitsIndex);
            if (largeurChamp < 32) {
                double nThreshold = (headerInts + overflowInts) / (1.0 - (largeurChamp / 32.0));
                System.out.printf("- Seuil n (approx)       : > %.2f éléments (overflow observé=%d)%n",
                        nThreshold, overflowInts);
            }
        }
    }
}
