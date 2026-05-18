import pandas as pd

# Load dataset
data = pd.read_csv("crop_dataset.csv")

# Step 1: Filter only Rice
rice_data = data[data['Crop'].astype(str).str.strip().str.lower() == 'rice'].copy()

# Step 2: Keep only required columns
rice_data = rice_data[[
    "Nitrogen",
    "Phosphorus",
    "Potassium",
    "Temperature",
    "Humidity",
    "pH_Value",
    "Rainfall",
    "Crop"
]]

# Step 3: Function to calculate Yield
def compute_rice_yield(row):
    base = (
        row["Nitrogen"] * 0.025 +
        row["Phosphorus"] * 0.015 +
        row["Potassium"] * 0.02 +
        row["Temperature"] * 0.05 +
        row["Humidity"] * 0.01 +
        row["Rainfall"] * 0.005
    )
    return base / 5  # normalized yield (t/ha)

# Step 4: Add Yield column
rice_data["Yield"] = rice_data.apply(compute_rice_yield, axis=1)

# Step 5: Save final dataset
rice_data.to_csv("rice_dataset.csv", index=False)

print("Clean Rice dataset with Yield created successfully!")