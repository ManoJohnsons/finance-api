-- V1__Create_initial_tables.sql

-- User table
CREATE TABLE tab_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Category table
CREATE TABLE tab_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    hex_color VARCHAR(7),
    icon VARCHAR(50),
    monthly_goal DECIMAL(19, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_categories_users FOREIGN KEY (user_id) REFERENCES tab_users(id)
);

-- Transaction table
CREATE TABLE tab_transactions (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(7) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    user_id BIGINT NOT NULL,
    category_id BIGINT, -- Allows null values, as specified in ER Diagram
    CONSTRAINT fk_transactions_users FOREIGN KEY (user_id) REFERENCES tab_users(id),
    CONSTRAINT fk_transactions_categories FOREIGN KEY (category_id) REFERENCES tab_categories(id)
);