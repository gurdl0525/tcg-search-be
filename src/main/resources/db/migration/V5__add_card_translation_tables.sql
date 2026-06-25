create table card_identity_translations (
    id uuid primary key default gen_random_uuid(),
    card_identity_id uuid not null references card_identities(id) on delete cascade,
    language_code text not null,
    name text not null,
    effect_text text,
    trigger_text text,
    search_vector tsvector generated always as (
        to_tsvector(
            'simple',
            coalesce(name, '') || ' ' ||
            coalesce(effect_text, '') || ' ' ||
            coalesce(trigger_text, '')
        )
    ) stored,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index card_identity_translations_identity_language_unique
    on card_identity_translations (card_identity_id, language_code);

create index card_identity_translations_search_vector_idx
    on card_identity_translations using gin (search_vector);

create table card_set_translations (
    id uuid primary key default gen_random_uuid(),
    card_set_id uuid not null references card_sets(id) on delete cascade,
    language_code text not null,
    name text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index card_set_translations_set_language_unique
    on card_set_translations (card_set_id, language_code);

create table attribute_translations (
    id uuid primary key default gen_random_uuid(),
    attribute_id uuid not null references attributes(id) on delete cascade,
    language_code text not null,
    name text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index attribute_translations_attribute_language_unique
    on attribute_translations (attribute_id, language_code);

create table trait_translations (
    id uuid primary key default gen_random_uuid(),
    trait_id uuid not null references traits(id) on delete cascade,
    language_code text not null,
    name text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index trait_translations_trait_language_unique
    on trait_translations (trait_id, language_code);
