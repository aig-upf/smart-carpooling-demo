#! /usr/bin/python

import sys
import argparse
import json
import os, shutil
import webbrowser
from flask import Flask, request
from flask_cors import CORS, cross_origin
import time
from multiprocessing import Process

from osmutils import *
from pddlutils import *
from planutils import *


def getArguments():
    argParser = argparse.ArgumentParser()
    argParser.add_argument("config", help="JSON configuration file to create the problem")
    argParser.add_argument("--plan", "-p", default=False, action="store_true", help="launch a planning algorithm to solve the generated problem")
    argParser.add_argument("--json", "-j", default=False, action="store_true", help="convert resulting problem into JSON file")
    argParser.add_argument("--time", "-t", default=3600, help="maximum number of seconds during which the planner will run (default: 3600 seconds)")
    argParser.add_argument("--memory", "-m", default=4096, help="maximum amount of memory in MiB to be used by the planner (default: 4096 MiB)")
    argParser.add_argument("--visualize", "-v", default=False, action="store_true", help="show a map with the planned route")
    argParser.add_argument("--iterated", dest="iterated", action="store_true", help="look for more solutions after finding the first one")
    argParser.add_argument("--no-iterated", dest="iterated", action="store_false", help="stop after finding the first solution")
    argParser.set_defaults(iterated=False)
    return argParser.parse_args()


def parseConfigFile(configFilePath):
    jsonContent = ""
    with open(configFilePath, 'r') as f:
        lines = f.readlines()
        jsonContent = "".join(lines)
    return json.loads(jsonContent)


def getPlanFileNames():
    solFiles = [i for i in os.listdir(".") if i.startswith("tmp_sas_plan") and not i.endswith("json")]
    solFiles.sort(reverse=True)
    return solFiles


def getLastPlanFileName():
    solFiles = getPlanFileNames()
    if len(solFiles) == 0:
        return None
    else:
        return solFiles[0]


def runPlanner(baseFolder, time, memory, problemFile, planFilePrefix, iteratedSolution):
    removeExistingJSONPlans()
    print "Planning..."
    tmpPlanningFolder = os.path.realpath(baseFolder + "/../temporal-planning")
    domainFile = baseFolder + "/domains/domain.pddl"
    iteratedFlag = None
    if iteratedSolution:
        iteratedFlag = "--iterated"
    else:
        iteratedFlag = "--no-iterated"
    tmpPlanningCmd = "%s/bin/plan.py -t %s -m %s %s --plan-file %s she %s %s" % (tmpPlanningFolder, time, memory, iteratedFlag, planFilePrefix, domainFile, problemFile)
    os.system(tmpPlanningCmd)


def runSelfishPlanner(baseFolder, time, memory, problemFile, planFilePrefix, problemFolder):
    os.chdir(problemFolder)
    runPlanner(baseFolder, time, memory, problemFile, planFilePrefix, False)


def removeExistingJSONPlans():
    print "Removing existing JSON plans..."
    jsonPlans = [i for i in os.listdir(".") if i.startswith("tmp_sas_plan") and i.endswith("json")]
    for plan in jsonPlans:
        os.remove(plan)


def removeExistingSelfishFiles():
    print "Removing existing merged plans and part folders..."
    selfishFiles = [i for i in os.listdir(".") if i.startswith("tmp_sas_plan") or i.startswith("part_")]
    print selfishFiles
    for selfishFile in selfishFiles:
        try:
            os.remove(selfishFile)
        except:
            shutil.rmtree(selfishFile)


def convertLastPlanToJSON(mapParser, configObj):
    lastPlanFile = None
    solutionType = None
    if "solution_type" in configObj:
        solutionType = configObj["solution_type"]
    if solutionType is None or solutionType == "cooperative":
        lastPlanFile = getLastPlanFileName()
    elif solutionType == "selfish":
        lastPlanFile = "tmp_sas_plan"
    if lastPlanFile is not None:
        convertPlanToJSON(mapParser, configObj, lastPlanFile)
    else:
        print "Error: There was not any plan to JSONify"


def convertAllPlansToJSON(mapParser, configObj):
    solFiles = getPlanFileNames()
    for planFile in solFiles:
        convertPlanToJSON(mapParser, configObj, planFile)


def convertPlanToJSON(mapParser, configObj, planFile):
    print "Parsing plan %s..." % planFile
    planParser = PlanToJSONConverter()
    planParser.parse(planFile)
    planFileName = "%s.json" % planFile
    with open(planFileName, "w") as f:
        print "Writing JSONed plan to %s" % planFileName
        f.write(json.dumps(planParser.getJSON(mapParser)))


def convertLastPlanToGeoJSON(mapParser, configObj):
    lastPlanFile = None
    solutionType = None
    if "solution_type" in configObj:
        solutionType = configObj["solution_type"]
    if solutionType is None or solutionType == "cooperative":
        lastPlanFile = getLastPlanFileName()
    elif solutionType == "selfish":
        lastPlanFile = "tmp_sas_plan"
    if lastPlanFile is not None:
        print "Parsing plan %s..." % lastPlanFile
        planParser = PlanToGeoJSONConverter()
        planParser.parse(lastPlanFile)
        geoJsonFeatures = planParser.getGeoJSON(mapParser, configObj)
        retObj = {"geojson": geoJsonFeatures, "timestamp": int(time.time())}
        return json.dumps(retObj)
    else:
        print "Error: There was not any plan to GeoJSONify"
        return None


