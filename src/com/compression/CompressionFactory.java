package com.compression;

/**
 * Fabrique permettant de créer des objets Compression
 * selon le type choisi via l'enum TypeCompression.
 */
public class CompressionFactory {

    public static Compression create(TypeCompression type) {
        switch (type) {
            case AVEC_CHEVAUCHEMENT:
                System.out.println("→ Type de compression sélectionné : AVEC_CHEVAUCHEMENT");
                return new CompressionAvecChevauchement();

            case SANS_CHEVAUCHEMENT:
                System.out.println("→ Type de compression sélectionné : SANS_CHEVAUCHEMENT");
                return new CompressionSansChevauchement();

            case AVEC_DEBORDEMENT:
                System.out.println("→ Type de compression sélectionné : AVEC_DEBORDEMENT");
                return new CompressionAvecDebordement();

            default:
                throw new IllegalArgumentException("Type de compression inconnu : " + type);
        }
    }
}
