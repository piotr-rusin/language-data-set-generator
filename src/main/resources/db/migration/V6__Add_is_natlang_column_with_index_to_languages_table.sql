alter table languages add column is_natlang boolean default false;
create index on languages (is_natlang);