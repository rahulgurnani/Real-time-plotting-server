import numpy as np
from numpy import array
from scipy.cluster.vq import vq, kmeans, whiten
from sklearn import svm
import analyser

# globals
directions = ['x', 'y', 'z']

def extract_magnitudes(data, nsamples):
	size = len(data)
	samples = []
	for i in range(nsamples):
		samples.append(data[int(i*size/nsamples)])
	return samples

def extract_gradient(data, nsamples):
	gradient = np.gradient(data)
	samples = []
	for i in range(nsamples):
		samples.append(data[int(i*len(data)/nsamples)])
	return samples
class Classifier():
	"""This class uses kmeans clustering for classification of data"""
	def __init__(self):
		self.data = None
		self.classes = None
		self.clf = svm.LinearSVC()

	def append_reading(self, reading, curr_class):
		if self.data == None:
			# if data is uninitialized then create a new array
			self.data = array([reading])
			self.classes = [curr_class]
		else:
			# else append data at the end
			self.data = np.append(self.data, [reading], axis=0)
			self.classes.append(curr_class)

	def classify(self):
		self.clf.fit(self.data, self.classes)

	# Given a reading tells which class it belongs to
	def predict(self, reading):
		input_feature = array([reading])
		# since input has only one reading, predict_class will be an array of 1 element
		return self.clf.predict(input_feature)
# class Classifier():
# 	"""This class uses kmeans clustering for classification of data"""
# 	def __init__(self):
# 		self.data = None
# 		self.kmeans_centroids = None
# 		self.distortion = None

# 	def append_reading(self, reading):
# 		if self.data == None:
# 			# if data is uninitialized then create a new array
# 			self.data = array([reading])
# 		else:
# 			# else append data at the end
# 			self.data = np.append(self.data, [reading], axis=0)

# 	# Takes the input as number of classes
# 	def cluster(self, nclasses):
# 		print self.data
# 		whitened = whiten(self.data)
# 		self.kmeans_centroids, self.distortion = kmeans(whitened, nclasses)

# 	def print_classifier_info(self):
# 		print self.kmeans_centroids
# 	# Given a reading tells which class it belongs to
# 	def predict(self, reading):
# 		input_feature = array([reading])
# 		# whitened = whiten(input_feature)
# 		predict_class, distortion = vq(input_feature, self.kmeans_centroids)
# 		# since input has only one reading, predict_class will be an array of 1 element
# 		return predict_class

def get_features_for_classifier(exercise_data):
	# We now extract features from data and create a large feature list
	reading = []
	nsamples = 50
	# Append acceleration readings
	for direction in directions:
		reading.extend(extract_magnitudes(exercise_data.Acc[direction],nsamples))
		reading.extend(extract_gradient(exercise_data.Acc[direction],nsamples))
	# Append Gyrometer readings
	for direction in ['x','y']:
		reading.extend(extract_magnitudes(exercise_data.Gyr[direction],nsamples))
		reading.extend(extract_gradient(exercise_data.Gyr[direction],nsamples))
	# Append Orientation readings
	for direction in ['x','y']:
		reading.extend(extract_magnitudes(exercise_data.Gyr[direction],nsamples))
		reading.extend(extract_gradient(exercise_data.Gyr[direction],nsamples))

	return reading

# Collects exercise data for one exercise
# In one exercise there are some training readings and some testing readings
def collect_exercise_data(file_name, total):
	ex1 = total * [None]
	for i in range(0,total):
		ex1[i] = analyser.ExerciseData()
	data1 = open(file_name + ".dat", "r") 
	for line in data1:
		ex1[int(line.split()[-1].strip()) - 1].append_data(line)

	# For each reading in list of reading
	for data in ex1:
		# integrate acceleration
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

	return ex1

def main():
	# create a classifier object
	classifier = Classifier()
	ex1 = collect_exercise_data("Exercise1",20)
	ex2 = collect_exercise_data("Exercise2",20)
	ex3 = collect_exercise_data("Exercise3",20)
	exercises = [ex1, ex2, ex3]
	train = 15
	print "Appending Data to Classifier"
	j = 0
	for exercise in exercises:
		# Append the training data to classifier
		print "Exercise Change"
		for i in range(train):
			print "Current Data : " + str(i)
			data = exercise[i]
			features = get_features_for_classifier(data)
			classifier.append_reading(features,j)
		j+=1

	print "Data collection phase complete \nNow training the classifier"
	classifier.classify()
	# print "Printing the Classifier Information"
	# classifier.print_classifier_info()
	# predict for test data
	for exercise in exercises:
		for i in range(train,20):
			data = exercise[i]
			features = get_features_for_classifier(data)
			print "Test data: " + str(i) + " belongs to class: " + str(classifier.predict(features))
		print ""
	
if __name__ == '__main__':
	main()


