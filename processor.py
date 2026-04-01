import cv2
import numpy as np

def clean_leaf_image(image):
    if image is None or image.size == 0:
        raise ValueError("Input image is empty")

    # 1) Build a broader leaf mask (green + yellow/brown disease tones).
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    green_mask = cv2.inRange(hsv, np.array([25, 30, 30]), np.array([95, 255, 255]))
    yellow_brown_mask = cv2.inRange(hsv, np.array([5, 20, 20]), np.array([35, 255, 255]))
    mask = cv2.bitwise_or(green_mask, yellow_brown_mask)

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
    mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)

    # Fallback for difficult lighting/background scenes where HSV mask is too small.
    leaf_ratio = float(np.count_nonzero(mask)) / float(mask.size)
    if leaf_ratio < 0.05:
        mask = np.full(mask.shape, 255, dtype=np.uint8)

    segmented = cv2.bitwise_and(image, image, mask=mask)

    # 2) Simple gray-world white balance to stabilize color between captures.
    balanced = segmented.astype(np.float32)
    channel_means = balanced.reshape(-1, 3).mean(axis=0) + 1e-6
    gray_mean = channel_means.mean()
    scale = gray_mean / channel_means
    balanced = np.clip(balanced * scale, 0, 255).astype(np.uint8)

    # 3) Improve lesion visibility with CLAHE on luminance only.
    lab = cv2.cvtColor(balanced, cv2.COLOR_BGR2LAB)
    l_chan, a_chan, b_chan = cv2.split(lab)
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    l_chan = clahe.apply(l_chan)
    enhanced = cv2.cvtColor(cv2.merge((l_chan, a_chan, b_chan)), cv2.COLOR_LAB2BGR)

    # Keep background suppressed after enhancement.
    enhanced = cv2.bitwise_and(enhanced, enhanced, mask=mask)

    return cv2.resize(enhanced, (224, 224), interpolation=cv2.INTER_AREA)