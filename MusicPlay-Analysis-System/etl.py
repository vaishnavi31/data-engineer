import os
import glob
import psycopg2
import pandas as pd
from sql_queries import *


def process_song_file(cur, filepath):
    
    """
    - Reads the json files for songs_data from the folder and subfolders given in the filepath
    - Inserts data into songs and artsits table
    - Executes the cursor object in sparkifydb 
    """
    
    # open song file
    df = pd.read_json(filepath, typ= 'frame', lines= True)

    for value in df.values:
        num_songs, artist_id, artist_latitude, artist_longitude,artist_location, artist_name, song_id, title, duration,year = value
        
        # insert song record
        song_data = [song_id, title, artist_id, year, duration]
        cur.execute(song_table_insert, song_data)
    
        # insert artist record
        artist_data = [artist_id, artist_name, artist_location, artist_latitude, artist_longitude]
        cur.execute(artist_table_insert, artist_data)
        

def process_log_file(cur, filepath):
    
    """
    - Reads the json files for log_data from the folder and subfolders given in the filepath
    - Inserts data into users
    - Inserts data into songplay table by joining joining artists and songs tables using artist_id
    - Executes the cursor object in sparkifydb
    """
    
    # open log file
    df = pd.read_json(filepath, typ= 'frame', lines= True)

    # filter by NextSong action
    df = df[df.page == 'NextSong']
    
    # convert timestamp column to datetime
    t = pd.to_datetime(df['ts'], unit='ms')
    
    # insert time data records
    time_data = (df.ts.values, t.dt.hour.values, t.dt.day.values, t.dt.isocalendar().week.values, t.dt.month.values, t.dt.year.values, t.dt.day_name().values)
    column_labels = (['start_time', 'hour','day', 'week','month','year','weekday'])
    time_df = pd.DataFrame(dict(zip(column_labels, time_data))) 

    for i, row in time_df.iterrows():
        cur.execute(time_table_insert, list(row))

    # load user table
    user_df = df.loc[:, ['userId', 'firstName', 'lastName', 'gender', 'level']]

    # insert user records
    for i, row in user_df.iterrows():
        cur.execute(user_table_insert, row)

    # insert songplay records
    for index, row in df.iterrows():
        
        # get songid and artistid from song and artist tables
        cur.execute(song_select, (row.song, row.artist, row.length))
        results = cur.fetchone()
        
        if results:
            songid, artistid = results
        else:
            songid, artistid = None, None

        # insert songplay record
#         songplay_data = (row.ts, row.userId, row.level, songid, artistid, row.sessionId, row.location, row.userAgent)
        songplay_data = (str(row.ts), str(row.userId), str(row.level), str(songid) if songid is not None else None , str(artistid) if artistid is not None else None, str(row.sessionId), str(row.location), str(row.userAgent))
        cur.execute(songplay_table_insert, songplay_data)


def process_data(cur, conn, filepath, func):
    
    """
    - This function will put all the file name in folders and subfolders from the filepath specified into a list
    - Enumerates the list and processes the files by calling the appropriate functions
    """
    
    # get all files matching extension from directory
    all_files = []
    for root, dirs, files in os.walk(filepath):
        files = glob.glob(os.path.join(root,'*.json'))
        for f in files :
            all_files.append(os.path.abspath(f))

    # get total number of files found
    num_files = len(all_files)
    print('{} files found in {}'.format(num_files, filepath))

    # iterate over files and process
    for i, datafile in enumerate(all_files, 1):
        func(cur, datafile)
        conn.commit()
        print('{}/{} files processed.'.format(i, num_files))


def main():
    
    """
    - Creates and connects to the sparkifydb
    - Calls the process_data function for song and log files
    - Finally, Close the connection
    """
    
    conn = psycopg2.connect("host=127.0.0.1 dbname=vaish user=postgres password=Student")
    cur = conn.cursor()

    process_data(cur, conn, filepath='data/song_data', func=process_song_file)
    process_data(cur, conn, filepath='data/log_data', func=process_log_file)

    conn.close()


if __name__ == "__main__":
    main()
