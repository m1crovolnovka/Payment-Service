CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE payments (
                          id             UUID PRIMARY KEY,
                          order_id       UUID NOT NULL,
                          user_id        UUID NOT NULL,
                          status         VARCHAR(50) NOT NULL,
                          payment_amount NUMERIC(10,2) NOT NULL,
                          timestamp      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);