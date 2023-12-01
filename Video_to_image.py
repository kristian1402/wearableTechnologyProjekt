# Importing all necessary libraries
import cv2
import os

# Read the video from specified path
cam = cv2.VideoCapture("C:\\Users\\chilo\\OneDrive - Aalborg Universitet\\Medialogi\\7. Semester\\Project\\Data\\picwall.mp4")

try:

    # creating a folder named data
    if not os.path.exists('data'):
        os.makedirs('data')

# if not created then raise error
except OSError:
    print('Error: Creating directory of data')

# frame
i = 0
currentframe = 0
frame_skip = 20

while (True):

    # reading from frame
    ret, frame = cam.read()

    if not ret:
        break
    if i > frame_skip - 1:
        currentframe += 1
        # if video is still left continue creating images
        name = './data/frame' + str(currentframe) + '.jpg'
        print('Creating...' + name)

        # writing the extracted images
        cv2.imwrite(name, frame)
        i = 0
        continue
    i += 1

cam.release()
cv2.destroyAllWindows()