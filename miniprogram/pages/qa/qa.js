var api = require('../../utils/api');

// 本地知识库（后续可由管理端动态更新）
var KNOWLEDGE_BASE = [
  {
    keywords: ['入党', '流程', '材料'],
    answer: '入党基本流程：\n1. 提交入党申请书\n2. 经团支部推优成为入党积极分子（需提交思想汇报）\n3. 经考察成为发展对象\n4. 党支部大会讨论接收为预备党员\n5. 预备期满转正成为正式党员\n\n各阶段材料要求请查看"我的党团进度"页面。',
    links: [{ title: '查看我的党团进度', url: '/pages/progress/progress' }]
  },
  {
    keywords: ['推优入党', '条件', '基本'],
    answer: '推优入党基本条件：\n1. 年满18周岁，已递交入党申请书\n2. 团龄满1年以上\n3. 学习成绩优良，无不及格科目\n4. 无违纪违规记录\n5. 积极参加团组织活动和志愿服务\n\n具体标准请咨询辅导员。'
  },
  {
    keywords: ['在学证明', '申请', '证明'],
    answer: '在学证明办理流程：\n1. 进入"我的证明"页面\n2. 选择"在学证明"类型\n3. 填写申请信息并上传相关附件\n4. 提交后等待班主任审核\n5. 审核通过后可在详情页下载 PDF 证明\n\n如需盖章，请审核通过后至学院办公室盖章。',
    links: [{ title: '前往申请证明', url: '/pages/certificate/list/list' }]
  },
  {
    keywords: ['请假', '请假申请', '请假条'],
    answer: '请假流程：\n1. 进入"我的证明"页面选择"请假申请"\n2. 填写请假原因、起止时间\n3. 学生请假一般由班主任审批，辅导员备案\n4. 3天以上需辅导员+学院领导审批\n\n请假申请审核通过后可下载请假条。',
    links: [{ title: '申请请假', url: '/pages/certificate/apply/apply' }]
  },
  {
    keywords: ['奖学金', '奖助学金', '申请条件'],
    answer: '奖助学金一般申请条件：\n1. 家庭经济困难学生（需提供证明材料）\n2. 学习成绩优良\n3. 无违纪记录\n\n各类奖学金具体申请时间和材料要求请关注学院通知，或咨询辅导员、学工办。'
  },
  {
    keywords: ['成绩单', '成绩', '绩点'],
    answer: '成绩单办理：\n1. 进入"我的证明"页面选择"成绩单证明"\n2. 系统将自动拉取你的学籍和成绩信息\n3. 提交后等待审核\n4. 审核通过后可下载正式成绩单\n\n如有疑问请联系教务处或辅导员。'
  },
  {
    keywords: ['思想汇报', '提交', '积极分子'],
    answer: '思想汇报要求：\n1. 入党积极分子一般每3个月提交一篇思想汇报\n2. 内容应结合时事和学习生活实际\n3. 在"我的党团进度"页面选择对应节点上传\n\n逾期未交可能影响入党进度，请关注系统提醒。'
  },
  {
    keywords: ['党员证明', '政治面貌'],
    answer: '政治面貌证明（党员证明）办理：\n1. 进入"我的证明"页面选择"党员证明"\n2. 系统将根据数据库中的政治面貌信息自动生成\n3. 提交后等待审核\n4. 审核通过后可下载正式证明。',
    links: [{ title: '前往申请证明', url: '/pages/certificate/list/list' }]
  }
];

function matchAnswer(question) {
  var q = question.toLowerCase();
  var best = null;
  var bestScore = 0;

  for (var i = 0; i < KNOWLEDGE_BASE.length; i++) {
    var item = KNOWLEDGE_BASE[i];
    var score = 0;
    for (var j = 0; j < item.keywords.length; j++) {
      if (q.indexOf(item.keywords[j].toLowerCase()) !== -1) {
        score += 1;
      }
    }
    if (score > bestScore) {
      bestScore = score;
      best = item;
    }
  }

  if (bestScore > 0) {
    return { answer: best.answer, links: best.links || [] };
  }
  return null;
}

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
    var result = matchAnswer(question);

    // 模拟延迟，给用户反馈感
    setTimeout(function() {
      if (result) {
        that.data.messages.push({
          role: 'bot',
          content: result.answer,
          links: result.links || []
        });
      } else {
        that.data.messages.push({
          role: 'bot',
          content: '我暂时无法回答这个问题。建议：\n1. 尝试使用不同关键词搜索\n2. 联系辅导员或团委老师咨询\n3. 查看学院官网通知公告'
        });
      }
      that.setData({ messages: that.data.messages });
    }, 600);
  },

  openLink(e) {
    var url = e.currentTarget.dataset.url;
    if (!url) return;
    // 如果是内部页面路径，使用 navigateTo
    if (url.indexOf('/pages/') === 0) {
      wx.navigateTo({ url: url });
      return;
    }
    wx.setClipboardData({ data: url });
    wx.showToast({ title: '链接已复制', icon: 'success' });
  }
});
