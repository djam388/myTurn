INSERT INTO doctors (first_name, last_name, specialization, phone_number, email, created_at, updated_at, is_active)
VALUES 
('Иван', 'Петров', 'Терапевт', '+00001234567', 'ivan.petrov@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('Елена', 'Сидорова', 'Кардиолог', '+00009876543', 'elena.sidorova@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('Алексей', 'Иванов', 'Невролог', '+00005554433', 'alexey.ivanov@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('Ольга', 'Николаева', 'Педиатр', '+00007778899', 'olga.kozlova@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
('Дмитрий', 'Смирнов', 'Хирург', '+00003332211', 'dmitry.smirnov@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

INSERT INTO clinic_employees (first_name, last_name, username, password, role, created_at, updated_at)
VALUES 
('Админ', 'Администраторов', 'admin', '$2a$10$sHbZsSb9ztAN7rFuSpIAj.xMge9t2HOj3SgGxRg3LZNjggTmqpafG', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Регина', 'Регистраторова', 'receptionist', '$2a$10$4u32w3rjLBiHebTou2lHxutPYxFnhn2n2867OFNqlSYjQ09WivCpq', 'RECEPTIONIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);