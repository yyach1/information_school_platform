-- 插入流程模板（幂等：同名同类型不重复插入）
INSERT INTO process (name, type, description, status)
SELECT '入党申请流程', 'PARTY', '共青团员推优入党，需经团支部推荐、党支部考察、党委审批等环节', 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM process WHERE name = '入党申请流程' AND type = 'PARTY');

INSERT INTO process (name, type, description, status)
SELECT '入团申请流程', 'LEAGUE', '青年学生申请加入中国共产主义青年团的标准流程', 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM process WHERE name = '入团申请流程' AND type = 'LEAGUE');

-- 入党流程节点（幂等：用子查询获取正确的 process_id）
INSERT INTO process_node (process_id, node_name, node_order, required_material, approver_role)
SELECT p.id, v.node_name, v.node_order, v.required_material, v.approver_role
FROM (VALUES
  ('提交入党申请书', 0, '[{"materialType":"入党申请书","required":true,"maxSizeMB":10,"ext":["pdf","doc","jpg","png"]}]', 'COUNSELOR'),
  ('团支部推优', 1, '[{"materialType":"推优表","required":true,"maxSizeMB":10,"ext":["pdf","jpg","png"]}]', 'LEAGUE_SECRETARY'),
  ('入党积极分子确定', 2, '[{"materialType":"入党积极分子登记表","required":true,"maxSizeMB":10,"ext":["pdf","doc","jpg","png"]}]', 'COUNSELOR'),
  ('党校培训', 3, '[{"materialType":"党校结业证书","required":true,"maxSizeMB":10,"ext":["pdf","jpg","png"]}]', 'COUNSELOR'),
  ('政治审查', 4, '[{"materialType":"政审材料","required":true,"maxSizeMB":10,"ext":["pdf","doc","jpg"]}]', 'COUNSELOR'),
  ('预备党员接收', 5, '[{"materialType":"入党志愿书","required":true,"maxSizeMB":10,"ext":["pdf"]}]', 'COUNSELOR'),
  ('预备党员转正', 6, '[{"materialType":"转正申请书","required":true,"maxSizeMB":10,"ext":["pdf","doc"]}]', 'COUNSELOR')
) AS v(node_name, node_order, required_material, approver_role)
JOIN process p ON p.name = '入党申请流程' AND p.type = 'PARTY'
WHERE NOT EXISTS (
  SELECT 1 FROM process_node pn
  WHERE pn.process_id = p.id AND pn.node_order = v.node_order
);

-- 入团流程节点
INSERT INTO process_node (process_id, node_name, node_order, required_material, approver_role)
SELECT p.id, v.node_name, v.node_order, v.required_material, v.approver_role
FROM (VALUES
  ('提交入团申请书', 0, '[{"materialType":"入团申请书","required":true,"maxSizeMB":10,"ext":["pdf","doc","jpg"]}]', 'LEAGUE_SECRETARY'),
  ('团课学习', 1, '[{"materialType":"团课学习证明","required":true,"maxSizeMB":10,"ext":["pdf","jpg","png"]}]', 'LEAGUE_SECRETARY'),
  ('团支部讨论', 2, '[{"materialType":"入团志愿书","required":true,"maxSizeMB":10,"ext":["pdf"]}]', 'LEAGUE_SECRETARY'),
  ('团委审批', 3, null, 'LEAGUE_SECRETARY'),
  ('入团宣誓', 4, null, 'LEAGUE_SECRETARY')
) AS v(node_name, node_order, required_material, approver_role)
JOIN process p ON p.name = '入团申请流程' AND p.type = 'LEAGUE'
WHERE NOT EXISTS (
  SELECT 1 FROM process_node pn
  WHERE pn.process_id = p.id AND pn.node_order = v.node_order
);
