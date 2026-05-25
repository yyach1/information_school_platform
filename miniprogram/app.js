App({
  onLaunch() {
    this.checkLogin();
  },

  checkLogin() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.reLaunch({ url: '/pages/index/index' });
    }
  },

  globalData: {
    baseUrl: 'http://localhost:8080/api',
    userInfo: null
  }
});
