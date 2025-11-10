CREATE DATABASE orderdb;
CREATE DATABASE inventorydb;

-- Drop existing table if any (for dev use)
DROP TABLE IF EXISTS products;

-- Create table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_products_sku ON products (sku_code);
CREATE INDEX idx_products_status ON products (status);


DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;

-- Orders table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'PLACED' NOT NULL,
    created_at TIMESTAMP DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW(),
    version INT DEFAULT 0
);

-- Optional indexes for faster lookups
CREATE INDEX idx_orders_number_lower ON orders (LOWER(order_number));
CREATE INDEX idx_orders_status ON orders (status);

-- Order Items table (One-to-Many relationship)
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    sku_code VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price NUMERIC(10,2) NOT NULL CHECK (price > 0)
);

-- Index for product lookups
CREATE INDEX idx_order_items_sku ON order_items (sku_code);




GRANT ALL PRIVILEGES ON DATABASE orderdb TO postgres;





















Optional (Future)	user_id or customer_ref	Optional	If you plan multi-user orders; not required in your current scope.
⚙️ Optional	total_amount	Optional	Can be derived (price * quantity), no need to store.
⚙️ Optional	payment_status, payment_mode	Optional	Only if you later extend to payment flow.
⚙️ Optional	source_system	Optional	Useful if orders can come from multiple apps (mobile/web).