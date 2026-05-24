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
    if (url) {
      wx.downloadFile({
        url: url,
        success: function(res) {
          wx.openDocument({ filePath: res.tempFilePath });
        }
      });
    }
  },

  downloadPdf() {
    var that = this;
    api.downloadCertificate(this.data.id).then(function(data) {
      if (data.pdfUrl) {
        wx.downloadFile({
          url: data.pdfUrl,
          success: function(res) {
            wx.openDocument({ filePath: res.tempFilePath });
          }
        });
      }
    }).catch(function(err) {
      wx.showToast({ title: err.message, icon: 'none' });
    });
  },

  resubmit() {
    wx.navigateTo({
      url: '/pages/certificate/apply/apply?resubmit=' + this.data.id
    });
  }
});
