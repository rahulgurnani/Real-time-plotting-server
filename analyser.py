import static_plotter
import filter
import numpy as np
import matplotlib.cm as cmx
import matplotlib.colors as colors

# globals
directions = ['x', 'y', 'z']


#following parameters can be adjusted 
const_k = 0.1
exercise = "Exercise1"		# Exercise to be considered
def get_cmap(N):
        '''Returns a function that maps each index in 0, 1, ... N-1 to a distinct
        RGB color.'''
        color_norm  = colors.Normalize(vmin=0, vmax=N-1)
        scalar_map = cmx.ScalarMappable(norm=color_norm, cmap='hsv')
        def map_index_to_rgb_color(index):
            return scalar_map.to_rgba(index)
        return map_index_to_rgb_color

def non_zero(number):
	if number == 0.0:
		return 0.000001

class ExerciseData(object):
	"""docstring for ExerciseData"""
	def __init__(self):
		self.Ori = dict()
		self.Acc = dict()
		self.Gyr = dict()
		for direction in directions:
			self.Ori[direction] = list()
			self.Acc[direction] = list()
			self.Gyr[direction] = list()

	def append_data(self, data):
		values = data.split()
		self.Acc['x'].append(float(values[0].strip()))
		self.Acc['y'].append(float(values[1].strip()))
		self.Acc['z'].append(float(values[2].strip()))
		self.Ori['x'].append(float(values[3].strip()))
		self.Ori['y'].append(float(values[4].strip()))
		self.Ori['z'].append(float(values[5].strip()))
		self.Gyr['x'].append(float(values[6].strip()))
		self.Gyr['y'].append(float(values[7].strip()))
		self.Gyr['z'].append(float(values[8].strip()))
		# Make things non zero if any zero present


	def reduce_to(self, value):
		interval = int(len(self.Acc['x'])/value)
		i = 0
		cur = 0
		temp = dict()
		for direction in directions:
			temp[direction] = list()
		while i < value:
			for direction in directions:
				temp[direction].append((self.Acc[direction][cur]))
			cur = cur + interval
			i = i + 1

		for direction in directions:
			self.Acc[direction] = temp[direction] 
		i = 0
		cur = 0
		temp = dict()	
		for direction in directions:
			temp[direction] = list()
		while i < value:
			for direction in directions:
				temp[direction].append((self.Gyr[direction][cur]))
			cur = cur + interval
			i = i + 1
		for direction in directions:
			self.Gyr[direction] = temp[direction] 
		i = 0
		cur = 0
		temp = dict()
		for direction in directions:
				temp[direction] = list()
		while i < value:
			for direction in directions:
				temp[direction].append((self.Ori[direction][cur]))
			cur = cur + interval
			i = i + 1

		for direction in directions:
			self.Ori[direction] = temp[direction] 

	def filter_data(self):
		for direction in directions:
			for i in range(1, len(self.Acc[direction])):
				self.Acc[direction][i] = filter.k_filtering(self.Acc[direction][i-1], self.Acc[direction][i], const_k)
				self.Ori[direction][i] = filter.k_filtering(self.Ori[direction][i-1], self.Ori[direction][i], const_k)
				self.Gyr[direction][i] = filter.k_filtering(self.Gyr[direction][i-1], self.Gyr[direction][i], const_k)

	def make_continous(self):
		for direction in directions:
			for i in range(1,len(self.Ori[direction])):
				if abs(self.Ori[direction][i] - self.Ori[direction][i-1]) > 0.1:
					self.Ori[direction][i] = self.Ori[direction][i-1]
					"""if self.Ori[direction][i] > 0:
						self.Ori[direction][i] = self.Ori[direction][i-1]
					else:
						self.Ori[direction][i] = self.Ori[direction][i] +0.5"""

	"""In this function we try to normalize the input data. We take the largest differnece across all directions and normalize using it."""
	def normalize(self):
		diff_max = 0
		for direction in directions:
			diff = max(self.Acc[direction]) - min(self.Acc[direction])
			if diff_max < diff:
				diff_max = diff
		for direction in directions:
			mean = float(sum(self.Acc[direction]))/len(self.Acc[direction])
			for i in range(0, len(self.Acc[direction])):
				self.Acc[direction][i] = (self.Acc[direction][i] - mean)/diff_max
			
		diff_max = 0
		for direction in directions:
			diff = max(self.Gyr[direction]) - min(self.Gyr[direction])
			if diff_max < diff:
				diff_max = diff
		for direction in directions:
			mean = float(sum(self.Gyr[direction]))/len(self.Gyr[direction])
			if diff_max == 0:
				diff_max = 1
			for i in range(0, len(self.Gyr[direction])):
				self.Gyr[direction][i] = (self.Gyr[direction][i] - mean)/diff_max

		diff_max = 0
		for direction in directions:
			diff = max(self.Ori[direction]) - min(self.Ori[direction])
			if diff_max < diff:
				diff_max = diff
		for direction in directions:
			# mean = float(sum(self.Ori[direction]))/len(self.Ori[direction])			
			for i in range(0, len(self.Ori[direction])):
				# self.Ori[direction][i] = (self.Ori[direction][i] - mean)/diff_max
				self.Ori[direction][i] = (self.Ori[direction][i])/diff_max

	def save_data(self, file_name):
		f = open("processed_datasets/"+exercise +"/"+file_name, "w" )
		for i in range(0, len(self.Acc['x'])):
			acc = str()
			gyr = str()
			ori = str()
			for direction in directions:
				acc = acc + " " + str(self.Acc[direction][i])
				ori = ori + " " + str(self.Ori[direction][i])
				gyr = gyr + " " + str(self.Gyr[direction][i])
			put = acc + " " + ori + " " + gyr + "\n"
			f.write(put)
		f.close()


