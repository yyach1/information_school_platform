var validator = require('../../utils/validator');
var api = require('../../utils/api');

Component({
  properties: {
    requiredList: {
      type: Array,
      value: []
    },
    files: {
      type: Object,
      value: {}
    }
  },

  data: {
    MAX_SIZE: validator.FILE_LIMITS.all.maxSize
  },

  methods: {
    chooseFile(e) {
      var that = this;
      var materialType = e.currentTarget.dataset.type;
      if (!materialType) {
        wx.showToast({ title: '材料类型未知', icon: 'none' });
        return;
      }

      wx.chooseMessageFile({
        count: 1,
        type: 'file',
        success(res) {
          var file = res.tempFiles[0];
          if (!file) {
            wx.showToast({ title: '未选择文件', icon: 'none' });
            return;
          }
          var check = validator.validateFile(file, 'all');
          if (!check.valid) {
            wx.showToast({ title: check.message, icon: 'none' });
            return;
          }

          var files = that.data.files || {};
          files[materialType] = { name: file.name, path: file.path, progress: 0 };
          that.setData({ files: files });

          that.triggerEvent('filechange', {
            materialType: materialType,
            name: file.name,
            path: file.path
          });

          api.uploadFile(file.path, 'MATERIAL').then(function(data) {
            files[materialType].url = data.fileUrl;
            files[materialType].progress = 100;
            that.setData({ files: files });
            that.triggerEvent('uploaddone', {
              materialType: materialType,
              fileUrl: data.fileUrl,
              fileName: file.name
            });
          }).catch(function(err) {
            files[materialType].progress = -1;
            that.setData({ files: files });
            wx.showToast({ title: err.message || '上传失败，请重试', icon: 'none' });
          });
        },
        fail(err) {
          console.log('chooseMessageFile failed:', err);
          if (err.errMsg && err.errMsg.includes('cancel')) {
            // 用户取消选择，不提示
            return;
          }
          wx.showToast({ title: '选择文件失败，请重试', icon: 'none' });
        }
      });
    },

    removeFile(e) {
      var materialType = e.currentTarget.dataset.type;
      var files = this.data.files || {};
      delete files[materialType];
      this.setData({ files: files });
      this.triggerEvent('fileremove', { materialType: materialType });
    }
  }
});
