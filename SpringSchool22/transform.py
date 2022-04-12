import pandas as pd
from collections import defaultdict
#path = "Predicting Cluster Transitions with Member Engagements.csv"
path = "Transition_Feature_Importances.csv"
df = pd.read_csv(path, header=[0])

for row in df.itertuples():
    for i,v in enumerate(row):
        i_val = i - 2
        if i_val >= 0:
            if v != "-":
                print(list(row)[0],i_val,v)
            

d = defaultdict(lambda: [])
columns = list(df)[2:]
for row in df.itertuples():
    for column in columns:
        value = getattr(row, column)
        if value > 0:
            d[str(row.start) + "," + str(row.end)].append((column, value))

for key in list(d.keys()):
    values = d[key]
    sort = sorted(values, key = lambda x: x[1], reverse=True)
    transform = list(map(lambda x: str(x[0]) + "," + str(x[1]), sort))
    row = ",".join(transform)
    complete = key + "," + row
    print(complete)
