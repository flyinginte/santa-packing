from sets import Set
from scipy.stats.stats import pearsonr
import numpy as np

with open("output/compare30.csv") as f:
    lines = map(lambda x: map(lambda y: int(y.strip("\n")) ,x.split(",")), f.readlines())


# for some reason there are duplicates in the compare file, so put into set to remove
lineSet = Set()

for line in lines:
    lineSet.add((line[0],line[1]))

x = []
y = []
for pair in lineSet:
    x.append(pair[1])
    y.append(pair[0])

corr = pearsonr(x,y)
print corr