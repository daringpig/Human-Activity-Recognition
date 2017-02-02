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
        walk = pd.read_csv('walk.csv')
        down = pd.read_csv('down.csv')
        up = pd.read_csv('up.csv')

        # Group the dataframes by step then split
        walk = walk.groupby('step')
        self.walk = [walk.get_group(x) for x in walk.groups]
        down = down.groupby('step')
        self.down = [down.get_group(x) for x in down.groups]
        up = up.groupby('step')
        self.up = [up.get_group(x) for x in up.groups]

        # Create X and Y then extract features
        self.X = []
        self.Y = [0] * len(walk) + [1] * len(down) + [2] * len(up)

        #======================================================================
        # Mean
        #======================================================================
        # Vertical-acceleration
        x = [step['v-acc'].mean() for step in self.walk]
        y = [step['v-acc'].mean() for step in self.down]
        z = [step['v-acc'].mean() for step in self.up]
        self.X.append(x+y+z)
        # Total-acceleration
        x = [step['t-acc'].mean() for step in self.walk]
        y = [step['t-acc'].mean() for step in self.down]
        z = [step['t-acc'].mean() for step in self.up]
        self.X.append(x+y+z)
        # Pressure
        x = [step['pressure'].mean() for step in self.walk]
        y = [step['pressure'].mean() for step in self.down]
        z = [step['pressure'].mean() for step in self.up]
        self.X.append(x+y+z)
        # Horizontal-rotation
        x = [step['h-rot'].mean() for step in self.walk]
        y = [step['h-rot'].mean() for step in self.down]
        z = [step['h-rot'].mean() for step in self.up]
        self.X.append(x+y+z)
        # Vertical-rotation
        x = [step['v-rot'].mean() for step in self.walk]
        y = [step['v-rot'].mean() for step in self.down]
        z = [step['v-rot'].mean() for step in self.up]
        self.X.append(x+y+z)

        #======================================================================
        # Min
        #======================================================================
        # Vertical-acceleration
        x = [step['v-acc'].min() for step in self.walk]
        y = [step['v-acc'].min() for step in self.down]
        z = [step['v-acc'].min() for step in self.up]
        self.X.append(x+y+z)
        # Total-acceleration
        x = [step['t-acc'].min() for step in self.walk]
        y = [step['t-acc'].min() for step in self.down]
        z = [step['t-acc'].min() for step in self.up]
        self.X.append(x+y+z)
        # Pressure
        x = [step['pressure'].min() for step in self.walk]
        y = [step['pressure'].min() for step in self.down]
        z = [step['pressure'].min() for step in self.up]
        self.X.append(x+y+z)
        # Horizontal rotation
        x = [step['h-rot'].min() for step in self.walk]
        y = [step['h-rot'].min() for step in self.down]
        z = [step['h-rot'].min() for step in self.up]
        self.X.append(x+y+z)
        # Vertical rotation
        x = [step['v-rot'].min() for step in self.walk]
        y = [step['v-rot'].min() for step in self.down]
        z = [step['v-rot'].min() for step in self.up]
        self.X.append(x+y+z)

        #======================================================================
        # Max
        #======================================================================
        # Vertical-acceleration
        x = [step['v-acc'].max() for step in self.walk]
        y = [step['v-acc'].max() for step in self.down]
        z = [step['v-acc'].max() for step in self.up]
        self.X.append(x+y+z)
        # Total-acceleration
        x = [step['t-acc'].max() for step in self.walk]
        y = [step['t-acc'].max() for step in self.down]
        z = [step['t-acc'].max() for step in self.up]
        self.X.append(x+y+z)
        # Pressure
        x = [step['pressure'].max() for step in self.walk]
        y = [step['pressure'].max() for step in self.down]
        z = [step['pressure'].max() for step in self.up]
        self.X.append(x+y+z)
        # Horizontal rotation
        x = [step['h-rot'].max() for step in self.walk]
        y = [step['h-rot'].max() for step in self.down]
        z = [step['h-rot'].max() for step in self.up]
        self.X.append(x+y+z)
        # Vertical rotation
        x = [step['v-rot'].max() for step in self.walk]
        y = [step['v-rot'].max() for step in self.down]
        z = [step['v-rot'].max() for step in self.up]
        self.X.append(x+y+z)

        #======================================================================
        # Kurtosis (Fisher's definition)
        #======================================================================
        # Vertical-acceleration
        x = [step['v-acc'].kurtosis() for step in self.walk]
        y = [step['v-acc'].kurtosis() for step in self.down]
        z = [step['v-acc'].kurtosis() for step in self.up]
        self.X.append(x+y+z)
        # Total-acceleration
        x = [step['t-acc'].kurtosis() for step in self.walk]
        y = [step['t-acc'].kurtosis() for step in self.down]
        z = [step['t-acc'].kurtosis() for step in self.up]
        self.X.append(x+y+z)
        # Pressure
        x = [step['pressure'].kurtosis() for step in self.walk]
        y = [step['pressure'].kurtosis() for step in self.down]
        z = [step['pressure'].kurtosis() for step in self.up]
        self.X.append(x+y+z)
        # Horizontal rotation
        x = [step['h-rot'].kurtosis() for step in self.walk]
        y = [step['h-rot'].kurtosis() for step in self.down]
        z = [step['h-rot'].kurtosis() for step in self.up]
        self.X.append(x+y+z)
        # Vertical rotation
        x = [step['v-rot'].kurtosis() for step in self.walk]
        y = [step['v-rot'].kurtosis() for step in self.down]
        z = [step['v-rot'].kurtosis() for step in self.up]
        self.X.append(x+y+z)

        #======================================================================
        # Mean Absolute deviation
        #======================================================================
        # Vertical-acceleration
        x = [step['v-acc'].mad() for step in self.walk]
        y = [step['v-acc'].mad() for step in self.down]
        z = [step['v-acc'].mad() for step in self.up]
        self.X.append(x+y+z)
        # Total-acceleration
        x = [step['t-acc'].mad() for step in self.walk]
        y = [step['t-acc'].mad() for step in self.down]
        z = [step['t-acc'].mad() for step in self.up]
        self.X.append(x+y+z)
        # Pressure
        x = [step['pressure'].mad() for step in self.walk]
        y = [step['pressure'].mad() for step in self.down]
        z = [step['pressure'].mad() for step in self.up]
        self.X.append(x+y+z)
        # Horizontal rotation
        x = [step['h-rot'].mad() for step in self.walk]
        y = [step['h-rot'].mad() for step in self.down]
        z = [step['h-rot'].mad() for step in self.up]
        self.X.append(x+y+z)
        # Vertical rotation
        x = [step['v-rot'].mad() for step in self.walk]
        y = [step['v-rot'].mad() for step in self.down]
        z = [step['v-rot'].mad() for step in self.up]
        self.X.append(x+y+z)

        #======================================================================
        # Standard deviation
        #======================================================================
        # Vertical-acceleration
        x = [step['v-acc'].std() for step in self.walk]
        y = [step['v-acc'].std() for step in self.down]
        z = [step['v-acc'].std() for step in self.up]
        self.X.append(x+y+z)
        # Total-acceleration
        x = [step['t-acc'].std() for step in self.walk]
        y = [step['t-acc'].std() for step in self.down]
        z = [step['t-acc'].std() for step in self.up]
        self.X.append(x+y+z)
        # Pressure
        x = [step['pressure'].std() for step in self.walk]
        y = [step['pressure'].std() for step in self.down]
        z = [step['pressure'].std() for step in self.up]
        self.X.append(x+y+z)
        # Horizontal rotation
        x = [step['h-rot'].std() for step in self.walk]
        y = [step['h-rot'].std() for step in self.down]
        z = [step['h-rot'].std() for step in self.up]
        self.X.append(x+y+z)
        # Vertical rotation
        x = [step['v-rot'].std() for step in self.walk]
        y = [step['v-rot'].std() for step in self.down]
        z = [step['v-rot'].std() for step in self.up]
        self.X.append(x+y+z)

        #======================================================================
        # Skewness
        #======================================================================
        # Vertical-acceleration
        x = [step['v-acc'].skew() for step in self.walk]
        y = [step['v-acc'].skew() for step in self.down]
        z = [step['v-acc'].skew() for step in self.up]
        self.X.append(x+y+z)
        # Total-acceleration
        x = [step['t-acc'].skew() for step in self.walk]
        y = [step['t-acc'].skew() for step in self.down]
        z = [step['t-acc'].skew() for step in self.up]
        self.X.append(x+y+z)
        # Pressure
        x = [step['pressure'].skew() for step in self.walk]
        y = [step['pressure'].skew() for step in self.down]
        z = [step['pressure'].skew() for step in self.up]
        self.X.append(x+y+z)
        # Horizontal rotation
        x = [step['h-rot'].skew() for step in self.walk]
        y = [step['h-rot'].skew() for step in self.down]
        z = [step['h-rot'].skew() for step in self.up]
        self.X.append(x+y+z)
        # Vertical rotation
        x = [step['v-rot'].skew() for step in self.walk]
        y = [step['v-rot'].skew() for step in self.down]
        z = [step['v-rot'].skew() for step in self.up]
        self.X.append(x+y+z)
        # TODO: Compute pairwise correlation of columns

    def get_X_Y(self):
        '''
        Returns X and Y
        '''
        return self.X, self.Y

    def signal_magnitude_area(self):
        '''

        The signal magnitude area (abbreviated SMA or sma) is a
        statistical measure of the magnitude of a varying quantity.

        TODO: Needs X,Y,Z accelometer data

        sum_N(i)((|x(i)|)+(|y(i)|)+(|z(i)|))

        '''
        pass

    def energy(self):
        '''

        Energy measure. Sum of the squares divided by the number of values.

        '''
        pass

    def entropy(self):
        '''

        Signal entropy.
        @params:
            df - a dataframe containing a single step
        '''
        pass

    def iqr(self):
        '''

        Interquartile range

        @params:
            df - a dataframe containing a single step
        '''
        pass

if __name__ == '__main__':
    pass
