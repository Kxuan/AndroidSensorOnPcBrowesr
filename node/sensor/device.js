var EventEmitter = require('events').EventEmitter,
    util = require('util'),
    dgram = require('dgram'),
    sensorId = require('./id');

var MSG_OK = new Buffer('OK\0');
/**
 *
 * @param {dgram#Socket} server
 * @param {{address:string,port:int}} rinfo
 * @param {Buffer} initialMsg
 * @constructor
 */
function SensorDevice(server, rinfo, initialMsg) {
    var newId = sensorId.generate();

    this.lastActive = Date.now();
    this.binaryId = newId.buffer;
    this.id = newId.str;
    this.model = initialMsg.toString('utf8', 0, initialMsg.length - 1);
    this.send = function (buffer) {
        server.send(buffer, 0, buffer.length, rinfo.port, rinfo.address);
    };


    var buffer = new Buffer(MSG_OK.length + this.binaryId.length);
    MSG_OK.copy(buffer);
    this.binaryId.copy(buffer, MSG_OK.length);
    this.send(buffer);

}
util.inherits(SensorDevice, EventEmitter);

var sensorNames = SensorDevice.sensorNames = {
    __proto__: null,

    1: 'accelerometer',
    2: 'gyroscope',
    3: 'key',
    4: 'orientation'
};
/**
 *
 * @param {int} sensorType
 * @param {Buffer} rawData
 */
SensorDevice.prototype.receive = function (sensorType, rawData) {
    if (sensorType in sensorNames) {
        this.emit(sensorNames[sensorType], rawData);
    } else {
        console.log("unknown sensor type");
    }
};
module.exports = exports = SensorDevice;
