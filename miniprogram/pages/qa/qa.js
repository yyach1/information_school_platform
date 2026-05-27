var KNOWLEDGE_BASE = [
  {
    keywords: ['入党', '流程', '材料'],
    answer: '入党基本流程：\n1. 提交入党申请书\n2. 团支部推优成为入党积极分子\n3. 经考察成为发展对象\n4. 党支部大会讨论接收为预备党员\n5. 预备期满转正\n\n各阶段材料要求请查看"我的党团进度"。',
    links: [{ title: '查看党团进度', url: '/pages/progress/progress' }]
  },
  {
    keywords: ['推优入党', '条件', '基本'],
    answer: '推优入党基本条件：\n1. 年满18周岁，已递交入党申请书\n2. 团龄满1年以上\n3. 学习成绩优良，无不及格科目\n4. 无违纪违规记录\n5. 积极参加团组织活动和志愿服务\n\n具体标准请咨询辅导员。'
  },
  {
    keywords: ['在学证明', '证明', '就读'],
    answer: '在学证明办理：\n1. 进入"我的证明"→ 选择"在学证明"\n2. 填写申请信息\n3. 提交后等待审核\n4. 审核通过后可下载 PDF\n\n如需盖章，请至学院办公室。',
    links: [{ title: '办理证明', url: '/pages/certificate/list/list' }]
  },
  {
    keywords: ['请假', '请假条', '请假申请'],
    answer: '请假流程：\n1. 进入"我的证明"→ 选择"请假申请"\n2. 填写请假原因和起止时间\n3. 班主任审批，辅导员备案\n4. 3天以上需学院领导审批\n\n审核通过后可下载请假条。',
    links: [{ title: '申请请假', url: '/pages/certificate/apply/apply' }]
  },
  {
    keywords: ['奖学金', '奖助学金', '申请'],
    answer: '奖助学金申请条件：\n1. 家庭经济困难（需提供证明材料）\n2. 学习成绩优良\n3. 无违纪记录\n\n具体申请时间和材料请关注学院通知或咨询辅导员。'
  },
  {
    keywords: ['成绩单', '成绩', '绩点'],
    answer: '成绩单办理：\n1. 进入"我的证明"→ 选择"成绩单证明"\n2. 填写申请信息\n3. 提交后等待审核\n4. 审核通过后可下载正式成绩单。\n\n如有疑问请联系教务处。',
    links: [{ title: '办理证明', url: '/pages/certificate/list/list' }]
  },
  {
    keywords: ['思想汇报', '提交', '积极分子'],
    answer: '思想汇报要求：\n1. 入党积极分子一般每3个月提交一篇\n2. 内容应结合时事和学习生活实际\n3. 在"我的党团进度"中选择对应节点上传\n\n逾期未交可能影响入党进度。'
  },
  {
    keywords: ['党员证明', '政治面貌'],
    answer: '党员证明办理：\n1. 进入"我的证明"→ 选择"党员证明"\n2. 系统根据数据库信息自动生成\n3. 提交后等待审核\n4. 审核通过后可下载正式证明。',
    links: [{ title: '办理证明', url: '/pages/certificate/list/list' }]
  },
  {
    keywords: ['密码', '修改密码', '重置密码', '忘记密码'],
    answer: '修改密码：\n1. 学生请使用PC端管理后台（联系辅导员）\n2. 教师/管理员可登录管理端自行修改\n\n如遗忘密码，请联系系统管理员重置。'
  },
  {
    keywords: ['毕业', '离校', '退学'],
    answer: '与毕业、离校、退学相关的事项：\n1. 请先到学院办公室领取表格\n2. 办理离校手续需各部门签字盖章\n3. 详情请咨询辅导员或学工办。'
  },
  {
    keywords: ['你好', '帮助', '能用', '功能', '可以做什么'],
    answer: '你好！我是学院服务平台智能助手，可以解答以下问题：\n• 党团流程与材料要求\n• 证明申请与办理流程\n• 奖学金申请条件\n• 成绩单办理\n• 请假流程\n\n请直接输入问题，或点击下方快捷提问。'
  }
];

function matchAnswer(question) {
  var q = question.toLowerCase().replace(/\s+/g, '');
  var best = null;
  var bestScore = 0;

  for (var i = 0; i < KNOWLEDGE_BASE.length; i++) {
    var item = KNOWLEDGE_BASE[i];
    var score = 0;
    for (var j = 0; j < item.keywords.length; j++) {
      if (q.indexOf(item.keywords[j].toLowerCase()) !== -1) score += 1;
    }
    // 越长的问题匹配到越多关键词
    if (q.length < 15) score = score / 2;
    if (score > bestScore) {
      bestScore = score;
      best = item;
    }
  }

  return bestScore > 0 ? { answer: best.answer, links: best.links || [] } : null;
}

Page({
  data: {
    quickList: [
      '入党流程需要哪些材料？',
      '推优入党的基本条件是什么？',
      '如何申请在学证明？',
      '请假流程怎么办理？',
      '奖学金申请条件有哪些？',
      '怎么修改密码？'
    ],
    messages: [],
    input: ''
  },

  clearChat: function () {
    this.setData({ messages: [], input: '' });
  },

  onInput: function (e) {
    this.setData({ input: e.detail.value });
  },

  quickAsk: function (e) {
    var question = e.currentTarget.dataset.question;
    this.setData({ messages: [{ role: 'user', content: question }] });
    this.doAsk(question);
  },

  send: function () {
    var input = this.data.input.trim();
    if (!input) return;
    var messages = this.data.messages.slice();
    messages.push({ role: 'user', content: input });
    this.setData({ messages: messages, input: '' });
    this.doAsk(input);
  },

  doAsk: function (question) {
    var that = this;
    var messages = that.data.messages.slice();

    // 添加思考状态
    messages.push({ role: 'bot', content: '', thinking: true });
    that.setData({ messages: messages });

    setTimeout(function () {
      var result = matchAnswer(question);
      messages.pop(); // 移除思考气泡

      if (result) {
        messages.push({
          role: 'bot',
          content: result.answer,
          links: result.links || []
        });
      } else {
        messages.push({
          role: 'bot',
          content: '抱歉，我暂时无法回答这个问题。\n\n建议：\n• 尝试换一种说法或更简短的关键词\n• 联系辅导员或团委老师咨询\n• 查看学院官网通知公告'
        });
      }
      that.setData({ messages: messages });
    }, 500);
  },

  openLink: function (e) {
    var url = e.currentTarget.dataset.url;
    if (!url) return;
    if (url.indexOf('/pages/') === 0) {
      wx.navigateTo({ url: url });
      return;
    }
    wx.setClipboardData({ data: url, success: function () { wx.showToast({ title: '已复制链接', icon: 'success' }); } });
  }
});
