import pandas as pd

# Load dataset
data = pd.read_csv("crop_dataset.csv")

# Filter only rice rows (optional)
# data = data[data["Crop"] == "Rice"]

# Function to compute realistic rice yield
def compute_rice_yield(row):
    base = (
        row["Nitrogen"] * 0.025 +      # N improves yield strongly
        row["Phosphorus"] * 0.015 +    # P moderate effect
        row["Potassium"] * 0.02 +      # K moderate effect
        row["Temperature"] * 0.05 +    # optimal 20–35
        row["Humidity"] * 0.01 +
        row["Rainfall"] * 0.005
    )

    # Normalize to realistic t/ha range (around 3–8 t/ha)
    rice_yield = base / 5

    return rice_yield

# Apply yield generation
data["Yield"] = data.apply(compute_rice_yield, axis=1)

# Save updated dataset
data.to_csv("dataset_with_yield.csv", index=False)

print("Realistic Rice Yield column added successfully!")