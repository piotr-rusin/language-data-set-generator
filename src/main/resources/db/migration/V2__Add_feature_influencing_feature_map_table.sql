create table feature_influencing_feature_map
(
    feature             integer not null
        constraint fk_feature_influencing_feature_map_feature_id
            references features
            on update restrict on delete restrict,
    influencing_feature integer not null
        constraint fk_feature_influencing_feature_map_influencing_feature_id
            references features
            on update restrict on delete restrict,
    constraint pk_feature_influencing_feature_map
        primary key (feature, influencing_feature)
);