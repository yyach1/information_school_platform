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
          reject(new Error('登录已过期，请重新登录'));
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
  // relatedType: 'MATERIAL' | 'CERTIFICATE' | 'OTHER'
  // relatedId: 业务记录ID（如 certificate.id）
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
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            resolve(data.data);
          } else {
            reject(new Error(data.message || '上传失败'));
          }
        },
        fail(err) {
          wx.showToast({ title: '上传失败', icon: 'none' });
          reject(err);
        }
      });
    });
  },

  // === 智能问答 ===
  askQuestion: (question) => request('POST', '/qa/ask', { question })
};
