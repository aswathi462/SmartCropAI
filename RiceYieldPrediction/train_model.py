import pandas as pd
import joblib
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor


data = pd.read_csv("rice_dataset.csv")


features = [
    'Nitrogen', 'Phosphorus', 'Potassium',
    'Temperature', 'Humidity', 'pH_Value', 'Rainfall'
]

X = data[features]
y = data['Yield']


X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

model = RandomForestRegressor(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

# Save model
joblib.dump(model, "rice_yield_model.pkl")

print("Model saved successfully as rice_yield_model.pkl")