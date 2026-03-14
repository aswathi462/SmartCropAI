from fastapi import FastAPI
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error
import numpy as np

# Create FastAPI app
app = FastAPI()

# Load dataset
data = pd.read_csv("crop_dataset_with_yield.csv")

# Filter rice only
rice_data = data[data['Crop'] == 'Rice']

# Features
features = ['Nitrogen','Phosphorus','Potassium','Temperature','Humidity','pH_Value','Rainfall']
X = rice_data[features]

# Target
y = rice_data['Yield']

# Split data
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)

# Train model
model = RandomForestRegressor()
model.fit(X_train, y_train)

print("Model trained successfully")

# Model evaluation
y_pred = model.predict(X_test)
r2 = model.score(X_test, y_test)
mae = mean_absolute_error(y_test, y_pred)
rmse = np.sqrt(mean_squared_error(y_test, y_pred))

print("Model Performance:")
print("R2 Score:", round(r2,2))
print("MAE:", round(mae,2))
print("RMSE:", round(rmse,2))


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


# Prediction API
@app.get("/predict")
def predict(
    N: float,
    P: float,
    K: float,
    temp: float,
    hum: float,
    ph: float,
    rain: float
):

    sample = pd.DataFrame([[N,P,K,temp,hum,ph,rain]], columns=features)

    prediction = model.predict(sample)

    tips = recommendation(N,P,K,ph,rain)

    return {
        "Predicted_Yield": round(float(prediction[0]),2),
        "Unit": "tons/hectare",
        "Suggestions": tips
    }