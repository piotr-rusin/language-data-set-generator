create table feature_areas
(
    id   serial      not null
        constraint feature_areas_pkey
            primary key,
    name varchar(20) not null
        constraint feature_areas_name_unique
            unique
);

create table features
(
    id      serial       not null
        constraint features_pkey
            primary key,
    wals_id varchar(4)   not null
        constraint features_wals_id_unique
            unique,
    name    varchar(104) not null
        constraint features_name_unique
            unique,
    area    integer      not null
        constraint fk_features_area_id
            references feature_areas
            on update restrict on delete restrict
);

create index features_area
    on features (area);

create table feature_values
(
    id      serial      not null
        constraint feature_values_pkey
            primary key,
    wals_id varchar(7)  not null
        constraint feature_values_wals_id_unique
            unique,
    name    varchar(94) not null
        constraint feature_values_name_unique
            unique,
    feature integer     not null
        constraint fk_feature_values_feature_id
            references features
            on update restrict on delete restrict
);

create index feature_values_feature
    on feature_values (feature);

create table language_families
(
    id   serial      not null
        constraint language_families_pkey
            primary key,
    name varchar(30) not null
        constraint language_families_name_unique
            unique
);

create table macroareas
(
    id   serial      not null
        constraint macroareas_pkey
            primary key,
    name varchar(13) not null
        constraint macroareas_name_unique
            unique
);

create table languages
(
    id        serial      not null
        constraint languages_pkey
            primary key,
    wals_id   varchar(3)  not null
        constraint languages_wals_id_unique
            unique,
    name      varchar(46) not null
        constraint languages_name_unique
            unique,
    family    integer     not null
        constraint fk_languages_family_id
            references language_families
            on update restrict on delete restrict,
    macroarea integer     not null
        constraint fk_languages_macroarea_id
            references macroareas
            on update restrict on delete restrict
);

create index languages_family
    on languages (family);

create index languages_macroarea
    on languages (macroarea);

create table language_feature_value_map
(
    language      integer not null
        constraint fk_language_feature_value_map_language_id
            references languages
            on update restrict on delete restrict,
    feature_value integer not null
        constraint fk_language_feature_value_map_feature_value_id
            references feature_values
            on update restrict on delete restrict,
    constraint pk_language_feature_value_map
        primary key (language, feature_value)
);