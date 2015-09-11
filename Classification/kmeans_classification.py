import numpy as np
from numpy import array
from scipy.cluster.vq import vq, kmeans, whiten

def extract_magnitudes(data, nsamples):
	size = len(data)
	samples = []
	for i in range(nsamples):
		samples.append(data[int(i*size/nsamples)])
	return array(samples)

def extract_gradient(data, nsamples):
	gradient = np.gradient(data)
	samples = []
	for i in range(nsamples):
		samples.append(data[int(i*size/nsamples)])
	return array(samples)

class Classifier():
	"""This class uses kmeans clustering for classification of data"""
	def __init__(self):
		self.data = None
		self.kmeans_centroids = None
		self.distortion = None

	def append_reading(reading):
		if self.data == None:
			# if data is uninitialized then create a new array
			self.data = array([reading])
		else:
			# else append data at the end
			self.data = np.append(self.data, [reading], axis=0)

	# Takes the input as number of classes
	def cluster(nclasses):
		whitened = whiten(self.data)
		self.kmeans_centroids, self.distortion = kmeans(whitened, nclasses)

	# Given a reading tells which class it belongs to
	def predict(reading):
		input_feature = array([reading])
		predict_class, distortion = vq(input_feature, self.kmeans_centroids)
		# since input has only one reading, predict_class will be an array of 1 element
		return predict_class[0]


