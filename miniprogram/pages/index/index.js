var api = require('../../utils/api');

Page({
  data: {
    userInfo: {},
    notices: [],
    processes: []
  },

  onShow() {
    this.loadUserInfo();
    this.loadProcesses();
  },

  loadUserInfo() {
    var userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({ userInfo: userInfo });
    }
  },

  loadProcesses() {
    var that = this;
    api.getProcesses({ status: 'ENABLED', page: 1, size: 50 }).then(function(data) {
      that.setData({ processes: data.items || data.records || [] });
    }).catch(function() {});
  },

  startProcess(e) {
    var id = e.currentTarget.dataset.id;
    wx.showLoading({ title: '进入中...' });
    var that = this;
    api.getStudentProcesses(id).then(function(data) {
      wx.hideLoading();
      wx.navigateTo({ url: '/pages/progress/detail/detail?id=' + data.id });
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '进入失败', icon: 'none' });
    });
  },

  goNotice(e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/notice/detail?id=' + id });
  },

  goApplyProcess() {
    wx.switchTab({ url: '/pages/progress/progress' });
  },

  goUploadMaterial() {
    wx.navigateTo({ url: '/pages/upload/upload' });
  },

  goApplyCertificate() {
    wx.switchTab({ url: '/pages/certificate/list/list' });
  },

  goQA() {
    wx.switchTab({ url: '/pages/qa/qa' });
  }
});
