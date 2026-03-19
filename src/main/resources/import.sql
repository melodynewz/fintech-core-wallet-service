-- สร้างข้อมูล Wallet 2 บัญชี (สมมติ ID เป็น 1 และ 2)
INSERT INTO wallets (account_number, owner_name, balance, created_at) VALUES ('12345', 'Somchai Fintech', 1000.00, CURRENT_TIMESTAMP);
INSERT INTO wallets (account_number, owner_name, balance, created_at) VALUES ('67890', 'John Doe', 500.00, CURRENT_TIMESTAMP);
