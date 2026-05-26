var api = require('../../utils/api');

var ROLE_LABELS = {
  STUDENT: '学生',
  TEACHER: '教师',
  ADMIN: '管理员'
};

Page({
  data: {
    userInfo: null,
    roleLabel: ''
  },

  onShow() {
    var userInfo = wx.getStorageSync('userInfo');
    this.setData({
      userInfo: userInfo || {},
      roleLabel: ROLE_LABELS[(userInfo && userInfo.role)] || userInfo.role || ''
    });
  },

  handleLogout() {
    var that = this;
    wx.showModal({
      title: '确认退出',
      content: '退出后需要重新登录',
      success: function (res) {
        if (res.confirm) {
          api.logout().finally(function () {
            wx.removeStorageSync('token');
            wx.removeStorageSync('userInfo');
            wx.reLaunch({ url: '/pages/login/login' });
          });
        }
      }
    });
  }
});
