create extension if not exists pgcrypto;

create table app_users (
    id uuid primary key default gen_random_uuid(),
    email text not null unique,
    display_name text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table attributes (
    id uuid primary key default gen_random_uuid(),
    name text not null unique
);

create table colors (
    id uuid primary key default gen_random_uuid(),
    code text not null unique,
    name text not null
);

create table rarities (
    id uuid primary key default gen_random_uuid(),
    code text not null unique,
    name text not null
);

create table traits (
    id uuid primary key default gen_random_uuid(),
    name text not null unique
);

create table card_sets (
    id uuid primary key default gen_random_uuid(),
    code text not null unique,
    name text not null,
    product_type text not null,
    release_date date,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table card_identities (
    id uuid primary key default gen_random_uuid(),
    card_no text not null unique,
    name text not null,
    card_type text not null,
    cost smallint,
    life smallint,
    power integer,
    counter integer,
    attribute_id uuid references attributes(id),
    effect_text text,
    trigger_text text,
    block_no integer,
    search_vector tsvector generated always as (
        to_tsvector(
            'simple',
            coalesce(card_no, '') || ' ' ||
            coalesce(name, '') || ' ' ||
            coalesce(effect_text, '') || ' ' ||
            coalesce(trigger_text, '')
        )
    ) stored,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint card_identities_cost_non_negative check (cost is null or cost >= 0),
    constraint card_identities_life_non_negative check (life is null or life >= 0),
    constraint card_identities_power_non_negative check (power is null or power >= 0),
    constraint card_identities_counter_non_negative check (counter is null or counter >= 0),
    constraint card_identities_block_no_non_negative check (block_no is null or block_no >= 0)
);

create table card_identity_colors (
    card_identity_id uuid not null references card_identities(id) on delete cascade,
    color_id uuid not null references colors(id),
    primary key (card_identity_id, color_id)
);

create table card_identity_traits (
    card_identity_id uuid not null references card_identities(id) on delete cascade,
    trait_id uuid not null references traits(id),
    primary key (card_identity_id, trait_id)
);

create table card_printings (
    id uuid primary key default gen_random_uuid(),
    card_identity_id uuid not null references card_identities(id) on delete cascade,
    card_set_id uuid not null references card_sets(id),
    rarity_id uuid references rarities(id),
    language_code text not null,
    region_code text,
    variant_name text,
    is_parallel boolean not null default false,
    foil_treatment text,
    illustration_type text,
    image_url text,
    source_url text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create unique index card_printings_variant_unique
    on card_printings (
        card_identity_id,
        card_set_id,
        rarity_id,
        language_code,
        region_code,
        variant_name,
        is_parallel,
        foil_treatment
    )
    nulls not distinct;

create table user_collection_entries (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references app_users(id) on delete cascade,
    card_printing_id uuid not null references card_printings(id) on delete cascade,
    quantity integer not null,
    condition text not null default 'unspecified',
    note text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint user_collection_entries_quantity_positive check (quantity > 0),
    unique (user_id, card_printing_id, condition)
);

create table decks (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references app_users(id) on delete cascade,
    leader_identity_id uuid not null references card_identities(id),
    leader_printing_id uuid references card_printings(id),
    name text not null,
    visibility text not null default 'private',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint decks_visibility_supported check (visibility in ('private', 'unlisted', 'public'))
);

create table deck_cards (
    id uuid primary key default gen_random_uuid(),
    deck_id uuid not null references decks(id) on delete cascade,
    card_identity_id uuid not null references card_identities(id),
    preferred_printing_id uuid references card_printings(id),
    quantity integer not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint deck_cards_quantity_positive check (quantity > 0),
    unique (deck_id, card_identity_id)
);

create table external_marketplaces (
    id uuid primary key default gen_random_uuid(),
    code text not null unique,
    name text not null,
    search_url_template text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table external_card_links (
    id uuid primary key default gen_random_uuid(),
    marketplace_id uuid not null references external_marketplaces(id) on delete cascade,
    card_identity_id uuid references card_identities(id) on delete cascade,
    card_printing_id uuid references card_printings(id) on delete cascade,
    url text not null,
    url_type text not null default 'manual',
    priority integer not null default 100,
    is_active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint external_card_links_exactly_one_target check (
        (card_identity_id is not null and card_printing_id is null)
        or (card_identity_id is null and card_printing_id is not null)
    ),
    constraint external_card_links_priority_non_negative check (priority >= 0),
    constraint external_card_links_url_type_supported check (url_type in ('manual'))
);

create index card_identities_search_vector_idx
    on card_identities using gin (search_vector);

create index card_printings_card_identity_id_idx
    on card_printings (card_identity_id);

create index card_printings_card_set_id_idx
    on card_printings (card_set_id);

create index card_printings_rarity_id_idx
    on card_printings (rarity_id);

create index external_card_links_marketplace_identity_idx
    on external_card_links (marketplace_id, card_identity_id)
    where card_identity_id is not null;

create index external_card_links_marketplace_printing_idx
    on external_card_links (marketplace_id, card_printing_id)
    where card_printing_id is not null;
