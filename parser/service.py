#! /usr/bin/python

import os, time, json, sys
from flask import Flask, request
from flask_cors import CORS, cross_origin

import randominit, parser

app = Flask(__name__)
CORS(app)

cooperativeLastPlanModification = 0
sefilshLastPlanModification = 0

cooperativeLastPlanName = "cooperative_tmp_sas_plan.geojson"
sefishLastPlanName = "selfish_tmp_sas_plan.geojson"

baseFolder = None

@app.route("/generate_scenario")
def generateRandomScenerario():
    randominit.generateRandomScenerario("../trento/Trento.world", 2, 2, 46.0643, 46.0715, 11.1164, 11.1272, 0, 10)
    parser.solveSmartCarpoolingProblem(baseFolder, "random_init.json", True, True, True)
    return "Done!"


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
    print request.json
    return "Hello"


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
