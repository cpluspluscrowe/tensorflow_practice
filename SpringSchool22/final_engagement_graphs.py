import pandas as pd
from collections import defaultdict
path = "Transition_Engagements.csv"

df = pd.read_csv(path)
class Engagement():
    def __init__(self,name,amount):
        self.name = name
        self.amount = amount

class State():
    def __init__(self, name, engagements):
        self.name = name
        self.engagements = engagements

metrics = '''impressions landingpageclicks otherengagements externalwebsiteconversions externalwebsitepost-viewconversions externalwebsitepost-clickconversions likes commentlikes comments shares follows one-clickleadformopens one-clickleads'''.split()

name_mapping ={    "likes":"Likes",
    "shares":"Shares",
                   "follows":"Follows",
    "externalwebsiteconversions":"External Website Conversions",
    "impressions":"Impressions",
                   "one-clickleads":"One-Click Leads",
    "landingpageclicks":"Landing Page Clicks",
    "company-page clicks":"Company Page Clicks",
"commentlikes":"Comment Likes",
    "comments":"Comments",
    "otherengagements":"Other Engagements",
    "externalwebsitepost-clickconversions":"External Website Post-Click Conversion",
                   "one-clickleadformopens":"One-Click Lead Form Opens",
    "externalwebsitepost-viewconversions":"External Website Post-View Conversion"}    

states = defaultdict(lambda: defaultdict(lambda: 0))
for row in df.itertuples():
    end = row.Description2.strip()
    start = row.Description1.strip()
    engagements = []
    for metric in metrics:
        value = getattr(row,metric,0)
        metric_name = name_mapping[metric]
        if value > 0:
            engagement = Engagement(metric_name, value)
            engagements.append(engagement)
    state = State(end, engagements)
    for engagement in engagements:
        states[end][engagement.name] += engagement.amount

import pandas as pd
import numpy as np
import holoviews as hv
import plotly.graph_objects as go
import plotly.express as pex
import urllib, json
import plotly.graph_objects as go

for key in states:
    #state = states[0]
    names = [key] + list(map(lambda x: x, list(states[key].keys())))
    state_index = names.index(key)
    
    sources = []
    targets = []
    values = []
    for engagement_key in states[key]:
        name_index = names.index(engagement_key)
        amount = states[key][engagement_key]
        sources.append(name_index)
        targets.append(state_index)
        values.append(amount)
        
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
            color = color[:len(names)]
        ))])
    
    fig.update_layout(title_text="Sankey Diagram of Engagements Preceeding {0}".format(key), font_size=12)
    fig.write_image("PreceedingEngagement/" + key + ".png")



states = defaultdict(lambda: defaultdict(lambda: 0))
for row in df.itertuples():
    end = row.Description1.strip()
    start = row.Description2.strip()
    engagements = []
    for metric in metrics:
        value = getattr(row,metric,0)
        metric_name = name_mapping[metric]
        if value > 0:
            engagement = Engagement(metric_name, value)
            engagements.append(engagement)
    state = State(end, engagements)
    for engagement in engagements:
        states[engagement.name][start] += engagement.amount

# what engagements are impressions driving.
# 25% of impressions goes to cluster x, vs cluster y




for key in states:
    #state = states[0]
    names = [key] + list(map(lambda x: x, list(states[key].keys())))
    state_index = names.index(key)
    
    sources = []
    targets = []
    values = []
    for engagement_key in states[key]:
        name_index = names.index(engagement_key)
        amount = states[key][engagement_key]
        sources.append(state_index)
        targets.append(name_index)
        values.append(amount)
        
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
            color = color[:len(names)]
        ))])
    
    fig.update_layout(title_text="Influence of {0} toward Funnel Stages".format(key), font_size=12)
    fig.write_image("ImportantEngagements/" + key + ".png")

    


