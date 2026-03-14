import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras import layers, models

# 1. Prepare the data
# Added shear and zoom to help the model not just "memorize" healthy leaves
datagen = ImageDataGenerator(
    rescale=1./255, 
    validation_split=0.2,
    rotation_range=20,
    horizontal_flip=True
)

train_data = datagen.flow_from_directory(
    'dataset',
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    subset='training'
)

val_data = datagen.flow_from_directory(
    'dataset',
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    subset='validation'
)

# 2. Build the Model
model = models.Sequential([
    layers.Conv2D(32, (3, 3), activation='relu', input_shape=(224, 224, 3)),
    layers.MaxPooling2D((2, 2)),
    layers.Conv2D(64, (3, 3), activation='relu'),
    layers.MaxPooling2D((2, 2)),
    
    layers.Flatten(),
    
    # --- DROPOUT ADDED HERE ---
    # This shuts off 50% of neurons during training so the model 
    # doesn't get "lazy" and just guess 'Healthy' every time.
    layers.Dropout(0.5), 
    
    layers.Dense(128, activation='relu'),
    layers.Dense(3, activation='softmax') 
])



model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

# 3. Start Training
print("Training started for 3 categories with Dropout...")
model.fit(train_data, validation_data=val_data, epochs=10)

# 4. Save the actual model
model.save('rice_model.h5')
print("Success! Your improved AI model is saved as rice_model.h5")