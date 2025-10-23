package com.compression;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

    public static void debut_benchmark(String[] args) {
        List<TypeCompression> types = List.of(
                TypeCompression.AVEC_CHEVAUCHEMENT,
                TypeCompression.SANS_CHEVAUCHEMENT,
                TypeCompression.AVEC_DEBORDEMENT
        );

        // Jeux de données : tu peux en ajouter/retirer
        List<int[]> jeuxDeDonnees = List.of(
                genererTableauAleatoire(8, 0, 1023),           // petits nombres (forte compression)
                genererTableauAleatoire(8, 0, 1_000_000),      // large amplitude
                genererTableauAleatoire(32, 0, 65_535),        // plus long, valeurs moyennes
                new int[]{5, 12, 31, 7, 15, 1023, 2000, 999999} // exemple fixe
        );

        System.out.println("=== BENCHMARK DES MÉTHODES DE COMPRESSION ===\n");

        for (int[] data : jeuxDeDonnees) {
            System.out.println("Jeu de données : " + Arrays.toString(data));
            System.out.println("Max = " + Arrays.stream(data).max().orElse(0));
            System.out.println("Longueur = " + data.length);
            System.out.println();

            for (TypeCompression type : types) {
                Compression algo = CompressionFactory.create(type);
                benchmark(type, algo, data);
                System.out.println();
            }

            System.out.println("------------------------------------------------------------\n");
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
}
