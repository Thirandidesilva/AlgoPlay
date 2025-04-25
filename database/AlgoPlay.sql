CREATE TABLE users (
    user_id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(20) NOT NULL
);

CREATE TABLE games (
    game_id integer NOT NULL,
    game_name character varying(50) NOT NULL
);

CREATE TABLE algorithm_performance (
    performance_id integer NOT NULL,
    game_id integer,
    round_id integer,
    algorithm_name character varying(100) NOT NULL,
    execution_time bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE game_rounds (
    round_id integer NOT NULL,
    game_id integer,
    user_id integer,
    is_correct boolean DEFAULT false,
    score integer,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.tower_of_hanoi_rounds (
    hanoi_id integer NOT NULL,
    user_id integer,
    num_disks integer NOT NULL,
    moves_count integer NOT NULL,
    moves_sequence text,
    optimal_moves integer NOT NULL,
    is_correct boolean NOT NULL
);

CREATE TABLE hanoi_algorithm_performance (
    performance_id integer NOT NULL,
    hanoi_id integer,
    algorithm_type character varying(20) NOT NULL,
    execution_time bigint NOT NULL,
    move_sequence text
);
