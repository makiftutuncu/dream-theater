-- !Ups

CREATE TABLE "users"(
    "id"         UUID PRIMARY KEY,
    "email"      TEXT NOT NULL UNIQUE,
    "password"   TEXT NOT NULL,
    "salt"       TEXT NOT NULL,
    "first_name" TEXT,
    "last_name"  TEXT,
    "gender"     TEXT,
    "birth_date" DATE,
    "created_at" TIMESTAMPTZ(3) NOT NULL,
    "updated_at" TIMESTAMPTZ(3) NOT NULL,
    "deleted_at" TIMESTAMPTZ(3)
);

CREATE TABLE "sessions"(
    "id"         UUID PRIMARY KEY,
    "user_id"    UUID NOT NULL REFERENCES "users"("id"),
    "token"      TEXT NOT NULL UNIQUE,
    "created_at" TIMESTAMPTZ(3) NOT NULL,
    "updated_at" TIMESTAMPTZ(3) NOT NULL,
    "deleted_at" TIMESTAMPTZ(3)
);

-- !Downs

DROP TABLE "sessions";
DROP TABLE "users";
