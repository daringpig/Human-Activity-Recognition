'''

data_labeller.py
~~~~~~~~~~~~~~~~~~~

Labels the data by step

Author: David O'Keeffe, Lachlan Giles
'''

import csv
import sys


if __name__ == '__main__':
    # Read in the data
    try:
        csvinput = open(sys.argv[1], 'r')
    except IOError:
        print('File opening failed!!')

    csvoutput = open(sys.argv[1] + '_n', 'w')
    writer = csv.writer(csvoutput, lineterminator='\n')
    reader = csv.reader(csvinput)
    step = 0
    all_rows = []
    next(reader)  # skip header

    # Label the steps
    for row in reader:
        if float(row[0]) == 0.0:
            step += 1
        row.append(step)
        all_rows.append(row)

    # Write to CSV
    writer.writerows(all_rows)
