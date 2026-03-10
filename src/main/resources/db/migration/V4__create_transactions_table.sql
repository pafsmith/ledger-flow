CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    account_id UUID NOT NULL,
    category_id UUID,
    destination_account_id UUID,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    transaction_date DATE NOT NULL,
    merchant VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,

    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,

    CONSTRAINT fk_transactions_destination_account
        FOREIGN KEY (destination_account_id) REFERENCES accounts(id) ON DELETE SET NULL
);
