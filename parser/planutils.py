from parse import *


class PlanParser:
    def __init__(self):
        self.parsePattern = "{}: ({}) [{}]"
        self.vehicleMovements = {}
        self.pedestrianMovements = {}

    def parse(self, inputPlanPath):
        with open(inputPlanPath, 'r') as f:
            for line in f:
                line = line.strip()
                if len(line) > 0 and not line.startswith(';'):
                    time, action, dur = parse(self.parsePattern, line)
                    actionSplit = action.split(' ')
                    actionName = actionSplit[0].lower()
                    if actionName == "travel":
                        carpoolId, originLocation, destLocation = [item.lower() for item in actionSplit[1:]]
                        if carpoolId not in self.vehicleMovements:
                            self.vehicleMovements[carpoolId] = []
                        infoItem = {"timestamp": float(time),
                                    "duration": float(dur),
                                    "origin": originLocation,
                                    "destination": destLocation,
                                    "action": actionName,
                                    "agent_type": "vehicle"}
                        self.vehicleMovements[carpoolId].append(infoItem)
                    elif actionName == "walk":
                        pedestrianId, originLocation, destLocation = [item.lower() for item in actionSplit[1:]]
                        if pedestrianId not in self.pedestrianMovements:
                            self.pedestrianMovements[pedestrianId] = []
                        infoItem = {"timestamp": float(time),
                                    "duration": float(dur),
                                    "origin": originLocation,
                                    "destination": destLocation,
                                    "action": actionName,
                                    "agent_type": "pedestrian"}
                        self.pedestrianMovements[pedestrianId].append(infoItem)
                    elif actionName == "embark" or actionName == "debark":
                        pedestrianId, carpoolId, location = [item.lower() for item in actionSplit[1:]]
                        if pedestrianId not in self.pedestrianMovements:
                            self.pedestrianMovements[pedestrianId] = []
                        infoItem = {"timestamp": float(time),
                                    "duration": float(dur),
                                    "location": location,
                                    "vehicle": carpoolId,
                                    "action": actionName,
                                    "agent_type": "pedestrian"}
                        self.pedestrianMovements[pedestrianId].append(infoItem)

    def compareMovements(self, mov1, mov2):
        if mov1["timestamp"] > mov2["timestamp"]:
            return 1
        return -1


class PlanToJSONConverter(PlanParser):
    def getJSON(self, mapParser):
        retJSON = []

        for pedestrianId in self.pedestrianMovements:
            movements = self.pedestrianMovements[pedestrianId]
            sortedMovements = sorted(movements, self.compareMovements)
            eventsList = []
            for mov in sortedMovements:
                if mov["action"] == "walk":
                    self.__addWalkTravelActionToEventList(mapParser, mov, eventsList)
                elif mov["action"] == "embark" or mov["action"] == "debark":
                    locationNode = mapParser.getNodeForId(int(mov["location"][3:]))
                    mov["location"] = (locationNode.getLatitude(), locationNode.getLongitude())
                    eventsList.append(mov)

            newItem = {"type": "pedestrian", "id": pedestrianId, "events": eventsList}
            retJSON.append(newItem)

        for vehicleId in self.vehicleMovements:
            movements = self.vehicleMovements[vehicleId]
            sortedMovements = sorted(movements, self.compareMovements)
            eventsList = []
            for mov in sortedMovements:
                if mov["action"] == "travel":
                    self.__addWalkTravelActionToEventList(mapParser, mov, eventsList)
            newItem = {"type": "vehicle", "id": vehicleId, "events": eventsList}
            retJSON.append(newItem)

        return retJSON

    def __addWalkTravelActionToEventList(self, mapParser, mov, eventsList):
        originNode = mapParser.getNodeForId(int(mov["origin"][3:]))
        destNode = mapParser.getNodeForId(int(mov["destination"][3:]))

        mov["origin"] = (originNode.getLatitude(), originNode.getLongitude())
        mov["destination"] = (destNode.getLatitude(), destNode.getLongitude())

        intermediateLocations = []
        for link in originNode.getLinks():
            if link.getOriginNode() == originNode and link.getLinkedNode() == destNode:
                for subNode in link.getGeometry():
                    intermediateLocations.append((subNode.getLatitude(), subNode.getLongitude()))
        mov["path"] = intermediateLocations

        eventsList.append(mov)


