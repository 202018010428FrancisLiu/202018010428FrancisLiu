let BASE_URL = 'http://8.137.158.103:2222';
const HOST = "/api";
let header = {};
export default class Request {
	http({
		method,
		url,
		params,
		hideLoading = false
	}) {
		url = BASE_URL + HOST + url;
		let data = Object.assign(params || {});

		if (method) {
			header = Object.assign({
				'content-type': "application/json"
			}, {});
		}

		if (!hideLoading) {
			uni.showLoading()
		}

		return new Promise((resolve, reject) => {
			uni.request({
				url,
				data: data,
				method: method,
				header: header,
				success: (res) => {
					if (res && res.statusCode == 200) {
						const data = res.data || {};
						if (data.message) {
							uni.showModal({
								title: 'error',
								content: data.message,
								showCancel: false,
								confirmText: "Ok",
							});
							// uni.showToast({
							// 	icon: "error",
							// 	title: data.message,
							// 	duration: 2000,
							// });
							reject({
								res
							});
							return;
						}
						resolve({
							data: data?.data || {},
							res
						});
						return;
					}
					uni.showModal({
						title: 'error',
						content: 'request was aborted',
						showCancel: false,
						confirmText: "Ok",
					});
					// uni.showToast({
					// 	icon: "error",
					// 	title: 'request was aborted',
					// 	duration: 2000,
					// });
					reject({
						res
					})
				},
				fail: (err) => {
					reject(err)
				},
				complete: () => {
					if (!hideLoading) {
						uni.hideLoading();
					};
				}
			})
		})
	}
}