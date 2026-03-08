import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error,mean_squared_error
import numpy as np
# Load dataset
data = pd.read_csv("crop_dataset_with_yield.csv")

# Filter rice only
rice_data = data[data['Crop'] == 'Rice']

# Features
X = rice_data[['Nitrogen','Phosphorus','Potassium','Temperature','Humidity','pH_Value','Rainfall']]

# Target
y = rice_data['Yield']

# Split data
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)

# Train model
model = RandomForestRegressor()
model.fit(X_train, y_train)

print("Model trained successfully")

y_pred=model.predict(X_test)
r2=model.score(X_test,y_test)
mae=mean_absolute_error(y_test,y_pred)
rmse=np.sqrt(mean_squared_error(y_test,y_pred))
print("\nModel  Performance:")
print("R2 Score:",round(r2,2))
print("Mean Absolute Error:",round(mae,2))
print("RMSE:",round(rmse,2))

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
        advice.append("Soil conditions look good. Maintain proper irrigation and pest control.")

    return advice


# -------- USER INPUT --------
N = float(input("Enter Nitrogen: "))
P = float(input("Enter Phosphorus: "))
K = float(input("Enter Potassium: "))
temp = float(input("Enter Temperature: "))
hum = float(input("Enter Humidity: "))
ph = float(input("Enter pH value: "))
rain = float(input("Enter Rainfall: "))

sample = [[N,P,K,temp,hum,ph,rain]]

prediction = model.predict(sample)

print("\nPredicted Rice Yield:", round(prediction[0],2),"tons/hectare")


# -------- RECOMMENDATION --------
tips = recommendation(N,P,K,ph,rain)

print("\nSuggestions to Improve Yield:")

for tip in tips:
    print("-", tip)