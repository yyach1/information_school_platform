-- 将 process_node 的 required_material 从纯文本更新为 JSON 格式
-- JSON 格式：[{"materialType": "材料名", "required": true, "maxSizeMB": 10, "ext": ["pdf","jpg"]}]

-- 入党流程节点
UPDATE process_node SET required_material = '[{"materialType":"入党申请书","required":true,"maxSizeMB":10,"ext":["pdf","doc","jpg","png"]}]'
WHERE process_id IN (SELECT id FROM process WHERE name = '入党申请流程' AND type = 'PARTY') AND node_order = 0;

UPDATE process_node SET required_material = '[{"materialType":"推优表","required":true,"maxSizeMB":10,"ext":["pdf","jpg","png"]}]'
WHERE process_id IN (SELECT id FROM process WHERE name = '入党申请流程' AND type = 'PARTY') AND node_order = 1;

UPDATE process_node SET required_material = '[{"materialType":"入党积极分子登记表","required":true,"maxSizeMB":10,"ext":["pdf","doc","jpg","png"]}]'
WHERE process_id IN (SELECT id FROM process WHERE name = '入党申请流程' AND type = 'PARTY') AND node_order = 2;

UPDATE process_node SET required_material = '[{"materialType":"党校结业证书","required":true,"maxSizeMB":10,"ext":["pdf","jpg","png"]}]'
WHERE process_id IN (SELECT id FROM process WHERE name = '入党申请流程' AND type = 'PARTY') AND node_order = 3;

UPDATE process_node SET required_material = '[{"materialType":"政审材料","required":true,"maxSizeMB":10,"ext":["pdf","doc","jpg"]}]'
WHERE process_id IN (SELECT id FROM process WHERE name = '入党申请流程' AND type = 'PARTY') AND node_order = 4;

UPDATE process_node SET required_material = '[{"materialType":"入党志愿书","required":true,"maxSizeMB":10,"ext":["pdf"]}]'
WHERE process_id IN (SELECT id FROM process WHERE name = '入党申请流程' AND type = 'PARTY') AND node_order = 5;

UPDATE process_node SET required_material = '[{"materialType":"转正申请书","required":true,"maxSizeMB":10,"ext":["pdf","doc"]}]'
WHERE process_id IN (SELECT id FROM process WHERE name = '入党申请流程' AND type = 'PARTY') AND node_order = 6;

-- 入团流程节点
UPDATE process_node SET required_material = '[{"materialType":"入团申请书","required":true,"maxSizeMB":10,"ext":["pdf","doc","jpg"]}]'
WHERE process_id IN (SELECT id FROM process WHERE name = '入团申请流程' AND type = 'LEAGUE') AND node_order = 0;

UPDATE process_node SET required_material = '[{"materialType":"团课学习证明","required":true,"maxSizeMB":10,"ext":["pdf","jpg","png"]}]'
WHERE process_id IN (SELECT id FROM process WHERE name = '入团申请流程' AND type = 'LEAGUE') AND node_order = 1;

UPDATE process_node SET required_material = '[{"materialType":"入团志愿书","required":true,"maxSizeMB":10,"ext":["pdf"]}]'
WHERE process_id IN (SELECT id FROM process WHERE name = '入团申请流程' AND type = 'LEAGUE') AND node_order = 2;

-- node_order 3 和 4 无材料要求（团委审批和入团宣誓）
