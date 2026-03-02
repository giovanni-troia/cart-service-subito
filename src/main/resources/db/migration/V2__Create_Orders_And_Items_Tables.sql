-- Flyway Migration: V2__Create_Orders_And_Items_Tables
-- Creates tables for orders and order items with pricing snapshots

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    total_net_price NUMERIC(19, 2) NOT NULL CHECK (total_net_price >= 0),
    total_vat_amount NUMERIC(19, 2) NOT NULL CHECK (total_vat_amount >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_created_at ON orders(created_at);

COMMENT ON TABLE orders IS 'Orders created by customers';
COMMENT ON COLUMN orders.total_net_price IS 'Total net price of all items (sum of line net prices)';
COMMENT ON COLUMN orders.total_vat_amount IS 'Total VAT amount (sum of line VAT amounts)';

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGSERIAL NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_net_price NUMERIC(19, 2) NOT NULL CHECK (unit_net_price > 0),
    vat_rate NUMERIC(5, 4) NOT NULL CHECK (vat_rate >= 0 AND vat_rate <= 1),
    line_net_price NUMERIC(19, 2) NOT NULL CHECK (line_net_price >= 0),
    line_vat_amount NUMERIC(19, 2) NOT NULL CHECK (line_vat_amount >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_product_id ON order_items(product_id);

COMMENT ON TABLE order_items IS 'Line items in orders (snapshot pattern for pricing)';
COMMENT ON COLUMN order_items.unit_net_price IS 'Unit net price at time of order (snapshot)';
COMMENT ON COLUMN order_items.vat_rate IS 'VAT rate at time of order (snapshot)';
COMMENT ON COLUMN order_items.line_net_price IS 'Line net price (unit_net_price * quantity)';
COMMENT ON COLUMN order_items.line_vat_amount IS 'Line VAT amount (line_net_price * vat_rate)';

