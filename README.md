# Quasar Mobile - Plateforme RAT Android Open Source

Ce projet vise à recréer une plateforme de contrôle à distance (RAT) pour Android, similaire à Quasar RAT, mais en tant que projet open source. Il est destiné à des fins de recherche en cybersécurité, d'éducation et de tests de pénétration éthiques.

**Avertissement :** L'utilisation de cet outil doit se faire dans le respect des lois locales et avec le consentement explicite des propriétaires des appareils ciblés. Une utilisation malveillante peut entraîner des poursuites judiciaires.

## Environnement de Développement : GitHub Codespaces

Ce dépôt est configuré pour être utilisé avec [GitHub Codespaces](https://github.com/features/codespaces). Lancez un Codespace pour obtenir un environnement de développement prêt à l'emploi avec Node.js, Java, Git et les outils nécessaires.

### Lancement de Codespaces

1.  Créez un dépôt GitHub pour ce projet.
2.  Ajoutez les fichiers de configuration `.devcontainer/devcontainer.json` et `.devcontainer/Dockerfile` à la racine de votre dépôt.
3.  Ajoutez le reste du code source (serveur, client Android) dans la structure appropriée.
4.  Poussez vos modifications sur GitHub.
5.  Sur la page de votre dépôt, cliquez sur le bouton "Code" et sélectionnez "Codespaces" > "Create new codespace" (en choisissant le fichier `devcontainer.json`).

## Composants Principaux

1.  **Serveur C&C (Command and Control) :** Gère la communication avec les appareils infectés (Node.js, Express, Socket.IO).
2.  **Application RAT Android :** Le client qui tourne sur l'appareil cible (Java).
3.  **Outil d'Injection APK :** Permet d'intégrer le RAT dans des fichiers APK existants (implémentation à venir).
4.  **Interface Web :** Tableau de bord pour visualiser les appareils et envoyer des commandes (React, Node.js - à venir).

## Démarrage du Serveur C&C (dans Codespaces)

1.  Ouvrez le terminal dans Codespaces.
2.  Naviguez vers le répertoire du serveur : `cd server`
3.  Installez les dépendances : `npm install`
4.  Démarrez le serveur : `node index.js`
5.  Le serveur écoutera sur le port 3000. L'URL publique de votre Codespace sera nécessaire pour le client Android. Vous pouvez la trouver en exécutant `curl ifconfig.me` dans le terminal.

## Développement du Client Android

Le code source du client Android est fourni dans le répertoire `android-client/`. Il est recommandé de copier ces fichiers dans un projet Android Studio sur votre machine locale pour la compilation et le test, car la configuration complète du SDK Android dans Codespaces peut être complexe.

N'oubliez pas de mettre à jour `Constants.java` avec l'URL publique de votre serveur C&C Codespace.

## Prochaines Étapes

*   Implémentation de fonctionnalités plus avancées dans le client Android.
*   Développement de l'outil d'injection d'APK.
*   Création de l'interface Web (Dashboard).
*   Mise en place de la base de données (MongoDB).
