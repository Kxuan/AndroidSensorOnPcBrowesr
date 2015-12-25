window.socket = io();
socket.on('accelerometer', function (data) {
    //console.log('accelerometer', data.x, data.y, data.z);
});

var shakingValue = 0;

setInterval(function () {
    if (window.GAME === undefined)
        return;
    if (shakingValue > 12) {
        if (GAME.state == GS.GameStates.Play) {
            GAME.openMenu();
            GAME.play();
        } else if (GAME.state == GS.GameStates.Menu) {
            GAME.closeMenu();
            GAME.play();
        }
    }
    console.log(shakingValue);
    shakingValue = 0;
}, 500);

function parseXYZ(data) {
    var v = new DataView(data);
    return {
        x: v.getFloat32(0, false),
        y: v.getFloat32(4, false),
        z: v.getFloat32(8, false)
    }
}
socket.on('gyroscope', function (data) {
    if (window.GAME === undefined)
        return;
    if (window.keyShiftDown)
        return;
    var e = parseXYZ(data);
    if (Math.abs(e.x) < 1e-6)
        e.x = 0;
    if (Math.abs(e.y) < 1e-6)
        e.y = 0;
    if (Math.abs(e.z) < 1e-6)
        e.z = 0;

    //Any Problem?
    if (e.x == 0 && e.y == 0 && e.z == 0)
        return;

    shakingValue += Math.abs(e.y);

    if (GAME.state == GS.GameStates.Play) {
        var controls = GAME.grid.player.controls;
        controls.setViewAngles(controls.xAngle + e.x * -5, controls.yAngle + e.z * 5);
    }
});

var movement = {
    x: 0,
    z: 0
};

socket.on('accelerometer', function (data) {
    var e = parseXYZ(data);

    if (Math.abs(e.x) < 1e-6)
        e.x = 0;
    if (Math.abs(e.y) < 1e-6)
        e.y = 0;
    if (Math.abs(e.z) < 1e-6)
        e.z = 0;

    movement.x += e.z;
});

window.keyShootDown = false;
window.keyShiftDown = false;

socket.on('key', function (data) {
    if (window.GS === undefined)
        return;
    var v = new DataView(data);
    var type = v.getInt8(0),
        code = v.getUint32(1, false);

    switch (code) {
        case 24:
            window.keyShootDown = type == 0;
            break;
        case 25:
            window.keyShiftDown = type == 0;
            if(keyShiftDown){
                $('#sensor_fix').show();
            }else{
                $('#sensor_fix').hide();
            }
            break;
        default:
            console.log(code);
            break;
    }
});