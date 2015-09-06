#!/usr/bin/python
import socket
import sys
import time
import numpy as np
import sklearn as skml
import matplotlib.pyplot as plt
import random
import filter
import math
#let's set up some constants
#HOST="78.91.80.123"
PORT = 8080   #arbitrary port not currently in use
#ADDR = (HOST,PORT)    #we need a tuple for the address
BUFSIZE = 4096    #reasonably sized buffer for data
serv=socket.socket(socket.AF_INET, socket.SOCK_STREAM)
HOST = ''
print HOST
print socket.gethostbyname(HOST)


#bind our socket to the address
def listen():
    try:
        serv.bind((HOST, PORT))
        serv.listen(5)
    except KeyboardInterrupt:
        print "Keyboard Interrupt"
        serv.close()
        exit(1)
    f=open('Exercise1.dat', 'w')
    lin=np.zeros(6)
    limit=0
    check=0
    j=0
    while j<20 :
        i=0
        print j
        try:
            print "server listening"
            conn,addr = serv.accept()
            print "Koi toh mila!" + str(j)
            while True:
                data=conn.recv(4096)
                sys.stdout.flush()
                readings = data.split('\n')[0]
                readings=readings.split(',')
                if readings[0]=="start":
                    check=1
                    i=0
                    j=j+1
                    continue
                if readings[0]=="Stop":
                    check=0
                    print " kjgkjjkgkgjkg"
                    break
                if check==1:
                    i=i+1
                    if readings[0]=="lin":
                        lin[0]=float(readings[1])
                        lin[1]=float(readings[2])
                        lin[2]=float(readings[3])
                    if readings[0]=="ori":
                        lin[3]=float(readings[1])
                        lin[4]=float(readings[2])
                        lin[5]=float(readings[3])
                        f.write(str(lin[0])+" "+str(lin[1])+" "+str(lin[2])+" "+str(lin[3])+" "+str(lin[4])+" "+str(lin[5])+" "+str(i)+" "+str(j)+"\n")

            conn.close()
        except KeyboardInterrupt:
            conn.close()
            print "bye!"
        except IndexError:
            conn.close()
            print "indexError"
        # if len(readings) == 4:
        #     new_plot(readings[0].strip(),float(readings[1].strip()), float(readings[2].strip()), float(readings[3].strip()))
        # print limit
        # for i in xrange(3):
        #     plt.plot(lin[0:limit,i])
        #     plt.ylabel("Lin"+str(i))
        #     plt.savefig("Lin"+str(i)+".png")
        # for i in xrange(3):
        #     plt.plot(ori[0:limit,i])
        #     plt.ylabel("Ori"+str(i))
        #     plt.savefig("Ori"+str(i)+".png")


""" globals, parameter_dict -> stores the list for corresponding sensor """
# data for each sensor
sensors_data = dict()
#directions = ['x', 'y', 'z']
directions = ['x']
period = 500
time1 = range(0,period)
# the following function is used to plot data
def magnitude(x, y, z):
    return math.sqrt(x**2 + y**2 + z**2)
def new_plot(sensor, x, y, z):
    cur_time = int(round(time.time() * 1000))
    if sensor not in sensors_data:
        sensors_data[sensor] = dict()
        for direction in directions:
            sensors_data[sensor][direction] = dict()
            sensors_data[sensor][direction]['data'] = period * [10]
            sensors_data[sensor][direction]['k_filtering_data'] = period * [10]
            sensors_data[sensor][direction]['figure'] = plt.figure()
            sensors_data[sensor][direction]['axis'] = sensors_data[sensor][direction]['figure'].add_subplot(111)
            sensors_data[sensor]['time'] = list()
            sensors_data[sensor][direction]['temp_data'] = list()
            sensors_data[sensor][direction]['k_filtering_temp_data'] = list()
            li, = sensors_data[sensor][direction]['axis'].plot(time1, sensors_data[sensor][direction]['data'], 'r')
            sensors_data[sensor][direction]['list'] = li
            li, = sensors_data[sensor][direction]['axis'].plot(time1, sensors_data[sensor][direction]['k_filtering_data'], 'b')
            sensors_data[sensor][direction]['k_filtering_list'] = li
            sensors_data[sensor][direction]['figure'].canvas.set_window_title(sensor+"_"+direction)
            sensors_data[sensor][direction]['figure'].canvas.draw()
            plt.show(block=False)
            plt.ylim(-5,5)
    sensors_data[sensor]['time'].append(cur_time)
    #sensors_data[sensor]['x']['temp_data'].append(magnitude(x,y,z))
    if sensor == 'ori':
        sensors_data[sensor]['x']['temp_data'].append(y/36.0)
    elif sensor == 'lin':
        sensors_data[sensor]['x']['temp_data'].append(z/2)

    if len(sensors_data[sensor]['x']['k_filtering_temp_data'])==0:
        for direction in directions:
            sensors_data[sensor][direction]['k_filtering_temp_data'].append(sensors_data[sensor][direction]['temp_data'][-1])
    else:
        for direction in directions:
            sensors_data[sensor][direction]['k_filtering_temp_data'].append(filter.k_filtering(sensors_data[sensor][direction]['k_filtering_temp_data'][-1], sensors_data[sensor][direction]['temp_data'][-1]))

    if len(sensors_data[sensor]['x']['temp_data']) == 10:
        for direction in directions:
            sensors_data[sensor][direction]['data'][:-10] = sensors_data[sensor][direction]['data'][10:]
            sensors_data[sensor][direction]['data'][-10:] = sensors_data[sensor][direction]['temp_data']
            sensors_data[sensor][direction]['list'].set_ydata(sensors_data[sensor][direction]['data'])
            
            sensors_data[sensor][direction]['k_filtering_data'][:-10] = sensors_data[sensor][direction]['k_filtering_data'][10:]
            sensors_data[sensor][direction]['k_filtering_data'][-10:] = sensors_data[sensor][direction]['k_filtering_temp_data']
            sensors_data[sensor][direction]['k_filtering_list'].set_ydata(sensors_data[sensor][direction]['k_filtering_data'])
#            print len(sensors_data[sensor][direction]['k_filtering_temp_data']), len(sensors_data[sensor][direction]['k_filtering_data']), len(sensors_data[sensor][direction]['data']), len(time1)
            print "redrawn"
            sensors_data[sensor][direction]['figure'].canvas.draw()
            sensors_data[sensor][direction]['temp_data'] = list()
            sensors_data[sensor][direction]['k_filtering_temp_data'] = list() 



listen()
"""

for i in range(1,1000):
    try:
        new_plot("accc",random.randrange(-10,10),random.randrange(-10,10),random.randrange(-10,10))
        time.sleep(0.01)
    except KeyboardInterrupt:
        break

"""