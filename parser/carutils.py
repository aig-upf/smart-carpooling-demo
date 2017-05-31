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

    def __getFilteredCars(self, filters):
        if filters is None:
            return self.cars
        filteredCars = []
        for car in self.cars:
            filterCar = True
            for filterAttr in filters:
                filterValArray = filters[filterAttr]
                if car[filterAttr] not in filterValArray:
                    filterCar = False
                    break
            if filterCar:
                filteredCars.append(car)
        return filteredCars

    def getRandomCar(self, filters):
        filteredCars = self.__getFilteredCars(filters)
        numCars = len(filteredCars)
        if numCars > 0:
            return filteredCars[randint(0, numCars - 1)]
        return None
