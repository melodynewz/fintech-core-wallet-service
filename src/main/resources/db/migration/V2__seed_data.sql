-- Seed initial data for demo
-- Uses MERGE to avoid duplicates (H2 syntax, also works with PostgreSQL's ON CONFLICT)
MERGE INTO wallets (account_number, owner_name, balance, created_at) KEY(account_number) 
VALUES ('12345', 'Somchai Fintech', 1000.00, CURRENT_TIMESTAMP);

MERGE INTO wallets (account_number, owner_name, balance, created_at) KEY(account_number) 
VALUES ('67890', 'John Doe', 500.00, CURRENT_TIMESTAMP);