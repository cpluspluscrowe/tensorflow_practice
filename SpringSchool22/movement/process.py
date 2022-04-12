# Vear ran
# /usr/local/anaconda3/bin/python3
import pandas as pd
path = "./movement.csv"
df = pd.read_csv(path)
l = []
columns = ["Column0","Column1","Column2","Column3","Column4","Column5"]
for row in df.itertuples():
    for column in columns:
        value = getattr(row, column)
        if value > 0:
            column = column.replace("Column","")
            if column != str(row.Index):
                l.append((str(row.Index), str(int(column.replace("Column",""))),value))

sources = []
targets = []
values = []
for x in l:
    a,b,c = x
    sources.append(a)
    targets.append(b)
    values.append(c)
    
import pandas as pd
import numpy as np

import holoviews as hv
import plotly.graph_objects as go
import plotly.express as pex

import plotly.graph_objects as go

names = ["External Website Conversions", "Likes","Shares","Website Postview Conversions","Impressions","Langing Page Clicks","Company Page Clicks","Comments/Likes","Company Page Clicks/Other Engagements","External Website Postclick Conversions"]
import urllib, json
url = 'https://raw.githubusercontent.com/plotly/plotly.js/master/test/image/mocks/sankey_energy.json'
response = urllib.request.urlopen(url)
data = json.loads(response.read())
color =  data['data'][0]['node']['color']
fig = go.Figure(data=[go.Sankey(
    node = dict(
      pad = 15,
      thickness = 20,
      line = dict(color = "black", width = 0.5),
        label = names,
      color = "grey"
    ),
    link = dict(
        source = sources,# [0, 1, 0, 2, 3, 3], # indices correspond to labels, eg A1, A2, A1, B1, ...
        target = targets,#[2, 3, 3, 4, 4, 5],
        value = values,#[8, 4, 2, 8, 4, 2] 
        color = color
  ))])

fig.update_layout(title_text="Sankey Diagram of Cluster Transitions", font_size=24)
fig.show()
