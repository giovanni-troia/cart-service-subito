-- Flyway Migration: V1__Create_Products_Table
-- Creates the products table for the product catalog

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    unit_net_price NUMERIC(19, 2) NOT NULL CHECK (unit_net_price >= 0),
    vat_rate NUMERIC(5, 4) NOT NULL CHECK (vat_rate >= 0 AND vat_rate <= 1),
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT product_positive_price CHECK (unit_net_price > 0)
);

CREATE INDEX idx_products_created_at ON products(created_at);

COMMENT ON TABLE products IS 'ProductEntity catalog with pricing and VAT information';
COMMENT ON COLUMN products.unit_net_price IS 'Price per unit (net, without VAT)';
COMMENT ON COLUMN products.vat_rate IS 'VAT rate as decimal (e.g., 0.22 for 22%)';