class PlanToGeoJSONConverter(PlanParser):
    def getGeoJSON(self, mapParser, configObj):
        featureVector = []

        for pedestrian in configObj["pedestrians"]:
            agentId = pedestrian["id"]
            self.__addGeoJSONFeatureForInitPosition(agentId, pedestrian["init_pos"], mapParser, featureVector, "pedestrian")
            if "target_pos" in pedestrian and agentId in self.pedestrianMovements:
                agentMovements = self.pedestrianMovements[agentId]
                if len(agentMovements) > 0:
                    lastMovementTimestamp = agentMovements[-1]["timestamp"] + agentMovements[-1]["duration"]
                    self.__addGeoJSONFeatureForEndPosition(agentId, pedestrian["target_pos"], lastMovementTimestamp, mapParser, featureVector, agentMovements[-1]["agent_type"])

        for vehicle in configObj["carpools"]:
            agentId = vehicle["id"]
            self.__addGeoJSONFeatureForInitPosition(vehicle["id"], vehicle["init_pos"], mapParser, featureVector, "vehicle")
            if "target_pos" in vehicle and agentId in self.vehicleMovements:
                agentMovements = self.vehicleMovements[agentId]
                if len(agentMovements) > 0:
                    lastMovementTimestamp = agentMovements[-1]["timestamp"] + agentMovements[-1]["duration"]
                    self.__addGeoJSONFeatureForEndPosition(agentId, vehicle["target_pos"], lastMovementTimestamp, mapParser, featureVector, agentMovements[-1]["agent_type"])

        for vehicle in self.vehicleMovements:
            movements = sorted(self.vehicleMovements[vehicle], self.compareMovements)
            self.__addGeoJSONFeatureForMovements(vehicle, mapParser, movements, featureVector)

        for pedestrian in self.pedestrianMovements:
            movements = sorted(self.pedestrianMovements[pedestrian], self.compareMovements)
            self.__addGeoJSONFeatureForMovements(pedestrian, mapParser, movements, featureVector)

        self.__addAllLinksGeoJSONFeatures(mapParser, configObj, featureVector)

        return featureVector

    def __addGeoJSONFeatureForInitPosition(self, agentId, location, mapParser, featureVector, agentType):
        if location.startswith("vehicle"):
            feature = self.__getPointFeatureForAgentAndCoordinates(agentId, None, location, 0.0, "", agentType)
            feature["properties"]["init_node"] = True
            feature["properties"]["hide"] = True
            featureVector.append(feature)
        else:
            locationNode = mapParser.getNodeForLabel(location)
            eventContent = "Initial position of %s" % agentId
            feature = self.__getPointFeatureForAgentAndCoordinates(agentId, [locationNode.getLongitude(), locationNode.getLatitude()], locationNode.getName(), 0.0, eventContent, agentType)
            feature["properties"]["init_node"] = True
            featureVector.append(feature)

    def __addGeoJSONFeatureForEndPosition(self, agentId, location, timestamp, mapParser, featureVector, agentType):
        if location.startswith("vehicle"):
            feature = self.__getPointFeatureForAgentAndCoordinates(agentId, None, location, 0.0, "", agentType)
            feature["properties"]["end_node"] = True
            feature["properties"]["hide"] = True
            featureVector.append(feature)
        else:
            locationNode = mapParser.getNodeForLabel(location)
            eventContent = "Ending position of %s (%s)" % (agentId, timestamp)
            feature = self.__getPointFeatureForAgentAndCoordinates(agentId, [locationNode.getLongitude(), locationNode.getLatitude()], locationNode.getName(), timestamp, eventContent, agentType)
            feature["properties"]["end_node"] = True
            featureVector.append(feature)

    def __addGeoJSONFeatureForMovements(self, agentId, mapParser, movements, featureVector):
        for mov in movements:
            if mov["action"] == "travel" or mov["action"] == "walk":
                originNode = mapParser.getNodeForId(int(mov["origin"][3:]))
                destNode = mapParser.getNodeForId(int(mov["destination"][3:]))
                coordinates = []
                selectedLink = None

                for link in originNode.getLinks():
                    if link.getOriginNode() == originNode and link.getLinkedNode() == destNode:
                        selectedLink = link
                        for subNode in link.getGeometry():
                            coordinates.append([subNode.getLongitude(), subNode.getLatitude()])
                        break
                if len(coordinates) > 0:
                    movTimestamp = mov["timestamp"] + mov["duration"]
                    featureVector.append(self.__getLineFeatureForAgentAndCoordinates(agentId, coordinates, movTimestamp, originNode.getName(), destNode.getName(), selectedLink.getDistanceToLinkedNode(), mov["agent_type"]))
                    eventContent = ""
                    if mov["action"] == "travel":
                        eventContent = "Vehicle %s travels (%s)" % (agentId, movTimestamp)
                    elif mov["action"] == "walk":
                        eventContent = "Pedestrian %s walks (%s)" % (agentId, movTimestamp)
                    featureVector.append(self.__getPointFeatureForAgentAndCoordinates(agentId, [destNode.getLongitude(), destNode.getLatitude()], destNode.getName(), movTimestamp, eventContent, mov["agent_type"]))
            elif mov["action"] == "embark":
                node = mapParser.getNodeForId(int(mov["location"][3:]))
                movTimestamp = mov["timestamp"] + mov["duration"]
                eventContent = "Pedestrian %s embarks on vehicle %s (%s)" % (agentId, mov["vehicle"], movTimestamp)
                label = "vehicle:%s" % mov["vehicle"]
                featureVector.append(self.__getPointFeatureForAgentAndCoordinates(agentId, [node.getLongitude(), node.getLatitude()], label, movTimestamp, eventContent, mov["agent_type"]))
            elif mov["action"] == "debark":
                node = mapParser.getNodeForId(int(mov["location"][3:]))
                movTimestamp = mov["timestamp"] + mov["duration"]
                eventContent = "Pedestrian %s gets out of vehicle %s (%s)" % (agentId, mov["vehicle"], movTimestamp)
                featureVector.append(self.__getPointFeatureForAgentAndCoordinates(agentId, [node.getLongitude(), node.getLatitude()], node.getName(), movTimestamp, eventContent, mov["agent_type"]))

    def __addAllLinksGeoJSONFeatures(self, mapParser, configObj, featureVector):
        mapBoundaries = configObj["map_boundaries"]
        regionNodes = mapParser.getNodesForRegion(mapBoundaries["min_latitude"],
                                                mapBoundaries["max_latitude"],
                                                mapBoundaries["min_longitude"],
                                                mapBoundaries["max_longitude"])
        regionNodeIds = set([node.getId() for node in regionNodes])

        blockedLinks = self.__getBlockedStreetLinks(configObj)

        for labelPair in mapParser.getLinks():
            startLabel = labelPair[0]
            destLabel = labelPair[1]
            link = mapParser.getLinks()[labelPair]
            if link.getOriginNode().getId() in regionNodeIds and link.getLinkedNode().getId() in regionNodeIds:
                coordinates = []
                for subNode in link.getGeometry():
                    coordinates.append([subNode.getLongitude(), subNode.getLatitude()])
                isBlockedLink = (startLabel, destLabel) in blockedLinks
                featureVector.append(self.__getLineFeatureForLink(startLabel, destLabel, coordinates, isBlockedLink))

    def __getBlockedStreetLinks(self, configObj):
        blockedLinks = []
        if "blocked_streets" in configObj:
            for blockedLink in configObj["blocked_streets"]:
                blockedLinks.append((blockedLink["init_pos"], blockedLink["target_pos"]))
        return blockedLinks

    def __getLineFeatureForAgentAndCoordinates(self, agentId, coordinates, timestamp, startLabel, endLabel, distance, agentType):
        return {"type": "Feature",
                "properties": {
                    "agent_id": agentId.lower(),
                    "timestamp": timestamp,
                    "link_in_plan": True,
                    "start_label": startLabel,
                    "end_label": endLabel,
                    "distance": distance,
                    "agent_type": agentType
                },
                "geometry": {
                    "type": "LineString",
                    "coordinates": coordinates
                 }
                }

    def __getPointFeatureForAgentAndCoordinates(self, agentId, coordinates, label, timestamp, eventContent, agentType):
        return {"type": "Feature",
                "properties": {
                    "agent_id": agentId.lower(),
                    "timestamp": timestamp,
                    "event_content": eventContent,
                    "node_label": label,
                    "agent_type": agentType
                },
                "geometry": {
                    "type": "Point",
                    "coordinates": coordinates
                }
               }

    def __getLineFeatureForLink(self, startLabel, destLabel, coordinates, isBlockedLink):
        return {"type": "Feature",
                "properties": {"start_label": startLabel, "end_label": destLabel, "original_link": True, "blocked": isBlockedLink},
                "geometry": {
                    "type": "LineString",
                    "coordinates": coordinates
                }
            }
