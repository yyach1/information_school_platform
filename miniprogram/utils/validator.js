const FILE_LIMITS = {
  image: { ext: ['jpg', 'jpeg', 'png'], maxSize: 10 * 1024 * 1024 },
  document: { ext: ['pdf', 'doc', 'docx'], maxSize: 20 * 1024 * 1024 },
  all: { ext: ['jpg', 'jpeg', 'png', 'pdf', 'doc', 'docx'], maxSize: 20 * 1024 * 1024 }
};

function getExt(name) {
  return (name || '').split('.').pop().toLowerCase();
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + 'B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB';
  return (bytes / (1024 * 1024)).toFixed(1) + 'MB';
}

/**
 * 校验单个文件
 * @param {object} file - 微信 chooseFile/chooseImage 返回的文件对象
 * @param {string} type - 限制类型: 'image' | 'document' | 'all'
 * @returns {{ valid: boolean, message: string }}
 */
function validateFile(file, type) {
  type = type || 'all';
  var limit = FILE_LIMITS[type] || FILE_LIMITS.all;

  var ext = getExt(file.name || file.path);
  if (limit.ext.indexOf(ext) === -1) {
    return { valid: false, message: '不支持的文件格式：' + ext };
  }

  if (file.size > limit.maxSize) {
    return {
      valid: false,
      message: '文件过大（' + formatSize(file.size) + '），上限 ' + formatSize(limit.maxSize)
    };
  }

  return { valid: true, message: '' };
}

/**
 * 校验文件列表，返回第一个错误或 valid
 */
function validateFiles(files, type, requiredFields) {
  if (requiredFields && requiredFields.length > 0) {
    var uploaded = (files || []).map(function (f) { return f.materialType || f.name; });
    for (var i = 0; i < requiredFields.length; i++) {
      var found = false;
      for (var j = 0; j < uploaded.length; j++) {
        if (uploaded[j].indexOf(requiredFields[i]) !== -1) {
          found = true;
          break;
        }
      }
      if (!found) {
        return { valid: false, message: '请上传必传材料：' + requiredFields[i] };
      }
    }
  }

  for (var k = 0; k < (files || []).length; k++) {
    var r = validateFile(files[k], type);
    if (!r.valid) return r;
  }
  return { valid: true, message: '' };
}

module.exports = {
  validateFile: validateFile,
  validateFiles: validateFiles,
  getExt: getExt,
  formatSize: formatSize,
  FILE_LIMITS: FILE_LIMITS
};
