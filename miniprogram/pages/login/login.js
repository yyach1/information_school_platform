var api = require('../../utils/api');

Page({
  data: {
    username: '',
    password: '',
    loading: false
  },

  onInputUsername(e) {
    this.setData({ username: e.detail.value });
  },

  onInputPassword(e) {
    this.setData({ password: e.detail.value });
  },

  handleLogin() {
    var that = this;
    var username = this.data.username.trim();
    var password = this.data.password;

    if (!username) {
      wx.showToast({ title: '请输入用户名', icon: 'none' });
      return;
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }

    this.setData({ loading: true });

    api.login(username, password).then(function(res) {
      wx.setStorageSync('token', res.token);
      wx.setStorageSync('userInfo', res.userInfo);
      wx.reLaunch({ url: '/pages/index/index' });
    }).catch(function(err) {
      wx.showToast({ title: err.message || '登录失败', icon: 'none' });
    }).finally(function() {
      that.setData({ loading: false });
    });
  }
});
