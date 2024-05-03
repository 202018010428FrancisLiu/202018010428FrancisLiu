export default class websocketUtil {
	constructor(url, time) {
		this.is_open_socket = false; //避免重复连接
		this.is_manual_close = false; //区分手动断开和自动断开
		this.url = url;
		this.data = null;

		//心跳检测
		this.timeout = time || 10000; //执行检测的时间
		this.heartbeatInterval = null;
		this.reconnectTimeOut = null;
		try {
			return this.connectSocketInit();
		} catch (e) {
			this.is_open_socket = false;
			this.reconnect();
			console.error(e);
		}
	}

	connectSocketInit() {
		this.socketTask = uni.connectSocket({
			url: this.url,
			success: () => {
				console.log("正准备建立websocket中...");
				return this.socketTask
			},
			fail: (err) => {
				console.error(err);
			}
		});
		this.socketTask.onOpen((res) => {
			console.log("websocket连接正常！");
			this.is_open_socket = true;
			this.clearTimer();
			this.start();
			this.socketTask.onMessage((res) => {
				console.log(res.data)
			});
		})

		this.socketTask.onClose(() => {
			console.log("websocket连接关闭");
			this.is_open_socket = false;
			this.reconnect();
		})
	}

	//发送消息
	send(value, callback) {
		this.socketTask.send({
			data: value,
			success() {
				console.log("消息发送成功", value);
				callback && callback();
			},
		});
	}

	clearTimer() {
		this.reconnectTimeOut && clearTimeout(this.reconnectTimeOut);
		this.heartbeatInterval && clearInterval(this.heartbeatInterval);
	}

	//开启心跳检测
	start() {
		this.heartbeatInterval = setInterval(() => {
			this.data = "心跳检测";
			this.send(this.data);
		}, this.timeout)
	}

	//重连
	reconnect() {
		this.heartbeatInterval && clearInterval(this.heartbeatInterval);
		if (!this.is_open_socket && !this.is_manual_close) {
			this.reconnectTimeOut = setTimeout(() => {
				this.connectSocketInit();
			}, 2000)
		}

		this.is_manual_close = false;
	}

	closeSocket() {
		this.clearTimer();
		if (this.is_open_socket) {
			this.is_manual_close = true;
			uni.closeSocket();
		}
	}

	//外部获取消息
	getMessage(callback) {
		this.socketTask.onMessage((res) => {
			if (res.data == "心跳检测") return;
			return callback(res)
		})
	}

}