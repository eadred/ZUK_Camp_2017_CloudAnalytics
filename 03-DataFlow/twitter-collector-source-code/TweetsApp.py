import json
import os
from os.path import join, dirname
import sys
from pprint import pprint
from dotenv import load_dotenv
from google.cloud import pubsub
import tweepy

dotenv_path = join(dirname(__file__), '.env')
dotenv_path_private = join(dirname(__file__), '.private.env')
load_dotenv(dotenv_path)
load_dotenv(dotenv_path_private)

class MyStreamListener(tweepy.StreamListener):

    def __init__(self):
        super(MyStreamListener, self).__init__()

        self.__client = pubsub.Client()
        self.__topic = self.__client.topic(os.environ.get("TOPIC_NAME"))
        if not self.__topic.exists():
            self.__topic.create()

    def on_status(self, status):

        location = {'geolocated': False}

        if status.coordinates is not None:
            if status.coordinates['type'] == 'Point':
                location['coordinates'] = {
                    'lon': status.coordinates['coordinates'][0],
                    'lat': status.coordinates['coordinates'][1]
                }
                location['geolocated'] = True

        message = {
            'usr': status.user.screen_name,
            'tstamp': str(status.created_at),
            'tweet': status.text,
            'hashtags': [{'text': ht['text']} for ht in status.entities['hashtags']],
            'mentions': [{'screen_name': ht['screen_name']} for ht in status.entities['user_mentions']],
            'location': location,
            'lang': status.lang
        }
        
        if status.place is not None:
            message['place'] = {
                'name': status.place.name,
                'full_name': status.place.full_name,
                'country_code': status.place.country_code,
                'place_type': status.place.place_type,
                'bounding_box': {
                    'coordinates': [ {'lon': coord_pair[0], 'lat': coord_pair[1]} for coord_pair in status.place.bounding_box.coordinates[0] ], # bounding_box.coordinates is an array of an array of lon/lat pairs
                    'type': 'polygon'
                }
            }
        
        data = json.dumps(message)

        self.__print_utf8(data)

        try:
            res = self.__topic.publish(data)
            pprint(res)
        except Exception as e:
            print('Error')
            print(e)

        print("")

    def on_error(self, status_code):
        if status_code == 420:
            return False

    def __print_utf8(self, unicode_string):
        encoded = unicode_string.encode('utf-8')
        print(encoded)

auth = tweepy.OAuthHandler(
    os.environ.get("TWITTER_CONSUMER_TOKEN"),
    os.environ.get("TWITTER_CONSUMER_SECRET")
)
auth.set_access_token(
    os.environ.get("TWITTER_ACCESS_TOKEN"),
    os.environ.get("TWITTER_ACCESS_TOKEN_SECRET")
)

def do_stream():
    stream = tweepy.Stream(auth = auth, listener=MyStreamListener())

    try:
        # Tweets for the whole world
        stream.filter(locations=[-180,-90,180,90])
        
        # Tweets for Europe
        # stream.filter(locations=[-10,36,60,72])

        # Only listen for tweets in the UK
        # stream.filter(locations=[-10.854,49.823,2.021,59.479])
    except Exception as e:
        print(e)
        do_stream()

do_stream()