if __name__ == '__main__':
	ex1 = 20 * [None]
	for i in range(0,20):
		ex1[i] = ExerciseData()
	data1 = open(exercise + ".dat", "r") 
	for line in data1:
		ex1[int(line.split()[-1].strip()) - 1].append_data(line)
	limits = (-1, 1)
	cmap = get_cmap(30)
	plt_ori = dict()
	plt_acc = dict()
	plt_gyr = dict()
	for direction in directions:
		plt_ori[direction] = static_plotter.Plot(range(0,100),	 "ori_" + direction, limits)
		plt_acc[direction] = static_plotter.Plot(range(0,100),	 "acc_" + direction, limits)
		plt_gyr[direction] = static_plotter.Plot(range(0,100),	 "gyr_" + direction, limits)
	clr = 0
	for data in ex1:
		for direction in directions:
			temp = list()
			for i in range(0, len(data.Acc[direction])):
				temp.append(np.trapz(data.Acc[direction][0: (i+1) ]))
			data.Acc[direction] = temp
			temp = list()
			for i in range(0, len(data.Acc[direction])):
				temp.append(np.trapz(data.Acc[direction][0: (i+1) ]))
			data.Acc[direction] = temp
		data.reduce_to(100)
		data.filter_data()
		data.normalize()
		# data.make_continous()
		for direction in directions:
			plt_acc[direction].add_curve(data.Acc[direction], np.random.rand(3,1), str(clr))
			plt_gyr[direction].add_curve(data.Gyr[direction], np.random.rand(3,1), str(clr))
			plt_ori[direction].add_curve(data.Ori[direction], np.random.rand(3,1),  str(clr))
		clr = clr + 1
		data.save_data("data_" + str(clr))
	
	for direction in directions:
	#		plt_acc[direction].show_fig()
	#		plt_gyr[direction].show_fig()
	#		plt_ori[direction].show_fig()
			path = "data_plots/"+ exercise + "/"
			plt_acc[direction].save_fig(path)
			plt_gyr[direction].save_fig(path)
			plt_ori[direction].save_fig(path)