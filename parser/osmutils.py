import geopy.distance
try:
    from queue import Queue
except:
    from Queue import Queue

class OpenStreeMapNode:
    def __init__(self, id, name, latitude, longitude):
        self.__id = id
        self.__name = name
        self.__latitude = float(latitude)
        self.__longitude = float(longitude)
        self.__links = []

    def getId(self):
        return self.__id

    def getName(self):
        return self.__name

    def getLongitude(self):
        return self.__longitude

    def getLatitude(self):
        return self.__latitude

    def getLinks(self):
        return self.__links

    def addLink(self, link):
        return self.__links.append(link)

    def __str__(self):
        return "Node - Id: %s ;; Name: %s ;; Latitude: %s ;; Longitude: %s" % (self.__id, self.__name, self.__latitude, self.__longitude)


class OpenStreetMapLink:
    def __init__(self, label, originNode, linkedNode, name, speed, geometry):
        self.__label = label
        self.__originNode = originNode
        self.__linkedNode = linkedNode
        self.__name = name
        self.__speed = speed
        self.__geometry = geometry
        self.__inverseLink = None

    def getLabel(self):
        return self.__label

    def getOriginNode(self):
        return self.__originNode

    def getLinkedNode(self):
        return self.__linkedNode

    def getName(self):
        return self.__name

    def getSpeed(self):
        return self.__speed

    def getGeometry(self):
        return self.__geometry

    def setInverseLink(self, inverseLink):
        self.__inverseLink = inverseLink

    def getInverseLink(self):
        return self.__inverseLink

    def getDistanceToLinkedNode(self):
        totalDistance = 0
        for i in range(0, len(self.__geometry) - 1):
            coords_1 = (self.__geometry[i].getLatitude(), self.__geometry[i].getLongitude())
            coords_2 = (self.__geometry[i + 1].getLatitude(), self.__geometry[i + 1].getLongitude())
            totalDistance += geopy.distance.vincenty(coords_1, coords_2).m
        return totalDistance


