App({
  onLaunch() {
    this.checkLogin();
  },

  checkLogin() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.reLaunch({ url: '/pages/login/login' });
    }
  },

  globalData: {
    baseUrl: 'http://10.10.0.30/api',
    userInfo: null
  }
});
