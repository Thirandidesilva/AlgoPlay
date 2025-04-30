-- USERS table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL
);

-- GAMES table
CREATE TABLE games (
    game_id SERIAL PRIMARY KEY,
    game_name VARCHAR(50) NOT NULL
);

-- GAME_ROUNDS table
CREATE TABLE game_rounds (
    round_id SERIAL PRIMARY KEY,
    game_id INTEGER REFERENCES games(game_id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    is_correct BOOLEAN DEFAULT false,
    score INTEGER,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ALGORITHM_PERFORMANCE table
CREATE TABLE algorithm_performance (
    performance_id SERIAL PRIMARY KEY,
    game_id INTEGER REFERENCES games(game_id) ON DELETE CASCADE,
    round_id INTEGER REFERENCES game_rounds(round_id) ON DELETE CASCADE,
    algorithm_name VARCHAR(100) NOT NULL,
    execution_time BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- TOWER_OF_HANOI_ROUNDS table
CREATE TABLE public.tower_of_hanoi_rounds (
    hanoi_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    num_disks INTEGER NOT NULL,
    moves_count INTEGER NOT NULL,
    moves_sequence TEXT,
    optimal_moves INTEGER NOT NULL,
    is_correct BOOLEAN NOT NULL
);

-- HANOI_ALGORITHM_PERFORMANCE table
CREATE TABLE hanoi_algorithm_performance (
    performance_id SERIAL PRIMARY KEY,
    hanoi_id INTEGER REFERENCES tower_of_hanoi_rounds(hanoi_id) ON DELETE CASCADE,
    algorithm_type VARCHAR(20) NOT NULL,
    execution_time BIGINT NOT NULL,
    move_sequence TEXT
);

CREATE TABLE tictactoe_rounds (
    round_id SERIAL PRIMARY KEY,
    game_id INTEGER REFERENCES games(game_id),
    user_id INTEGER REFERENCES users(user_id),
    result VARCHAR(10) NOT NULL, -- 'win', 'loss', 'draw'
    difficulty VARCHAR(10) NOT NULL, -- 'EASY', 'MEDIUM', 'HARD'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO games (game_name)
VALUES
    ('Tic-Tac-Toe'),
    ('Tower-Of-Hanoi'),
    ('Traveling Salesman Problem'),
    ('Eight Queens Puzzle'),
    ('Knights Tour Problem');


CREATE TABLE ttt_game_results (
    result_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id),
    player_name VARCHAR(50) NOT NULL,
    difficulty VARCHAR(10) NOT NULL, -- 'EASY', 'MEDIUM', 'HARD'
    result VARCHAR(10) NOT NULL, -- 'win', 'loss', 'draw'
    player_moves INTEGER NOT NULL,
    ai_moves INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE ttt_algorithm_performance (
    perf_id SERIAL PRIMARY KEY,
    result_id INTEGER REFERENCES ttt_game_results(result_id) ON DELETE CASCADE,
    algorithm_name VARCHAR(100) NOT NULL,
    execution_time BIGINT NOT NULL,
    move_number INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--KNIGHT_TOUR_SOLUTION table
CREATE TABLE IF NOT EXISTS knight_tour_solutions (
    id SERIAL PRIMARY KEY,
    game_id INTEGER DEFAULT 5 REFERENCES games(game_id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    start_position VARCHAR(10) NOT NULL,
    solution_path TEXT NOT NULL,
    execution_time BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
