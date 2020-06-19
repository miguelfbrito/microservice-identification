import sys
import pandas as pd
import matplotlib.pyplot as plt

data = pd.read_csv('./results_filtered_1.csv')
print(data.head())
print(data.info())

rows_for_removal = []
for i, j in data.iterrows():

    if pd.isna(j['CHD']):
        rows_for_removal.append(i)

    if j['CHM'] ==  1: 
        print(j['CHM'])
        rows_for_removal.append(i)

for row in rows_for_removal:
    data = data.drop([row])

print("\n")
for i, j in data.iterrows():
    print(j['CHM'])

fig = plt.figure()


fig1 = data.boxplot(column=['CHM', 'CHD'])

plt.show()
