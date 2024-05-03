<template>
	<view class="container w100 h100 flex flex-column bg-white over_y_auto">
		<uni-section class="solid-top" title="time frame" type="line">
			<template v-slot:right>
				<view class="flex align-center">
					<view class="margin-right text-grey">{{ time }}</view>
					<button class="mini-btn" type="primary" size="mini" plain="true" @click="toPreview">view
						formula</button>
				</view>
			</template>
		</uni-section>
		<view><uni-datetime-picker v-model="datetimerange" type="datetimerange" :clear-icon="false"
				@change="dateChange" /></view>
		<view class="padding-lr flex flex-wrap justify-between">
			<view :class="`box-sizing padding shadow margin-top radius  bg-${i.color}`" style="width: 48%;"
				v-for="(i,index) in fieldList" :key="index">
				<view class="text-bold text-lg">{{i.label}}</view>
				<view class="text-right text-bold text-xxl ellipsis">{{ numFilter(searchData[i.field] || 0) }}
					<text v-if="i.unit" class="text-df">( {{ i.unit || "" }} )</text>
				</view>
			</view>
		</view>

		<uni-popup class="formula_modal" ref="popup" background-color="#fff" borderRadius="10px 10px 10px 10px">
			<view class=" padding text-grey">
				<view class="text-center text-bold text-xl margin-bottom text-black">Calculation formula</view>
				<view class="view margin-bottom" v-for="(i,index) in formulaList" :key="index">
					<uni-section class="modal_head" :title="i.title" :sub-title="i.content">
						<template v-slot:decoration>
							<view class="decoration"></view>
						</template>
					</uni-section>
				</view>
			</view>
		</uni-popup>

	</view>
</template>

<script>
	import {
		post
	} from '../../../request/api';
	export default {
		data() {
			return {
				time: new Date().FormatDateTime(),
				timer: null,
				searchData: {},
				datetimerange: [new Date().FormatDate() + " 00:00", new Date().FormatDate() + " 23:59"],
				formulaList: [{
						title: "Energy Consumption Calculation",
						content: "Energy Consumption（E）= voltage（V） × current（I） × Usage time（t）",
					},
					{
						title: "Energy efficiency ratio calculation",
						content: "Energy efficiency ratio（Efficiency）= Effective power（P） / Input power（W）"
					},
					{
						title: "Power factor calculation",
						content: "Power factor（Power Factor）= Work hard（P） / Apparent power（S）"
					},
					{
						title: "Apparent power calculation",
						content: "Apparent power（S）= voltage（V） × current（I）"
					},
					{
						title: "Calorific value calculation",
						content: "Calorific value（Heat）= current（I）^2 × resistance（R）"
					},
				],
				fieldList: [{
						label: 'Power factor',
						field: 'powerFactor',
						color: 'blue',
						unit: 'W'
					},
					{
						label: 'Apparent power',
						field: 'apparentPower',
						color: 'blue',
						unit: 'W'
					},
					{
						label: 'Average effective power',
						field: 'effectivePower',
						color: 'blue',
						unit: 'W'
					},
					{
						label: 'Electricity consumption',
						field: 'electricityConsumed',
						color: 'orange',
						unit: 'kWh'
					},
					{
						label: 'Average voltage',
						field: 'voltage',
						color: 'orange',
						unit: "V"
					},
					{
						label: 'Average current',
						field: 'current',
						color: 'orange',
						unit: "A"
					},
					// {
					// 	label: 'Energy consumption',
					// 	field: 'energyConsumption',
					// 	color: 'red',
					// 	unit: 'kWh'
					// },
					{
						label: 'Run time',
						field: 'time',
						color: 'olive',
						unit: 'S'
					},
				]

			}
		},
		mounted() {},
		onShow() {
			this.time = new Date().FormatDateTime();
			this.timer = setInterval(() => {
				this.time = new Date().FormatDateTime();
			}, 1000)
			this.getData();
		},
		onHide() {
			uni.stopPullDownRefresh();
			this.timer && clearInterval(this.timer);
		},
		onPullDownRefresh() {
			this.getData();
		},
		methods: {
			numFilter(value) {
				let tempVal = parseFloat(value).toFixed(3)
				let realVal = tempVal.substring(0, tempVal.length - 1)
				return realVal
			},
			dateChange(val) {
				this.getData()
			},
			toPreview() {
				this.$refs.popup.open("center");
			},
			getData() {
				post("/getHistory", {
					startTime: this.datetimerange[0],
					endTime: this.datetimerange[1]
				}).then(({
					data
				}) => {
					console.log(data);
					this.searchData = data || {};
					uni.stopPullDownRefresh();
				}).catch(err => {
					uni.stopPullDownRefresh();
				})
			}
		}
	}
</script>

<style scoped>
	.formula_modal :deep(.uni-popup__wrapper) {
		max-width: 95%;
		border-radius: 10px;
	}

	.modal_head :deep(.uni-section-header) {
		align-items: baseline;
	}

	.decoration {
		width: 6px;
		height: 6px;
		margin-right: 4px;
		border-radius: 50%;
		background-color: #39b54a;
	}
</style>