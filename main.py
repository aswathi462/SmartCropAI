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



# Recommendation Function

def recommendation(N, P, K, ph, rain):
    advice = []

    if N < 50:
        advice.append("Apply urea fertilizer to increase nitrogen.")

    if P < 30:
        advice.append("Apply DAP fertilizer for root development.")

    if K < 30:
        advice.append("Use potash fertilizer for stronger plants.")

    if ph < 5.5:
        advice.append("Add lime to increase soil pH.")

    if rain < 150:
        advice.append("Provide irrigation for rice crop.")

    if len(advice) == 0:
        advice.append("Soil conditions look good. Maintain irrigation and pest control.")

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