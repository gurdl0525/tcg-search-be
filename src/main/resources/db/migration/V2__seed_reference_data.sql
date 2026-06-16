insert into colors (code, name) values
    ('red', 'Red'),
    ('green', 'Green'),
    ('blue', 'Blue'),
    ('purple', 'Purple'),
    ('black', 'Black'),
    ('yellow', 'Yellow')
on conflict (code) do update set name = excluded.name;

insert into attributes (name) values
    ('Slash'),
    ('Strike'),
    ('Ranged'),
    ('Special'),
    ('Wisdom')
on conflict (name) do nothing;

insert into rarities (code, name) values
    ('L', 'Leader'),
    ('C', 'Common'),
    ('UC', 'Uncommon'),
    ('R', 'Rare'),
    ('SR', 'Super Rare'),
    ('SEC', 'Secret Rare'),
    ('P', 'Promotion')
on conflict (code) do update set name = excluded.name;

insert into external_marketplaces (code, name, search_url_template) values
    ('snkrdunk', 'SNKR DUNK', 'https://snkrdunk.com/en/search/result?keyword={query}')
on conflict (code) do update
set
    name = excluded.name,
    search_url_template = excluded.search_url_template,
    updated_at = now();
