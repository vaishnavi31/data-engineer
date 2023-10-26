## MusicPlay Analysis System - Data Modeling with Postgres

## Dataset Information
# Song Dataset

The Song Dataset is a subset of data from the [MillionSongDataSet](http://millionsongdataset.com/). This dataset comprises JSON files, each containing metadata about a song and the artist of that song. The files are organized and partitioned by the first three letters of each song's track ID. Here's an example of how the dataset is structured:

>           song_data/A/B/C/TRABCEI128F424C983.json
>           song_data/A/A/B/TRAABJL12903CDCF1A.json

- Sample data: TRAABJL12903CDCF1A.json
>           {"num_songs": 1, "artist_id": "ARJIE2Y1187B994AB7", "artist_latitude": null, "artist_longitude": null, "artist_location": "", "artist_name": "Line Renaud", "song_id": "SOUPIRU12A6D4FA1E1", "title": "Der Kleine Dompfaff", "duration": 152.92036, "year": 0}
    
# Log Dataset

The Log Dataset comprises log files in JSON format generated by the [event simulator](https://github.com/Interana/eventsim). These logs simulate user activity from a music streaming app and are based on the songs in the Song Dataset described above. The log files in this dataset are organized and partitioned by year and month. Here's an example of how the dataset is structured:

> log_data/2018/11/2018-11-12-events.json
> log_data/2018/11/2018-11-13-events.json

- Sample data:
>           {"artist":null,"auth":"Logged In","firstName":"Kaylee","gender":"F","itemInSession":0,"lastName":"Summers","length":null,"level":"free","location":"Phoenix-Mesa-Scottsdale, AZ","method":"GET","page":"Home","registration":1540344794796.0,"sessionId":139,"song":null,"status":200,"ts":1541106106796,"userAgent":"\"Mozilla\/5.0 (Windows NT 6.1; WOW64) AppleWebKit\/537.36 (KHTML, like Gecko) Chrome\/35.0.1916.153 Safari\/537.36\"","userId":"8"}

## ER Diagram

![ER Diagram](https://github.com/vaishnavi31/data-engineering-projects/blob/main/MusicPlay-Analysis-System/ERDiagram.png)

## Schema for Song Play Analysis

For the Song Play Analysis project, a star schema is used to optimize queries on song play data. This schema includes the following tables:

### Fact Table

**songplays** - Records in the log data associated with song plays (i.e., records with the "page NextSong" action).
- `songplay_id` (SERIAL): Unique identifier for each song play.
- `start_time` (BIGINT): Timestamp when the song play occurred.
- `user_id` (INT): User ID associated with the song play.
- `level` (VARCHAR): User's subscription level (e.g., free or paid).
- `song_id` (VARCHAR): ID of the song played.
- `artist_id` (VARCHAR): ID of the song's artist.
- `session_id` (INT): Unique session identifier.
- `location` (VARCHAR): Location where the song was played.
- `user_agent` (VARCHAR): User agent information.

### Dimension Tables

**users** - Information about app users.
- `user_id` (INT): Unique identifier for each user.
- `first_name` (VARCHAR): User's first name.
- `last_name` (VARCHAR): User's last name.
- `gender` (CHAR(1)): User's gender.
- `level` (VARCHAR): User's subscription level.

**songs** - Information about songs in the music database.
- `song_id` (VARCHAR): Unique identifier for each song.
- `title` (TEXT): Title of the song.
- `artist_id` (VARCHAR): ID of the song's artist.
- `year` (INT): Year of the song's release.
- `duration` (NUMERIC): Duration of the song in seconds.

**artists** - Information about artists in the music database.
- `artist_id` (VARCHAR): Unique identifier for each artist.
- `name` (VARCHAR): Artist's name.
- `location` (TEXT): Artist's location.
- `latitude` (DECIMAL): Latitude of the artist's location.
- `longitude` (DECIMAL): Longitude of the artist's location.

**time** - Timestamps of records in songplays, broken down into specific units.
- `start_time` (BIGINT): Timestamp.
- `hour` (INT): Hour of the timestamp.
- `day` (INT): Day of the month of the timestamp.
- `week` (INT): Week of the year of the timestamp.
- `month` (INT): Month of the timestamp.
- `year` (INT): Year of the timestamp.
- `weekday` (VARCHAR): Day of the week of the timestamp.

This star schema facilitates efficient analysis and querying of song play data from the log files, enabling insights into user behavior, music preferences, and more.


## How Fact and Dimension Tables Support Analysis

The Fact and Dimension tables in this schema enable comprehensive analysis of song plays and user behavior:

### Fact Table

**songplays** records key details of each song play. This table provides insights into song popularity, user interaction, and more. Analysts can query and aggregate this data to understand trends such as when and where songs are played.

### Dimension Tables

1. **users**: Offers a deep dive into user demographics and preferences. Analysts can segment users by gender, subscription level, and more to tailor recommendations and marketing strategies.

2. **songs**: Provides information about the songs, including release year and duration. This table helps in identifying trends in music preferences and analyzing song popularity over time.

3. **artists**: Offers insights into artist attributes and their distribution. Analysts can explore the geographic spread of artists and the relationship between artist location and song popularity.

4. **time**: Breaks down songplays into time units, making it easy to analyze trends by hour, day, week, month, and year. This dimension is invaluable for understanding when users are most active.

By leveraging these tables, the schema facilitates a wide range of queries and reports, empowering data analysts to uncover valuable insights about user engagement, music preferences, and more.
Feel free to use or modify this content in your GitHub README as needed.


## Files in Project workspace

* **test.ipynb** displays the first few rows of each table to let you check your database.
* **sql_queries.py** contains sql queries for creating, inserting and selecting fact and dimension tables.
* **create_tables.py** drops and creates your tables making use of sql_queries.py. You can also run this file to reset your tables before each time you run your ETL scripts.
* **etl.ipynb** is a test file I created that reads and processes a single file from song_data and log_data and loads them into your tables.
* **etl.py** reads and processes files from song_data and log_data and loads them into your tables.
* **requirements.txt** has the required dependencies. 
    Created this file in a local virtual environment using 
    >       pip freeze --local > requirements.txt
    Use below command to install dependencies of the project in your local branch:
    >       pip install -r requirements.txt
* **README.md** provides discussion on my project.

## How to run this project

* I also created a main.py that internally calls both of the above files, run below command in working directory:
    >       python main.py


### Probable issue and links to fix them
* [psycopg2 can't adapt tyoe int64 or uni32](https://devpress.csdn.net/python/6304c8c2c67703293080df4e.html)
* [iterrows() index error](https://stackoverflow.com/questions/47665812/index-out-of-bound-when-iterrow-how-is-this-possible)
