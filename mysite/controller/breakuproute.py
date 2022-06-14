import json

#maxtraveltime = 3600000 #1 hour for the purposes of testing, should change

#with open('routejson.txt') as json_file:
#	data = json.load(json_file)

def break_up_route(data,maxtraveltime):
	time=0
	stops = []
	for i in range(0,len(data['paths'][0]['instructions'])):
		time += data['paths'][0]['instructions'][i]['time']
		if time >= maxtraveltime:
			stops.append(data['paths'][0]['instructions'][i]['interval'][0])
			
			time = 0
	coordinates = []
	for x in stops:
		coordinates.append(str(data['paths'][0]['points']['coordinates'][x][::-1])[1:-1])
	return coordinates

#print(break_up_route(data,maxtraveltime)[0])

