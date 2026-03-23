-- Flyway migration script V4: Hash plaintext passwords with BCrypt
-- Compatible with H2 and PostgreSQL

-- Update demo user password from plaintext 'password' to BCrypt hash
UPDATE users
SET password = '$2a$10$Ze7U5GOgzDz.F8BdaHLhuOZZ9K5wrDMGqMYm/c3eWENLYc7wpdKqe'
WHERE username = 'demo';

-- Add a check to ensure password column length is sufficient (already VARCHAR(255))
-- No action needed.