CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS routes (
                                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      user_id BIGINT REFERENCES users(id),
    origin_lat DOUBLE PRECISION,
    origin_lon DOUBLE PRECISION,
    destination_lat DOUBLE PRECISION,
    destination_lon DOUBLE PRECISION,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS blocked_areas (
                                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                             route_id BIGINT REFERENCES routes(id),
    polygon_coordinates_json TEXT NOT NULL
    );
