<template>
	<view class="container w100 h100">
		<view class="login_form bg-white radius padding-lr-xl padding-tb-xl margin-lr ">
			<view class="text-xl text-bold text-center margin-bottom">Welcome to login</view>
			<uni-forms :modelValue="formData">
				<uni-forms-item label="UserName" name="username">
					<uni-easyinput type="text" v-model="formData.username" placeholder="Please enter username" />
				</uni-forms-item>
				<uni-forms-item label="Password" name="password">
					<uni-easyinput type="text" v-model="formData.password" placeholder="Please enter password" />
				</uni-forms-item>
			</uni-forms>
			<button type="primary" @click="submitForm">{{ isLogin ? "login" : "register" }}</button>
			<view class=" text-center margin-top">
				<text class="text-gray" @click="toSwitch">{{ isLogin ? "register" : "login" }}</text>
			</view>
		</view>
	</view>
</template>

<script>
	import {
		post
	} from "@/request/api.js";
	export default {
		data() {
			return {
				isLogin: true,
				formData: {
					username: "",
					password: ""
				}
			}
		},
		onLoad() {

		},
		methods: {
			toSwitch() {
				this.isLogin = !this.isLogin;
				this.formData = {
					username: "",
					password: ""
				};
			},
			submitForm() {
				if (!this.formData.username || !this.formData.password) {
					uni.showModal({
						title: 'error',
						content: 'Form Incomplete',
						showCancel: false,
						confirmText: "Ok",
					});
					// uni.showToast({
					// 	icon: "error",
					// 	title: 'Form Incomplete',
					// 	duration: 2000,
					// });
					return;
				}

				post(this.isLogin ? "/login" : "/register", this.formData).then(({
					data,
				}) => {
					uni.setStorage({
						key: "logindata",
						data: JSON.stringify(data || {}),
						success() {
							uni.switchTab({
								url: "/pages/tabBar/device/device"
							})
						}
					})

				})
			}
		}
	}
</script>

<style scoped>
	.container {
		display: flex;
		flex-direction: column;
		justify-content: center;
		background: url("@/static/login_bg.jpg") no-repeat;
		background-size: 100% 100%;
	}

	.login_form {}
</style>