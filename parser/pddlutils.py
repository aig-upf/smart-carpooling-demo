

class PDDLInstanceBuilder:
    def __init__(self):
        pass

    def getPDDL(self, mapParser, configObj):
        print "Building PDDL..."

        regionNodes = self.__getNodesInMapBoundaries(mapParser, configObj)

        insPDDL  = "(define (problem journey)\n"
        insPDDL += "(:domain journey-planner)\n"
        insPDDL += self.__getObjectsPDDL(mapParser, configObj, regionNodes)
        insPDDL += self.__getInitGoalPDDL(mapParser, configObj, regionNodes)
        insPDDL += ")"

        print "Done!"

        return insPDDL

    def __getNodesInMapBoundaries(self, mapParser, configObj):
        mapBoundaries = configObj["map_boundaries"]
        return mapParser.getNodesForRegion(mapBoundaries["min_latitude"],
                                           mapBoundaries["max_latitude"],
                                           mapBoundaries["min_longitude"],
                                           mapBoundaries["max_longitude"])

    def __getObjectsPDDL(self, mapParser, configObj, regionNodes):
        objPDDL  = "(:objects\n"

        # carpools
        if len(configObj["carpools"]) > 0:
            objPDDL += "\t"
            for carpool in configObj["carpools"]:
                objPDDL += carpool["id"] + " "
            objPDDL += "- carpool\n"

        # pedestrians
        if len(configObj["pedestrians"]) > 0:
            objPDDL += "\t"
            for pedestrian in configObj["pedestrians"]:
                objPDDL += pedestrian["id"] + " "
            objPDDL += "- pedestrian\n"

        # locations
        if len(regionNodes) > 0:
            objPDDL += "\t"
            for node in regionNodes:
                objPDDL += "loc%d " % node.getId()
            objPDDL += "- location\n"

        objPDDL += ")\n"
        return objPDDL

    def __getPDDLForPedestrianPosition(self, mapParser, pedestrianId, pedestrianPos):
        retNode = None
        retPddl = ""
        if pedestrianPos.startswith("vehicle"):
            _, vehicleId = pedestrianPos.split(":")
            retPddl = "\t(in %s %s)\n" % (pedestrianId, vehicleId)
        else:
            retNode = mapParser.getNodeForLabel(pedestrianPos)
            retPddl = "\t(at %s loc%s)\n" % (pedestrianId, retNode.getId())
        return retNode, retPddl

    def __getInitGoalPDDL(self, mapParser, configObj, regionNodes):
        regionNodeIds = set([node.getId() for node in regionNodes])
        vehicleBlockedLinks = self.__getBlockedLinks(mapParser, configObj)

        initPDDL = "(:init\n"
        goalPDDL = "(:goal (and\n"

        # carpool positions
        for carpool in configObj["carpools"]:
            initCarpoolId = carpool["init_pos"]
            initCarpoolNodeId = mapParser.getNodeForLabel(initCarpoolId).getId()
            initPDDL += "\t(at %s loc%s)\n" % (carpool["id"], initCarpoolNodeId)
            if "target_pos" in carpool:
                endCarpoolId = carpool["target_pos"]
                endCarpoolNodeId = mapParser.getNodeForLabel(endCarpoolId).getId()
                goalPDDL += "\t(at %s loc%s)\n" % (carpool["id"], endCarpoolNodeId)

        initPDDL += "\n"
        goalPDDL += "\n"

        # pedestrian positions
        initPedestrianNodes = []
        endPedestrianNodes = []

        for pedestrian in configObj["pedestrians"]:
            retInitNode, retInitPddl = self.__getPDDLForPedestrianPosition(mapParser, pedestrian["id"], pedestrian["init_pos"])
            retEndNode, retEndPddl = self.__getPDDLForPedestrianPosition(mapParser, pedestrian["id"], pedestrian["target_pos"])

            initPedestrianNodes.append(retInitNode)
            endPedestrianNodes.append(retEndNode)

            initPDDL += retInitPddl
            goalPDDL += retEndPddl

        initPDDL += "\n"

        # links
        pedestrianList = configObj["pedestrians"]
        carpoolList = configObj["carpools"]
        pedestrianLinks = self.__getPedestrianLinks(pedestrianList, initPedestrianNodes, endPedestrianNodes)

        initPDDL += self.__getFootpathsPDDL(pedestrianList, vehicleBlockedLinks, pedestrianLinks, regionNodeIds)
        initPDDL += self.__getStreetsPDDL(carpoolList, regionNodes, regionNodeIds, pedestrianLinks, vehicleBlockedLinks)

        initPDDL += ")\n"
        goalPDDL += "))\n"

        return initPDDL + goalPDDL

    def __getFootpathsPDDL(self, pedestrianList, blockedLinks, pedestrianLinks, nodeIds):
        initPDDL = ""
        for pedestrian in pedestrianList:
            for link in blockedLinks:
                originId = link.getOriginNode().getId()
                destId = link.getLinkedNode().getId()
                if originId in nodeIds and destId in nodeIds:
                    initPDDL += "\t(has-footpath %s loc%s loc%s)\n" % (pedestrian["id"], originId, destId)
                    initPDDL += "\t(= (velocity %s loc%s loc%s) 1)\n" % (pedestrian["id"], originId, destId)
                    initPDDL += "\t(= (distance loc%s loc%s) %s)\n" % (originId, destId, link.getDistanceToLinkedNode())
                    initPDDL += "\n"
            for link in pedestrianLinks:
                originId = link.getOriginNode().getId()
                destId = link.getLinkedNode().getId()
                if originId in nodeIds and destId in nodeIds:
                    initPDDL += "\t(has-footpath %s loc%s loc%s)\n" % (pedestrian["id"], originId, destId)
                    initPDDL += "\t(= (velocity %s loc%s loc%s) 1)\n" % (pedestrian["id"], originId, destId)
                    initPDDL += "\t(= (distance loc%s loc%s) %s)\n" % (originId, destId, link.getDistanceToLinkedNode())
                    initPDDL += "\n"
        return initPDDL

    def __getStreetsPDDL(self, vehicleList, regionNodes, regionNodeIds, pedestrianLinks, vehicleBlockedLinks):
        initPDDL = ""
        for node in regionNodes:
            for link in node.getLinks():
                if link not in vehicleBlockedLinks: # and link not in pedestrianLinks
                    originId = node.getId()
                    destId = link.getLinkedNode().getId()
                    if originId in regionNodeIds and destId in regionNodeIds:
                        initPDDL += "\t(has-street loc%s loc%s)\n" % (originId, destId)
                        initPDDL += "\t(= (distance loc%s loc%s) %s)\n" % (originId, destId, link.getDistanceToLinkedNode())
                        for carpool in vehicleList:
                            initPDDL += "\t(= (velocity %s loc%s loc%s) %s)\n" % (carpool["id"], originId, destId, link.getSpeed())
                        initPDDL += "\n"
        return initPDDL

    def __getPedestrianLinks(self, pedestrianList, initPedestrianNodes, endPedestrianNodes): # get links used exclusively by pedestrians according to their walk ranges
        pedestrianLinks = []
        for i in range(0, len(pedestrianList)):
            remainingDistance = pedestrianList[i]["walk_range"]
            if initPedestrianNodes[i] is not None:
                visitedNodeIds = set()
                self.__getLinksInWalkRange(initPedestrianNodes[i], pedestrianLinks, remainingDistance, visitedNodeIds)
            if endPedestrianNodes[i] is not None:
                visitedNodeIds = set()
                self.__getLinksInWalkRange(endPedestrianNodes[i], pedestrianLinks, remainingDistance, visitedNodeIds)
        return pedestrianLinks

    def __getLinksInWalkRange(self, currentNode, linksList, remainingDistance, visitedNodeIds):
        currentId = currentNode.getId()
        if currentId not in visitedNodeIds:
            visitedNodeIds.add(currentId)
            for link in currentNode.getLinks():
                if link.getLinkedNode().getId() not in visitedNodeIds:
                    linkDistance = link.getDistanceToLinkedNode()
                    if remainingDistance >= linkDistance:
                        linksList.append(link)
                        if link.getInverseLink() is not None: # there could be no inverse link
                            linksList.append(link.getInverseLink())
                        newRemainingDistance = remainingDistance - linkDistance
                        self.__getLinksInWalkRange(link.getLinkedNode(), linksList, newRemainingDistance, visitedNodeIds)

    def __getBlockedLinks(self, mapParser, configObj):
        blockedLinks = []
        if "blocked_streets" in configObj:
            for blockedStreet in configObj["blocked_streets"]:
                linkTuple = (blockedStreet["init_pos"], blockedStreet["target_pos"])
                link = mapParser.getLinks()[linkTuple]
                blockedLinks.append(link)
                invLinkTuple = (blockedStreet["target_pos"], blockedStreet["init_pos"])
                invLink = mapParser.getLinks()[invLinkTuple]
                blockedLinks.append(invLink)
        if "blocked_frontiers" in configObj:
            for frontier in configObj["blocked_frontiers"]:
                if "latitude" in frontier:
                    frontierLat = frontier["latitude"]
                    for linkName in mapParser.getLinks():
                        link = mapParser.getLinks()[linkName]
                        lat1 = link.getOriginNode().getLatitude()
                        lat2 = link.getLinkedNode().getLatitude()
                        if (lat1 - frontierLat) * (lat2 - frontierLat) < 0:
                            blockedLinks.append(link)
                elif "longitude" in frontier:
                    frontierLon = frontier["longitude"]
                    for linkName in mapParser.getLinks():
                        link = mapParser.getLinks()[linkName]
                        lon1 = link.getOriginNode().getLongitude()
                        lon2 = link.getLinkedNode().getLongitude()
                        if (lon1 - frontierLon) * (lon2 - frontierLon) < 0:
                            blockedLinks.append(link)
        return blockedLinks