def exportLabelCorrespondences(mapParser):
    nodeLabelCorresp = mapParser.getNodeLabelCorrespondences()
    with open("nodes.txt", "w") as f:
        print "Writing nodes correspondences to nodes.txt..."
        for c in nodeLabelCorresp:
            f.write("%s --> %s\n" % (c, nodeLabelCorresp[c]))


def generatePDDLForProblem(mapParser, configObj, problemFile):
    pddlBuilder = PDDLInstanceBuilder()
    outPDDL = pddlBuilder.getPDDL(mapParser, configObj)
    with open(problemFile, "w") as f:
        print "Writing PDDL to file %s..." % problemFile
        f.write(outPDDL)


def parseMapFile(mapParser, mapPath, configFilePath):
    if mapPath.startswith('/'): # absolute
        mapParser.parse(mapPath)
    else:
        configFolder = os.path.dirname(os.path.realpath(configFilePath))
        mapAbsPath = os.path.realpath(configFolder + '/' + mapPath)
        mapParser.parse(mapAbsPath)


def openMapVisualizer(baseFolder, visualizerPort):
    visualizePage = "file://" + os.path.realpath(baseFolder + "/visualizer/index.html?port=" + str(visualizerPort))
    webbrowser.open_new_tab(visualizePage)


def getProcessForConfigObj(configObj, mapParser, baseFolder, partId, argTime, argMemory):
    problemFolder = "part_%s" % partId
    os.makedirs(problemFolder)

    problemFileName = "output.pddl"
    problemFilePath = "%s/%s" % (problemFolder, problemFileName)
    generatePDDLForProblem(mapParser, configObj, problemFilePath)

    planFileName = "sas_plan"
    threadItem = Process(target=runSelfishPlanner, args=(baseFolder, argTime, argMemory, problemFileName, planFileName, problemFolder))
    return threadItem


def getSelfishPlannerProcesses(configObj, mapParser, baseFolder, argTime, argMemory):
    plannerProcesses = []
    for pedestrian in configObj["pedestrians"]:
        configObjCopy = configObj.copy()
        configObjCopy["pedestrians"] = [pedestrian]
        configObjCopy["carpools"] = []
        plannerProcesses.append(getProcessForConfigObj(configObjCopy, mapParser, baseFolder, len(plannerProcesses), argTime, argMemory))
    for carpool in configObj["carpools"]:
        configObjCopy = configObj.copy()
        configObjCopy["pedestrians"] = []
        configObjCopy["carpools"] = [carpool]
        plannerProcesses.append(getProcessForConfigObj(configObjCopy, mapParser, baseFolder, len(plannerProcesses), argTime, argMemory))
    return plannerProcesses


def mergePartialPlans():
    with open("tmp_sas_plan", 'w') as f:
        partFolders = [i for i in os.listdir(".") if i.startswith("part_")]
        for folder in partFolders:
            planName = "%s/%s" % (folder, "tmp_sas_plan")
            if os.path.exists(planName):
                with open(planName, 'r') as f2:
                    for line in f2:
                        f.write(line)


def solveSelfishProblem(mapParser, configObj, baseFolder, argTime, argMemory):
    removeExistingSelfishFiles()
    plannerProcesses = getSelfishPlannerProcesses(configObj, mapParser, baseFolder, argTime, argMemory)
    for p in plannerProcesses:
        p.start()
    for p in plannerProcesses:
        p.join()
    mergePartialPlans()


def solveCooperativeProblem(mapParser, configObj, baseFolder, argTime, argMemory, argIteratedSolution):
    problemFileName = "output.pddl"
    planFilePrefix = "sas_plan"
    generatePDDLForProblem(mapParser, configObj, problemFileName)
    runPlanner(baseFolder, argTime, argMemory, problemFileName, planFilePrefix, argIteratedSolution)


app = Flask(__name__)
CORS(app)
geoJsonPlan = None

@app.route("/")
def serveGeoJsonPlan():
    return geoJsonPlan

@app.route("/shutdown", methods=["POST"])
def shutdown():
    func = request.environ.get("werkzeug.server.shutdown")
    if func is not None:
        func()
        print "Exiting..."


if __name__ == "__main__":
    args = getArguments()
    baseFolder = os.path.dirname(os.path.realpath(sys.argv[0] + "/.."))
    configObj = parseConfigFile(args.config)

    mapParser = OpenStreeMapParser()
    parseMapFile(mapParser, configObj["map_path"], args.config)
    exportLabelCorrespondences(mapParser)

    if args.plan:
        solutionType = None
        if "solution_type" in configObj:
            solutionType = configObj["solution_type"]
        if (solutionType is None) or (solutionType == "cooperative"):
            solveCooperativeProblem(mapParser, configObj, baseFolder, args.time, args.memory, args.iterated)
        elif solutionType == "selfish":
            solveSelfishProblem(mapParser, configObj, baseFolder, args.time, args.memory)
        else:
            print "Error: Incorrect solution type"
            exit(-1)

        if args.json:
            convertAllPlansToJSON(mapParser, configObj)
        if args.visualize:
            geoJsonPlan = convertLastPlanToGeoJSON(mapParser, configObj)
            if geoJsonPlan is not None:
                visualizerPort = 5000
                if solutionType == "selfish":
                    visualizerPort = 5001
                openMapVisualizer(baseFolder, visualizerPort)
                app.run(port=visualizerPort)

'''
    (46.0643, 46.0715, 11.1164, 11.1272) # 1125 nodes
    (46.0575, 46.0715, 11.1164, 11.1367) # 2304 nodes
    (46.0575, 46.0777, 11.1164, 11.1378) # 3345 nodes
    (46.0347, 46.1304, 11.0803, 11.1548) # 13419 nodes
'''
