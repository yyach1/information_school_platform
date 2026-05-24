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
    baseUrl: 'https://your-server/api/v1',
    userInfo: null
  }
});
