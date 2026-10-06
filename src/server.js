import "dotenv/config";
import express from "express";
import pg from "pg";

const { Pool } = pg;

const app = express();
const pool = new Pool({
    connectionString: process.env.DATABASE_URL
});

app.use(express.json());

app.get("/api/v1/health", async (req, res) => {
    try {
        await pool.query("SELECT 1");
        res.json({ status: "ok", database: "connected" });
    } catch (error) {
        res.status(500).json({ status: "error" });
    }
});

const port = process.env.PORT || 3000;

app.listen(port, () => {
    console.log(`FlowerAPI running on port ${port}`);
});