BEGIN;

CREATE TABLE IF NOT EXISTS process (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name        VARCHAR(128) NOT NULL,
  type        VARCHAR(64)  NOT NULL,
  description VARCHAR(1024),
  status      VARCHAR(32)  NOT NULL DEFAULT 'ENABLED' CHECK (status IN ('ENABLED', 'DISABLED')),
  version     INT          NOT NULL DEFAULT 1 CHECK (version >= 1),
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_process_type ON process(type);
CREATE INDEX IF NOT EXISTS idx_process_status ON process(status);
CREATE INDEX IF NOT EXISTS idx_process_created_at ON process(created_at);

CREATE TABLE IF NOT EXISTS process_node (
  id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  process_id        BIGINT       NOT NULL,
  node_name         VARCHAR(128) NOT NULL,
  node_order        INT          NOT NULL CHECK (node_order >= 0),
  required_material TEXT,
  approver_role     VARCHAR(64)  NOT NULL,
  CONSTRAINT fk_process_node_process FOREIGN KEY (process_id) REFERENCES process(id),
  CONSTRAINT uq_process_node_process_order UNIQUE (process_id, node_order)
);

CREATE INDEX IF NOT EXISTS idx_process_node_process_id ON process_node(process_id);
CREATE INDEX IF NOT EXISTS idx_process_node_approver_role ON process_node(approver_role);

CREATE TABLE IF NOT EXISTS student_process (
  id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  student_id       BIGINT      NOT NULL,
  process_id       BIGINT      NOT NULL,
  current_node_id  BIGINT,
  status           VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
  start_time       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_student_process_process FOREIGN KEY (process_id) REFERENCES process(id),
  CONSTRAINT fk_student_process_current_node FOREIGN KEY (current_node_id) REFERENCES process_node(id)
);

CREATE INDEX IF NOT EXISTS idx_student_process_student_id ON student_process(student_id);
CREATE INDEX IF NOT EXISTS idx_student_process_process_id ON student_process(process_id);
CREATE INDEX IF NOT EXISTS idx_student_process_status ON student_process(status);
CREATE INDEX IF NOT EXISTS idx_student_process_update_time ON student_process(update_time);

CREATE TABLE IF NOT EXISTS material (
  id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  student_process_id BIGINT       NOT NULL,
  student_id        BIGINT       NOT NULL,
  node_id           BIGINT       NOT NULL,
  material_type     VARCHAR(64)  NOT NULL,
  file_url          VARCHAR(512) NOT NULL,
  file_name         VARCHAR(256) NOT NULL,
  description       VARCHAR(512),
  status            VARCHAR(32)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'RETURNED')),
  submit_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_material_student_process FOREIGN KEY (student_process_id) REFERENCES student_process(id),
  CONSTRAINT fk_material_node FOREIGN KEY (node_id) REFERENCES process_node(id),
  CONSTRAINT uq_material_sp_node_type UNIQUE (student_process_id, node_id, material_type)
);

CREATE INDEX IF NOT EXISTS idx_material_student_process_id ON material(student_process_id);
CREATE INDEX IF NOT EXISTS idx_material_student_id ON material(student_id);
CREATE INDEX IF NOT EXISTS idx_material_node_id ON material(node_id);
CREATE INDEX IF NOT EXISTS idx_material_status ON material(status);
CREATE INDEX IF NOT EXISTS idx_material_submit_time ON material(submit_time);

CREATE TABLE IF NOT EXISTS approval_record (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  material_id  BIGINT       NOT NULL,
  approver_id  BIGINT       NOT NULL,
  result       VARCHAR(16)  NOT NULL CHECK (result IN ('APPROVED', 'RETURNED')),
  comment      VARCHAR(1024),
  approve_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_approval_record_material FOREIGN KEY (material_id) REFERENCES material(id),
  CONSTRAINT ck_approval_record_return_comment CHECK (result <> 'RETURNED' OR (comment IS NOT NULL AND LENGTH(TRIM(comment)) > 0))
);

CREATE INDEX IF NOT EXISTS idx_approval_record_material_id ON approval_record(material_id);
CREATE INDEX IF NOT EXISTS idx_approval_record_approver_id ON approval_record(approver_id);
CREATE INDEX IF NOT EXISTS idx_approval_record_approve_time ON approval_record(approve_time);

COMMIT;

