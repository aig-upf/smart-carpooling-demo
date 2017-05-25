import openpyxl
from random import randint

class CarDataManager:
    def __init__(self):
        self.cars = []

    def parseCarDataFile(self, filePath):
        print "Parsing car data file..."

        wb = openpyxl.load_workbook(filename=filePath, read_only=True)
        ws = wb["Sheet1"]

        firstRow = True
        attributes = []

        for row in ws.rows:
            if firstRow:
                for cell in row:
                    if cell.value is not None:
                        attributes.append(cell.value)
                firstRow = False
            else:
                carItem = {}
                cellCount = 0
                for cell in row:
                    if cellCount < len(attributes):
                        carItem[attributes[cellCount]] = cell.value
                        cellCount += 1
                self.cars.append(carItem)

        print "Parsing completed!"

    def getRandomCar(self):
        numCars = len(self.cars)
        if numCars > 0:
            return self.cars[randint(0, numCars - 1)]
        return None
