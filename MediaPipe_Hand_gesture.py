import cv2
import numpy as np
import mediapipe as mp
from tensorflow.keras.models import load_model




cap = cv2.VideoCapture(0)

while True:
    _, frame = cap.read()
    x,y,c = frame.shape

