

CREATE TABLE traffic_signs (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_version                    VARCHAR(64) NOT NULL,
    source_sign_id                  INTEGER NOT NULL,
    content_builder_traffic_sign_id UUID NOT NULL,
    code                            VARCHAR(32) NOT NULL,
    name                            VARCHAR(255) NOT NULL,
    category                        VARCHAR(32) NOT NULL,
    category_name                   VARCHAR(100) NOT NULL,
    meaning                         TEXT NOT NULL,
    quick_tip                       TEXT NOT NULL,
    source_image                    VARCHAR(255) NOT NULL,
    media_file_id                   UUID NOT NULL,
    applicable_licenses             JSONB NOT NULL,
    status                          VARCHAR(20) NOT NULL,
    published_at                    TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_practice_traffic_sign_source_id
        CHECK (source_sign_id > 0),
    CONSTRAINT ck_practice_traffic_sign_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED', 'HIDDEN')),
    CONSTRAINT ck_practice_traffic_sign_licenses
        CHECK (jsonb_typeof(applicable_licenses) = 'array'),
    CONSTRAINT uq_practice_traffic_sign_version_source
        UNIQUE (bank_version, source_sign_id),
    CONSTRAINT uq_practice_traffic_sign_builder_id
        UNIQUE (content_builder_traffic_sign_id)
);

-- Same case-insensitive and trim-normalized code identity as Content Builder.
CREATE UNIQUE INDEX uq_practice_traffic_sign_version_code
    ON traffic_signs (bank_version, lower(btrim(code)));

CREATE INDEX idx_practice_traffic_sign_listing
    ON traffic_signs (bank_version, status, category, source_sign_id);

CREATE INDEX idx_practice_traffic_sign_licenses
    ON traffic_signs USING GIN (applicable_licenses);

CREATE TABLE memory_tips (
    id                            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_version                  VARCHAR(64) NOT NULL,
    source_tip_id                 INTEGER NOT NULL,
    content_builder_memory_tip_id UUID NOT NULL,
    sort_order                    INTEGER NOT NULL,
    title                         VARCHAR(255) NOT NULL,
    quick_rule                    TEXT NOT NULL,
    explanation                   TEXT NOT NULL,
    status                        VARCHAR(20) NOT NULL,
    published_at                  TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    created_at                    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                    TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_practice_memory_tip_source_id
        CHECK (source_tip_id > 0),
    CONSTRAINT ck_practice_memory_tip_sort_order
        CHECK (sort_order > 0),
    CONSTRAINT ck_practice_memory_tip_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED', 'HIDDEN')),
    CONSTRAINT uq_practice_memory_tip_version_source
        UNIQUE (bank_version, source_tip_id),
    CONSTRAINT uq_practice_memory_tip_version_order
        UNIQUE (bank_version, sort_order),
    CONSTRAINT uq_practice_memory_tip_builder_id
        UNIQUE (content_builder_memory_tip_id)
);

CREATE INDEX idx_practice_memory_tip_listing
    ON memory_tips (bank_version, status, sort_order);
