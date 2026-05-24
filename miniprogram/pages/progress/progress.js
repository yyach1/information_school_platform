var api = require('../../utils/api');

Page({
  data: {
    processes: [],
    availableProcesses: [],
    loading: true
  },

  onShow() {
    this.loadAll();
  },

  loadAll() {
    var that = this;
    this.setData({ loading: true });

    api.getProcesses({ status: 'ENABLED', page: 1, size: 50 }).then(function(data) {
      that.setData({ availableProcesses: data.items || [] });
    }).catch(function() {});

    // 加载已参与的流程 - 先请求可用流程，逐个查询进度
    // 实际联调时改为成员A提供的列表接口
    this.setData({ loading: false });
  },

  goDetail(e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/progress/detail/detail?id=' + id });
  },

  startProcess(e) {
    var processId = e.currentTarget.dataset.id;
    wx.showLoading({ title: '发起中...' });
    var that = this;
    api.getStudentProcesses(processId).then(function(data) {
      wx.hideLoading();
      wx.showToast({ title: '发起成功', icon: 'success' });
      wx.navigateTo({ url: '/pages/progress/detail/detail?id=' + data.id });
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '发起失败', icon: 'none' });
    });
  }
});
