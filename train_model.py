import tensorflow as tf
from tensorflow.keras import layers, models
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input
from tensorflow.keras.preprocessing.image import ImageDataGenerator
import cv2
import numpy as np
import processor
from PIL import Image
from pathlib import Path
import shutil


def preprocess_leaf_for_mobilenet(image):
    # ImageDataGenerator provides RGB arrays; processor expects BGR OpenCV format.
    image_uint8 = np.clip(image, 0, 255).astype(np.uint8)
    image_bgr = cv2.cvtColor(image_uint8, cv2.COLOR_RGB2BGR)
    cleaned_bgr = processor.clean_leaf_image(image_bgr)
    cleaned_rgb = cv2.cvtColor(cleaned_bgr, cv2.COLOR_BGR2RGB).astype(np.float32)
    return preprocess_input(cleaned_rgb)


def quarantine_invalid_images(dataset_dir="dataset", quarantine_dir="dataset_invalid"):
    valid_extensions = {".jpg", ".jpeg", ".png", ".bmp", ".gif", ".tif", ".tiff"}
    dataset_path = Path(dataset_dir)
    quarantine_path = Path(quarantine_dir)
    invalid_count = 0

    if not dataset_path.exists():
        raise FileNotFoundError(f"Dataset folder not found: {dataset_dir}")

    for class_dir in dataset_path.iterdir():
        if not class_dir.is_dir():
            continue

        for file_path in class_dir.iterdir():
            if not file_path.is_file() or file_path.suffix.lower() not in valid_extensions:
                continue

            try:
                # Verify file integrity and ensure PIL can decode it.
                with Image.open(file_path) as img:
                    img.verify()
                with Image.open(file_path) as img:
                    img.load()
            except Exception:
                target_dir = quarantine_path / class_dir.name
                target_dir.mkdir(parents=True, exist_ok=True)
                shutil.move(str(file_path), str(target_dir / file_path.name))
                invalid_count += 1

    if invalid_count > 0:
        print(f"Quarantined {invalid_count} invalid image(s) to '{quarantine_dir}'.")
    else:
        print("No invalid images found in dataset.")


quarantine_invalid_images("dataset", "dataset_invalid")

# 1. Setup Data Generators with MobileNetV2 Preprocessing
# Note: We use preprocess_input instead of manual 1./255 scaling

# Training data generator with augmentation
train_datagen = ImageDataGenerator(
    preprocessing_function=preprocess_leaf_for_mobilenet,
    rotation_range=25,
    width_shift_range=0.2,
    height_shift_range=0.2,
    shear_range=0.2,
    zoom_range=0.2,
    horizontal_flip=True,
    brightness_range=[0.9, 1.1],
    channel_shift_range=5.0,
    fill_mode='nearest',
    validation_split=0.2
)

# Validation data generator without augmentation (only preprocessing)
val_datagen = ImageDataGenerator(
    preprocessing_function=preprocess_leaf_for_mobilenet,
    validation_split=0.2
)

train_data = train_datagen.flow_from_directory(
    'dataset',
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    subset='training',
    shuffle=True  # Ensure shuffling for better training
)

val_data = val_datagen.flow_from_directory(
    'dataset',
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    subset='validation',
    shuffle=False  # No need to shuffle validation data
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
    layers.Dense(5, activation='softmax') # Assuming 5 classes: Brown Spot, Healthy, Hispa, Leaf Scald, Tungro
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