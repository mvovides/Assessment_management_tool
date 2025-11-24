-- V2__seed.sql: Seed data for development and testing
-- This file will automatically run when the application starts

-- BCrypt hash for password "admin123" (same for all users for testing)
-- Hash: $2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW

-- =============================================================================
-- USERS
-- =============================================================================

-- 1. Admin User (Teaching Support + Exams Officer)
INSERT INTO app_user (id, name, email, password_hash, base_type, is_exams_officer, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 
     'Admin User', 
     'admin@sheffield.ac.uk', 
     '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW',
     'TEACHING_SUPPORT', 
     TRUE, 
     CURRENT_TIMESTAMP);

-- 2. Dr. Alice Anderson (Academic - Module Leader)
INSERT INTO app_user (id, name, email, password_hash, base_type, is_exams_officer, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000002', 
     'Dr. Alice Anderson', 
     'alice.anderson@sheffield.ac.uk', 
     '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW',
     'ACADEMIC', 
     FALSE, 
     CURRENT_TIMESTAMP);

-- 3. Dr. Bob Brown (Academic - Assessment Setter)
INSERT INTO app_user (id, name, email, password_hash, base_type, is_exams_officer, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000003', 
     'Dr. Bob Brown', 
     'bob.brown@sheffield.ac.uk', 
     '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW',
     'ACADEMIC', 
     FALSE, CURRENT_TIMESTAMP);

-- 4. Dr. Carol Chen (Academic - Independent Checker)
INSERT INTO app_user (id, name, email, password_hash, base_type, is_exams_officer, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000004', 
     'Dr. Carol Chen', 
     'carol.chen@sheffield.ac.uk', 
     '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW',
     'ACADEMIC', 
     FALSE, CURRENT_TIMESTAMP);

-- 5. Prof. David Davis (External Examiner)
INSERT INTO app_user (id, name, email, password_hash, base_type, is_exams_officer, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000005', 
     'Prof. David Davis', 
     'david.davis@example.com', 
     '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW',
     'EXTERNAL_EXAMINER', 
     FALSE, CURRENT_TIMESTAMP);

-- 6. Emma Wilson (Teaching Support)
INSERT INTO app_user (id, name, email, password_hash, base_type, is_exams_officer, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000006', 
     'Emma Wilson', 
     'emma.wilson@sheffield.ac.uk', 
     '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW',
     'TEACHING_SUPPORT', 
     FALSE, CURRENT_TIMESTAMP);

-- Additional staff for CSV import examples
INSERT INTO app_user (id, name, email, password_hash, base_type, is_exams_officer, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000007', 'Phil McMinn', 'phil.mcminn@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000008', 'Kirill Bogdanov', 'kirill.bogdanov@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000009', 'Tahsin Khan', 'tahsin.khan@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-00000000000a', 'Donghwan Shin', 'donghwan.shin@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-00000000000b', 'Maksim Zhukovskii', 'maksim.zhukovskii@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-00000000000c', 'Delvin Ce Zhang', 'delvin.zhang@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-00000000000d', 'Georgios Moulantzikos', 'georgios.moulantzikos@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-00000000000e', 'Parinya Chalermsook', 'parinya.chalermsook@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-00000000000f', 'Prosanta Gope', 'prosanta.gope@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000010', 'James Mapp', 'james.mapp@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000011', 'Georg Struth', 'georg.struth@sheffield.ac.uk', '$2a$10$thiTybJO1/Xyv2MSYAQlc.f0x9N25tj6knK8IKROPMq0duJdNyYwW', 'ACADEMIC', FALSE, CURRENT_TIMESTAMP);

-- =============================================================================
-- SEED DATA SUMMARY
-- =============================================================================
-- Users: 17 (1 Admin/EO, 14 Academics, 1 External Examiner, 1 Teaching Support)
-- All passwords: "admin123"
-- 
-- Test Accounts:
-- - admin@sheffield.ac.uk (Admin + EO) - password: admin123
-- - alice.anderson@sheffield.ac.uk (Academic) - password: admin123
-- - bob.brown@sheffield.ac.uk (Academic) - password: admin123
-- - carol.chen@sheffield.ac.uk (Academic) - password: admin123
-- - david.davis@example.com (External Examiner) - password: admin123
-- - emma.wilson@sheffield.ac.uk (Teaching Support) - password: admin123
-- 
-- CSV Import Example Users (all Academics):
-- - Phil McMinn, Kirill Bogdanov, Tahsin Khan, Donghwan Shin
-- - Maksim Zhukovskii, Delvin Ce Zhang, Georgios Moulantzikos, Parinya Chalermsook
-- - Prosanta Gope, James Mapp, Georg Struth

-- =============================================================================
-- MODULES (for testing filtering)
-- =============================================================================

-- Test module for Phil McMinn
INSERT INTO module (id, code, title)
VALUES 
    ('10000000-0000-0000-0000-000000000001', 'COM1001', 'Introduction to Software Engineering');

-- Test module for Georg Struth
INSERT INTO module (id, code, title)
VALUES 
    ('10000000-0000-0000-0000-000000000002', 'COM4507', 'Software and Hardware Verification');

-- =============================================================================
-- MODULE STAFF ROLES (for testing filtering)
-- =============================================================================

-- Phil McMinn as MODULE_LEAD for COM1001
INSERT INTO module_staff_role (id, module_id, user_id, role)
VALUES 
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000007', 'MODULE_LEAD');

-- Kirill Bogdanov as STAFF for COM1001
INSERT INTO module_staff_role (id, module_id, user_id, role)
VALUES 
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000008', 'STAFF');

-- Georg Struth as MODULE_LEAD for COM4507
INSERT INTO module_staff_role (id, module_id, user_id, role)
VALUES 
    ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000011', 'MODULE_LEAD');
