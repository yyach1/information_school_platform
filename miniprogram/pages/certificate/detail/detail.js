var api = require('../../../utils/api');

var CERT_TYPE_LABELS = {
  ENROLLMENT: '在学证明',
  LEAVE: '请假申请',
  SEAL: '盖章申请',
  PARTY: '党员证明',
  TRANSCRIPT: '成绩单证明'
};

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
    api.getCertificateDetail(that.data.id).then(function(data) {
      data.certTypeLabel = CERT_TYPE_LABELS[data.certType] || data.certType;
      that.setData({ detail: data });
      wx.hideLoading();
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    });
  },

  previewFile() {
    var url = this.data.detail.attachmentUrl;
    if (!url) return;
    wx.showLoading({ title: '下载中...' });
    api.downloadFile(url).then(function(res) {
      wx.hideLoading();
      wx.openDocument({ filePath: res.tempFilePath });
    }).catch(function() {
      wx.hideLoading();
      wx.showToast({ title: '文件预览失败', icon: 'none' });
    });
  },

  downloadPdf() {
    var that = this;
    wx.showLoading({ title: '获取下载链接...' });
    api.downloadCertificate(this.data.id).then(function(data) {
      if (!data.pdfUrl) {
        wx.hideLoading();
        wx.showToast({ title: 'PDF尚未生成', icon: 'none' });
        return;
      }
      return api.downloadFile(data.pdfUrl);
    }).then(function(res) {
      wx.hideLoading();
      if (res) {
        wx.openDocument({ filePath: res.tempFilePath });
      }
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '下载失败', icon: 'none' });
    });
  },

  resubmit() {
    wx.navigateTo({
      url: '/pages/certificate/apply/apply?resubmit=' + this.data.id
    });
  }
});
