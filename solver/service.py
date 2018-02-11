#! /usr/bin/python

import os, time, json, sys
from flask import Flask, request
from flask_cors import CORS, cross_origin

import randominit, solver

app = Flask(__name__)
CORS(app)

cooperativeLastPlanModification = 0
sefilshLastPlanModification = 0

cooperativeLastPlanName = "cooperative_tmp_sas_plan.geojson"
sefishLastPlanName = "selfish_tmp_sas_plan.geojson"

baseFolder = None

@app.route("/generate_scenario", methods=['GET', 'POST'])
def generateRandomScenerario():
    requestObj = request.json
    randominit.generateRandomScenerario(os.path.join(baseFolder, "data/Trento.world"), \
                                        requestObj["passengers"], \
                                        requestObj["carpools"], \
                                        requestObj["min_latitude"], \
                                        requestObj["max_latitude"], \
                                        requestObj["min_longitude"], \
                                        requestObj["max_longitude"], \
                                        requestObj["min_walk_range"], \
                                        requestObj["max_walk_range"])
    solver.solveSmartCarpoolingProblem(baseFolder, "random_init.json", True, True)
    return "Success"


@app.route("/get_cooperative_plan")
def getLastCollectivePlan():
    try:
        global cooperativeLastPlanModification
        lastModification = os.stat(cooperativeLastPlanName)[-2]
        if lastModification != cooperativeLastPlanModification:
            cooperativeLastPlanModification = lastModification
            with open(cooperativeLastPlanName, 'r') as f:
                return f.read()
    except OSError as e:
        return str(e), 400
    return "Error", 400


@app.route("/get_selfish_plan")
def getLastSelfishPlan():
    try:
        global sefilshLastPlanModification
        lastModification = os.stat(sefishLastPlanName)[-2]
        if lastModification != sefilshLastPlanModification:
            sefilshLastPlanModification = lastModification
            with open(sefishLastPlanName, 'r') as f:
                return f.read()
    except OSError as e:
        return str(e), 400
    return "Error", 400


@app.route("/run_adaptation", methods=['GET', 'POST'])
def runAdaptation():
    solver.solveSmartCarpoolingProblemWithAdaptations(baseFolder, "random_init.json", request.json, True, True)
    # solver.solveSmartCarpoolingProblemWithAdaptations(baseFolder, "random_init.json", request.json, True, True, isCollectiveAdaptation=False)
    return "Sucess"


def removeGeoJSONFiles():
    try:
        os.remove(cooperativeLastPlanName)
        print "Removed existing cooperative plan %s" % cooperativeLastPlanName
    except OSError as e:
        pass

    try:
        os.remove(sefishLastPlanName)
        print "Removed existing selfish plan %s" % sefishLastPlanName
    except OSError as e:
        pass


def setBaseFolder(currentFolder):
    global baseFolder
    baseFolder = os.path.dirname(os.path.realpath(os.path.join(currentFolder, "..")))


if __name__ == "__main__":
    removeGeoJSONFiles()
    setBaseFolder(sys.argv[0])
    app.run()
