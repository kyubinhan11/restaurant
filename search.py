import requests
import os
import json
import argparse

def get_label(radius):
    url= "https://api.yelp.com/v3/businesses/search"
    bearer_token = "FxUGHPWV1OvUnkx_x9aHc5fZHGD2L7-9qOUgftzrvqv2rnI_EpFflVb3yJNaKTQ8cIltpIJHgBU-PPH8suWYL1tEpaqDUcpFnVu6_w_E5aRClAXQ9lYQVyWdmKvIWHYx";
    url_params = {
            'radius': radius,
            'latitude': '49.24',
            'longitude': '-122.98'}
    headers = {
        'Authorization': 'Bearer %s' % bearer_token,
    }

    r = requests.request('GET', url, headers=headers, params=url_params)

    dic_label = r.json()
    #text = dic_label.values()[0]
    print dic_label


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('image_file', help='The image you\'d like to label.')
    args = parser.parse_args()
    get_label(args.image_file)
