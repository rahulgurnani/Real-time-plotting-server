# Butterworth band pass filter, referenced from
# http://wiki.scipy.org/Cookbook/ButterworthBandpass

from numpy import array
#from scipy.signal import butter, lfilter

def butter_bandpass(lowcut, highcut, fs, order=5):
    nyq = 0.5 * fs
    low = lowcut / nyq
    high = highcut / nyq
    b, a = butter(order, [low, high], btype='band')
    return b, a


def butter_bandpass_filter(data, lowcut, highcut, fs, order=5):
    b, a = butter_bandpass(lowcut, highcut, fs, order=order)
    y = lfilter(b, a, data)
    return y

# Filter a noisy signal.
def butter_low_pass_filter(data, nsamples, lowcut, a, f0, seconds = 5):
    # some hardcoded value can be changed
    highcut = 1200
    fs = nsamples / seconds
    t = np.linspace(0, seconds, nsamples, endpoint=False)
    # a = 0.02
    # f0 = 600.0
    x = array(data)
    y = butter_bandpass_filter(x, lowcut, highcut, fs, order=6)
    return y

# moving window low pass filter
# takes average of the last 5 readings of the data and sends the average
def moving_window(data, current, window_size = 5):
    average = 0.0
    for reading in data[-window_size:]:
        average += reading
    average /= window_size
    return average

# K filtering mechanism
# simply uses k% from current and (100-k)% from previous. Therefore, there cannot be many jerks
# Reference : http://stackoverflow.com/questions/6942626/accelerometer-low-pass-filtering
def k_filtering(previous, current, k_filtering_factor = 0.1):
    return ((current * k_filtering_factor) + (previous * (1.0 - k_filtering_factor)))
