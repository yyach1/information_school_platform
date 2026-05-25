var api = require('../../utils/api');

Page({
  data: {
    processId: '',
    nodeId: '',
    requiredList: [],
    files: {},
    allRequiredDone: false
  },

  onLoad(options) {
    var processId = options.processId;
    if (!processId) {
      // 如果没有指定流程，引导用户先去党团进度选择
      wx.showModal({
        title: '提示',
        content: '请先在"党团进度"中选择一个流程节点',
        confirmText: '去选择',
        success: function(res) {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/progress/progress' });
          } else {
            wx.navigateBack();
          }
        }
      });
      return;
    }
    this.setData({ processId: processId });
    this.loadRequirements(processId);
  },

  loadRequirements(processId) {
    var that = this;
    wx.showLoading({ title: '加载中...' });
    api.getNodeRequirements(processId).then(function(data) {
      that.setData({
        nodeId: data.nodeId,
        requiredList: data.requiredMaterial || []
      });
      wx.hideLoading();
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: err.message, icon: 'none' });
    });
  },

  onFileChange(e) {},
  onFileRemove(e) {},

  onUploadDone(e) {
    this.setData({
      files: this.data.files
    });
    this.checkAllDone();
  },

  checkAllDone() {
    var required = this.data.requiredList.filter(function(item) {
      return item.required;
    });
    var files = this.data.files;
    var done = required.every(function(item) {
      var f = files[item.materialType];
      return f && f.progress === 100;
    });
    this.setData({ allRequiredDone: done });
  },

  submitAll() {
    if (!this.data.allRequiredDone) {
      wx.showToast({ title: '请上传所有必传材料', icon: 'none' });
      return;
    }
    var that = this;
    wx.showLoading({ title: '提交中...' });
    var files = this.data.files;
    var required = this.data.requiredList;

    var promises = required.filter(function(item) {
      return files[item.materialType] && files[item.materialType].url;
    }).map(function(item) {
      var f = files[item.materialType];
      return api.submitMaterial(that.data.processId, {
        nodeId: that.data.nodeId,
        materialType: item.materialType,
        fileUrl: f.url,
        fileName: f.name
      });
    });

    Promise.all(promises).then(function() {
      wx.hideLoading();
      wx.showToast({ title: '提交成功', icon: 'success' });
      setTimeout(function() { wx.navigateBack(); }, 1500);
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
    });
  }
});
