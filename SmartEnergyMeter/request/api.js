import Request from './index.js';
let request = new Request().http;


export const get = (url, params, hideLoading) => {
	return request({
		method: "GET",
		url,
		params,
		hideLoading
	})
}

export const post = (url, params, hideLoading) => {
	return request({
		method: "POST",
		url,
		params,
		hideLoading
	})
}