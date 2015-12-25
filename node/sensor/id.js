function stringify(msg) {
    if (msg.length < 8) {
        return null;
    }
    var strid = '';
    for (var i = 0; i < 8; i++) {
        var s = msg[i].toString(16);
        if (s.length <= 1)
            strid += '0' + s;
        else
            strid += s.slice(0, 2);
    }
    return strid;
}
exports.stringify = stringify;

function generate() {
    var buf = new Buffer(8);
    for (var i = 0; i < 8; i++) {
        buf[i] ^= 256 * Math.random();
    }
    return {
        buffer: buf,
        str: stringify(buf)
    }
}
exports.generate = generate;