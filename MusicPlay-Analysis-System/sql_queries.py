# DROP TABLES

songplay_table_drop = "DROP TABLE IF EXISTS songplays"
user_table_drop = "DROP TABLE IF EXISTS users"
song_table_drop = "DROP TABLE IF EXISTS songs"
artist_table_drop = "DROP TABLE IF EXISTS artists"
time_table_drop = "DROP TABLE IF EXISTS time"

# CREATE TABLES

# Since songplays is a fact table we have mostly id's in this table
# The serial keyword in postgresql allows the column to auto-increment when an entry is done in this table. Primary key works just like any other SQL
songplay_table_create = ("""
CREATE TABLE IF NOT EXISTS songplays
                (songplay_id SERIAL CONSTRAINT pk_songPlays PRIMARY KEY, 
                 start_time BIGINT REFERENCES time (start_time), 
                 user_id INT REFERENCES users (user_id), 
                 level VARCHAR NOT NULL, 
                 song_id VARCHAR REFERENCES songs (song_id), 
                 artist_id VARCHAR REFERENCES artists (artist_id), 
                 session_id INT NOT NULL,  
                 location VARCHAR NOT NULL, 
                 user_agent VARCHAR NOT NULL)
""")

user_table_create = ("""
CREATE TABLE IF NOT EXISTS users
                (user_id INT CONSTRAINT pk_users PRIMARY KEY, 
                 first_name VARCHAR, 
                 last_name VARCHAR, 
                 gender CHAR(1), 
                 level VARCHAR NOT NULL)
""")

# REFERENCES artists (artist_id)
song_table_create = ("""
CREATE TABLE IF NOT EXISTS songs
                (song_id VARCHAR CONSTRAINT pk_songs PRIMARY KEY, 
                 title TEXT, 
                 artist_id VARCHAR, 
                 year INT, 
                 duration NUMERIC,
                 CONSTRAINT ck_songsYearGreaterThanZero CHECK (year>=0))
""")

artist_table_create = ("""
CREATE TABLE IF NOT EXISTS artists
                (artist_id VARCHAR CONSTRAINT pk_artists PRIMARY KEY, 
                 name VARCHAR, 
                 location TEXT, 
                 latitude DECIMAL, 
                 longitude DECIMAL)
""")

time_table_create = ("""
CREATE TABLE IF NOT EXISTS time
                (start_time BIGINT CONSTRAINT pk_time PRIMARY KEY, 
                 hour INT NOT NULL, 
                 day  INT NOT NULL, 
                 week INT NOT NULL, 
                 month INT NOT NULL, 
                 year INT NOT NULL, 
                 weekday VARCHAR NOT NULL,
                 CONSTRAINT ck_timeGreaterThanZero CHECK(hour>=0 and day>=0 and week>=0 and month>=0 AND year>=0))
""")

# INSERT RECORDS

songplay_table_insert = ("""
INSERT INTO songplays (start_time, user_id, level, song_id, artist_id, session_id, location, user_agent) 
VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
""")

# The users table has a special clause "ON CONFLICT" that lets you handle the insert when there is a conflict in the mentioned columns
# Based on the assumption that a new user entry with a conflict will have a different level, we are doing an update 

user_table_insert = ("""
INSERT INTO users (user_id, first_name, last_name, gender, level) 
VALUES (%s, %s, %s, %s, %s)
ON CONFLICT (user_id)
DO UPDATE
SET level =  EXCLUDED.level
""")

song_table_insert = ("""
INSERT INTO songs (song_id, title, artist_id, year, duration) 
VALUES (%s, %s, %s, %s, %s)
ON CONFLICT (song_id)
DO NOTHING
""")

artist_table_insert = ("""
INSERT INTO artists (artist_id, name, location, latitude, longitude) 
VALUES (%s, %s, %s, %s, %s)
ON CONFLICT (artist_id)
DO NOTHING
""")


time_table_insert = ("""
INSERT INTO time (start_time, hour, day, week, month, year, weekday) 
VALUES (%s, %s, %s, %s, %s, %s, %s)
ON CONFLICT (start_time) 
DO NOTHING
""")

# FIND SONGS

song_select = ("""
SELECT S.song_id, S.title as Song_Name, A.name Artist_Name  
FROM SONGS S
JOIN ARTISTS A
ON S.artist_id = A.artist_id
WHERE S.song_id = %s
and A.name = %s
and S.duration = %s
""")

# QUERY LISTS

create_table_queries = [user_table_create, time_table_create, artist_table_create, song_table_create,songplay_table_create]
drop_table_queries = [songplay_table_drop, user_table_drop, song_table_drop, artist_table_drop, time_table_drop]