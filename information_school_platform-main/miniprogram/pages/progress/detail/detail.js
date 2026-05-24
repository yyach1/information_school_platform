var api = require('../../../utils/api');

Page({
  data: {
    id: '',
    detail: {}
  },

  onLoad(options) {
    this.setData({ id: options.id });
    this.loadDetail();
  },

  loadDetail() {
    var that = this;
    wx.showLoading({ title: '加载中...' });
    api.getProcessDetail(that.data.id).then(function(data) {
      that.setData({ detail: data });
      wx.hideLoading();
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    });
  },

  goUpload() {
    wx.navigateTo({ url: '/pages/upload/upload?processId=' + this.data.id });
  }
});
