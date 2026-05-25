const BASE_URL = getApp().globalData.baseUrl;

function request(method, url, data) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    wx.request({
      method,
      url: BASE_URL + url,
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success(res) {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data.data);
        } else if (res.statusCode === 401) {
          wx.removeStorageSync('token');
          wx.reLaunch({ url: '/pages/login/login' });
          reject(new Error('登录已过期'));
        } else {
          reject(new Error(res.data.message || '请求失败'));
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常', icon: 'none' });
        reject(err);
      }
    });
  });
}

module.exports = {
  get: (url, params) => request('GET', url, params),
  post: (url, data) => request('POST', url, data),
  put: (url, data) => request('PUT', url, data),

  // === 登录 ===
  login: (username, password) => {
    return new Promise((resolve, reject) => {
      wx.request({
        method: 'POST',
        url: BASE_URL + '/auth/login',
        data: { username, password },
        header: { 'Content-Type': 'application/json' },
        success(res) {
          if (res.statusCode === 200 && res.data.code === 200) {
            resolve(res.data.data);
          } else {
            reject(new Error(res.data.message || '登录失败'));
          }
        },
        fail() {
          wx.showToast({ title: '网络异常', icon: 'none' });
          reject(new Error('网络异常'));
        }
      });
    });
  },

  // === 成员A接口 ===
  getProcesses: (params) => request('GET', '/student/processes', params),
  getStudentProcesses: (processId) =>
    request('POST', '/student/student-processes', { processId }),
  getProcessDetail: (id) => request('GET', `/student/student-processes/${id}`),
  getNodeRequirements: (id) =>
    request('GET', `/student/student-processes/${id}/current-node/requirements`),
  submitMaterial: (processId, data) =>
    request('POST', `/student/student-processes/${processId}/materials`, data),
  getTimeline: (id) =>
    request('GET', `/student/student-processes/${id}/timeline`),

  // === 电子证明接口（成员B） ===
  getCertificateTypes: () => request('GET', '/student/certificates/types'),
  createCertificate: (data) => request('POST', '/student/certificates', data),
  getCertificates: (params) => request('GET', '/student/certificates', params),
  getCertificateDetail: (id) => request('GET', `/student/certificates/${id}`),
  updateCertificate: (id, data) => request('PUT', `/student/certificates/${id}`, data),
  downloadCertificate: (id) =>
    request('GET', `/student/certificates/${id}/download`),

  // === 文件上传（成员D提供）===
  uploadFile: (filePath, relatedType, relatedId) => {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token');
      const formData = {};
      if (relatedType) formData.relatedType = relatedType;
      if (relatedId) formData.relatedId = String(relatedId);

      wx.uploadFile({
        url: BASE_URL + '/files/upload',
        filePath,
        name: 'file',
        formData: Object.keys(formData).length ? formData : undefined,
        header: {
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success(res) {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200) {
              resolve(data.data);
            } else {
              reject(new Error(data.message || '上传失败'));
            }
          } catch (e) {
            reject(new Error('服务器响应异常，状态码：' + res.statusCode));
          }
        },
        fail(err) {
          var msg = '上传失败';
          if (err.errMsg) {
            msg = err.errMsg.includes('timeout') ? '上传超时，请重试' : '上传失败，请检查网络';
          }
          wx.showToast({ title: msg, icon: 'none' });
          reject(err);
        }
      });
    });
  },

  // === 智能问答 ===
  askQuestion: (question) => request('POST', '/qa/ask', { question })
};
