import pandas as pd
import numpy as np
import json
files = ["burglary.xlsx","kidnapping.xlsx","robbery.xlsx", "theft_private_cars.xlsx", "theft.xlsx", "serious_assault.xlsx"]
severity = [0.2,0.8,0.5,0.6,0.5, 0.7]
# read data from file
data = [pd.read_excel(filename) for filename in files]


# Extract list of countries in all files
countries = data[0]['Country'].unique()
for i in range(1,len(data)):
    for country in countries:
        if country not in data[i].Country.unique():
            countries = np.delete(countries,np.where(countries == country))


scores  = {country: 0 for country in countries}
for i in range(len(data)):
    dataset = data[i]
    sev = severity[i]
    for country in countries:
        rates = dataset[dataset['Country'] == country]
        rates = rates[rates['Year']==max(rates['Year'])] 
        scores[country] += rates['Rate'].values[0] / 1000 * sev  

with open('scores.json', 'w') as outfile:  
    json.dump(scores, outfile)