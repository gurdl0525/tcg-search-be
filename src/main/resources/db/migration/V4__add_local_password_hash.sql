alter table app_users
    add column password_hash text;

alter table app_users
    add constraint app_users_password_hash_not_blank
    check (password_hash is null or length(trim(password_hash)) > 0);
