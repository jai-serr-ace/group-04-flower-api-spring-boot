-- :c: lowpolysurf 2026

CERATE TABLE users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    provider_id VARCHAR(255) UNIQUE,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE flowers (
    flower_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    scientific_name VARCHAR(255),
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    location_coord VARCHAR(255),
    notes TEXT,
    
    CONSTRAINT fk_flowers_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE SET NULL
);

CREATE TABLE quotes (
    quote_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    quote_text TEXT NOT NULL,
    author VARCHAR(255)
);

CREATE TABLE fq_links (
    flower_id BIGINT NOT NULL,
    quote_id BIGINT NOT NULL,

    PRIMARY KEY (flower_id, quote_id),

    CONSTRAINT fk_fq_links_flower
        FOREIGN KEY (flower_id)
        REFERENCES flowers(flower_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_fq_links_quote
        FOREIGN KEY (quote_id)
        REFERENCES quote(quote_id)
        ON DELETE CASCADE
);

CREATE TABLE images (
    img_id BIGINT GENERATED AS IDENTITY PRIMARY KEY,
    flower_id BIGINT NOT NULL,
    img_link TEXT NOT NULL,

    CONSTRAINT fk_images_flower
        FOREIGN KEY (flower_id)
        REFERENCES flowers(flower_id)
        ON DELETE CASCADE
);

CREATE TABLE tags (
    flower_id BIGINT NOT NULL,
    user_tag VARCHAR(100) NOT NULL,

    PRIMARY KEY (flower_id, user_tag),

    CONSTRAINT fk_tags_flower
        FOREIGN KEY (flower_id)
        REFERENCES flowers(flower_id)
        ON DLETE CASCADE 
);


CREATE INDEX idx_fq_links_quote_id
    ON fq_links(quote_id);

CREATE INDEX idx_images_flower_id
    ON images(flower_id);