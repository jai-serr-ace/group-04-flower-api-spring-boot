// :c: lowpolysurf 2026

import "dotenv/config";
import pg from "pg";

const { Pool } = pg;

if (!process.env.DATABASE_URL) {
    throw new Error("DATABASE_URL is missing from your .env file");
}

console.log("DATABASE_URL loaded:", Boolean(process.env.DATABASE_URL));

export const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl:
        process.env.DATABASE_SSL === "true"
            ? { rejectUnauthorized: false }
            : false
});

pool.on("error", (error) => {
    console.error("Unexpected PostgreSQL error:", error);
});

export async function query(text, params) {
    return pool.query(text, params);
}