-- Sample STP transaction data for testing
-- Before running this script:
-- 1. Make sure you have clients in the database (with valid client IDs)
-- 2. Update client_id values below to match existing clients in your database

-- Active STPs
INSERT INTO transactions (client_id, type, amount, date, status, description, frequency, next_execution_date, expiry_date, remaining_amount, source_balance, from_scheme, to_scheme, created_at, updated_at)
VALUES 
-- Client 1
(1, 'STP', 5000.00, '2023-12-15', 'Active', 'Monthly STP from HDFC Liquid to SBI Bluechip', 'Monthly', '2024-06-15', '2025-06-15', 30000.00, 35000.00, 'HDFC Liquid Fund', 'SBI Bluechip Fund', NOW(), NOW()),
(1, 'STP', 10000.00, '2024-01-10', 'Active', 'Monthly STP from Kotak Liquid to HDFC Midcap', 'Monthly', '2024-06-10', '2025-01-10', 60000.00, 70000.00, 'Kotak Liquid Fund', 'HDFC Midcap Opportunities', NOW(), NOW()),

-- Client 2
(2, 'STP', 7500.00, '2024-02-05', 'Active', 'Weekly STP from Axis Liquid to DSP Equity', 'Weekly', CURRENT_DATE, '2024-08-05', 22500.00, 30000.00, 'Axis Liquid Fund', 'DSP Equity Fund', NOW(), NOW()),
(2, 'STP', 3000.00, '2024-02-20', 'Active', 'Monthly STP from SBI Liquid to ICICI Value Discovery', 'Monthly', '2024-06-20', '2024-12-20', 18000.00, 21000.00, 'SBI Liquid Fund', 'ICICI Value Discovery Fund', NOW(), NOW()),

-- Client 3
(3, 'STP', 2500.00, '2024-03-01', 'Active', 'Bi-weekly STP from Aditya Birla Liquid to Mirae Asset Large Cap', 'Bi-weekly', '2024-06-15', '2024-09-01', 12500.00, 15000.00, 'Aditya Birla Liquid Fund', 'Mirae Asset Large Cap Fund', NOW(), NOW()),

-- Executing Today (set next_execution_date to current date)
(1, 'STP', 8000.00, '2024-01-25', 'Active', 'Weekly STP executing today', 'Weekly', CURRENT_DATE, '2024-10-25', 40000.00, 48000.00, 'HDFC Liquid Fund', 'Axis Focused 25 Fund', NOW(), NOW()),
(2, 'STP', 6000.00, '2024-02-10', 'Active', 'Monthly STP executing today', 'Monthly', CURRENT_DATE, '2024-11-10', 36000.00, 42000.00, 'ICICI Liquid Fund', 'Franklin India Prima Fund', NOW(), NOW()),

-- Expiring in next 3 months
(3, 'STP', 4000.00, '2023-12-01', 'Active', 'Monthly STP expiring soon', 'Monthly', '2024-06-01', CURRENT_DATE + INTERVAL '2 MONTH', 4000.00, 8000.00, 'Kotak Liquid Fund', 'L&T Midcap Fund', NOW(), NOW()),
(1, 'STP', 5500.00, '2024-01-05', 'Active', 'Weekly STP expiring soon', 'Weekly', '2024-06-05', CURRENT_DATE + INTERVAL '3 MONTH', 11000.00, 16500.00, 'Axis Liquid Fund', 'SBI Magnum Midcap Fund', NOW(), NOW()),

-- Zero source balance
(2, 'STP', 3500.00, '2024-01-15', 'Active', 'Monthly STP with zero balance', 'Monthly', '2024-06-15', '2024-12-15', 14000.00, 0.00, 'HDFC Liquid Fund', 'Kotak Emerging Equity Fund', NOW(), NOW()),
(3, 'STP', 2000.00, '2024-02-25', 'Active', 'Weekly STP with zero balance', 'Weekly', '2024-06-01', '2024-08-25', 6000.00, 0.00, 'SBI Liquid Fund', 'HDFC Top 100 Fund', NOW(), NOW());

-- Previous months for trend data
INSERT INTO transactions (client_id, type, amount, date, status, description, created_at, updated_at)
VALUES 
-- January
(1, 'STP', 45000.00, '2024-01-15', 'Completed', 'January STP', NOW(), NOW()),
(2, 'STP', 35000.00, '2024-01-20', 'Completed', 'January STP', NOW(), NOW()),
(3, 'STP', 25000.00, '2024-01-25', 'Completed', 'January STP', NOW(), NOW()),

-- February
(1, 'STP', 50000.00, '2024-02-15', 'Completed', 'February STP', NOW(), NOW()),
(2, 'STP', 40000.00, '2024-02-20', 'Completed', 'February STP', NOW(), NOW()),
(3, 'STP', 30000.00, '2024-02-25', 'Completed', 'February STP', NOW(), NOW()),

-- March
(1, 'STP', 55000.00, '2024-03-15', 'Completed', 'March STP', NOW(), NOW()),
(2, 'STP', 45000.00, '2024-03-20', 'Completed', 'March STP', NOW(), NOW()),
(3, 'STP', 35000.00, '2024-03-25', 'Completed', 'March STP', NOW(), NOW()),

-- April
(1, 'STP', 60000.00, '2024-04-15', 'Completed', 'April STP', NOW(), NOW()),
(2, 'STP', 50000.00, '2024-04-20', 'Completed', 'April STP', NOW(), NOW()),
(3, 'STP', 40000.00, '2024-04-25', 'Completed', 'April STP', NOW(), NOW()),

-- May
(1, 'STP', 65000.00, '2024-05-15', 'Completed', 'May STP', NOW(), NOW()),
(2, 'STP', 55000.00, '2024-05-20', 'Completed', 'May STP', NOW(), NOW()),
(3, 'STP', 45000.00, '2024-05-25', 'Completed', 'May STP', NOW(), NOW());

-- IMPORTANT: Update the client_id values above to match existing clients in your database
-- Run this script against your database to create sample STP data for testing 