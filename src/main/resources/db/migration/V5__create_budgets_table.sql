CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    limit_amount NUMERIC(19,2) NOT NULL,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_budgets_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_budgets_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,

    CONSTRAINT uq_budgets_user_category_year_month
        UNIQUE (user_id, category_id, year, month),

    CONSTRAINT chk_budgets_month
        CHECK (month BETWEEN 1 AND 12),

    CONSTRAINT chk_budgets_limit_amount
        CHECK (limit_amount >= 0)
);
