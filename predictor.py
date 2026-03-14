import tensorflow as tf
import numpy as np

# Load the real model
model = tf.keras.models.load_model('rice_model.h5')

def detect_disease(processed_image):
    img_array = np.expand_dims(processed_image, axis=0) / 255.0
    
    # Actually ask the AI for the answer
    predictions = model.predict(img_array)
    
    # Updated list for 3 categories (Ensure these match your folder names)
    diseases = ["Bacterial Blight", "Brown Spot", "Healthy"] 
    
    class_idx = np.argmax(predictions[0])
    confidence = float(np.max(predictions[0]) * 100)
    
    return diseases[class_idx], confidence