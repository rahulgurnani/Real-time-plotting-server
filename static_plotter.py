import time
import numpy as np
import matplotlib.pyplot as plt


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
                line, = self.axis.plot(self.time, self.curves[label]["data"], c = self.curves[label]["color"], label = label)
                self.lines.append(line)
        
        def show_fig(self):
                plt.show()
        def save_fig(self, path):
                #self.figure.legend(handles = self.lines)
                self.figure.savefig(path +self.title + "_.png")


""" sample code for testing """
"""plt.show(block=False)
time = range(0,1000)
y = range(0,1000)
y2 = []
for i in range(10,1010):
        y2.append(i)
first = Plot(time, "demo", (-1, 1001))
first.add_curve(y, 'r', 'demo1')
first.add_curve(y2, 'b', 'demo2')
first.save_fig()
first.show_fig()"""