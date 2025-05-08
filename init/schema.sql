CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS routes (
                                      id SERIAL PRIMARY KEY,
                                      user_id INTEGER REFERENCES users(id),
    origin_lat DOUBLE PRECISION,
    origin_lon DOUBLE PRECISION,
    destination_lat DOUBLE PRECISION,
    destination_lon DOUBLE PRECISION,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS blocked_areas (
                                             id SERIAL PRIMARY KEY,
                                             route_id INTEGER REFERENCES routes(id),
    polygon_coordinates_json TEXT NOT NULL
    );
