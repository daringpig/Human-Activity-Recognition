'''

stair_data.py
~~~~~~~~~~~~~~~~~~~

Attempts to classify between walking, walking upstairs, and walking downstairs
from sensor data collected on a Samsung S6 Galaxy Smartphone

Authors: David O'Keeffe, Lachlan Giles
'''

import pandas as pd


class StairData:
    def __init__(self):
        # Read in the data using Pandas
        df_all = pd.read_csv('all_data_V2.csv')

        # Pull out every column
        features = [df_all[col].tolist() for col in list(df_all.columns.values)]
        print(features)



if __name__ == '__main__':
    StairData()
