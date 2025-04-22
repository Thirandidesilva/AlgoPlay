CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(20)NOT NULL UNIQUE 
);  

CREATE TABLE games (
    game_id SERIAL PRIMARY KEY,
    game_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tower_of_hanoi_rounds (
    hanoi_id SERIAL PRIMARY KEY,
    round_id INTEGER REFERENCES game_rounds(round_id),
    num_disks INTEGER NOT NULL,
    moves_count INTEGER NOT NULL,
    moves_sequence TEXT,
    algorithm_recursive_time BIGINT,
    algorithm_iterative_time BIGINT,
    algorithm_four_peg_time BIGINT
); 

CREATE TABLE game_rounds (
    round_id SERIAL PRIMARY KEY,
    game_id INTEGER REFERENCES games(game_id),
    user_id INTEGER REFERENCES users(user_id),
    is_correct BOOLEAN DEFAULT FALSE,
    score INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
); 

CREATE TABLE algorithm_performance (
    performance_id SERIAL PRIMARY KEY,
    game_id INTEGER REFERENCES games(game_id),
    round_id INTEGER REFERENCES game_rounds(round_id),
    algorithm_name VARCHAR(100) NOT NULL,
    execution_time BIGINT NOT NULL,  -- in milliseconds
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
); 

insert into games (game_name) values 
('Tic-Tac-Toe'),
('Tower-Of-Hanoi'), 
('Traveling Salesman Problem'),
('Eight Queens Puzzle'),
('knights tour Problem');
