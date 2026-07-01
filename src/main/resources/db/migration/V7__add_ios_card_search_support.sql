alter table card_printings
    add column detail_tags text[] not null default '{}';

update card_printings
set detail_tags = array_remove(array[
    case when is_parallel then 'PARALLEL' end,
    case when upper(coalesce(variant_name, '')) like '%SP%' then 'SP' end,
    case when upper(coalesce(variant_name, '')) like '%MANGA%' then 'MANGA' end,
    case when upper(coalesce(variant_name, '')) like '%PROMO%' then 'PROMO' end
], null);

create index card_printings_detail_tags_idx
    on card_printings using gin (detail_tags);

create table illustrators (
    id uuid primary key default gen_random_uuid(),
    name text not null unique,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint illustrators_name_not_blank check (length(trim(name)) > 0)
);

create table card_printing_illustrators (
    card_printing_id uuid not null references card_printings(id) on delete cascade,
    illustrator_id uuid not null references illustrators(id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (card_printing_id, illustrator_id)
);

create index card_printing_illustrators_illustrator_idx
    on card_printing_illustrators (illustrator_id, card_printing_id);

create table card_search_events (
    id uuid primary key default gen_random_uuid(),
    user_id uuid references app_users(id) on delete set null,
    event_type text not null,
    query text,
    language text not null default 'all',
    filters jsonb not null default '{}'::jsonb,
    result_count integer,
    selected_printing_id uuid references card_printings(id) on delete set null,
    created_at timestamptz not null default now(),
    constraint card_search_events_event_type_supported
        check (event_type in ('search', 'filter_apply', 'card_open')),
    constraint card_search_events_language_supported
        check (language in ('all', 'ko', 'en', 'jp')),
    constraint card_search_events_result_count_non_negative
        check (result_count is null or result_count >= 0)
);

create index card_search_events_event_type_created_idx
    on card_search_events (event_type, created_at desc);

create index card_search_events_selected_printing_idx
    on card_search_events (selected_printing_id)
    where selected_printing_id is not null;
