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
      var extStr = e.currentTarget.dataset.ext || '';
      var allowedExt = extStr.split(',').filter(Boolean);

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
            wx.showToast({ title: '上传失败，请重试', icon: 'none' });
          });
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
