-- Chama Member Registry database
CREATE DATABASE IF NOT EXISTS chama_db;
USE chama_db;

CREATE TABLE IF NOT EXISTS members (
    member_id            INT AUTO_INCREMENT PRIMARY KEY,
    full_name            VARCHAR(100)  NOT NULL,
    national_id          VARCHAR(8)    NOT NULL UNIQUE,
    phone                VARCHAR(13)   NOT NULL UNIQUE,
    county               VARCHAR(50)   NOT NULL,
    monthly_contribution DECIMAL(10,2) NOT NULL,
    join_date            DATE          NOT NULL
);

-- Optional sample data
INSERT INTO members (full_name, national_id, phone, county, monthly_contribution, join_date) VALUES
('Wanjiku Kamau', '23456789', '0712345678', 'Kiambu',  2000.00, '2024-01-15'),
('Otieno Odhiambo', '31234567', '0722111222', 'Kisumu', 1500.00, '2024-03-02');
