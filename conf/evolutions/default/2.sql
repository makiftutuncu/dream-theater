-- !Ups

CREATE TABLE "dreams"(
    "id"             UUID PRIMARY KEY,
    "user_id"        UUID NOT NULL REFERENCES "users"("id"),
    "title"          TEXT NOT NULL,
    "body"           TEXT NOT NULL,
    "attachment_url" TEXT,
    "created_at"     TIMESTAMPTZ(3) NOT NULL,
    "updated_at"     TIMESTAMPTZ(3) NOT NULL,
    "deleted_at"     TIMESTAMPTZ(3)
);

-- !Downs

DROP TABLE "dreams";
