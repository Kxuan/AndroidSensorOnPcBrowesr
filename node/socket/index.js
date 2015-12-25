var SocketIO = require('socket.io'),
    sensorServer = require('../sensor'),
    SensorDevice=require('../sensor/device');

module.exports = exports = function Server(server) {
    exports.server = this;

    var io = SocketIO(server);

    io.on('connection', function (socket) {
        socket.once('disconnect', function () {

        });
    });


    sensorServer.on('device', function (device) {
        for (var i in SensorDevice.sensorNames) {
            var name = SensorDevice.sensorNames[i];
            device.on(name, io.emit.bind(io, name));
        }
    });

    setInterval(function () {
        io.emit('now', Date.now());
    }, 1000);
};
exports.server = null;
