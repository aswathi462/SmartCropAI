import tensorflow as tf
import numpy as np
# Import the specific preprocessing MobileNetV2 requires
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input

# Load the real model
model = tf.keras.models.load_model('rice_model.h5')

def detect_disease(processed_image):
    # 1. Expand dimensions to (1, 224, 224, 3)
    img_array = np.expand_dims(processed_image, axis=0)
    
    # 2. VITAL: Use MobileNetV2's specific math (-1 to 1 scaling)
    # Instead of / 255.0, we use this:
    img_array = preprocess_input(img_array)
    
    # 3. Ask the AI for the answer
    predictions = model.predict(img_array)
    
    # 4. Final Labels (Standard alphabetical order)
    # Double-check your folder names: 
    # folder 0: Brown Spot, folder 1: Healthy, folder 2: Leaf scald
    diseases = [ "brown_spot", "healthy","leaf_scald"] 
    
    class_idx = np.argmax(predictions[0])
    confidence = float(np.max(predictions[0]) * 100)
    
    return diseases[class_idx], confidence