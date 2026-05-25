BEGIN;

CREATE TABLE IF NOT EXISTS certificate_application (
  id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  student_id       BIGINT       NOT NULL,
  cert_type        VARCHAR(64)  NOT NULL CHECK (cert_type IN ('ENROLLMENT', 'LEAVE', 'SEAL', 'PARTY', 'TRANSCRIPT')),
  title            VARCHAR(256) NOT NULL,
  description      VARCHAR(1024),
  attachment_url   VARCHAR(512),
  attachment_name  VARCHAR(256),
  status           VARCHAR(32)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'RETURNED', 'ISSUED')),
  approver_id      BIGINT,
  approve_comment  VARCHAR(1024),
  pdf_url          VARCHAR(512),
  apply_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  approve_time     TIMESTAMP,
  issue_time       TIMESTAMP,
  update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_cert_return_comment CHECK (status <> 'RETURNED' OR (approve_comment IS NOT NULL AND LENGTH(TRIM(approve_comment)) > 0)),
  CONSTRAINT ck_cert_issue_pdf CHECK (status <> 'ISSUED' OR pdf_url IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_cert_student_id ON certificate_application(student_id);
CREATE INDEX IF NOT EXISTS idx_cert_cert_type ON certificate_application(cert_type);
CREATE INDEX IF NOT EXISTS idx_cert_status ON certificate_application(status);
CREATE INDEX IF NOT EXISTS idx_cert_apply_time ON certificate_application(apply_time);
CREATE INDEX IF NOT EXISTS idx_cert_approver_id ON certificate_application(approver_id);

COMMIT;
