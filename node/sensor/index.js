var dgram = require('dgram'),
    sensorId = require('./id'),
    SensorDevice = require('./device');

var MSG_PROTOCOL_ERROR = new Buffer("Unexpected Protocol");

/**
 * @type {Object.<string,SensorDevice>}
 */
var allDevices = {};

/**
 * @type {Socket}
 */
var server = dgram.createSocket({
    type: 'udp4',
    reuseAddr: true
}, onReceiveMessage);
module.exports = exports = server;
server.bind(3001);

function send(rinfo, buffer) {
    server.send(buffer, 0, buffer.length, rinfo.port, rinfo.address);
}

var receivedBytes = 0;
setInterval(function () {
    if (receivedBytes != 0) {
        console.log("传感器数据回传：%d Bps", receivedBytes);
        receivedBytes = 0;
    }
}, 1000);
/**
 *
 * @param {Buffer} msg
 * @param {{address:string,port:int}} rinfo
 */
function onReceiveMessage(msg, rinfo) {
    receivedBytes += msg.length;
    var offset = 0, endOffset;
    var id = sensorId.stringify(msg);
    if (!id) {
        send(rinfo, MSG_PROTOCOL_ERROR);
        return;
    }

    var device;

    if (id == "0102030405060708") {
        //register: A new sensor device
        device = new SensorDevice(server, rinfo, msg.slice(8));
        console.log("New sensor device registered, Model: %s", device.model);
        server.emit('device', device);
        allDevices[device.id] = device;
    } else if (!(id in allDevices)) {
        //id not found
        send(rinfo, MSG_PROTOCOL_ERROR);
    } else {
        device = allDevices[id];
        device.receive(msg[8], msg.slice(9));
    }
}


