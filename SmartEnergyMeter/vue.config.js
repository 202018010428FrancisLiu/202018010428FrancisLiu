module.exports = {
	devServer: {
		proxy: {
			'/api': {
				target: 'http://8.137.158.103:2222',
				changeOrigin: false,
				pathRewrite: {
					'^/api': ''
				}
			}
		}
	}
}