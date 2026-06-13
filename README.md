# 🌾 SmartCropAI

SmartCropAI is an AI-powered agriculture application that helps farmers make informed decisions through **Rice Disease Detection**, **Rice Yield Prediction**, and **Personalized Agricultural Recommendations**.

The system uses **Convolutional Neural Networks (CNN)** to identify rice diseases from leaf images and **Random Forest Regression** to predict crop yield using soil nutrient and weather parameters. Based on the predictions, SmartCropAI provides treatment measures for detected diseases and recommendations to improve crop yield.

---

## 🚀 Features

### 🍃 Rice Disease Detection
- Detects rice plant diseases from leaf images.
- Uses a CNN-based deep learning model.
- Provides disease classification with confidence scores.
- Supports image upload through the Android application.

### 💊 Disease Treatment Recommendations
- Suggests treatment measures for detected diseases.
- Provides preventive actions to reduce disease spread.
- Recommends best farming practices for healthier crops.

### 🌾 Rice Yield Prediction
- Predicts rice crop yield using:
  - Nitrogen (N)
  - Phosphorus (P)
  - Potassium (K)
  - Temperature
  - Humidity
  - Rainfall
- Uses Random Forest Regression for accurate predictions.

### 📈 Yield Improvement Recommendations
- Provides suggestions to improve crop productivity.
- Recommends nutrient management strategies.
- Offers guidance based on soil and environmental conditions.

### 📱 Android Mobile Application
- Built using Kotlin in Android Studio.
- User-friendly and farmer-friendly interface.
- Real-time prediction and recommendation system.

---

## 🧠 AI Models Used

### Rice Disease Detection
- **Model:** Convolutional Neural Network (CNN)
- **Input:** Rice leaf image
- **Output:** Disease prediction and confidence score

### Rice Yield Prediction
- **Model:** Random Forest Regression
- **Input:** NPK values and weather parameters
- **Output:** Predicted rice yield

---

## 🛠️ Technology Stack

### Mobile Frontend
- Kotlin
- Android Studio
- XML

### Backend
- FastAPI
- Python

### Image Processing
- OpenCV
  
---

## 📋 How It Works

### Disease Detection Module
1. User uploads a rice leaf image.
2. The image is preprocessed and analyzed by the CNN model.
3. The model predicts the disease category.
4. The application displays:
   - Disease name
   - Prediction confidence
   - Treatment recommendations
   - Preventive measures

### Yield Prediction Module
1. User enters:
   - Nitrogen (N)
   - Phosphorus (P)
   - Potassium (K)
   - Temperature
   - Humidity
   - Rainfall
2. The Random Forest model predicts the expected yield.
3. The application displays:
   - Predicted yield
   - Recommendations to improve productivity
   - Nutrient and cultivation suggestions

---

## 🎯 Key Benefits

- Early disease identification
- Reduced crop losses
- Better disease management
- Data-driven farming decisions
- Improved crop productivity
- Personalized agricultural recommendations
- Support for sustainable farming practices

---

## 🔮 Future Enhancements

- Multi-crop disease detection
- Multilingual support
- Offline prediction capability
- Farm analytics dashboard

---
