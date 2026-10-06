// :c: lowpolysurf 2026

if (!process.env.DATABASE_URL) {
  throw new Error("DATABASE_URL is not defined in your .env file");
}

export const pool = new Pool({
  connectionString: process.env.DATABASE_URL,

  // Enable this only when DATABASE_SSL=true
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