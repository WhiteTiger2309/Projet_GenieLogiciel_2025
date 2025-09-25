package com.compression;

/**
 * Fabrique permettant de créer dynamiquement un objet de compression
 * selon le type choisi.
 */
public class CompressionFactory {
    public static Compression creer(String type) {
        switch (type.toLowerCase()) {
            case "chevauchement":
                return new CompressionAvecChevauchement();
            case "sanschevauchement":
                return new CompressionSansChevauchement();
            case "debordement":
                return new CompressionAvecDebordement();
            default:
                throw new IllegalArgumentException("Type inconnu : " + type);
        }
    }
}
