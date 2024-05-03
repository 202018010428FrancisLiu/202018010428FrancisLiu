<template>
	<view class="container p_relative flex flex-column">
		<view class="userInfo margin-bottom">
			<image class="cu-avatar xl round" width="150rpx" height="150rpx" v-if="avatar" :src="avatar"></image>

			<view v-else class="default_avatar_url">
				<uni-icons color="#ffffff" size="50" type="person-filled" />
			</view>

			<view class="logo-title">
				<text class="uer-name text-bold">{{logindata.username || "XXX"}}</text>
			</view>
		</view>
		<uni-section title="Function List" type="line">
			<uni-list>
				<uni-list-item title="Change password" clickable="true" showArrow @click="openModal" />
			</uni-list>
		</uni-section>
		<view class="flex1 flex align-end padding-lr padding-bottom">
			<button @click="logout" class=" w100 round  " type="warn">logout</button>
		</view>

		<uni-popup class="update_modal" ref="popup" background-color="#fff" borderRadius="10px 10px 10px 10px">
			<view class=" padding text-grey">
				<view class="text-center text-bold text-xl margin-bottom text-black">Change password</view>
				<view>
					<text class="uni-title text-black">New password：</text>
					<uni-easyinput class="uni-mt-5" trim="all" v-model="newPassword"
						placeholder="Please enter a new password"></uni-easyinput>
				</view>
				<view class="flex align-center flex-around margin-top">
					<button class="modal_btn" @click="onCancel">取消</button>
					<button class="modal_btn margin-left" type="primary" @click="confirm">确认</button>
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
				avatar: "https://ts2.cn.mm.bing.net/th?id=OIP-C.jHUH4s7TQ48X_B-1iozuJgHaHa&w=250&h=250&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2",
				logindata: {},
				newPassword: "",
			}
		},
		onLoad() {
			const _this = this;
			uni.getStorage({
				key: 'logindata',
				success: function(res) {
					_this.logindata = JSON.parse(res.data);
				}
			});
		},
		methods: {
			logout() {
				uni.setStorage({
					key: "logindata",
					data: "{}",
					success() {
						uni.redirectTo({
							url: "/pages/index/index"
						})
					}
				})
			},
			openModal() {
				this.$refs.popup.open("center");
			},
			onCancel() {
				this.newPassword = ";"
				this.$refs.popup.close();
			},
			confirm() {
				post("/updateUser", {
					id: this.logindata.id,
					password: this.newPassword
				}).then(({
					data
				}) => {
					this.onCancel();
					this.logout();
				})
			},
			switchChange({
				value
			}) {
				console.log("eeeee", value)
			}
		}
	}
</script>

<style scoped>
	.container {
		width: 100%;
		height: 100%;
		background-color: #fff;
	}

	.userInfo {
		display: flex;
		padding-top: 60px;
		background-color: #62abfb;
		flex-direction: column;
		align-items: center;
	}

	.default_avatar_url {
		width: 150rpx;
		height: 150rpx;
		display: flex;
		background-color: #007aff;
		border-radius: 100%;
		justify-content: center;
		align-items: center;
	}

	.logo-title {
		display: flex;
		flex: 1;
		align-items: center;
		justify-content: space-between;
		flex-direction: row;
	}

	.uer-name {
		height: 100rpx;
		line-height: 100rpx;
		font-size: 38rpx;
		color: #FFFFFF;
	}

	.update_modal :deep(.uni-popup__wrapper) {
		width: 90%;
		border-radius: 10px;
	}

	.modal_btn {
		width: 200rpx;
	}
</style>