class OpenStreeMapParser:
    def __init__(self):
        self.__numNodes = 0
        self.__numLinks = 0
        self.__nodes = {}
        self.__interNodes = {}
        self.__splitNodes = {}
        self.__invNodes = {}
        self.__linkCache = {} # for getting the reverse nodes

    def parse(self, inputMapPath):
        print "Parsing file %s" % inputMapPath
        with open(inputMapPath, 'r') as f:
            parsingNodes = False
            numNodes = 0
            parsingLinks = False
            numLinks = 0
            for line in f:
                line = line.strip()
                if len(line) > 0:
                    if parsingNodes:
                        self.__parseNode(line)
                    elif parsingLinks:
                        self.__parseLink(line)
                    else:
                        elementName, numElements = line.split(' ')
                        if elementName == "NODES":
                            print "Parsing nodes..."
                            parsingNodes = True
                            numNodes = int(numElements)
                        elif elementName == "LINKS":
                            print "Parsing links..."
                            parsingLinks = True
                            numLinks = int(numElements)
                else:
                    parsingNodes = False
                    parsingLinks = False

            if numNodes != self.__numNodes:
                print "Error: The number of scanned nodes does not match the expected number of nodes"
                exit(-1)

            if numLinks != self.__numLinks:
                print "Error: The number of scanned links does not match the expected number of links"
                exit(-1)

        print "Parsing completed!"

    def getTotalNumNodes(self):
        return self.__numNodes

    def getNumNodes(self):
        return len(self.__nodes)

    def getNumInterNodes(self):
        return len(self.__interNodes)

    def getNumSplitNodes(self):
        return len(self.__splitNodes)

    def addNode(self, key, node):
        self.__numNodes += 1
        self.__nodes[key] = node
        self.__invNodes[node.getId()] = node

    def addInterNode(self, key, node):
        self.__numNodes += 1
        self.__interNodes[key] = node
        self.__invNodes[node.getId()] = node

    def addSplitNode(self, key, node):
        self.__numNodes += 1
        self.__splitNodes[key] = node
        self.__invNodes[node.getId()] = node

    def __parseNode(self, nodeLine):
        label, longitude, latitude = nodeLine.split(";;")
        newNode = OpenStreeMapNode(self.__numNodes, label, latitude, longitude)
        if "->" in label:
            self.__interNodes[label] = newNode
        else:
            if label.startswith("split"):
                self.__splitNodes[label] = newNode
            else:
                self.__nodes[label] = newNode
        self.__invNodes[newNode.getId()] = newNode
        self.__numNodes += 1

    def __parseLink(self, linkLine):
        linkLabel, startNodeLabel, destNodeLabel, linkName, speed, geometry = linkLine.split(";;")

        geometryNodes = []
        for nodeLabel in geometry.split(" "):
            node = self.getNodeForLabel(nodeLabel)
            if node is not None:
                geometryNodes.append(node)

        startNode = self.getNodeForLabel(startNodeLabel)
        destNode = self.getNodeForLabel(destNodeLabel)

        newLink = OpenStreetMapLink(linkLabel, startNode, destNode, linkName, speed, geometryNodes)
        startNode.addLink(newLink)

        self.__linkCache[(startNodeLabel, destNodeLabel)] = newLink

        if (destNodeLabel, startNodeLabel) in self.__linkCache:
            invLink = self.__linkCache[(destNodeLabel, startNodeLabel)]
            invLink.setInverseLink(newLink)
            newLink.setInverseLink(invLink)

        self.__numLinks += 1

    def getLinks(self):
        return self.__linkCache

    def getNodeForLabel(self, nodeLabel):
        if nodeLabel in self.__nodes:
            return self.__nodes[nodeLabel]
        elif nodeLabel in self.__interNodes:
            return self.__interNodes[nodeLabel]
        elif nodeLabel in self.__splitNodes:
            return self.__splitNodes[nodeLabel]

    def getNodeForId(self, nodeId):
        return self.__invNodes[nodeId]

    def getNodeLabelCorrespondences(self):
        return self.__invNodes.copy()

    def getMinMaxLatLon(self):
        retLat = [90.0, -90.0]
        retLon = [180.0, -180.0]

        for key in self.__nodes:
            node = self.__nodes[key]
            retLat[0] = min(retLat[0], node.getLatitude())
            retLat[1] = max(retLat[1], node.getLatitude())
            retLon[0] = min(retLon[0], node.getLongitude())
            retLon[1] = max(retLon[1], node.getLongitude())

        for key in self.__interNodes:
            node = self.__interNodes[key]
            retLat[0] = min(retLat[0], node.getLatitude())
            retLat[1] = max(retLat[1], node.getLatitude())
            retLon[0] = min(retLon[0], node.getLongitude())
            retLon[1] = max(retLon[1], node.getLongitude())

        for key in self.__splitNodes:
            node = self.__splitNodes[key]
            retLat[0] = min(retLat[0], node.getLatitude())
            retLat[1] = max(retLat[1], node.getLatitude())
            retLon[0] = min(retLon[0], node.getLongitude())
            retLon[1] = max(retLon[1], node.getLongitude())

        return (retLat, retLon)

    def __isNodeContainedInRegion(self, node, minLat, maxLat, minLon, maxLon):
        nodeLat = node.getLatitude()
        nodeLon = node.getLongitude()

        return minLat  <= nodeLat and \
               nodeLat <= maxLat  and \
               minLon  <= nodeLon and \
               nodeLon <= maxLon
    '''
    def __isLinkInRegion(self, link, minLat, maxLat, minLon, maxLon):
        if (self.__isNodeContainedInRegion(link.getNode1(), minLat, maxLat, minLon, maxLon) and \
            self.__isNodeContainedInRegion(link.getNode2(), minLat, maxLat, minLon, maxLon)):
            for n in link.getGeometry():
                if not self.__isNodeContainedInRegion(n, minLat, maxLat, minLon, maxLon):
                    return False
            return True
        return False

    def getLinksForRegion(self, minLat, maxLat, minLon, maxLon):
        retLinks = []
        for link in self.links:
            if self.__isLinkInRegion(link, minLat, maxLat, minLon, maxLon):
                retLinks.append(link.clone())
        return retLinks
    '''
    def __getReachableNodesFromNode(self, node, visited, nodeIds):
        reachableNodes = []
        q = Queue()
        q.put(node)
        visited.add(node.getId())

        while not q.empty():
            currentNode = q.get()
            reachableNodes.append(currentNode)
            for link in currentNode.getLinks():
                linkedNode = link.getLinkedNode()
                if linkedNode.getId() not in visited and linkedNode.getId() in nodeIds:
                    q.put(linkedNode)
                    visited.add(linkedNode.getId())

        return reachableNodes

    def __getNodesInGiantComponent(self, nodes):
        visited = set()
        components = []
        nodeIds = set([node.getId() for node in nodes])

        for node in nodes:
            nodeId = node.getId()
            if nodeId not in visited and nodeId in nodeIds:
                components.append(self.__getReachableNodesFromNode(node, visited, nodeIds))

        maxSize = 0
        maxComp = None
        for c in components:
            if len(c) > maxSize:
                maxComp = c
                maxSize = len(c)

        return maxComp

    def getNodesForRegion(self, minLat, maxLat, minLon, maxLon):
        retNodes = []
        for node in self.__nodes:
            if self.__isNodeContainedInRegion(self.__nodes[node], minLat, maxLat, minLon, maxLon):
                retNodes.append(self.__nodes[node])
        for node in self.__splitNodes:
            if self.__isNodeContainedInRegion(self.__splitNodes[node], minLat, maxLat, minLon, maxLon):
                retNodes.append(self.__splitNodes[node])
        giantCompNodes = self.__getNodesInGiantComponent(retNodes)
        return giantCompNodes
