import argparse
import csv
from math import log2

NStates, NTrans, NTraces, NNonFitting, Coverage, Relevance, CostBits = range(7)

parser = argparse.ArgumentParser(description='Postprocess entropic relevance values.')
parser.add_argument('ifname', metavar='IN', type=str, 
                   help='Input file name')

args = parser.parse_args()

with open(args.ifname) as csvfile:
    for row in csv.reader(csvfile, quoting=csv.QUOTE_NONNUMERIC):
        size, relevance, background, selector = (
            row[NStates]+row[NTrans],       # Size
            row[Relevance],                 # Relevance
            row[CostBits] / row[NTraces],   # Background
            0 if row[Coverage] in [0,1] else -row[Coverage]*log2(row[Coverage]) - (1 - row[Coverage])*log2(1 - row[Coverage])
        )
        print("%d,%f,%f,%f,%f" % (size, relevance, background, relevance - background - selector, selector))
