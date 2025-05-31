const WebSocket = require('ws');

/**
 * Send a message to the WebSocket server
 * @param socket The WebSocket connection
 * @param message The message to send
 * @returns {void}
 */
function sendMessage(socket, message) {
    if (socket.readyState === WebSocket.OPEN) {
        console.log(message);
        socket.send(message);
    } else {
        console.error(`WebSocket is not open when sending: "${message}"`);
    }
}


module.exports = {
    sendMessage,
};