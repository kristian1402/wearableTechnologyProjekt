import numpy as np
import pandas as pd
import tensorflow as tf
import os
import cv2
import matplotlib.pyplot as plt
from tqdm import tqdm
from sklearn.model_selection import train_test_split
from keras import layers, callbacks, utils, applications, optimizers
from keras.models import Sequential, Model, load_model
import gc

#define path
path = "Data"
files = os.listdir(path)

#Create image and labels in case we get more than one gesture
image_array = []
label_array = []
#looping through them
for i in tqdm(range(len(files))): #tqdm makes a progress meter
    #Find a list of files in each folder, only one at this point
    sub_file = os.listdir(path+"/"+files[i])
    print(len(sub_file))

    for j in range(len(sub_file)):
        # find path of each image
                    #Data/Subfolder_name/imagename1
        file_path = path+"/"+files[i]+"/"+sub_file[j]
        image = cv2.imread(file_path)
        #resize all images
        image = cv2.resize(image, (96,96))
        image = cv2.cvtColor(image,cv2.COLOR_BGR2RGB)
        #add these to the image array
        image_array.append(image)
        #add labels to label array, i is just the number from 0 to len. start/stop label will be 0
        label_array.append(i)

#convert the list to a numpy array
image_array = np.array(image_array)
label_array = np.array(label_array, dtype="float")

#split set into train and test set.
x_train, x_test, y_train, y_test = train_test_split(image_array, label_array, test_size=0.2)

#delete the old arrays to free memory.
del image_array, label_array
gc.collect()


#Sequential model is appropritate when we have one input- and one output-tensor.
model = Sequential()
#we ad a pretrained model to sequential model, it returns a classification model,
#with weights pretained on imageNet.
pretrained_model = tf.keras.applications.EfficientNetB0(input_shape=(96,96,3), include_top=False)
model.add(pretrained_model)

#add Pooling and dropout to modell
model.add(layers.GlobalAveragePooling2D())
#add dropout to increase accuracy by reducing overfitting.
model.add(layers.Dropout(0.3))
#Add dense layer as an output
model.add(layers.Dense(1))

#build model
model.build(input_shape=(None, 96, 96, 3))

#model sum
model.summary()
tf.config.list_physical_devices('GPU')

model.compile(optimizer="adam", loss="mae", metrics=["mae"])

ckp_path = "trained_model/model"
model_checkpoint = tf.keras.callbacks.ModelCheckpoint(filepath=ckp_path,
                                                      monitor="val_mae",
                                                      mode="auto",
                                                      save_best_only="True",
                                                      save_weights_only="True")

#learning rate reducer, when accuracy does not improve
lr_reduce = tf.keras.callbacks.ReduceLROnPlateau(factor=0.9,
                                        monitor="val_mae",
                                        mode="auto",
                                        cooldown=0,
                                        patience=5,
                                        verbose=1,
                                        min_lr=1e-6)

#patience = reduce lr after x epoch when accuracy does not improve.

#start training
Epoch = 100
Batch_size = 32

history = model.fit(x_train,
                    y_train,
                    validation_data=(x_test, y_test),
                    batch_size=Batch_size,
                    epochs=Epoch,
                    callbacks=[model_checkpoint, lr_reduce])


model.load_weights(ckp_path)
converter=tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

with open("model.tflite", "wb") as f:
    f.write(tflite_model)

prediction_val = model.predict(x_test, batch_size=32)
print(prediction_val[:10])
print(y_test[:10])
