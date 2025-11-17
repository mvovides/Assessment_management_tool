-- V1__init.sql: Initial schema creation (H2 Compatible)

-- Users table
CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    base_type VARCHAR(50) NOT NULL CHECK (base_type IN ('TEACHING_SUPPORT', 'ACADEMIC', 'EXTERNAL_EXAMINER')),
    is_exams_officer BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_email ON app_user(email);
CREATE INDEX idx_user_base_type ON app_user(base_type);

-- Modules table
CREATE TABLE module (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    UNIQUE (code, academic_year)
);

CREATE INDEX idx_module_code ON module(code);
CREATE INDEX idx_module_year ON module(academic_year);

-- Module staff roles (association table)
CREATE TABLE module_staff_role (
    id UUID PRIMARY KEY,
    module_id UUID NOT NULL REFERENCES module(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id),
    role VARCHAR(50) NOT NULL CHECK (role IN ('MODULE_LEAD', 'STAFF', 'MODERATOR')),
    UNIQUE (module_id, user_id, role)
);

CREATE INDEX idx_module_staff_module ON module_staff_role(module_id);
CREATE INDEX idx_module_staff_user ON module_staff_role(user_id);

-- Module external examiners (many-to-many)
CREATE TABLE module_external_examiner (
    module_id UUID NOT NULL REFERENCES module(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id),
    PRIMARY KEY (module_id, user_id)
);

CREATE INDEX idx_module_ee_module ON module_external_examiner(module_id);
CREATE INDEX idx_module_ee_user ON module_external_examiner(user_id);

-- Assessments table
CREATE TABLE assessment (
    id UUID PRIMARY KEY,
    module_id UUID NOT NULL REFERENCES module(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('CW', 'TEST', 'EXAM')),
    current_state VARCHAR(50) NOT NULL,
    exam_date DATE NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_assessment_module ON assessment(module_id);
CREATE INDEX idx_assessment_state ON assessment(current_state);
CREATE INDEX idx_assessment_type ON assessment(type);
CREATE INDEX idx_assessment_exam_date ON assessment(exam_date);

-- Assessment roles (association table)
CREATE TABLE assessment_role (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessment(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_user(id),
    role VARCHAR(20) NOT NULL CHECK (role IN ('SETTER', 'CHECKER')),
    UNIQUE (assessment_id, user_id, role)
);

CREATE INDEX idx_assessment_role_assessment ON assessment_role(assessment_id);
CREATE INDEX idx_assessment_role_user ON assessment_role(user_id);

-- Assessment transitions (immutable audit log)
CREATE TABLE assessment_transition (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessment(id) ON DELETE CASCADE,
    from_state VARCHAR(50) NOT NULL,
    to_state VARCHAR(50) NOT NULL,
    at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    by_user_id UUID NULL REFERENCES app_user(id),
    by_display_name VARCHAR(255) NOT NULL,
    note VARCHAR(2000) NULL,
    is_override BOOLEAN NOT NULL DEFAULT FALSE,
    is_reversion BOOLEAN NOT NULL DEFAULT FALSE,
    reverted_transition_id UUID NULL REFERENCES assessment_transition(id)
);

CREATE INDEX idx_transition_assessment ON assessment_transition(assessment_id);
CREATE INDEX idx_transition_at ON assessment_transition(at);
CREATE INDEX idx_transition_user ON assessment_transition(by_user_id);

-- Checker feedback
CREATE TABLE checker_feedback (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessment(id) ON DELETE CASCADE,
    author_user_id UUID NOT NULL REFERENCES app_user(id),
    text VARCHAR(5000) NOT NULL,
    secure_doc_ref VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_checker_feedback_assessment ON checker_feedback(assessment_id);
CREATE INDEX idx_checker_feedback_author ON checker_feedback(author_user_id);

-- External examiner feedback (one per EXAM assessment)
CREATE TABLE external_examiner_feedback (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL UNIQUE REFERENCES assessment(id) ON DELETE CASCADE,
    examiner_user_id UUID NOT NULL REFERENCES app_user(id),
    feedback VARCHAR(5000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ee_feedback_assessment ON external_examiner_feedback(assessment_id);
CREATE INDEX idx_ee_feedback_examiner ON external_examiner_feedback(examiner_user_id);

-- Setter response (one per EXAM assessment, mandatory after external feedback)
CREATE TABLE setter_response (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL UNIQUE REFERENCES assessment(id) ON DELETE CASCADE,
    author_user_id UUID NOT NULL REFERENCES app_user(id),
    response_text VARCHAR(5000) NOT NULL,
    secure_doc_ref VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_setter_response_assessment ON setter_response(assessment_id);
CREATE INDEX idx_setter_response_author ON setter_response(author_user_id);

-- CSV import jobs
CREATE TABLE csv_import_job (
    id UUID PRIMARY KEY,
    file_name VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    errors VARCHAR(5000) NULL
);

CREATE INDEX idx_import_job_status ON csv_import_job(status);
CREATE INDEX idx_import_job_created ON csv_import_job(created_at);
