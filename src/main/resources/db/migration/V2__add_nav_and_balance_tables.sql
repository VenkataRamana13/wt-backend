-- Create nav_history table
CREATE TABLE nav_history (
    id BIGSERIAL PRIMARY KEY,
    fund_id BIGINT NOT NULL,
    nav_date DATE NOT NULL,
    nav DECIMAL(20,6) NOT NULL,
    source VARCHAR(64) NOT NULL,
    last_updated TIMESTAMP DEFAULT NOW(),
    UNIQUE (fund_id, nav_date)
);

-- Create index on fund_id and nav_date for faster lookups
CREATE INDEX idx_nav_history_fund_date ON nav_history(fund_id, nav_date);

-- Create fund_balance table
CREATE TABLE fund_balance (
    id BIGSERIAL PRIMARY KEY,
    fund_id VARCHAR(128) NOT NULL,
    client_id BIGINT NOT NULL,
    balance DECIMAL(20,6) NOT NULL,
    as_of_date DATE NOT NULL,
    last_updated TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_client FOREIGN KEY (client_id) REFERENCES clients(id),
    UNIQUE (fund_id, client_id)
);

-- Create index on fund_id and client_id for faster lookups
CREATE INDEX idx_fund_balance_fund_client ON fund_balance(fund_id, client_id);

-- Create index on client_id for faster client-based queries
CREATE INDEX idx_fund_balance_client ON fund_balance(client_id); 