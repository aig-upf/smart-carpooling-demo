#! /usr/bin/python

import sys
import argparse
import json
import os, shutil
import time
from multiprocessing import Process

from utils.osmutils import *
from utils.pddlutils import *
from utils.planutils import *


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
    tmpPlanningFolder = os.path.realpath(os.path.join(baseFolder, "../temporal-planning"))
    domainFile = os.path.join(baseFolder, "domains/domain.pddl")
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
        json.dump(planParser.getJSON(mapParser), f, indent=4)


def convertLastPlanToGeoJSON(mapParser, configObj):
    lastPlanFile = None
    solutionType = "cooperative"
    if "solution_type" in configObj:
        solutionType = configObj["solution_type"]
    if solutionType == "cooperative":
        lastPlanFile = getLastPlanFileName()
    elif solutionType == "selfish":
        lastPlanFile = "tmp_sas_plan"
    if lastPlanFile is not None:
        print "Parsing plan %s..." % lastPlanFile
        planParser = PlanToGeoJSONConverter()
        planParser.parse(lastPlanFile)
        geoJsonFeatures = planParser.getGeoJSON(mapParser, configObj)
        retObj = {"geojson": geoJsonFeatures, "ensembles": planParser.getEnsembles(), "timestamp": int(time.time())}

        geoJsonPlanName = "%s_tmp_sas_plan.geojson" % solutionType
        with open(geoJsonPlanName, 'w') as f:
            print "Writing GeoJSONed plan to %s" % geoJsonPlanName
            json.dump(retObj, f, indent=4)
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
        mapAbsPath = os.path.realpath(os.path.join(configFolder, mapPath))
        mapParser.parse(mapAbsPath)


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


def modifyConfigurationWithAdaptations(configObj, adaptations):
    if (adaptations is not None) and (adaptations["adaptations"] is not None):
        blockedStreets = adaptations["adaptations"]["blocked_streets"]
        if blockedStreets is not None:
            configObj["blocked_streets"] = blockedStreets
        agents = adaptations["adaptations"]["agents"]
        if agents is not None:
            pedestrians = configObj["pedestrians"]
            carpools = configObj["carpools"]
            for p in pedestrians:
                if p["id"] in agents:
                    p["init_pos"] = agents[p["id"]]
                else:
                    p["init_pos"] = p["target_pos"]  # assume they have reached their target
            for c in carpools:
                if c["id"] in agents:
                    c["init_pos"] = agents[c["id"]]
                else:
                    c["init_pos"] = c["target_pos"]  # assume they have reached their target


def createSelfishConfigurationFromCooperative(cooperativeConfigObj):
    selfishConfigObj = dict(cooperativeConfigObj)
    selfishConfigObj["solution_type"] = "selfish"

    selfishConfigObj["carpools"].extend(selfishConfigObj["pedestrians"])
    selfishConfigObj["pedestrians"] = []

    return selfishConfigObj


def solveSmartCarpoolingProblemWithAdaptations(baseFolder, configFilePath, adaptations, doPlan=False, outputJSON=False, outputGeoJSON=False, timeLimit=3600, memoryLimit=4096, iteratedSolution=False, isCollectiveAdaptation=True):
    configObj = parseConfigFile(configFilePath)
    modifyConfigurationWithAdaptations(configObj, adaptations)
    with open("cooperative_scenario.json", 'w') as f:
        json.dump(configObj, f, indent=4)

    if isCollectiveAdaptation:
        solveSmartCarpoolingProblem(baseFolder, "cooperative_scenario.json", doPlan, outputJSON, outputGeoJSON, timeLimit, memoryLimit, iteratedSolution)
    else:
        selfishConfigObj = createSelfishConfigurationFromCooperative(configObj)
        with open("selfish_scenario.json", 'w') as f:
            json.dump(selfishConfigObj, f, indent=4)
        solveSmartCarpoolingProblem(baseFolder, "selfish_scenario.json", doPlan, outputJSON, outputGeoJSON, timeLimit, memoryLimit, iteratedSolution)


def solveSmartCarpoolingProblem(baseFolder, configFilePath, doPlan=False, outputJSON=False, outputGeoJSON=False, timeLimit=3600, memoryLimit=4096, iteratedSolution=False):
    configObj = parseConfigFile(configFilePath)

    mapParser = OpenStreeMapParser()
    parseMapFile(mapParser, configObj["map_path"], configFilePath)
    exportLabelCorrespondences(mapParser)

    if doPlan:
        solutionType = "cooperative"
        if "solution_type" in configObj:
            solutionType = configObj["solution_type"]
        if solutionType == "cooperative":
            solveCooperativeProblem(mapParser, configObj, baseFolder, timeLimit, memoryLimit, iteratedSolution)
        elif solutionType == "selfish":
            solveSelfishProblem(mapParser, configObj, baseFolder, timeLimit, memoryLimit)
        else:
            print "Error: Incorrect solution type"
            exit(-1)

        if outputJSON:
            convertAllPlansToJSON(mapParser, configObj)
        if outputGeoJSON:
            convertLastPlanToGeoJSON(mapParser, configObj)


if __name__ == "__main__":
    args = getArguments()
    baseFolder = os.path.dirname(os.path.realpath(os.path.join(sys.argv[0], "..")))
    solveSmartCarpoolingProblem(baseFolder, args.config, args.plan, args.json, args.visualize, args.time, args.memory, args.iterated)

'''
    (46.0643, 46.0715, 11.1164, 11.1272) # 1125 nodes
    (46.0575, 46.0715, 11.1164, 11.1367) # 2304 nodes
    (46.0575, 46.0777, 11.1164, 11.1378) # 3345 nodes
    (46.0347, 46.1304, 11.0803, 11.1548) # 13419 nodes
'''
