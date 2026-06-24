from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import joblib

from recommendation import generate_recommendation

app = FastAPI()


model = joblib.load("rice_yield_model.pkl")


features = [
    'Nitrogen', 'Phosphorus', 'Potassium',
    'Temperature', 'Humidity', 'pH_Value', 'Rainfall'
]

# ---------------- INPUT ----------------
class CropInput(BaseModel):
    N: float
    P: float
    K: float
    temp: float
    hum: float
    ph: float
    rain: float


@app.get("/")
def home():
    return {"message": "Rice Yield Prediction API Running"}

@app.post("/predict")
def predict(input: CropInput):

    sample = pd.DataFrame([[
        input.N,
        input.P,
        input.K,
        input.temp,
        input.hum,
        input.ph,
        input.rain
    ]], columns=features)

    prediction = model.predict(sample)

    tips = generate_recommendation(
        input.N,
        input.P,
        input.K,
        input.ph,
        input.rain,
        input.temp,
        input.hum
    )

    return {
        "Predicted_Yield": round(float(prediction[0]), 2),
        "Unit": "tons/hectare",
        "Suggestions": tips
    }