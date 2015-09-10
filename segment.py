from sys import argv
import filter
import time
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.cm as cmx
import matplotlib.colors as colors


# THis is the plot function written by Gurnani

class Plot(object):
        """object of this class is a plot """
        """
                title is the title of the window
                time is the list of time which we will plot
                limits(low_limit, upper_limit) of y axis
        """
        def __init__(self, time = [], title = "", limits = ()):
                self.time = time
                self.curves = dict()
                self.title = title
                self.limits = limits
                self.figure = plt.figure()
                self.axis = self.figure.add_subplot(111)
                plt.ylim(self.limits[0], self.limits[1])
                self.figure.canvas.set_window_title(self.title)
                self.lines = list()
        def add_curve(self, y, color, label):
                self.curves[label] = dict()
                self.curves[label]["color"] = color
                self.curves[label]["data"] = y
                line, = self.axis.plot(self.time, self.curves[label]["data"], color = self.curves[label]["color"], label = label)
                self.lines.append(line)
                print "plotting"

        def show_fig(self):
                plt.show()
        def save_fig(self):
                plt.legend(handles = self.lines)
                plt.savefig(self.title + "_.png")


class ExerciseData(object):
    """
    It will contain the exercise data with each dimension"
    """
    def __init__(self):
        self.exNo = None
        self.accx = []
        self.accy = []
        self.accz = []
        self.gyrx = []
        self.gyry = []
        self.gyrz = []
        self.orix = []
        self.oriy = []
        self.oriz = []
        self.timeStamp = []

    def addToList(self,input):
        self.exNo = float(input[10].strip("\n"))
        self.accx.append(float(float(input[0])))
        self.accy.append(float(input[1]))
        self.accz.append(float(input[2]))
        self.gyrx.append(float(input[3]))
        self.gyry.append(float(input[4]))
        self.gyrz.append(float(input[5]))
        self.orix.append(float(input[6]))
        self.oriy.append(float(input[7]))
        self.oriz.append(float(input[8]))
        self.timeStamp.append(float(input[9]))

    def getAccx(self):
        return self.accx
    def getAccy(self):
        return self.accy
    def getAccz(self):
        return self.accz
    def getGyrx(self):
        return self.gyrx
    def getGyry(self):
        return self.gyry
    def getGyrz(self):
        return self.gyrz
    def getOrix(self):
        return self.orix
    def getOriy(self):
        return self.oriy
    def getOriz(self):
        return self.oriz
    def getTimeList(self):
        return self.timeStamp
    def getExNo(self):
        return self.exNo

    def getExerciseWithLimit(self,scaleDownValue):
        myWidth = len(self.timeStamp)
        if myWidth == scaleDownValue:
            newExercise = ExerciseData()
            newExercise = self
            #newExercise.makeRangeBetween()
            return newExercise
        newExercise = ExerciseData()
        stepCount = myWidth/scaleDownValue
        i = 0
        while i<scaleDownValue:
            newExercise.exNo = self.exNo
            newExercise.accx.append(self.accx[int(i*stepCount)])
            newExercise.accy.append(self.accy[int(i*stepCount)])
            newExercise.accz.append(self.accz[int(i*stepCount)])
            newExercise.gyrx.append(self.gyrx[int(i*stepCount)])
            newExercise.gyry.append(self.gyry[int(i*stepCount)])
            newExercise.gyrz.append(self.gyrz[int(i*stepCount)])
            newExercise.orix.append(self.orix[int(i*stepCount)])
            newExercise.oriy.append(self.oriy[int(i*stepCount)])
            newExercise.oriz.append(self.oriz[int(i*stepCount)])
            newExercise.timeStamp.append(i)
            i = i+1

        #newExercise.makeRangeBetween()
        return newExercise

    def makeRangeBetween(self):
        max_val = getMax(self.accx)
        changeElement(self.accx,max_val)
        max_val = getMax(self.accy)
        changeElement(self.accy,max_val)
        max_val = getMax(self.accz)
        changeElement(self.accz,max_val)
        max_val = getMax(self.orix)
        changeElement(self.orix,max_val)
        max_val = getMax(self.oriy)
        changeElement(self.oriy,max_val)
        max_val = getMax(self.oriz)
        changeElement(self.oriz,max_val)
        max_val = getMax(self.gyrx)
        changeElement(self.gyrx,max_val)
        max_val = getMax(self.gyry)
        changeElement(self.gyry,max_val)
        max_val = getMax(self.gyrz)
        changeElement(self.gyrz,max_val)


def getMax(list):
    if min(list)<0:
            max_val = max(list) - min(list)
    else:
        max_val = max(list)
    return max_val

def changeElement(list,value):
    if(value!=0):
        i = 0
        while i<len(list):
                list[i] = list[i]/value
                i = i+1

def get_cmap(N):
        '''Returns a function that maps each index in 0, 1, ... N-1 to a distinct
        RGB color.'''
        color_norm  = colors.Normalize(vmin=0, vmax=N-1)
        scalar_map = cmx.ScalarMappable(norm=color_norm, cmap='hsv')
        def map_index_to_rgb_color(index):
            return scalar_map.to_rgba(index)
        return map_index_to_rgb_color


if __name__ == '__main__':

    exercises = []
    exercise_dict = {}
    f = open("Exercise3.dat","r")
    lines = f.readlines()
    last_read = 0
    occur = 0
    print len(lines)
    for line in lines:
        read = line.split(" ")
        #print read
        key = read[10]
        if key not in exercise_dict:
            #print key
            exercise_dict[key] = []
        exercise_dict[key].append(read)


    for key in exercise_dict.keys():
        exercise = ExerciseData()
        print "Printing",key,len(exercise_dict[key])
        for x in exercise_dict[key]:
            exercise.addToList(x)
        exercises.append(exercise)

    sum = 0
    min_val = 100000000

    for exercise in exercises:
        if(len(exercise.getAccx())<min_val):
            min_val = len(exercise.getAccx())

    print min_val
    trimmedDown = []
    for exercise in exercises:
        trimmedDown.append(exercise.getExerciseWithLimit(min_val))
    exercise = trimmedDown[0]
    timeList = exercise.timeStamp
    list_min = -5
    list_max = +5
    pltAccx = Plot(timeList,"AccX",(-1,1))
    cmap = get_cmap(30)
    i = 0
    for exercise in trimmedDown:
        # if int(exercise.exNo) in [3,16,17,1,13,2,18]:
        #     continue
        i = i+1
        print "Key ",exercise.exNo ,len(exercise.accx)
        accxList = exercise.orix
        print "max",max(accxList),"min",min(accxList)
        col = cmap(int(exercise.exNo))
        newList = list()
        newList.append(accxList[0])
        for i in range(1, len(accxList)):
            newList.append(filter.k_filtering(previous=newList[i-1], current=accxList[i], k_filtering_factor=0.5))
        c = str()
        pltAccx.add_curve(y = newList,color=np.random.rand(3,1),label=str(exercise.exNo))


    pltAccx.save_fig()
    pltAccx.show_fig()

    # plt.show(block=False)
    #
    # time = range(0,1000)
    # y = range(0,1000)
    # y2 = []
    # for i in range(10,1010):
    #         y2.append(i)
    # first = Plot(time, "demo", (-1, 1001))
    # first.add_curve(y, 'r', 'demo1')
    # first.add_curve(y2, 'b', 'demo2')
    # first.save_fig()
    # first.show_fig()