var api = require('../../utils/api');

function getGreeting() {
  var h = new Date().getHours();
  if (h < 6) return '夜深了';
  if (h < 9) return '早上好';
  if (h < 12) return '上午好';
  if (h < 14) return '中午好';
  if (h < 18) return '下午好';
  return '晚上好';
}

Page({
  data: {
    greeting: getGreeting(),
    userInfo: null,
    roleHint: '',
    notices: [],
    processes: [],
    hasRoleIssue: false
  },

  onShow() {
    this.loadUserInfo();
    this.loadProcesses();
  },

  loadUserInfo() {
    var userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      var roleHint = '';
      if (userInfo.role === 'ADMIN') {
        roleHint = '当前为管理员账号，仅展示可访问内容';
      }
      this.setData({
        userInfo: userInfo,
        greeting: getGreeting(),
        roleHint: roleHint,
        hasRoleIssue: userInfo.role === 'ADMIN'
      });
    }
  },

  loadProcesses() {
    var that = this;
    api.getProcesses({ status: 'ENABLED', page: 1, size: 50 }).then(function(data) {
      var list = data.items || data.records || [];
      that.setData({ processes: list });
    }).catch(function(err) {
      // 权限不足或网络异常，保持空列表
      if (err && err.message) {
        that.setData({ hasRoleIssue: true });
      }
    });
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
