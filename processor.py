import cv2
import numpy as np

def clean_leaf_image(image):
    # 1. Convert to HSV
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    
    # 2. Widen the range to include Brown, Yellow, and Green
    # This range covers almost all leaf colors (Healthy + Diseased)
    lower_leaf = np.array([10, 20, 20])   # Lowered Hue to catch Brown/Yellow
    upper_leaf = np.array([90, 255, 255]) # Upper Hue for Green
    
    # 3. Create the Mask
    mask = cv2.inRange(hsv, lower_leaf, upper_leaf)
    
    # 4. Optional: Clean up noise (small dots) in the mask
    kernel = np.ones((5,5), np.uint8)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)
    
    # 5. Apply the mask
    result = cv2.bitwise_and(image, image, mask=mask)
    
    # 6. Resize
    final_image = cv2.resize(result, (224, 224))
    
    return final_image