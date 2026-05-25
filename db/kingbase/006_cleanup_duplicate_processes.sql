-- 清理重复的流程模板和关联节点
-- 保留每组 (name, type) 中 id 最小的那条

-- 先删除重复流程的关联节点（保留最早的那条流程的节点）
DELETE FROM process_node
WHERE process_id IN (
  SELECT id FROM process
  WHERE (name, type) IN (
    SELECT name, type FROM process
    GROUP BY name, type
    HAVING COUNT(*) > 1
  )
  AND id NOT IN (
    SELECT MIN(id) FROM process
    GROUP BY name, type
    HAVING COUNT(*) > 1
  )
);

-- 再删除重复的流程模板（保留 id 最小的）
DELETE FROM process
WHERE id NOT IN (
  SELECT MIN(id) FROM process
  GROUP BY name, type
);
