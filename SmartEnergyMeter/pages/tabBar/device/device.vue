<template>
	<view class="container w100 h100 bg-white over_y_auto">
		<view class="flex flex-wrap justify-around">
			<view class="charts-box shadow bg-white margin-tb" v-for="(item,index) in chartList" :key="index">
				<l-echart ref="chart"></l-echart>
			</view>
		</view>
	</view>
</template>

<script>
	import * as echarts from 'echarts';
	import websocket from "@/utils/websocket.js";
	export default {
		data() {
			return {
				socket: null,
				chartData: {},
				chartList: [{
						title: 'voltage',
						field: "voltage",
						unit: "V",
						max: 240
					},
					{
						title: 'current',
						field: "current",
						unit: "A",
						max: 1
					},
					{
						title: 'power',
						field: "power",
						unit: "W",
						value: 9,
						max: 9
					},
					{
						title: 'effectivePower',
						field: "effectivePower",
						unit: "W",
						max: 10
					},

				],
				color: ["#1890FF", "#91CB74", "#FAC858", "#EE6666", "#73C0DE", "#3CA272", "#FC8452", "#9A60B4",
					"#ea7ccc"
				],
			}
		},
		mounted() {
			this.chartInit();
		},
		onUnload() {
			this.socket?.closeSocket?.();
		},
		onShow() {
			this.socketInit();
		},
		onHide() {
			this.socket?.closeSocket?.();
		},
		methods: {
			socketInit() {
				this.socket = new websocket(
					`ws://8.137.158.103:2222/indexWebSocket?time=${+new Date()}`,
					20000
				);
				this.socket.getMessage(res => {
					console.warn("消息接收：", res.data);
					this.chartData = JSON.parse(res?.data || "{}");
					console.log(this.chartData);
					this.chartInit();
				})
			},
			chartInit() {
				const _this = this;
				this.$refs?.chart?.map((i, index) => {
					i.init(echarts, chart => {
						const config = this.chartList[index] || {};
						const value = config.value || _this.chartData[config.field] || 0;
						console.log(value);
						const option = {
							series: {
								type: 'gauge',
								radius: "90%",
								// center: ['50%', '60%'],
								// startAngle: 200,
								// endAngle: -20,
								min: 0,
								max: config.max,
								itemStyle: {
									color: '#1890FF'
								},
								progress: {
									show: true,
								},
								pointer: {
									show: false
								},
								axisLine: {
									lineStyle: {}
								},
								axisTick: {
									show: false,
									distance: -45,
									splitNumber: 5,
									lineStyle: {
										color: '#999'
									}
								},
								splitLine: {
									show: false,
									distance: -10,
									lineStyle: {
										color: '#999'
									}
								},
								axisLabel: {
									show: false,
								},
								anchor: {
									show: false
								},
								title: {
									show: false,
								},
								detail: {
									valueAnimation: true,
									borderRadius: 8,
									offsetCenter: [0, 0],
									fontWeight: 'bolder',
									formatter: function() {
										return '{value|' + value.toFixed(2) + '}{unit|' + config
											.unit +
											'}\n{title|' + config.title + '}';
									},
									rich: {
										value: {
											fontSize: 22,
											fontWeight: 'bold',
											color: '#777'
										},
										unit: {
											fontSize: 18,
											color: '#777',
											fontWeight: 'bolder',
											padding: [0, 0, 0, 6]
										},
										title: {
											fontSize: 14,
											color: '#999',
											padding: [20, 0, 0, 0]
										}
									},
									color: '#414141'
								},
								data: [{
									value,
									name: "ceshi"
								}]
							}
						}
						chart.setOption(option);
					});
				})
			},
			numFilter(value) {
				let tempVal = parseFloat(value).toFixed(3)
				let realVal = tempVal.substring(0, tempVal.length - 1)
				return realVal
			},
		}
	}
</script>

<style scoped>
	.container {
		background: url("@/static/6fb644c4f4e03de0d865ad80a775db81b979c3efb2c93-6xiGDZ_fw658webp.webp") no-repeat;
		background-size: cover;
	}

	.charts-box {
		width: 45%;
		height: 300rpx;
		border-radius: 10rpx;

	}
</style>