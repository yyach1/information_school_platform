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
    baseUrl: 'http://localhost:8080/api',
    userInfo: null
  }
});
