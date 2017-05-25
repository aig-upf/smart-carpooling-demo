#! /usr/bin/python

import argparse
import json
from osmutils import *
from carutils import *
from random import randint
try:
    from queue import Queue
except:
    from Queue import Queue


def getArguments():
    argParser = argparse.ArgumentParser()
    argParser.add_argument("map", help="input Open Street Map (OSM) file")
    argParser.add_argument("pedestrians", type=int, help="number of pedestrians")
    argParser.add_argument("carpools", type=int, help="number of carpools")
    argParser.add_argument("minlat", type=float, help="minimum latitude")
    argParser.add_argument("maxlat", type=float, help="maximum latitude")
    argParser.add_argument("minlon", type=float, help="minimum longitude")
    argParser.add_argument("maxlon", type=float, help="maximum longitude")
    argParser.add_argument("minwalk", type=int, help="minimum walk range for a pedestrian")
    argParser.add_argument("maxwalk", type=int, help="maximum walk range for a pedestrian")
    argParser.add_argument("--block-pedestrian-links", action="store_true", default=False, help="whether to block links inside pedestrian walk range")
    argParser.add_argument("--carinfo", default=None, help="path to the file containing information about diverse cars")
    return argParser.parse_args()


def areNodesConnected(n1, n2, pedestrianNodeIds):
    q = Queue()
    q.put(n1)
    visited = set()
    visited.add(n1.getId())

    while not q.empty():
        currentNode = q.get()
        if currentNode == n2:
            return True
        for link in currentNode.getLinks():
            linkedNode = link.getLinkedNode()
            if linkedNode.getId() not in pedestrianNodeIds and linkedNode.getId() not in visited:
                visited.add(linkedNode.getId())
                q.put(n2)

    return False


def getPedestrianNodeIds(pedestrianNodeIds, currentNode, remainingDist):
    for link in currentNode.getLinks():
        linkedNode = link.getLinkedNode()
        distance = link.getDistanceToLinkedNode()
        newDistance = remainingDist - distance
        if newDistance >= 0 and linkedNode.getId() not in pedestrianNodeIds:
            pedestrianNodeIds.add(currentNode.getId())
            getPedestrianNodeIds(pedestrianNodeIds, linkedNode, newDistance)


def getLinksInWalkRange(currentNode, linksList, remainingDistance, visitedNodeIds):
    currentId = currentNode.getId()
    if currentId not in visitedNodeIds:
        visitedNodeIds.add(currentId)
        for link in currentNode.getLinks():
            if link.getLinkedNode().getId() not in visitedNodeIds:
                linkDistance = link.getDistanceToLinkedNode()
                if remainingDistance >= linkDistance:
                    linksList.append(link)
                    if link.getInverseLink() is not None:  # there could be no inverse link
                        linksList.append(link.getInverseLink())
                    newRemainingDistance = remainingDistance - linkDistance
                    getLinksInWalkRange(link.getLinkedNode(), linksList, newRemainingDistance, visitedNodeIds)


def getPedestrians(regionNodes, numPedestrians):
    pedestrians = []
    pedestrianLinks = []
    pedestrianNodeIds = set()
    pedestrianNodeIdsMap = {}
    for i in range(0, numPedestrians):
        pedestrianId = "p%d" % (i + 1)
        randomOriginNode = regionNodes[randint(0, len(regionNodes) - 1)]
        randomDestNode = regionNodes[randint(0, len(regionNodes) - 1)]
        walkRange = randint(args.minwalk, args.maxwalk)

        pedestrians.append({"id": pedestrianId, "init_pos": randomOriginNode.getName(), "target_pos": randomDestNode.getName(), "walk_range": walkRange})

        pedestrianNodeIdsMap[pedestrianId] = {}

        pedestrianOriginNodeIds = set()
        getPedestrianNodeIds(pedestrianOriginNodeIds, randomOriginNode, walkRange)
        pedestrianNodeIdsMap[pedestrianId]["init_pos"] = pedestrianOriginNodeIds

        pedestrianTargetNodeIds = set()
        getPedestrianNodeIds(pedestrianTargetNodeIds, randomDestNode, walkRange)
        pedestrianNodeIdsMap[pedestrianId]["target_pos"] = pedestrianTargetNodeIds

        pedestrianNodeIds |= (pedestrianOriginNodeIds | pedestrianTargetNodeIds)

        getLinksInWalkRange(randomOriginNode, pedestrianLinks, walkRange, set())
        getLinksInWalkRange(randomDestNode, pedestrianLinks, walkRange, set())

    return pedestrians, pedestrianLinks, pedestrianNodeIds, pedestrianNodeIdsMap


def getCarpools(regionNodes, numCarpools, pedestrianNodeIds, blockPedestrianLinks, carsInfomation):
    carpools = []
    carpoolNodes = [node for node in regionNodes if node.getId() not in pedestrianNodeIds]

    for i in range(0, numCarpools):
        carpoolId = "c%d" % (i + 1)
        randomOriginNode = None
        randomDestNode = None
        while (randomOriginNode is None) or (randomDestNode is None) or \
                (blockPedestrianLinks and not areNodesConnected(randomOriginNode, randomDestNode, pedestrianNodeIds)):
            randomOriginNode = carpoolNodes[randint(0, len(carpoolNodes) - 1)]
            randomDestNode = carpoolNodes[randint(0, len(carpoolNodes) - 1)]
        carpool = { "id": carpoolId,
                    "init_pos": randomOriginNode.getName(),
                    "target_pos": randomDestNode.getName()}
        if carsInfomation is not None:
            carpool.update(carsInfomation.getRandomCar())
        carpools.append(carpool)

    return carpools


if __name__ == "__main__":
    args = getArguments()

    mapParser = OpenStreeMapParser()
    mapParser.parse(args.map)

    carsInfomation = None
    if args.carinfo is not None:
        carsInfomation = CarDataManager()
        carsInfomation.parseCarDataFile(args.carinfo)

    regionNodes = mapParser.getNodesForRegion(args.minlat, args.maxlat, args.minlon, args.maxlon)

    pedestrians, pedestrianLinks, pedestrianNodeIds, pedestrianNodeIdsMap = getPedestrians(regionNodes, args.pedestrians)
    carpools = getCarpools(regionNodes, args.carpools, pedestrianNodeIds, args.block_pedestrian_links, carsInfomation)

    finalObj = {"carpools": carpools, "pedestrians": pedestrians, "blocked_streets": []}
    if args.block_pedestrian_links:
        for blocked in pedestrianLinks:
            finalObj["blocked_streets"].append({"init_pos": blocked.getOriginNode().getName(), "target_pos": blocked.getLinkedNode().getName()})
    finalObjStr = json.dumps(finalObj)

    with open("random_init.json", 'w') as f:
        f.write(finalObjStr)
        print "Initial random positions exported to 'random_init.json'"
