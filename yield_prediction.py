from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error
import numpy as np


# Create FastAPI App

app = FastAPI()

# Allow Android app to access backend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],   # allow all for testing, change later for security
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Load Dataset

data = pd.read_csv("crop_dataset_with_yield.csv")

# Filter rice only
rice_data = data[data["Crop"] == "Rice"]

# Features & Target

features = ['Nitrogen', 'Phosphorus', 'Potassium', 'Temperature', 'Humidity', 'pH_Value', 'Rainfall']
X = rice_data[features]
y = rice_data['Yield']


# Train-Test Split

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Train Model

model = RandomForestRegressor()
model.fit(X_train, y_train)

# Evaluation (optional)

y_pred = model.predict(X_test)
r2 = model.score(X_test, y_test)
mae = mean_absolute_error(y_test, y_pred)
rmse = np.sqrt(mean_squared_error(y_test, y_pred))

print("Model trained successfully!")
print("Performance:", {"R2": r2, "MAE": mae, "RMSE": rmse})


# Input Model for POST Request

class YieldRequest(BaseModel):
    N: float
    P: float
    K: float
    temp: float
    hum: float
    ph: float
    rain: float



# Enhanced Recommendation Function
def recommendation(N, P, K, ph, rain, temp=None, hum=None):
    advice = []

    # Macronutrients
    if N < 50:
        advice.append("Nitrogen is low: Apply urea fertilizer to boost leaf growth and overall plant health.")
    else:
        advice.append("Nitrogen level is sufficient for healthy growth.")

    if P < 30:
        advice.append("Phosphorus is low: Apply DAP fertilizer to improve root development and early growth.")
    else:
        advice.append("Phosphorus level is adequate for root development.")

    if K < 30:
        advice.append("Potassium is low: Use potash fertilizer to strengthen stems, improve stress resistance, and grain filling.")
    else:
        advice.append("Potassium level is adequate for strong plants.")

    # Soil pH
    if ph < 5.5:
        advice.append("Soil is acidic: Add lime to raise pH for better nutrient absorption.")
    elif ph > 7.5:
        advice.append("Soil is alkaline: Add sulfur or organic matter to lower pH and improve nutrient uptake.")
    else:
        advice.append("Soil pH is optimal for rice growth.")

    # Rainfall / irrigation
    if rain < 150:
        advice.append("Low rainfall detected: Provide supplemental irrigation to prevent drought stress.")
    elif rain > 300:
        advice.append("Excess rainfall: Ensure proper drainage to prevent waterlogging and root damage.")
    else:
        advice.append("Rainfall is adequate for rice growth.")

    # Temperature check
    if temp is not None:
        if temp < 20:
            advice.append("Temperature is low: Consider using cold-tolerant rice varieties or delay sowing.")
        elif temp > 35:
            advice.append("Temperature is high: Ensure shading or increase irrigation to reduce heat stress.")
        else:
            advice.append("Temperature is suitable for rice growth.")

    # Humidity check
    if hum is not None:
        if hum < 50:
            advice.append("Low humidity: Increase irrigation to reduce water stress.")
        elif hum > 90:
            advice.append("High humidity: Monitor closely for fungal diseases like leaf blast.")
        else:
            advice.append("Humidity is suitable for rice growth.")

    # Pest & disease general advice
    advice.append("Regularly monitor for pests and diseases; use integrated pest management (IPM) techniques if necessary.")

    # General best practices
    advice.append("Use certified seeds for higher yield.")
    advice.append("Maintain proper spacing to avoid overcrowding.")
    advice.append("Apply organic manure or compost to maintain soil fertility.")
    advice.append("Rotate crops to prevent pest and disease buildup.")
    advice.append("Keep records of irrigation, fertilization, and growth stages for better yield management.")

    return advice

# Home API

@app.get("/")
def home():
    return {"message": "Rice Yield Prediction API Running"}


# Prediction API (POST Method)

@app.post("/predict")
def predict_yield(data: YieldRequest):

    # Convert input to dataframe
    sample = pd.DataFrame([[
        data.N, data.P, data.K, data.temp, data.hum, data.ph, data.rain
    ]], columns=features)

    # Make prediction
    prediction = model.predict(sample)[0]

    # Get suggestions
    tips = recommendation(data.N, data.P, data.K, data.ph, data.rain)

    return {
        "Predicted_Yield": round(float(prediction), 2),
        "Unit": "tons/hectare",
        "Suggestions": tips
    }