var api = require('../../../utils/api');
var validator = require('../../../utils/validator');

Page({
  data: {
    certTypes: [],
    selectedType: '',
    selectedTypeInfo: {},
    title: '',
    description: '',
    attachmentFile: null,
    canSubmit: false
  },

  onShow() {
    var that = this;
    api.getCertificateTypes().then(function(data) {
      that.setData({ certTypes: data.items || data.records || [] });
    }).catch(function() {});
  },

  selectType(e) {
    var type = e.currentTarget.dataset.type;
    var types = this.data.certTypes;
    var info = null;
    for (var i = 0; i < types.length; i++) {
      if (types[i].certType === type) {
        info = types[i];
        break;
      }
    }
    this.setData({
      selectedType: type,
      selectedTypeInfo: info || {}
    });
    this.checkSubmit();
  },

  onTitleInput(e) { this.setData({ title: e.detail.value }); this.checkSubmit(); },
  onDescInput(e) { this.setData({ description: e.detail.value }); },

  chooseFile() {
    var that = this;
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      success(res) {
        var file = res.tempFiles[0];
        var check = validator.validateFile(file, 'all');
        if (!check.valid) {
          wx.showToast({ title: check.message, icon: 'none' });
          return;
        }

        that.setData({ attachmentFile: { name: file.name, path: file.path, progress: 0 } });

        api.uploadFile(file.path, 'CERTIFICATE', null, file.name).then(function(data) {
          that.setData({
            attachmentFile: { name: file.name, url: data.fileUrl, progress: 100 }
          });
          that.checkSubmit();
        }).catch(function() {
          that.setData({ attachmentFile: null });
          wx.showToast({ title: '上传失败', icon: 'none' });
        });
      }
    });
  },

  removeFile() {
    this.setData({ attachmentFile: null });
    this.checkSubmit();
  },

  checkSubmit() {
    var ok = true;
    if (!this.data.selectedType) ok = false;
    if (!this.data.title.trim()) ok = false;
    if (this.data.selectedTypeInfo.requireAttachment) {
      if (!this.data.attachmentFile || this.data.attachmentFile.progress !== 100) {
        ok = false;
      }
    }
    this.setData({ canSubmit: ok });
  },

  submit() {
    if (!this.data.canSubmit) return;
    var that = this;
    wx.showLoading({ title: '提交中...' });

    api.createCertificate({
      certType: this.data.selectedType,
      title: this.data.title.trim(),
      description: this.data.description.trim(),
      attachmentUrl: this.data.attachmentFile ? this.data.attachmentFile.url : null,
      attachmentName: this.data.attachmentFile ? this.data.attachmentFile.name : null
    }).then(function(data) {
      wx.hideLoading();
      wx.showToast({ title: '申请已提交', icon: 'success' });
      setTimeout(function() {
        wx.redirectTo({ url: '/pages/certificate/detail/detail?id=' + data.id });
      }, 1500);
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
    });
  }
});
