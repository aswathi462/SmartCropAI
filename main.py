from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor

# Import correct function
from recommendation import generate_recommendation

app = FastAPI()

# ---------------- LOAD & TRAIN MODEL ----------------
data = pd.read_csv("dataset_with_yield.csv")

rice_data = data[data['Crop'] == 'Rice']

features = ['Nitrogen', 'Phosphorus', 'Potassium', 'Temperature', 'Humidity', 'pH_Value', 'Rainfall']
X = rice_data[features]
y = rice_data['Yield']

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
model = RandomForestRegressor()
model.fit(X_train, y_train)

print("Model trained successfully")


# ---------------- INPUT MODEL ----------------
class CropInput(BaseModel):
    N: float
    P: float
    K: float
    temp: float
    hum: float
    ph: float
    rain: float


# ---------------- HOME ----------------
@app.get("/")
def home():
    return {"message": "Rice Yield Prediction API Running"}


# ---------------- PREDICTION ENDPOINT ----------------
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

    # Call corrected function
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