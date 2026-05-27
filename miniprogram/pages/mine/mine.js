var api = require('../../utils/api');

var ROLE_LABELS = {
  STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员'
};

Page({
  data: {
    userInfo: null,
    roleLabel: '',
    avatarUrl: '',
    showPwdDialog: false,
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    changingPassword: false
  },

  onShow() {
    var userInfo = wx.getStorageSync('userInfo');
    var avatarUrl = userInfo.avatarUrl || wx.getStorageSync('avatarUrl') || '';
    this.setData({
      userInfo: userInfo || {},
      roleLabel: ROLE_LABELS[(userInfo && userInfo.role)] || (userInfo && userInfo.role) || '',
      avatarUrl: avatarUrl
    });
  },

  onChooseAvatar: function () {
    var that = this;
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: function (res) {
        var tempPath = res.tempFilePaths[0];
        wx.setStorageSync('avatarUrl', tempPath);
        that.setData({ avatarUrl: tempPath });

        wx.showLoading({ title: '上传中...' });
        api.uploadFile(tempPath, 'AVATAR', null, 'avatar.jpg').then(function (data) {
          wx.hideLoading();
          var avatarUrl = data.fileUrl;
          return api.updateAvatar(avatarUrl).then(function () {
            wx.setStorageSync('avatarUrl', avatarUrl);
            var userInfo = wx.getStorageSync('userInfo') || {};
            userInfo.avatarUrl = avatarUrl;
            wx.setStorageSync('userInfo', userInfo);
            that.setData({ avatarUrl: avatarUrl });
            wx.showToast({ title: '头像已更新', icon: 'success' });
          });
        }).catch(function (err) {
          wx.hideLoading();
          wx.showToast({ title: err.message || '上传失败', icon: 'none' });
        });
      }
    });
  },

  openPwdDialog: function () {
    this.setData({
      showPwdDialog: true,
      oldPassword: '', newPassword: '', confirmPassword: ''
    });
  },

  closePwdDialog: function () {
    this.setData({ showPwdDialog: false });
  },

  onOldPwdInput: function (e) { this.setData({ oldPassword: e.detail.value }); },
  onNewPwdInput: function (e) { this.setData({ newPassword: e.detail.value }); },
  onConfirmPwdInput: function (e) { this.setData({ confirmPassword: e.detail.value }); },

  submitPassword: function () {
    var oldPwd = this.data.oldPassword.trim();
    var newPwd = this.data.newPassword.trim();
    var confirm = this.data.confirmPassword.trim();

    if (!oldPwd) { wx.showToast({ title: '请输入旧密码', icon: 'none' }); return; }
    if (!newPwd) { wx.showToast({ title: '请输入新密码', icon: 'none' }); return; }
    if (newPwd.length < 6) { wx.showToast({ title: '新密码至少6位', icon: 'none' }); return; }
    if (newPwd !== confirm) { wx.showToast({ title: '两次密码不一致', icon: 'none' }); return; }

    var that = this;
    this.setData({ changingPassword: true });
    api.changePassword(oldPwd, newPwd).then(function () {
      wx.showToast({ title: '密码修改成功', icon: 'success' });
      that.setData({ showPwdDialog: false, changingPassword: false });
    }).catch(function (err) {
      wx.showToast({ title: err.message || '修改失败', icon: 'none' });
      that.setData({ changingPassword: false });
    });
  },

  handleLogout() {
    var that = this;
    wx.showModal({
      title: '确认退出',
      content: '退出后需要重新登录',
      success: function (res) {
        if (res.confirm) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          wx.reLaunch({ url: '/pages/login/login' });
        }
      }
    });
  }
});
