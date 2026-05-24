var api = require('../../utils/api');

Page({
  data: {
    quickList: [
      '入党流程需要哪些材料？',
      '推优入党的基本条件是什么？',
      '如何申请在学证明？',
      '请假流程怎么办理？',
      '奖学金申请条件有哪些？'
    ],
    messages: [],
    input: ''
  },

  onInput(e) {
    this.setData({ input: e.detail.value });
  },

  quickAsk(e) {
    var question = e.currentTarget.dataset.question;
    this.setData({ messages: [{ role: 'user', content: question }] });
    this.doAsk(question);
  },

  send() {
    var input = this.data.input.trim();
    if (!input) return;

    var messages = this.data.messages;
    messages.push({ role: 'user', content: input });
    this.setData({ messages: messages, input: '' });
    this.doAsk(input);
  },

  doAsk(question) {
    var that = this;
    var messages = that.data.messages;
    messages.push({ role: 'bot', content: '正在查询...' });
    that.setData({ messages: messages });

    api.askQuestion(question).then(function(data) {
      var last = messages.pop();
      messages.push({
        role: 'bot',
        content: data.answer || '未找到相关答案，请联系辅导员或团委老师咨询',
        links: data.links || []
      });
      that.setData({ messages: messages });
    }).catch(function() {
      var last = messages.pop();
      messages.push({
        role: 'bot',
        content: '服务暂时不可用，请稍后重试，或联系辅导员咨询'
      });
      that.setData({ messages: messages });
    });
  },

  openLink(e) {
    var url = e.currentTarget.dataset.url;
    if (url) {
      wx.setClipboardData({ data: url });
      wx.showToast({ title: '链接已复制', icon: 'success' });
    }
  }
});
