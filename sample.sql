-- ==========================================
-- CHAMA SAVINGS MANAGEMENT SYSTEM
-- MySQL 8.0
-- ==========================================

CREATE DATABASE chama_management;

USE chama_management;

-- 1. MEMBERS TABLE
CREATE TABLE members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100),
    national_id VARCHAR(30) UNIQUE,
    join_date DATE NOT NULL,
    status ENUM('Active', 'Inactive')
        DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. CONTRIBUTIONS TABLE
CREATE TABLE contributions (
    contribution_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    contribution_date DATE NOT NULL,
    payment_method VARCHAR(30),
    reference_number VARCHAR(100),

    FOREIGN KEY (member_id)
        REFERENCES members(member_id),

    CHECK (amount > 0)
);

-- 3. LOANS TABLE
CREATE TABLE loans (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    loan_amount DECIMAL(12,2) NOT NULL,
    interest_rate DECIMAL(5,2) DEFAULT 0,
    loan_date DATE NOT NULL,
    due_date DATE,
    loan_status ENUM(
        'Pending',
        'Approved',
        'Rejected',
        'Paid'
    ) DEFAULT 'Pending',

    FOREIGN KEY (member_id)
        REFERENCES members(member_id),

    CHECK (loan_amount > 0)
);

-- 4. LOAN REPAYMENTS TABLE
CREATE TABLE repayments (
    repayment_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    repayment_date DATE NOT NULL,
    payment_reference VARCHAR(100),

    FOREIGN KEY (loan_id)
        REFERENCES loans(loan_id),

    CHECK (amount > 0)
);

-- 5. EXPENSES TABLE
CREATE TABLE expenses (
    expense_id INT AUTO_INCREMENT PRIMARY KEY,
    expense_name VARCHAR(100) NOT NULL,
    description TEXT,
    amount DECIMAL(12,2) NOT NULL,
    expense_date DATE NOT NULL,

    CHECK (amount > 0)
);

-- 6. MEETINGS TABLE
CREATE TABLE meetings (
    meeting_id INT AUTO_INCREMENT PRIMARY KEY,
    meeting_date DATE NOT NULL,
    meeting_location VARCHAR(150),
    agenda TEXT,
    minutes TEXT
);

-- 7. MEETING ATTENDANCE TABLE
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    meeting_id INT NOT NULL,
    member_id INT NOT NULL,
    attendance_status ENUM(
        'Present',
        'Absent',
        'Late'
    ) DEFAULT 'Present',

    FOREIGN KEY (meeting_id)
        REFERENCES meetings(meeting_id),

    FOREIGN KEY (member_id)
        REFERENCES members(member_id),

    UNIQUE (meeting_id, member_id)
);

-- 8. SYSTEM USERS TABLE
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Treasurer', 'Secretary')
        DEFAULT 'Secretary',
    member_id INT UNIQUE,

    FOREIGN KEY (member_id)
        REFERENCES members(member_id)
);

-- ==========================================
-- MEMBER SAVINGS SUMMARY
-- ==========================================

CREATE VIEW member_savings_summary AS
SELECT
    m.member_id,
    m.first_name,
    m.last_name,
    COALESCE(SUM(c.amount), 0) AS total_savings
FROM members m
LEFT JOIN contributions c
    ON m.member_id = c.member_id
GROUP BY
    m.member_id,
    m.first_name,
    m.last_name;

-- ==========================================
-- LOAN BALANCE SUMMARY
-- ==========================================

CREATE VIEW loan_balance_summary AS
SELECT
    l.loan_id,
    l.member_id,
    l.loan_amount,
    l.interest_rate,
    COALESCE(SUM(r.amount), 0) AS amount_repaid,
    (
        l.loan_amount * (1 + l.interest_rate / 100)
        - COALESCE(SUM(r.amount), 0)
    ) AS outstanding_balance,
    l.loan_status
FROM loans l
LEFT JOIN repayments r
    ON l.loan_id = r.loan_id
GROUP BY
    l.loan_id,
    l.member_id,
    l.loan_amount,
    l.interest_rate,
    l.loan_status;

-- ==========================================
-- MEMBER FINANCIAL STATEMENT
-- ==========================================

CREATE VIEW member_statements AS

SELECT
    c.member_id,
    c.contribution_date AS transaction_date,
    'Contribution' AS transaction_type,
    c.reference_number AS reference,
    c.amount AS credit,
    0.00 AS debit
FROM contributions c

UNION ALL

SELECT
    l.member_id,
    l.loan_date AS transaction_date,
    'Loan Issued' AS transaction_type,
    CONCAT('LOAN-', l.loan_id) AS reference,
    0.00 AS credit,
    l.loan_amount AS debit
FROM loans l
WHERE l.loan_status = 'Approved'

UNION ALL

SELECT
    l.member_id,
    r.repayment_date AS transaction_date,
    'Loan Repayment' AS transaction_type,
    r.payment_reference AS reference,
    r.amount AS credit,
    0.00 AS debit
FROM repayments r
JOIN loans l ON r.loan_id = l.loan_id;

-- ==========================================
-- END OF DATABASE CREATION
-- ==========================================
