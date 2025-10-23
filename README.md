# Projet Compression d'entiers (Bit Packing)

## Description
Ce projet implémente différentes méthodes de compression d'entiers basées sur le bit packing :
- Compression sans chevauchement
- Compression avec chevauchement
- Compression avec débordement

Chaque méthode permet de :
- compresser un tableau d'entiers
- le décompresser
- accéder directement au i-ème élément compressé

## Compilation & exécution
```bash
javac -d out src/com/compression/*.java
java -cp out com.compression.Main
