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

## Prérequis
- Java JDK 11+ (JDK 17 recommandé)
- Terminal Windows PowerShell (les commandes ci-dessous sont prêtes pour PowerShell)

## Lancer le programme (benchmark par défaut)
1) Compiler les sources (génère le dossier `out/` si besoin)
```powershell
if (-not (Test-Path out)) { New-Item -ItemType Directory -Path out | Out-Null }
javac -d out src/com/compression/*.java
```

2) Exécuter le programme principal
```powershell
java -cp out com.compression.Main
```

Par défaut, le programme lance un benchmark complet qui compare les 3 stratégies sur 6 jeux de données. Chaque jeu est clairement indiqué dans la console sous la forme:
```
Cas #n — <libellé du jeu>
```
