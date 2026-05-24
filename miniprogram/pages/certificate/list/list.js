var api = require('../../../utils/api');

Page({
  data: {
    list: [],
    loading: true,
    statusOptions: [
      { value: '', label: '全部状态' },
      { value: 'PENDING', label: '待审核' },
      { value: 'APPROVED', label: '已通过' },
      { value: 'RETURNED', label: '已退回' },
      { value: 'ISSUED', label: '已发放' }
    ],
    typeOptions: [
      { value: '', label: '全部类型' },
      { value: 'ENROLLMENT', label: '在学证明' },
      { value: 'LEAVE', label: '请假申请' },
      { value: 'SEAL', label: '盖章申请' },
      { value: 'PARTY', label: '党员证明' },
      { value: 'TRANSCRIPT', label: '成绩单证明' }
    ],
    activeStatus: '',
    activeType: '',
    activeStatusLabel: '',
    activeTypeLabel: ''
  },

  onShow() {
    this.loadList();
  },

  loadList() {
    var that = this;
    this.setData({ loading: true });
    var params = { page: 1, size: 50 };
    if (this.data.activeStatus) params.status = this.data.activeStatus;
    if (this.data.activeType) params.certType = this.data.activeType;

    api.getCertificates(params).then(function(data) {
      that.setData({ list: data.items || [], loading: false });
    }).catch(function() {
      that.setData({ loading: false });
    });
  },

  onStatusFilter(e) {
    var idx = e.detail.value;
    var opt = this.data.statusOptions[idx];
    this.setData({ activeStatus: opt.value, activeStatusLabel: opt.value ? opt.label : '' });
    this.loadList();
  },

  onTypeFilter(e) {
    var idx = e.detail.value;
    var opt = this.data.typeOptions[idx];
    this.setData({ activeType: opt.value, activeTypeLabel: opt.value ? opt.label : '' });
    this.loadList();
  },

  goDetail(e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/certificate/detail/detail?id=' + id });
  },

  goApply() {
    wx.navigateTo({ url: '/pages/certificate/apply/apply' });
  }
});
