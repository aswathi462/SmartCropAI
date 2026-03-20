import tensorflow as tf
from tensorflow.keras import layers, models
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input
from tensorflow.keras.preprocessing.image import ImageDataGenerator
import os

# 1. Setup Data Generators with MobileNetV2 Preprocessing
# Note: We use preprocess_input instead of manual 1./255 scaling
train_datagen = ImageDataGenerator(
    preprocessing_function=preprocess_input,
    rotation_range=25,
    width_shift_range=0.2,
    height_shift_range=0.2,
    shear_range=0.2,
    zoom_range=0.2,
    horizontal_flip=True,
    brightness_range=[0.8,1.2],
    fill_mode='nearest',
    validation_split=0.2
)

train_data = train_datagen.flow_from_directory(
    'dataset',
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    subset='training'
)

val_data = train_datagen.flow_from_directory(
    'dataset',
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    subset='validation'
)

# 2. Build Model using MobileNetV2 as the Base
base_model = MobileNetV2(weights='imagenet', include_top=False, input_shape=(224, 224, 3))
base_model.trainable = True

# Freeze most layers but allow last layers to train
for layer in base_model.layers[:-30]:
    layer.trainable = False # Keep the pre-trained weights frozen

model = models.Sequential([
    base_model,
    layers.GlobalAveragePooling2D(),
    layers.Dropout(0.5),  # Helps prevent the "Healthy" bias
    layers.Dense(128, activation='relu'),
    layers.Dense(3, activation='softmax') # Assuming 3 classes: Blight, Brown Spot, Healthy
])

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.0001),
    loss='categorical_crossentropy',
    metrics=['accuracy']
)

# 3. Train
print("Starting MobileNetV2 Transfer Learning...")
model.fit(
    train_data,
    validation_data=val_data,
    epochs=20  # MobileNetV2 usually converges faster
)

# 4. Save
model.save('rice_model.h5')
print("New MobileNetV2 model saved successfully!")