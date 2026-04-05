# 🎮 Guess Game - Client (Java TCP)

## 📌 Description

Ce projet est une application réseau développée en Java dans le cadre du cours **6GEN723 – Réseaux d’ordinateurs**.

Il s’agit de la partie **client** du jeu *Guess Game*, un jeu multijoueur basé sur un protocole de communication personnalisé (GG).

Le client permet aux utilisateurs de :
- se connecter à un serveur
- créer ou rejoindre des salles de jeu
- interagir avec d'autres joueurs
- participer à une partie de devinette de combinaison de couleurs

---

## 🧠 Fonctionnalités (actuelles)

- Connexion TCP au serveur
- Envoi de messages selon le protocole GG
- Réception et affichage des réponses du serveur
- Interface console interactive
- Parsing des messages du serveur
- Gestion des menus (liste, création, rejoindre salle)

---

## 🏗️ Structure du projet

src/
├── Client/
│ ├── TCPClient.java # Point d'entrée du client
│ ├── ClientConnection.java # Gestion de la connexion TCP
│ ├── ClientUI.java # Interface utilisateur console
│ ├── MessageParser.java # Analyse des messages du protocole GG
│
└── Server/
└── TCPServer.java # (partie serveur - en cours)


---

## ⚙️ Technologies utilisées

- Java
- TCP Sockets (`java.net`)
- Entrées/sorties (`java.io`)
- Programmation orientée objet

---

## 🔌 Protocole utilisé

Le client communique avec le serveur via des messages texte structurés :
GG|TYPE|champ1|champ2|...
### Exemples :

- Connexion : GG|CONNECT|NomJoueur
- Liste des salles : GG|LIST_ROOMS
- Rejoindre une salle : GG|JOIN_ROOM|NomSalle
- Réponse serveur : GG|ROOM_LIST|Salle1,Salle2

  
---

## 🚀 Lancer le client

### 1. Compiler

```bash
javac *.java

### 2. Exécuter
java TCPClient

📷 Interface

Le client utilise une interface console avec menus :

=== MENU PRINCIPAL ===
1. Lister les salles
2. Créer une salle
3. Rejoindre une salle
0. Quitter

État du projet

🔄 En cours de développement

 Connexion client TCP (OK)
 Parsing des messages (OK)
 Interface de base    (OK)
 Communication P2P
 Logique complète du jeu
 Gestion des parties
