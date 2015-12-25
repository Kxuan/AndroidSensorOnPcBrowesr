GS.DebugUI = {
	fontSize: 24,
	maxTempLines: 5,
	defaultTimeout: GS.msToFrames(5000),

	overlayMargin: 10,
	overlayPadding: 5,
	overlayWidth: 0,
	overlayHeight: 0,	
	overlayX: 0,
	overlayY: 0,

	visibleStaticLines: 0,

	staticLines: {},
	tempLines: [],
	valueTracking: {},

	hasChanged: false,

	_visible: false,
	set visible(value) {
		this._visible = value;
		if (!value && this.ctx) {
			this.ctx.clearRect(0, 0, this.width, this.height);
		}
	},

	get visible() {
		return this._visible;
	},

	init: function() {
		var that = this;

		this.minWidth = 1280;
		this.minHeight = 720;

		this.width = window.innerWidth;
		this.height = window.innerHeight;

		window.addEventListener("resize", function() { that.onResize(); }, false);		

		var canvas = document.createElement("canvas");
		canvas.id = "debug-ui-canvas";
		canvas.width = this.width;
		canvas.height = this.height;
		canvas.style.backgroundColor = "rgba(0, 0, 0, 0)";
		canvas.style.zIndex = 100;
		this.canvas = canvas;

		var ctx = canvas.getContext("2d");
		ctx.globalCompositeOperation = "source-over";
		ctx.save();
		this.ctx = ctx;
		this.updateFont();
		
		document.body.appendChild(this.canvas);

		this.onResize();
	},

	update: function() {
		var hasChanged = false;
		for (var i = this.tempLines.length - 1; i >= 0; i--) {
			var line = this.tempLines[i];
			line.timeout--;
			if (line.timeout === 0) {
				this.tempLines.splice(i, 1);
				this.hasChanged = true;
			}
		}

		if (this.hasChanged) {
			this.calculateOverlayCoords();
			this.draw();
			this.hasChanged = false;
		}
	},

	draw: function() {
		if (!this._visible) {
			return;
		}

		this.ctx.clearRect(0, 0, this.width, this.height);

		if (this.visibleStaticLines > 0 || this.tempLines.length > 0) {
			this.ctx.save();

			this.ctx.fillStyle = "rgba(0, 0, 0, 0.5)";
			this.ctx.fillRect(this.overlayX, this.overlayY, this.overlayWidth, this.overlayHeight);
			this.ctx.fillStyle = "#fff";

			var y = this.overlayY + this.overlayPadding;
			for (var i in this.staticLines) {
				if (this.staticLines[i].visible) {
					this.ctx.fillText(this.staticLines[i].text, this.overlayX + this.overlayPadding, y);
					y += this.fontSize + this.overlayPadding;
				}
			}

			for (var i = 0; i < this.tempLines.length; i++) {
				this.ctx.fillText(this.tempLines[i].text, this.overlayX + this.overlayPadding, y);
				y += this.fontSize + this.overlayPadding;				
			}

			this.ctx.restore();
		}
	},

	updateFont: function() {
		this.ctx.font = this.fontSize + "px 'Lucida Console', Monaco, monospace";
		this.ctx.textBaseline = "top";
	},

	calculateOverlayCoords: function() {
		this.overlayWidth = 0;
		this.visibleStaticLines = 0;

		for (var i in this.staticLines) {
			if (this.staticLines[i].visible) {
				this.visibleStaticLines++;
			}
		}

		this.overlayHeight = (this.visibleStaticLines + this.tempLines.length) * (this.fontSize + this.overlayPadding) + this.overlayPadding;

		// lower right
		// this.overlayX = this.width - this.overlayWidth - this.overlayMargin;
		// this.overlayY = this.height - this.overlayHeight - this.overlayMargin;

		// upper left
		this.overlayX = this.overlayMargin;
		this.overlayY = this.overlayMargin;

		for (var i in this.staticLines) {
			if (this.staticLines[i].visible) {
				this.overlayWidth = Math.max(this.overlayWidth, this.ctx.measureText(this.staticLines[i].text).width + this.overlayPadding * 2);
			}
		}

		for (var i = 0; i < this.tempLines.length; i++) {
			this.overlayWidth = Math.max(this.overlayWidth, this.ctx.measureText(this.tempLines[i].text).width + this.overlayPadding * 2);
		}
	},

	trackNumericValue: function(id, numericValue) {
		if (this.valueTracking[id] === undefined) {
			this.valueTracking[id] = {};
			this.valueTracking[id].min = Infinity;
			this.valueTracking[id].max = -Infinity;
			this.valueTracking[id].avg = 0;
			this.valueTracking[id].count = 0;
		}

		var v = this.valueTracking[id];
		v.min = Math.min(v.min, numericValue);
		v.max = Math.max(v.max, numericValue);
		v.count++;
		v.avg += (numericValue - v.avg) / v.count;
		this.setStaticLine(id, numericValue + " (min: " + v.min + ", max: " + v.max + ", avg: " + v.avg.toFixed(0) + ")");
	},

	setStaticLine: function(id, text) {
		text = id + ": " + text;
		if (id in this.staticLines) {
			this.staticLines[id].text = text;
		} else {
			this.staticLines[id] = {
				text: text,
				visible: true,
			};
		}
		this.hasChanged = true;
	},

	removeStaticLine: function(id) {
		delete this.staticLines[id];
		this.hasChanged = true;
	},

	setStaticLineVisibility: function(id, value) {
		if (id in this.staticLines) {
			this.staticLines[id].visible = (value === true);
		} else {
			this.staticLines[id] = {
				text: "",
				visible: value,
			}
		}
	},

	getStaticLineVisibility: function(id) {
		if (id in this.staticLines) {
			return this.staticLines[id].visible;
		}
	},

	addTempLine: function(text, timeout) {
		if (this.tempLines.length == this.maxTempLines) {
			this.tempLines.shift();
		}

		timeout = (timeout !== undefined) ? timeout : this.defaultTimeout;
		var line = {
			text: text,
			timeout: timeout,
		};
		this.tempLines.push(line);
		this.hasChanged = true;
	},

	onResize: function() {
		this.width = Math.max(window.innerWidth, this.minWidth);
		this.height = Math.max(window.innerHeight, this.minHeight);

		this.canvas.width = this.width;
		this.canvas.height = this.height;

		$(this.canvas).css("width", window.innerWidth + "px").css("height", window.innerHeight + "px");

		this.ctx.font = this.fontSize + "px 'Lucida Console', Monaco, monospace";
		this.ctx.textBaseline = "top";
		this.draw();
	},
};