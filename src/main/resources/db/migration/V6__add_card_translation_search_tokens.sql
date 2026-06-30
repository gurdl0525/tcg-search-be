create table card_identity_translation_search_tokens (
    id uuid primary key default gen_random_uuid(),
    card_identity_translation_id uuid not null references card_identity_translations(id) on delete cascade,
    card_identity_id uuid not null references card_identities(id) on delete cascade,
    language_code text not null,
    source_field text not null,
    token_type text not null,
    token text not null,
    weight smallint not null default 1,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint card_identity_translation_search_tokens_source_field_supported
        check (source_field in ('name', 'effect_text', 'trigger_text')),
    constraint card_identity_translation_search_tokens_token_type_supported
        check (token_type in ('normalized', 'word', 'prefix', 'choseong', 'choseong_prefix', 'ngram')),
    constraint card_identity_translation_search_tokens_token_not_blank
        check (length(trim(token)) > 0),
    constraint card_identity_translation_search_tokens_weight_positive
        check (weight > 0)
);

create unique index cit_search_tokens_translation_token_unique
    on card_identity_translation_search_tokens (
        card_identity_translation_id,
        source_field,
        token_type,
        token
    );

create index cit_search_tokens_token_identity_idx
    on card_identity_translation_search_tokens (token, card_identity_id);

create index cit_search_tokens_identity_idx
    on card_identity_translation_search_tokens (card_identity_id);

create index cit_search_tokens_language_token_idx
    on card_identity_translation_search_tokens (language_code, token);
