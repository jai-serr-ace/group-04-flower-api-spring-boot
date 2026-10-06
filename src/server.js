// :c: lowpolysurf 2026

import "dotenv/config";
import express from "express";
import { query } from "./db.js";

const app = express();

app.use(express.json());

app.get("/api/v1/health", async (req, res) => {
    try {
        await query("SELECT 1");

        res.json({
            status: "ok",
            database: "connected"
        });
    } catch (error) {
        console.error("Database connection failed:", error);

        res.status(500).json({
            status: "error",
            database: "disconnected"
        });
    }
});

const port = process.env.PORT || 3000;

app.listen(port, () => {
    console.log(`FlowerAPI running on port ${port}`);
});